package com.rafiq.data.repository

import com.rafiq.domain.model.Notification
import com.rafiq.domain.model.User
import com.rafiq.domain.repository.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : NotificationRepository {

    override fun getNotifications(): Flow<List<Notification>> = flow {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return@flow
        
        try {
            val notifications = supabaseClient.postgrest["notifications"]
                .select(Columns.list("*, users!notifications_sender_id_fkey(*)")) {
                    filter {
                        eq("recipient_id", currentUserId)
                    }
                }
                .decodeList<NotificationDto>()
                
            val domainNotifications = notifications.map { it.toDomain() }.sortedByDescending { it.createdAt }
            
            emit(domainNotifications)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    override suspend fun createNotification(recipientId: String, type: String, postId: String?, commentId: String?) {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        if (currentUserId == recipientId) return // Don't notify yourself

        try {
            val notification = Notification(
                id = java.util.UUID.randomUUID().toString(),
                recipientId = recipientId,
                senderId = currentUserId,
                type = type,
                postId = postId,
                commentId = commentId
            )
            supabaseClient.postgrest["notifications"].insert(notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            supabaseClient.postgrest["notifications"]
                .update({ set("read", true) }) {
                    filter { eq("id", notificationId) }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun markAllAsRead() {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        try {
            supabaseClient.postgrest["notifications"]
                .update({ set("read", true) }) {
                    filter { 
                        eq("recipient_id", currentUserId)
                        eq("read", false)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @kotlinx.serialization.Serializable
    private data class NotificationDto(
        val id: String = "",
        @kotlinx.serialization.SerialName("recipient_id") val recipientId: String = "",
        @kotlinx.serialization.SerialName("sender_id") val senderId: String = "",
        val type: String = "",
        @kotlinx.serialization.SerialName("post_id") val postId: String? = null,
        @kotlinx.serialization.SerialName("comment_id") val commentId: String? = null,
        val read: Boolean = false,
        @kotlinx.serialization.SerialName("created_at") val createdAt: Long = 0L,
        @kotlinx.serialization.SerialName("users") val sender: User? = null
    ) {
        fun toDomain() = Notification(
            id = id,
            recipientId = recipientId,
            senderId = senderId,
            type = type,
            postId = postId,
            commentId = commentId,
            read = read,
            createdAt = createdAt,
            sender = sender
        )
    }
}
