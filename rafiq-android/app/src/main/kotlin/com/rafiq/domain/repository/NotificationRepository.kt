package com.rafiq.domain.repository

import com.rafiq.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<Notification>>
    suspend fun createNotification(recipientId: String, type: String, postId: String? = null, commentId: String? = null)
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead()
    /** Records a profile visit — only sends a notification if the target user is Diamond tier */
    suspend fun recordProfileVisit(targetUserId: String)
}
