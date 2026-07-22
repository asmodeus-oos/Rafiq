package com.rafiq.presentation.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.domain.model.Message
import com.rafiq.domain.repository.ChatRepository
import com.rafiq.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import javax.inject.Inject

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val supabaseClient: SupabaseClient,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val currentUserId: String?
        get() = supabaseClient.auth.currentUserOrNull()?.id

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _targetUser = MutableStateFlow<com.rafiq.domain.model.User?>(null)
    val targetUser: StateFlow<com.rafiq.domain.model.User?> = _targetUser.asStateFlow()

    private val _currentUser = MutableStateFlow<com.rafiq.domain.model.User?>(null)
    val currentUser: StateFlow<com.rafiq.domain.model.User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _icebreakers = MutableStateFlow<List<String>>(emptyList())
    val icebreakers: StateFlow<List<String>> = _icebreakers.asStateFlow()

    private var chatJob: Job? = null
    private var typingJob: Job? = null
    private var socket: io.socket.client.Socket? = null
    private var currentChatPartner: String? = null

    init {
        try {
            socket = io.socket.client.IO.socket("https://rafiq-signaling.onrender.com")
            socket?.on("typing") { args ->
                val partnerId = args[0] as? String
                if (partnerId == currentChatPartner) {
                    _isTyping.value = true
                    typingJob?.cancel()
                    typingJob = viewModelScope.launch {
                        kotlinx.coroutines.delay(2000)
                        _isTyping.value = false
                    }
                }
            }
            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val user = supabaseClient.postgrest["users"]
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<com.rafiq.domain.model.User>()
                    _currentUser.value = user
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getMessages(otherUserId: String) {
        currentChatPartner = otherUserId
        chatJob?.cancel()
        
        // Fetch target user info
        viewModelScope.launch {
            try {
                val user = supabaseClient.postgrest["users"]
                    .select { filter { eq("id", otherUserId) } }
                    .decodeSingleOrNull<com.rafiq.domain.model.User>()
                _targetUser.value = user
                
                // Generate Icebreakers
                _currentUser.value?.let { me ->
                    user?.let { partner ->
                        _icebreakers.value = com.rafiq.util.IcebreakerGenerator.generate(me, partner)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        chatJob = viewModelScope.launch {
            _isLoading.value = true
            chatRepository.markMessagesAsRead(otherUserId)
            
            // Realtime Flow
            launch {
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
            
            // Fallback Polling every 1.5 seconds
            launch {
                while(isActive) {
                    kotlinx.coroutines.delay(1000)
                    try {
                        val history = supabaseClient.postgrest["messages"].select {
                            filter {
                                val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: ""
                                isIn("sender_id", listOf(currentUserId, otherUserId))
                                isIn("receiver_id", listOf(currentUserId, otherUserId))
                            }
                        }.decodeList<Message>()
                        _messages.value = history.sortedBy { it.createdAt }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // Poll target user every 5 seconds for online status updates
            launch {
                while(isActive) {
                    kotlinx.coroutines.delay(1000)
                    try {
                        val user = supabaseClient.postgrest["users"]
                            .select { filter { eq("id", otherUserId) } }
                            .decodeSingleOrNull<com.rafiq.domain.model.User>()
                        _targetUser.value = user
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun sendMessage(receiverId: String, textContent: String? = null, mediaUrl: String? = null, isVoice: Boolean = false, replyToId: String? = null) {
        viewModelScope.launch {
            chatRepository.sendMessage(
                receiverId = receiverId,
                textContent = textContent,
                mediaUrl = mediaUrl,
                isVoice = isVoice,
                replyToId = replyToId
            )
            // Use distinct notification type for voice messages
            notificationRepository.createNotification(
                recipientId = receiverId,
                type = if (isVoice) "voice_message" else "message",
                postId = null
            )
        }
    }
    
    fun deleteMessageForEveryone(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }
    fun sendTypingEvent() {
        currentChatPartner?.let {
            // Emitting to the global signaling server - in production requires room logic
            socket?.emit("typing", it) 
        }
    }

    override fun onCleared() {
        super.onCleared()
        socket?.disconnect()
    }
}
