package com.rafiq.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class Comment(
    val id: String = "",
    @SerialName("post_id") val postId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("text_content") val textContent: String = "",
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerialName("parent_id") val parentId: String? = null,
    
    @Transient val user: User? = null,
    @Transient val replies: List<Comment> = emptyList(),
    @Transient val replyingToUsername: String? = null
)
