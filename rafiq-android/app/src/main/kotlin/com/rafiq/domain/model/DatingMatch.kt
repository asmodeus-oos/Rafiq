package com.rafiq.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
enum class MatchStatus { PENDING, ACCEPTED, REJECTED, BLOCKED }

@Serializable
data class CompatibilityBreakdown(
    val lifestyle: Float,
    val intellect: Float,
    val ambition: Float,
    val social: Float,
    val values: Float
)

@Serializable
data class DatingMatch(
    val id: String = "",
    @SerialName("user_a_id") val userAId: String,
    @SerialName("user_b_id") val userBId: String,
    val status: MatchStatus = MatchStatus.PENDING,
    
    @SerialName("compatibility_score") val compatibilityScore: Float, // 0.0 - 1.0
    @SerialName("compatibility_breakdown") val breakdown: CompatibilityBreakdown,
    
    @SerialName("matched_at") val matchedAt: Long = System.currentTimeMillis(),
    @SerialName("last_interaction_at") val lastInteractionAt: Long = System.currentTimeMillis()
)
