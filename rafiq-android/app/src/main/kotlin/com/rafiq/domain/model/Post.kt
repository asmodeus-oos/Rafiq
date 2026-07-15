package com.rafiq.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Post(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("text_content") val textContent: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("voice_url") val audioUrl: String? = null,
    val timestamp: Long = 0L,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("liked_by") val likedBy: Map<String, Boolean> = emptyMap()
)
