package com.rafiq.presentation.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.Chat
import com.rafiq.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchChats()
    }

    fun refresh() {
        fetchChats()
    }

    private fun fetchChats() {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.getChats()
                .catch { e ->
                    _isLoading.value = false
                    e.printStackTrace()
                }
                .collect { newChats ->
                    _chats.value = newChats
                    _isLoading.value = false
                }
        }
    }
}
