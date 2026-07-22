package com.rafiq.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class PostType { TEXT, IMAGE, VIDEO, AUDIO, POLL, CONFESSION, REEL }

@Serializable
data class PollOption(
    val id: String,
    val text: String,
    @SerialName("vote_count") val voteCount: Int = 0
)

@Serializable
data class Post(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("type") val type: PostType = PostType.TEXT,
    @SerialName("text_content") val textContent: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("voice_url") val audioUrl: String? = null,
    @SerialName("video_url") val videoUrl: String? = null,
    val timestamp: Long = 0L,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("shares_count") val sharesCount: Int = 0,
    
    // Interactive & Discovery
    val hashtags: List<String> = emptyList(),
    @SerialName("location_tag") val locationTag: String? = null,
    @SerialName("poll_data") val pollOptions: List<PollOption>? = null,
    @SerialName("is_anonymous") val isAnonymous: Boolean = false,

    @kotlinx.serialization.Transient var likedBy: Map<String, Boolean> = emptyMap()
)
