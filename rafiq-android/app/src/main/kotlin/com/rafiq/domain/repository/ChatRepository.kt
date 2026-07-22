package com.rafiq.domain.repository

import com.rafiq.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(
        receiverId: String,
        textContent: String?,
        mediaUrl: String?,
        isVoice: Boolean,
        replyToId: String? = null
    ): Result<Message>
    suspend fun deleteMessage(messageId: String)
    suspend fun markMessagesAsRead(otherUserId: String)
    fun getMessagesFlow(otherUserId: String): Flow<List<Message>>
    fun getChats(): Flow<List<com.rafiq.domain.model.Chat>>
}
