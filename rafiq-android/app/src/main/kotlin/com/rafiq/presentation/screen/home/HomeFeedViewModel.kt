package com.rafiq.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.model.SwipeAction
import com.rafiq.domain.repository.DiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val dailyMatch: Pair<User, Int>? = null,
    val nearbyPeople: List<User> = emptyList(),
    val trendingRooms: List<VoiceRoom> = emptyList(),
    val recommendedPartners: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    private val discoveryRepository: DiscoveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshFeed()
        // Start a persistent room watcher in its own coroutine
        viewModelScope.launch {
            try {
                discoveryRepository.getTrendingRooms().collect { rooms ->
                    _uiState.value = _uiState.value.copy(trendingRooms = rooms)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Fetch match and partners concurrently
            val dailyMatchResult = discoveryRepository.getDailyMatch()
            val recommendedPartnersResult = discoveryRepository.getRecommendedPartners()

            _uiState.value = _uiState.value.copy(
                dailyMatch = dailyMatchResult.getOrNull(),
                recommendedPartners = recommendedPartnersResult.getOrDefault(emptyList()),
                isLoading = false
            )
        }
    }

    fun likeUser(userId: String) {
        viewModelScope.launch {
            discoveryRepository.recordSwipe(userId, SwipeAction.LIKE)
            _uiState.value = _uiState.value.copy(
                recommendedPartners = _uiState.value.recommendedPartners.filter { it.id != userId }
            )
            refreshFeed()
        }
    }

    fun skipUser(userId: String) {
        viewModelScope.launch {
            discoveryRepository.recordSwipe(userId, SwipeAction.SKIP)
            _uiState.value = _uiState.value.copy(
                recommendedPartners = _uiState.value.recommendedPartners.filter { it.id != userId }
            )
            refreshFeed()
        }
    }
}
