package com.rafiq.domain.manager

import android.content.Context
import android.util.Log
import com.rafiq.domain.model.CooldownStatus
import com.rafiq.domain.model.Gender
import com.rafiq.domain.model.Tier
import com.rafiq.domain.model.User
import com.rafiq.domain.model.VoiceMatchQueueEntry
import com.rafiq.domain.model.VoiceMatchState
import com.rafiq.domain.repository.VoiceMatchRepository
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * VoiceMatchService — sovereign singleton for random voice matchmaking.
 *
 * Responsibilities:
 *   1. Manage MALE / FEMALE queues via [VoiceMatchRepository]
 *   2. Enforce subscription daily limits via [SubscriptionManager]
 *   3. Enforce abuse cooldowns
 *   4. Match users: Male ↔ Female only
 *   5. Emit Socket.IO events for real-time matching; fall back to polling
 *   6. Trigger [CallManager.startOrJoinCall] when a match is found
 *
 * Does NOT handle WebRTC, audio routing, or UI — those belong to [CallManager].
 */
object VoiceMatchService {

    private const val TAG = "VoiceMatchService"
    private const val SIGNALING_URL = "https://rafiq-signaling.onrender.com"
    private const val POLL_INTERVAL_MS = 2000L
    private const val SOCKET_MATCH_TIMEOUT_MS = 4000L

    // ── Public State ─────────────────────────────────────────────────────────

    private val _state = MutableStateFlow<VoiceMatchState>(VoiceMatchState.Idle)
    val state: StateFlow<VoiceMatchState> = _state.asStateFlow()

    // ── Internal State ───────────────────────────────────────────────────────

    private var repository: VoiceMatchRepository? = null
    private var applicationContext: Context? = null

    private var currentUserId: String = ""
    private var currentUserGender: String = ""
    private var currentUserTier: Tier = Tier.FREE
    private var currentUser: User? = null
    private var myQueueEntryId: String? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollJob: Job? = null
    private var socket: Socket? = null
    private var socketMatchReceived = false

    private var callStartEpochMs: Long = 0L

    // ── Initialization ───────────────────────────────────────────────────────

    fun initialize(context: Context, repo: VoiceMatchRepository) {
        applicationContext = context.applicationContext
        repository = repo
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Entry point called by the ViewModel when the user presses "Start Search".
     * Performs limit/abuse checks then joins the queue.
     */
    fun joinQueue(user: User) {
        if (_state.value is VoiceMatchState.Searching) return
        currentUser = user
        currentUserId = user.id
        currentUserGender = user.gender.name  // "MALE" or "FEMALE"
        currentUserTier = user.tier

        serviceScope.launch {
            _state.value = VoiceMatchState.CheckingLimits

            // 1. Check abuse cooldown
            val repo = repository ?: return@launch emitError("Service not initialized")
            val cooldown = repo.checkAbuse(currentUserId)
            if (cooldown is CooldownStatus.OnCooldown) {
                Log.w(TAG, "joinQueue blocked: user is on cooldown until ${cooldown.cooldownUntilMs}")
                _state.value = VoiceMatchState.OnCooldown(
                    cooldownUntilMs = cooldown.cooldownUntilMs,
                    remainingMs = cooldown.remainingMs
                )
                return@launch
            }

            // 2. Check daily limit (only for non-Elite tiers)
            if (!SubscriptionManager.isUnlimited(currentUserTier)) {
                val usage = repo.checkDailyUsage(currentUserId)
                val limit = SubscriptionManager.getDailyMatchLimit(currentUserTier)
                if (usage.usedAttempts >= limit) {
                    Log.w(TAG, "joinQueue blocked: daily limit reached (${usage.usedAttempts}/$limit)")
                    _state.value = VoiceMatchState.LimitReached(
                        used = usage.usedAttempts,
                        max = limit,
                        tier = SubscriptionManager.tierDisplayName(currentUserTier)
                    )
                    return@launch
                }
            }

            // 3. Only MALE / FEMALE may match
            if (currentUserGender != "MALE" && currentUserGender != "FEMALE") {
                emitError("Random match requires your gender to be set to Male or Female in your profile.")
                return@launch
            }

            // 4. Join the queue
            val result = repo.joinQueue(currentUserId, currentUserGender, currentUserTier.name)
            result.onFailure {
                emitError("Failed to join queue: ${it.message}")
                return@launch
            }
            myQueueEntryId = result.getOrNull()?.id
            _state.value = VoiceMatchState.Searching
            Log.d(TAG, "Joined queue as $currentUserGender, entryId=$myQueueEntryId")

            // 5. Connect socket and start match engine
            connectSocket()
            startMatchLoop()
        }
    }

    /**
     * Called when the user cancels the search.
     * Records the leave for abuse tracking.
     */
    fun leaveQueue(isUserInitiated: Boolean = true) {
        pollJob?.cancel()
        pollJob = null

        serviceScope.launch {
            val repo = repository ?: return@launch
            repo.leaveQueue(currentUserId)
            if (isUserInitiated) {
                repo.recordLeave(currentUserId)
                Log.d(TAG, "leaveQueue: user-initiated leave recorded")
            }
            disconnectSocket()
            _state.value = VoiceMatchState.Cancelled
        }
    }

    /**
     * Called by [CallManager] when a random call ends so we can log it and
     * increment the daily usage counter.
     */
    fun onCallEnded(maleId: String, femaleId: String, roomId: String, durationSec: Int) {
        serviceScope.launch {
            val repo = repository ?: return@launch
            repo.recordCallEnd(maleId, femaleId, roomId, durationSec)
            repo.incrementUsage(currentUserId)
            Log.d(TAG, "onCallEnded: logged $durationSec seconds for roomId=$roomId")
        }
        _state.value = VoiceMatchState.Idle
        callStartEpochMs = 0L
    }

    /** Resets state back to Idle (e.g. after returning from the call screen). */
    fun reset() {
        pollJob?.cancel()
        pollJob = null
        disconnectSocket()
        myQueueEntryId = null
        socketMatchReceived = false
        _state.value = VoiceMatchState.Idle
    }

    // ── Socket.IO ────────────────────────────────────────────────────────────

    private fun connectSocket() {
        if (socket?.connected() == true) return
        try {
            socket = IO.socket(SIGNALING_URL)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected — emitting JOIN_RANDOM_QUEUE")
                socket?.emit(
                    "JOIN_RANDOM_QUEUE",
                    JSONObject().apply {
                        put("userId", currentUserId)
                        put("gender", currentUserGender)
                        put("tier", currentUserTier.name)
                    }
                )
            }

            socket?.on("MATCH_FOUND") { args ->
                val data = (args.firstOrNull() as? JSONObject) ?: return@on
                val roomId = data.optString("roomId", "")
                val partnerId = data.optString("partnerId", "")
                val partnerName = data.optString("partnerName", "Partner")
                val partnerAvatar = data.optString("partnerAvatar", "")
                Log.d(TAG, "Socket MATCH_FOUND: roomId=$roomId partnerId=$partnerId")

                if (roomId.isNotBlank() && partnerId.isNotBlank()) {
                    socketMatchReceived = true
                    serviceScope.launch {
                        handleMatch(
                            partnerEntryId = "",      // Socket-matched — no DB entry ID needed
                            partnerUserId = partnerId,
                            partnerName = partnerName,
                            partnerAvatar = partnerAvatar,
                            roomId = roomId
                        )
                    }
                }
            }

            socket?.on("USER_DISCONNECTED") { args ->
                val data = (args.firstOrNull() as? JSONObject) ?: return@on
                val disconnectedId = data.optString("userId")
                Log.w(TAG, "USER_DISCONNECTED: $disconnectedId")
                // If partner disconnected during a call, CallManager handles it via WebRTC
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket connection failed, using polling fallback", e)
        }
    }

    private fun disconnectSocket() {
        try {
            if (_state.value !is VoiceMatchState.Matched) {
                socket?.emit("LEAVE_RANDOM_QUEUE", JSONObject().put("userId", currentUserId))
            }
            socket?.disconnect()
            socket?.off()
        } catch (e: Exception) {
            Log.e(TAG, "Socket disconnect error", e)
        }
        socket = null
    }

    // ── Match Loop (DB Polling Fallback) ──────────────────────────────────────

    private fun startMatchLoop() {
        pollJob?.cancel()
        pollJob = serviceScope.launch {
            val oppositeGender = if (currentUserGender == "MALE") "FEMALE" else "MALE"
            Log.d(TAG, "Match loop started — looking for $oppositeGender")

            while (isActive) {
                delay(POLL_INTERVAL_MS)

                // If socket already delivered a match, stop polling
                if (socketMatchReceived) {
                    Log.d(TAG, "Match loop stopping — socket match already handled")
                    return@launch
                }

                val repo = repository ?: return@launch
                val match = repo.findMatch(currentUserId, oppositeGender)

                if (match != null && match.userId != currentUserId) {
                    Log.d(TAG, "DB match found: partnerId=${match.userId} entryId=${match.id}")
                    val partnerUser = repo.fetchUser(match.userId)

                    val roomId = buildRoomId(currentUserId, match.userId)

                    // Mark both matched in DB
                    myQueueEntryId?.let { myId ->
                        repo.markMatched(
                            myEntryId = myId,
                            myUserId = currentUserId,
                            partnerEntryId = match.id,
                            partnerUserId = match.userId,
                            roomId = roomId
                        )
                    }

                    handleMatch(
                        partnerEntryId = match.id,
                        partnerUserId = match.userId,
                        partnerName = partnerUser?.name?.takeIf { it.isNotBlank() }
                            ?: partnerUser?.username?.takeIf { it.isNotBlank() }
                            ?: "Partner",
                        partnerAvatar = partnerUser?.avatar ?: "",
                        roomId = roomId
                    )
                    return@launch
                }
            }
        }
    }

    // ── Match Handling ────────────────────────────────────────────────────────

    private suspend fun handleMatch(
        partnerEntryId: String,
        partnerUserId: String,
        partnerName: String,
        partnerAvatar: String,
        roomId: String
    ) {
        pollJob?.cancel()
        disconnectSocket()

        val partnerUser = repository?.fetchUser(partnerUserId) ?: User(
            id = partnerUserId,
            name = partnerName,
            avatar = partnerAvatar
        )

        callStartEpochMs = System.currentTimeMillis()

        Log.d(TAG, "Match confirmed: partner=$partnerName roomId=$roomId")

        _state.value = VoiceMatchState.Matched(partner = partnerUser, roomId = roomId)

        // Launch the call via CallManager after a brief display delay
        delay(1500)
        val ctx = applicationContext ?: return
        CallManager.startOrJoinCall(roomId, isVideo = false)

        // Enforce call duration limit for FREE tier
        val durationLimit = SubscriptionManager.getCallDurationLimitMs(currentUserTier)
        if (durationLimit != null) {
            Log.d(TAG, "FREE tier: enforcing ${durationLimit / 1000}s call duration limit")
            serviceScope.launch {
                delay(durationLimit)
                if (CallManager.callState.value.status == "In Voice Call" ||
                    CallManager.callState.value.status == "In Call"
                ) {
                    Log.w(TAG, "FREE tier call time limit reached — ending call")
                    CallManager.endCall()
                }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun buildRoomId(id1: String, id2: String): String =
        listOf(id1, id2).sorted().joinToString("_")

    private fun emitError(message: String) {
        Log.e(TAG, "Error: $message")
        _state.value = VoiceMatchState.Error(message)
    }
}
