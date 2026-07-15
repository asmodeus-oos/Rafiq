package com.rafiq.domain.repository

import com.rafiq.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(receiverId: String, textContent: String? = null, mediaUrl: String? = null, isVoice: Boolean = false)
    fun getMessagesFlow(otherUserId: String): Flow<List<Message>>
}
