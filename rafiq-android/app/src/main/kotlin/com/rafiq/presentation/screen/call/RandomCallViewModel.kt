package com.rafiq.presentation.screen.call

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.Tier
import com.rafiq.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class MatchState { IDLE, SEARCHING, MATCHED, CANCELLED, ERROR, LIMIT_REACHED }

data class RandomCallUiState(
    val state: MatchState = MatchState.IDLE,
    val matchedUser: User? = null,
    val roomId: String? = null,
    val attemptsRemaining: Int = 0,
    val maxAttempts: Int = 1,
    val myGender: String = "",
    val errorMessage: String? = null
)

@Serializable
data class CallQueueEntry(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val gender: String = "",
    @SerialName("entered_at") val enteredAt: String = ""
)

@HiltViewModel
class RandomCallViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(RandomCallUiState())
    val uiState: StateFlow<RandomCallUiState> = _uiState.asStateFlow()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("random_call_prefs", Context.MODE_PRIVATE)
    private var pollJob: Job? = null
    private var currentUserId: String? = null
    private var myQueueEntryId: String? = null

    // Tier-based daily limits
    private val FREE_LIMIT = 1
    private val DIAMOND_LIMIT = 10

    init {
        viewModelScope.launch { loadUserAndState() }
    }

    private suspend fun loadUserAndState() {
        try {
            currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
            val user = supabaseClient.postgrest["users"]
                .select(Columns.list("id", "gender", "tier")) {
                    filter { eq("id", currentUserId!!) }
                }
                .decodeSingleOrNull<User>()

            val tier = user?.tier ?: Tier.FREE
            val maxAttempts = if (tier == Tier.DIAMOND) DIAMOND_LIMIT else FREE_LIMIT
            val todayKey = "attempts_${todayDateString()}"
            val used = prefs.getInt(todayKey, 0)
            val remaining = (maxAttempts - used).coerceAtLeast(0)
            val myGender = user?.gender?.name?.lowercase() ?: "other"

            _uiState.value = RandomCallUiState(
                state = if (remaining <= 0) MatchState.LIMIT_REACHED else MatchState.IDLE,
                attemptsRemaining = remaining,
                maxAttempts = maxAttempts,
                myGender = myGender
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(state = MatchState.ERROR, errorMessage = e.message)
        }
    }

    fun startSearch() {
        val uid = currentUserId ?: return
        val myGender = _uiState.value.myGender
        if (_uiState.value.state == MatchState.LIMIT_REACHED) return
        if (_uiState.value.attemptsRemaining <= 0) {
            _uiState.value = _uiState.value.copy(state = MatchState.LIMIT_REACHED)
            return
        }

        _uiState.value = _uiState.value.copy(state = MatchState.SEARCHING, errorMessage = null)

        viewModelScope.launch {
            try {
                // Remove any stale entry first
                cleanupOwnEntry(uid)

                // Enter the queue
                val entry = supabaseClient.postgrest["call_queue"].insert(
                    mapOf("user_id" to uid, "gender" to myGender)
                ) { select() }.decodeSingle<CallQueueEntry>()
                myQueueEntryId = entry.id

                // Decrement attempts immediately on entry
                consumeAttempt()

                // Poll for a match every 2 seconds
                pollJob = viewModelScope.launch {
                    val oppositeGender = if (myGender == "male") "female" else "male"
                    while (isActive) {
                        val match = findMatch(uid, oppositeGender)
                        if (match != null) {
                            // Remove both from queue
                            cleanupOwnEntry(uid)
                            cleanupEntry(match.second)

                            val matchUser = fetchUser(match.first) ?: return@launch
                            val roomId = listOf(uid, match.first).sorted().joinToString("_")
                            _uiState.value = _uiState.value.copy(
                                state = MatchState.MATCHED,
                                matchedUser = matchUser,
                                roomId = roomId
                            )
                            return@launch
                        }
                        delay(2000)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(state = MatchState.ERROR, errorMessage = "Could not connect to matchmaking. Check your connection.")
            }
        }
    }

    fun cancelSearch() {
        pollJob?.cancel()
        viewModelScope.launch {
            currentUserId?.let { cleanupOwnEntry(it) }
        }
        _uiState.value = _uiState.value.copy(state = MatchState.CANCELLED)
    }

    private suspend fun findMatch(myUserId: String, targetGender: String): Pair<String, String>? {
        return try {
            val entries = supabaseClient.postgrest["call_queue"]
                .select {
                    filter {
                        eq("gender", targetGender)
                        neq("user_id", myUserId)
                    }
                }
                .decodeList<CallQueueEntry>()
            val oldest = entries.minByOrNull { it.enteredAt }
            if (oldest != null) Pair(oldest.userId, oldest.id) else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun cleanupOwnEntry(userId: String) {
        try {
            supabaseClient.postgrest["call_queue"].delete {
                filter { eq("user_id", userId) }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private suspend fun cleanupEntry(entryId: String) {
        try {
            supabaseClient.postgrest["call_queue"].delete {
                filter { eq("id", entryId) }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private suspend fun fetchUser(userId: String): User? {
        return try {
            supabaseClient.postgrest["users"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<User>()
        } catch (e: Exception) {
            null
        }
    }

    private fun consumeAttempt() {
        val todayKey = "attempts_${todayDateString()}"
        val used = prefs.getInt(todayKey, 0) + 1
        prefs.edit().putInt(todayKey, used).apply()
        val remaining = (_uiState.value.maxAttempts - used).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(attemptsRemaining = remaining)
    }

    private fun todayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
        viewModelScope.launch {
            currentUserId?.let { cleanupOwnEntry(it) }
        }
    }
}
