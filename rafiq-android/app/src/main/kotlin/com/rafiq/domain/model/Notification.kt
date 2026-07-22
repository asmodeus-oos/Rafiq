package com.rafiq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Notification(
    val id: String = "",
    @SerialName("recipient_id") val recipientId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    val type: String = "", // e.g. "reply", "like"
    @SerialName("post_id") val postId: String? = null,
    @SerialName("comment_id") val commentId: String? = null,
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    
    @Transient val sender: User? = null
)
