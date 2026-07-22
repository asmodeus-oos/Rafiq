package com.rafiq.domain.model

/** Describes whether a user is currently under an abuse cooldown. */
sealed class CooldownStatus {
    /** User is free to join the queue. */
    object Clear : CooldownStatus()

    /**
     * User is on cooldown until [cooldownUntilMs] (epoch millis).
     * [remainingMs] is how many milliseconds remain at the time of the check.
     */
    data class OnCooldown(val cooldownUntilMs: Long, val remainingMs: Long) : CooldownStatus()
}
