package com.rafiq.presentation.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.Message
import com.rafiq.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var chatJob: Job? = null

    fun getMessages(otherUserId: String) {
        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            _isLoading.value = true
            chatRepository.getMessagesFlow(otherUserId)
                .catch { e ->
                    _isLoading.value = false
                    // Handle error if needed
                }
                .collect { newMessages ->
                    _messages.value = newMessages
                    _isLoading.value = false
                }
        }
    }

    fun sendMessage(receiverId: String, textContent: String? = null, mediaUrl: String? = null, isVoice: Boolean = false) {
        viewModelScope.launch {
            chatRepository.sendMessage(
                receiverId = receiverId,
                textContent = textContent,
                mediaUrl = mediaUrl,
                isVoice = isVoice
            )
        }
    }
}
