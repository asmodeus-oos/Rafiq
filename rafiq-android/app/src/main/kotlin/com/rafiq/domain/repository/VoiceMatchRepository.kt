package com.rafiq.domain.repository

import com.rafiq.domain.model.CooldownStatus
import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceMatchQueueEntry
import com.rafiq.domain.model.VoiceMatchUsage

interface VoiceMatchRepository {

    // ── Queue ────────────────────────────────────────────────────────────────

    /** Removes any stale entry for the user, then inserts a fresh WAITING entry. */
    suspend fun joinQueue(userId: String, gender: String, tier: String): Result<VoiceMatchQueueEntry>

    /** Marks the user's WAITING entry as CANCELLED and removes it from the queue. */
    suspend fun leaveQueue(userId: String): Result<Unit>

    /**
     * Finds the oldest WAITING user of [oppositeGender] that is not [myUserId].
     * Returns null if no match is available.
     */
    suspend fun findMatch(myUserId: String, oppositeGender: String): VoiceMatchQueueEntry?

    /**
     * Atomically marks both entries as MATCHED, recording [roomId] and cross-referencing
     * [matchedWith] on each entry.
     */
    suspend fun markMatched(
        myEntryId: String,
        myUserId: String,
        partnerEntryId: String,
        partnerUserId: String,
        roomId: String
    ): Result<Unit>

    // ── Daily Usage ──────────────────────────────────────────────────────────

    /**
     * Reads daily usage for [userId].
     * Returns a zero-count entry if none exists yet or if the stored date is not today.
     */
    suspend fun checkDailyUsage(userId: String): VoiceMatchUsage

    /**
     * Increments today's attempt count by 1 for [userId].
     * Upserts the row (inserts on first use, updates on subsequent calls).
     */
    suspend fun incrementUsage(userId: String): Result<Unit>

    // ── Abuse / Cooldown ─────────────────────────────────────────────────────

    /**
     * Returns [CooldownStatus.OnCooldown] if the user is currently under a leave-spam
     * cooldown, or [CooldownStatus.Clear] if they are free to join.
     */
    suspend fun checkAbuse(userId: String): CooldownStatus

    /**
     * Records a queue leave event.
     * If the user has left 3+ times within a 5-minute window, applies a 5-minute cooldown.
     */
    suspend fun recordLeave(userId: String): Result<Unit>

    // ── Audit Log ────────────────────────────────────────────────────────────

    /**
     * Logs a completed random voice call to [random_voice_calls] for analytics.
     * [maleId] and [femaleId] are determined by the matched users' genders.
     */
    suspend fun recordCallEnd(
        maleId: String,
        femaleId: String,
        roomId: String,
        durationSec: Int
    ): Result<Unit>

    // ── User Fetch ───────────────────────────────────────────────────────────

    /** Fetches a user profile by [userId]. */
    suspend fun fetchUser(userId: String): User?
}
