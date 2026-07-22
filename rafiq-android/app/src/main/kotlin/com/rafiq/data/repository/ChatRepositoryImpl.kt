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
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.io.File
import java.util.UUID
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ChatRepository {

    override suspend fun sendMessage(
        receiverId: String,
        textContent: String?,
        mediaUrl: String?,
        isVoice: Boolean,
        replyToId: String?
    ): Result<Message> {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return Result.failure(Exception("User not authenticated"))
        
        var finalMediaUrl = mediaUrl
        if (mediaUrl != null && !mediaUrl.startsWith("http")) {
            val bucketName = if (isVoice) "chat_audio" else "chat_media"
            val extension = if (isVoice) "m4a" else "jpg"
            val storage = supabaseClient.storage.from(bucketName)
            val path = "${UUID.randomUUID()}.$extension"
            
            val file = File(mediaUrl.replace("file://", ""))
            if (file.exists()) {
                storage.upload(path, file.readBytes())
                finalMediaUrl = storage.publicUrl(path)
            }
        }
        
        val message = Message(
            id = UUID.randomUUID().toString(),
            senderId = currentUserId,
            receiverId = receiverId,
            textContent = textContent,
            mediaUrl = finalMediaUrl,
            isVoice = isVoice,
            replyToId = replyToId
        )
        return try {
            val response = supabaseClient.postgrest["messages"].insert(message)
            val insertedMessage = response.decodeSingle<Message>()
            Result.success(insertedMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(messageId: String) {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        try {
            supabaseClient.postgrest["messages"].update(
                mapOf("deleted_for_everyone" to true)
            ) {
                filter {
                    eq("id", messageId)
                    eq("sender_id", currentUserId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun markMessagesAsRead(otherUserId: String) {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        try {
            supabaseClient.postgrest["messages"].update(
                mapOf("is_read" to true)
            ) {
                filter {
                    eq("receiver_id", currentUserId)
                    eq("sender_id", otherUserId)
                    eq("is_read", false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getMessagesFlow(otherUserId: String): Flow<List<Message>> = callbackFlow {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return@callbackFlow
        
        val fetchHistory = suspend {
            try {
                supabaseClient.postgrest["messages"].select {
                    filter {
                        or {
                            and {
                                eq("sender_id", currentUserId)
                                eq("receiver_id", otherUserId)
                            }
                            and {
                                eq("sender_id", otherUserId)
                                eq("receiver_id", currentUserId)
                            }
                        }
                    }
                }.decodeList<Message>().sortedBy { it.createdAt }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList<Message>()
            }
        }

        trySend(fetchHistory())

        val channel = supabaseClient.channel("messages_$otherUserId")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }

        val job = launch {
            flow.collect { action ->
                // Filter manually for now as complex OR filters in realtime are tricky in some versions
                val newMessage = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<Message>()
                    is PostgresAction.Update -> action.decodeRecord<Message>()
                    else -> null
                }
                
                if (newMessage != null) {
                    if ((newMessage.senderId == currentUserId && newMessage.receiverId == otherUserId) ||
                        (newMessage.senderId == otherUserId && newMessage.receiverId == currentUserId)) {
                        trySend(fetchHistory())
                    }
                }
            }
        }

        launch { channel.subscribe() }

        awaitClose { 
            job.cancel()
            launch { channel.unsubscribe() }
        }
    }

    override fun getChats(): Flow<List<com.rafiq.domain.model.Chat>> = callbackFlow {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return@callbackFlow

        val fetchChats = suspend {
            try {
                // Fetch all messages where the current user is a participant
                val allMessages = supabaseClient.postgrest["messages"].select {
                    filter {
                        or {
                            eq("sender_id", currentUserId)
                            eq("receiver_id", currentUserId)
                        }
                    }
                }.decodeList<Message>()

                // Group by conversation partner and find the latest message per conversation
                val conversationMap = mutableMapOf<String, Message>()
                for (msg in allMessages) {
                    val partnerId = if (msg.senderId == currentUserId) msg.receiverId else msg.senderId
                    val existing = conversationMap[partnerId]
                    if (existing == null || (msg.createdAt ?: "") > (existing.createdAt ?: "")) {
                        conversationMap[partnerId] = msg
                    }
                }

                if (conversationMap.isEmpty()) {
                    emptyList<com.rafiq.domain.model.Chat>()
                } else {
                    // Fetch partner user info in one batch
                    val partnerIds = conversationMap.keys.toList()
                    val partnerUsers = supabaseClient.postgrest["users"]
                        .select(io.github.jan.supabase.postgrest.query.Columns.ALL) {
                            filter { isIn("id", partnerIds) }
                        }
                        .decodeList<com.rafiq.domain.model.User>()
                        .associateBy { it.id }

                    // Count unread messages per conversation
                    val unreadCounts = mutableMapOf<String, Int>()
                    for (msg in allMessages) {
                        if (msg.receiverId == currentUserId && msg.isRead == false) {
                            val partnerId = msg.senderId
                            unreadCounts[partnerId] = (unreadCounts[partnerId] ?: 0) + 1
                        }
                    }

                    // Build the Chat list, sorted by last message time descending
                    conversationMap.entries
                        .sortedByDescending { it.value.createdAt }
                        .mapNotNull { (partnerId, lastMsg) ->
                            val partner = partnerUsers[partnerId] ?: return@mapNotNull null
                            com.rafiq.domain.model.Chat(
                                id = partnerId,
                                participantName = partner.name.takeIf { it.isNotBlank() } ?: partner.username,
                                participantAvatar = partner.avatar.takeIf { it.isNotBlank() },
                                lastMessage = when {
                                    lastMsg.deletedForEveryone == true -> "Message deleted"
                                    lastMsg.isVoice == true -> "🎙️ Voice message"
                                    !lastMsg.mediaUrl.isNullOrBlank() -> "📷 Photo"
                                    else -> lastMsg.textContent ?: ""
                                },
                                unreadCount = unreadCounts[partnerId] ?: 0,
                                timestamp = lastMsg.createdAt ?: ""
                            )
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList<com.rafiq.domain.model.Chat>()
            }
        }

        trySend(fetchChats())

        val channel = supabaseClient.channel("chats_update_$currentUserId")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }

        val job = launch {
            flow.collect { action ->
                val newMessage = when (action) {
                    is PostgresAction.Insert -> action.decodeRecord<Message>()
                    is PostgresAction.Update -> action.decodeRecord<Message>()
                    else -> null
                }
                if (newMessage != null && (newMessage.senderId == currentUserId || newMessage.receiverId == currentUserId)) {
                    trySend(fetchChats())
                }
            }
        }

        launch { channel.subscribe() }

        awaitClose { 
            job.cancel()
            launch { channel.unsubscribe() }
        }
    }

}
