package com.rafiq.domain.model

/** All possible states for the voice matchmaking flow. */
sealed class VoiceMatchState {
    /** Initial state — hasn't started yet. */
    object Idle : VoiceMatchState()

    /** Verifying subscription limit / abuse status before joining queue. */
    object CheckingLimits : VoiceMatchState()

    /** In queue, actively searching for an opposite-gender partner. */
    object Searching : VoiceMatchState()

    /**
     * A match was found.
     * @param partner The matched user's profile.
     * @param roomId  The WebRTC room ID to use for the call.
     */
    data class Matched(val partner: User, val roomId: String) : VoiceMatchState()

    /**
     * Daily attempt limit reached.
     * @param used       How many attempts were used today.
     * @param max        Maximum allowed for the tier.
     * @param tier       Current subscription tier name.
     */
    data class LimitReached(val used: Int, val max: Int, val tier: String) : VoiceMatchState()

    /**
     * Abuse cooldown is active — too many queue leaves in a short window.
     * @param cooldownUntilMs Epoch millis when the cooldown expires.
     * @param remainingMs     Milliseconds remaining at last check.
     */
    data class OnCooldown(val cooldownUntilMs: Long, val remainingMs: Long) : VoiceMatchState()

    /** User cancelled the search. */
    object Cancelled : VoiceMatchState()

    /** An error occurred during matchmaking. */
    data class Error(val message: String) : VoiceMatchState()
}
