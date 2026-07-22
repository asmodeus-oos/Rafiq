package com.rafiq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    @SerialName("participant_id") val id: String,
    @SerialName("participant_name") val participantName: String,
    @SerialName("participant_avatar") val participantAvatar: String? = null,
    @SerialName("last_message") val lastMessage: String,
    @SerialName("unread_count") val unreadCount: Int,
    @SerialName("last_timestamp") val timestamp: String
)
