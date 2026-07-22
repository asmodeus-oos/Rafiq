package com.rafiq.presentation.screen.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.repository.DiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val rooms: List<VoiceRoom> = emptyList(),
    val isMatching: Boolean = false,
    val matchedRoomId: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class VoiceRoomViewModel @Inject constructor(
    private val discoveryRepository: DiscoveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
    }

    private fun loadRooms() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            discoveryRepository.getTrendingRooms().collect { rooms ->
                _uiState.value = _uiState.value.copy(rooms = rooms, isLoading = false)
            }
        }
    }

    fun startVoiceMatch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMatching = true)
            // Implementation for voice matchmaking logic
        }
    }
    
    fun createRoom(title: String, type: com.rafiq.domain.model.RoomType) {
        // Implementation for room creation
    }
}
