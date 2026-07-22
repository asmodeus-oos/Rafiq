package com.rafiq.presentation.screen.call

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.manager.SubscriptionManager
import com.rafiq.domain.manager.VoiceMatchService
import com.rafiq.domain.model.Tier
import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceMatchState
import com.rafiq.domain.repository.VoiceMatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ─────────────────────────────────────────────────────────────────

data class VoiceMatchUiState(
    val matchState: VoiceMatchState = VoiceMatchState.Idle,
    val myGender: String = "",
    val myTier: Tier = Tier.FREE,
    val attemptsRemaining: Int = 0,
    val maxAttempts: Int = 1,
    val cooldownRemainingMs: Long = 0L,
    val matchedUser: User? = null,
    val roomId: String? = null
)

@HiltViewModel
class VoiceMatchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient,
    private val voiceMatchRepository: VoiceMatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceMatchUiState())
    val uiState: StateFlow<VoiceMatchUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        // Initialize the service with context + repo on first use
        VoiceMatchService.initialize(context, voiceMatchRepository)

        viewModelScope.launch { loadUserProfile() }

        // Mirror VoiceMatchService state into UI state
        viewModelScope.launch {
            VoiceMatchService.state.collect { serviceState ->
                _uiState.value = _uiState.value.copy(
                    matchState = serviceState,
                    matchedUser = (serviceState as? VoiceMatchState.Matched)?.partner,
                    roomId = (serviceState as? VoiceMatchState.Matched)?.roomId,
                    cooldownRemainingMs = (serviceState as? VoiceMatchState.OnCooldown)?.remainingMs ?: 0L
                )
            }
        }
    }

    // ── Profile Loading ───────────────────────────────────────────────────────

    private suspend fun loadUserProfile() {
        try {
            val uid = supabaseClient.auth.currentUserOrNull()?.id ?: return
            val user = supabaseClient.postgrest["users"]
                .select(Columns.list("id", "gender", "tier", "name", "avatar_url", "username")) {
                    filter { eq("id", uid) }
                }
                .decodeSingleOrNull<User>() ?: return

            currentUser = user
            val tier = user.tier
            val limit = SubscriptionManager.getDailyMatchLimit(tier)
            val usage = voiceMatchRepository.checkDailyUsage(uid)
            val remaining = if (SubscriptionManager.isUnlimited(tier)) {
                Int.MAX_VALUE
            } else {
                (limit - usage.usedAttempts).coerceAtLeast(0)
            }

            _uiState.value = _uiState.value.copy(
                myGender = user.gender.name,
                myTier = tier,
                attemptsRemaining = remaining,
                maxAttempts = limit
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ── Public Actions ────────────────────────────────────────────────────────

    fun startSearch() {
        val user = currentUser
        if (user == null) {
            viewModelScope.launch {
                loadUserProfile()
                currentUser?.let { VoiceMatchService.joinQueue(it) }
            }
            return
        }
        VoiceMatchService.joinQueue(user)
    }

    fun cancelSearch() {
        VoiceMatchService.leaveQueue(isUserInitiated = true)
    }

    fun resetToIdle() {
        VoiceMatchService.reset()
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        // Only cancel queue if actively searching — don't interrupt a live call
        if (VoiceMatchService.state.value is VoiceMatchState.Searching) {
            VoiceMatchService.leaveQueue(isUserInitiated = false)
        }
    }
}
