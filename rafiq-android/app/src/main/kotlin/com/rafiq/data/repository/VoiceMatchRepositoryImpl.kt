package com.rafiq.data.repository

import android.util.Log
import com.rafiq.domain.model.CooldownStatus
import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceMatchQueueEntry
import com.rafiq.domain.model.VoiceMatchUsage
import com.rafiq.domain.repository.VoiceMatchRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "VoiceMatchRepository"

/** Raw DB shape of voice_match_abuse row. */
@Serializable
private data class AbuseRow(
    @SerialName("user_id") val userId: String = "",
    @SerialName("leave_count") val leaveCount: Int = 0,
    @SerialName("window_start") val windowStart: String = "",
    @SerialName("cooldown_until") val cooldownUntil: String? = null
)

@Singleton
class VoiceMatchRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : VoiceMatchRepository {

    // ── Queue ────────────────────────────────────────────────────────────────

    override suspend fun joinQueue(
        userId: String,
        gender: String,
        tier: String
    ): Result<VoiceMatchQueueEntry> = runCatching {
        // Remove any stale entry first
        leaveQueue(userId)

        supabaseClient.postgrest["voice_match_queue"].insert(
            mapOf(
                "user_id" to userId,
                "gender" to gender,
                "tier" to tier,
                "status" to "WAITING"
            )
        ) { select() }.decodeSingle<VoiceMatchQueueEntry>()
            .also { Log.d(TAG, "Joined queue: entryId=${it.id}") }
    }

    override suspend fun leaveQueue(userId: String): Result<Unit> = runCatching {
        supabaseClient.postgrest["voice_match_queue"].delete {
            filter { eq("user_id", userId) }
        }
        Log.d(TAG, "Left queue: userId=$userId")
    }

    override suspend fun findMatch(
        myUserId: String,
        oppositeGender: String
    ): VoiceMatchQueueEntry? = try {
        supabaseClient.postgrest["voice_match_queue"]
            .select(Columns.ALL) {
                filter {
                    eq("gender", oppositeGender)
                    eq("status", "WAITING")
                    neq("user_id", myUserId)
                }
                order("joined_at", Order.ASCENDING)
                limit(1)
            }
            .decodeList<VoiceMatchQueueEntry>()
            .firstOrNull()
    } catch (e: Exception) {
        Log.e(TAG, "findMatch error", e)
        null
    }

    override suspend fun markMatched(
        myEntryId: String,
        myUserId: String,
        partnerEntryId: String,
        partnerUserId: String,
        roomId: String
    ): Result<Unit> = runCatching {
        // Update my entry
        supabaseClient.postgrest["voice_match_queue"].update(
            {
                set("status", "MATCHED")
                set("matched_with", partnerUserId)
                set("room_id", roomId)
            }
        ) { filter { eq("id", myEntryId) } }

        // Update partner entry
        supabaseClient.postgrest["voice_match_queue"].update(
            {
                set("status", "MATCHED")
                set("matched_with", myUserId)
                set("room_id", roomId)
            }
        ) { filter { eq("id", partnerEntryId) } }

        Log.d(TAG, "markMatched: roomId=$roomId")
    }

    // ── Daily Usage ──────────────────────────────────────────────────────────

    override suspend fun checkDailyUsage(userId: String): VoiceMatchUsage {
        return try {
            val row = supabaseClient.postgrest["voice_match_usage"]
                .select(Columns.ALL) { filter { eq("user_id", userId) } }
                .decodeList<VoiceMatchUsage>()
                .firstOrNull()

            val today = todayIsoDate()
            if (row == null || row.usageDate != today) {
                // No row, or row is from a previous day → treat as 0 used today
                VoiceMatchUsage(userId = userId, usageDate = today, usedAttempts = 0)
            } else {
                row
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkDailyUsage error", e)
            VoiceMatchUsage(userId = userId, usageDate = todayIsoDate(), usedAttempts = 0)
        }
    }

    override suspend fun incrementUsage(userId: String): Result<Unit> = runCatching {
        val today = todayIsoDate()
        // Upsert: if row exists for today increment, otherwise insert 1
        supabaseClient.postgrest["voice_match_usage"].upsert(
            mapOf(
                "user_id" to userId,
                "usage_date" to today,
                "used_attempts" to 1
            )
        ) {
            // onConflict update used_attempts = used_attempts + 1 via raw SQL not available
            // in Supabase Kotlin client directly, so we do a read-increment-write
        }

        // Read current, increment, write back
        val current = checkDailyUsage(userId)
        val newCount = if (current.usageDate == today) current.usedAttempts + 1 else 1

        supabaseClient.postgrest["voice_match_usage"].upsert(
            mapOf(
                "user_id" to userId,
                "usage_date" to today,
                "used_attempts" to newCount
            )
        )
        Log.d(TAG, "incrementUsage: userId=$userId date=$today count=$newCount")
    }

    // ── Abuse / Cooldown ─────────────────────────────────────────────────────

    override suspend fun checkAbuse(userId: String): CooldownStatus {
        return try {
            val row = supabaseClient.postgrest["voice_match_abuse"]
                .select(Columns.ALL) { filter { eq("user_id", userId) } }
                .decodeList<AbuseRow>()
                .firstOrNull() ?: return CooldownStatus.Clear

            val cooldownUntil = row.cooldownUntil ?: return CooldownStatus.Clear
            val cooldownMs = parseIsoInstant(cooldownUntil)
            val nowMs = System.currentTimeMillis()

            return if (nowMs < cooldownMs) {
                CooldownStatus.OnCooldown(
                    cooldownUntilMs = cooldownMs,
                    remainingMs = cooldownMs - nowMs
                )
            } else {
                CooldownStatus.Clear
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkAbuse error", e)
            CooldownStatus.Clear
        }
    }

    override suspend fun recordLeave(userId: String): Result<Unit> = runCatching {
        val nowMs = System.currentTimeMillis()
        val windowMs = 5 * 60 * 1000L // 5-minute abuse window
        val leaveThreshold = 3

        val row = try {
            supabaseClient.postgrest["voice_match_abuse"]
                .select(Columns.ALL) { filter { eq("user_id", userId) } }
                .decodeList<AbuseRow>()
                .firstOrNull()
        } catch (e: Exception) { null }

        if (row == null) {
            // First leave — create row
            supabaseClient.postgrest["voice_match_abuse"].upsert(
                mapOf(
                    "user_id" to userId,
                    "leave_count" to 1,
                    "window_start" to isoFromMs(nowMs)
                )
            )
            Log.d(TAG, "recordLeave: first leave for $userId")
            return@runCatching
        }

        val windowStart = parseIsoInstant(row.windowStart)
        val withinWindow = (nowMs - windowStart) < windowMs

        val newCount: Int
        val newWindowStart: String

        if (withinWindow) {
            newCount = row.leaveCount + 1
            newWindowStart = row.windowStart
        } else {
            // Window expired — reset
            newCount = 1
            newWindowStart = isoFromMs(nowMs)
        }

        val cooldownUntil: String? = if (withinWindow && newCount >= leaveThreshold) {
            // Apply 5-minute cooldown
            isoFromMs(nowMs + windowMs).also {
                Log.w(TAG, "recordLeave: COOLDOWN applied for userId=$userId until $it")
            }
        } else {
            null
        }

        supabaseClient.postgrest["voice_match_abuse"].upsert(
            buildMap {
                put("user_id", userId)
                put("leave_count", newCount)
                put("window_start", newWindowStart)
                if (cooldownUntil != null) put("cooldown_until", cooldownUntil)
                else put("cooldown_until", null as String?)
            }
        )
    }

    // ── Audit Log ────────────────────────────────────────────────────────────

    override suspend fun recordCallEnd(
        maleId: String,
        femaleId: String,
        roomId: String,
        durationSec: Int
    ): Result<Unit> = runCatching {
        supabaseClient.postgrest["random_voice_calls"].insert(
            mapOf(
                "male_id" to maleId,
                "female_id" to femaleId,
                "room_id" to roomId,
                "ended_at" to isoFromMs(System.currentTimeMillis()),
                "duration_seconds" to durationSec,
                "call_type" to "RANDOM_MATCH"
            )
        )
        Log.d(TAG, "recordCallEnd: roomId=$roomId duration=${durationSec}s")
    }

    // ── User Fetch ───────────────────────────────────────────────────────────

    override suspend fun fetchUser(userId: String): User? = try {
        supabaseClient.postgrest["users"]
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<User>()
    } catch (e: Exception) {
        Log.e(TAG, "fetchUser error for $userId", e)
        null
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun todayIsoDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun isoFromMs(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(ms))

    private fun parseIsoInstant(iso: String): Long = try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Instant.parse(iso).toEpochMilli()
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(iso.take(19))?.time ?: 0L
        }
    } catch (e: Exception) { 0L }
}
