package com.rafiq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("audio_url") val audioUrl: String? = null,
    val caption: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    
    // Joined User Details
    @SerialName("users") val user: User? = null,
    
    // Transient local viewer tracking
    @kotlinx.serialization.Transient var isViewed: Boolean = false,
    @kotlinx.serialization.Transient var viewsCount: Int = 0
)

@Serializable
data class StoryGroup(
    val user: User,
    val stories: List<Story>,
    val hasUnseenStories: Boolean = true
)
