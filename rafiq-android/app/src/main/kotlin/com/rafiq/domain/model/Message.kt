package com.rafiq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("id")
    val id: String? = null,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    @SerialName("text_content")
    val textContent: String? = null,
    @SerialName("media_url")
    val mediaUrl: String? = null,
    @SerialName("is_voice")
    val isVoice: Boolean? = false,
    @SerialName("created_at")
    val createdAt: String? = null
)
