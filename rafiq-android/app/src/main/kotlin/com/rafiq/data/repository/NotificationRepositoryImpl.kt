package com.rafiq.data.repository

import com.rafiq.domain.model.Notification
import com.rafiq.domain.model.User
import com.rafiq.domain.repository.NotificationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonNull
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : NotificationRepository {

    override fun getNotifications(): Flow<List<Notification>> = callbackFlow {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return@callbackFlow
        
        val fetchNotifications = suspend {
            try {
                val notifications = supabaseClient.postgrest["notifications"]
                    .select(Columns.list("*, users!notifications_sender_id_fkey(*)")) {
                        filter {
                            eq("recipient_id", currentUserId)
                        }
                    }
                    .decodeList<NotificationDto>()
                    
                notifications.map { it.toDomain() }.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

        trySend(fetchNotifications())

        val channel = supabaseClient.channel("notifications_$currentUserId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
            filter("recipient_id", FilterOperator.EQ, currentUserId)
        }

        val job = launch {
            changeFlow.collect {
                trySend(fetchNotifications())
            }
        }

        launch { channel.subscribe() }

        awaitClose { 
            job.cancel()
            launch { channel.unsubscribe() }
        }
    }

    @kotlinx.serialization.Serializable
    data class InsertNotificationDto(
        val id: String,
        @kotlinx.serialization.SerialName("recipient_id") val recipientId: String,
        @kotlinx.serialization.SerialName("sender_id") val senderId: String,
        val type: String,
        @kotlinx.serialization.SerialName("post_id") val postId: String?,
        @kotlinx.serialization.SerialName("comment_id") val commentId: String?
    )

    override suspend fun createNotification(recipientId: String, type: String, postId: String?, commentId: String?) {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        if (currentUserId == recipientId) return // Don't notify yourself

        try {
            val notificationJson = buildJsonObject {
                put("id", java.util.UUID.randomUUID().toString())
                put("recipient_id", recipientId)
                put("sender_id", currentUserId)
                put("type", type)
                // NOTE: No 'timestamp' field — the DB column is 'created_at' with a DEFAULT.
                // Inserting a non-existent column causes PostgREST to reject the request.
                
                // Use null instead of empty string for UUID columns
                if (!postId.isNullOrBlank()) {
                    put("post_id", postId)
                } else {
                    put("post_id", JsonNull)
                }
                
                if (!commentId.isNullOrBlank()) {
                    put("comment_id", commentId)
                } else {
                    put("comment_id", JsonNull)
                }
            }
            supabaseClient.postgrest["notifications"].insert(notificationJson)
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
    data class NotificationDto(
        val id: String = "",
        @kotlinx.serialization.SerialName("recipient_id") val recipientId: String = "",
        @kotlinx.serialization.SerialName("sender_id") val senderId: String = "",
        val type: String = "",
        @kotlinx.serialization.SerialName("post_id") val postId: String? = null,
        @kotlinx.serialization.SerialName("comment_id") val commentId: String? = null,
        val read: Boolean = false,
        // Map the actual DB column 'created_at' to parse as a timestamp
        @kotlinx.serialization.SerialName("created_at") val createdAt: String? = null,
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
            // Parse ISO-8601 created_at string into epoch millis for the domain model
            timestamp = try {
                if (createdAt != null) {
                    java.time.OffsetDateTime.parse(createdAt).toInstant().toEpochMilli()
                } else {
                    System.currentTimeMillis()
                }
            } catch (e: Exception) { System.currentTimeMillis() },
            sender = sender
        )
    }

    override suspend fun recordProfileVisit(targetUserId: String) {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        if (currentUserId == targetUserId) return

        try {
            // Only notify Diamond-tier users of profile visits
            val targetUser = supabaseClient.postgrest["users"]
                .select(io.github.jan.supabase.postgrest.query.Columns.list("id", "tier")) {
                    filter { eq("id", targetUserId) }
                }
                .decodeSingleOrNull<com.rafiq.domain.model.User>()

            if (targetUser?.tier == com.rafiq.domain.model.Tier.DIAMOND) {
                createNotification(
                    recipientId = targetUserId,
                    type = "profile_visit"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
