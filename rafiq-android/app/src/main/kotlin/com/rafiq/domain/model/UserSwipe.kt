package com.rafiq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class SwipeAction { LIKE, SKIP, SUPERLIKE }

@Serializable
data class UserSwipe(
    val id: String = "",
    @SerialName("swiper_id") val swiperId: String = "",
    @SerialName("target_id") val targetId: String = "",
    val action: String = "LIKE",
    @SerialName("created_at") val createdAt: String? = null
)
