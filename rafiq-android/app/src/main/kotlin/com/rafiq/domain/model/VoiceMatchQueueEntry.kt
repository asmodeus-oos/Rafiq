package com.rafiq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class QueueStatus { WAITING, MATCHED, CANCELLED, IN_CALL }

@Serializable
data class VoiceMatchQueueEntry(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val gender: String = "",
    val tier: String = "FREE",
    val status: String = "WAITING",
    @SerialName("joined_at") val joinedAt: String = "",
    @SerialName("matched_with") val matchedWith: String? = null,
    @SerialName("room_id") val roomId: String? = null
)
