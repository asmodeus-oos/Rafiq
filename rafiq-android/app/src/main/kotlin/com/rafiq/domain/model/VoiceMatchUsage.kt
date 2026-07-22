package com.rafiq.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoiceMatchUsage(
    @SerialName("user_id") val userId: String = "",
    @SerialName("usage_date") val usageDate: String = "",   // ISO date: "2025-07-23"
    @SerialName("used_attempts") val usedAttempts: Int = 0
)
