package com.rafiq.data.repository

import com.rafiq.domain.model.Message
import com.rafiq.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ChatRepository {

    override suspend fun sendMessage(receiverId: String, textContent: String?, mediaUrl: String?, isVoice: Boolean) {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val message = Message(
            senderId = currentUserId,
            receiverId = receiverId,
            textContent = textContent,
            mediaUrl = mediaUrl,
            isVoice = isVoice
        )
        supabaseClient.postgrest["messages"].insert(message)
    }

    override fun getMessagesFlow(otherUserId: String): Flow<List<Message>> = flow {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return@flow
        
        // Fetch history
        val history = supabaseClient.postgrest["messages"].select {
            filter {
                isIn("sender_id", listOf(currentUserId, otherUserId))
                isIn("receiver_id", listOf(currentUserId, otherUserId))
            }
        }.decodeList<Message>()

        val currentMessages = history.sortedBy { it.createdAt }.toMutableList()
        emit(currentMessages.toList())

        // Subscribe to changes
        val channel = supabaseClient.realtime.channel("messages")
        
        val insertFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
        }
        
        channel.subscribe()
        
        insertFlow.collect { action ->
            val newMessage = action.decodeRecord<Message>()
            if ((newMessage.senderId == currentUserId && newMessage.receiverId == otherUserId) ||
                (newMessage.senderId == otherUserId && newMessage.receiverId == currentUserId)) {
                currentMessages.add(newMessage)
                // Re-sort just in case, though they should come in order
                val sorted = currentMessages.sortedBy { it.createdAt }
                currentMessages.clear()
                currentMessages.addAll(sorted)
                emit(currentMessages.toList())
            }
        }
    }
}
