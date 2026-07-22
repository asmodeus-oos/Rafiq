package com.rafiq.domain.manager

import com.rafiq.domain.model.Tier

/**
 * Single source of truth for subscription tier limits across the voice call system.
 * DM calls are always unlimited — this only governs random match attempts.
 */
object SubscriptionManager {

    private const val UNLIMITED = Int.MAX_VALUE

    /**
     * Returns the maximum number of random voice match attempts per day for the given [tier].
     * [UNLIMITED] means no cap is enforced.
     */
    fun getDailyMatchLimit(tier: Tier): Int = when (tier) {
        Tier.FREE             -> 1
        Tier.GOLD             -> 10
        Tier.PLATINUM         -> 10
        Tier.DIAMOND          -> 10
        Tier.ELITE            -> UNLIMITED
    }

    /**
     * Returns whether the user has unlimited daily match attempts.
     */
    fun isUnlimited(tier: Tier): Boolean = getDailyMatchLimit(tier) == UNLIMITED

    /**
     * Returns the call duration limit in milliseconds for random matches, or null if unlimited.
     *
     * FREE  → 15 minutes
     * Others → unlimited
     */
    fun getCallDurationLimitMs(tier: Tier): Long? = when (tier) {
        Tier.FREE -> 15 * 60 * 1000L   // 15 minutes
        else      -> null               // Unlimited
    }

    /** Human-readable tier label for UI. */
    fun tierDisplayName(tier: Tier): String = when (tier) {
        Tier.FREE     -> "Free"
        Tier.GOLD     -> "Gold"
        Tier.PLATINUM -> "Platinum"
        Tier.DIAMOND  -> "Diamond"
        Tier.ELITE    -> "Elite"
    }
}
