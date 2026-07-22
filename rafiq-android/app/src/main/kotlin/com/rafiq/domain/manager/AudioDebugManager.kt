package com.rafiq.domain.manager

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object AudioDebugManager {
    private const val TAG = "AudioDebugManager"

    private var loggingJob: Job? = null
    private var lastRecordedRoute: AudioRoute? = null
    private var secondsCounter = 0

    fun startPeriodicLogging(context: Context) {
        if (loggingJob?.isActive == true) return
        secondsCounter = 0
        val appContext = context.applicationContext

        loggingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(1000L)
                secondsCounter++
                logCurrentAudioState(appContext)
            }
        }
    }

    fun stopPeriodicLogging() {
        loggingJob?.cancel()
        loggingJob = null
        secondsCounter = 0
    }

    fun logRouteStateChange(context: Context, previousRoute: AudioRoute, newRoute: AudioRoute, callerComponent: String) {
        val stackTrace = Throwable().stackTrace.take(8).joinToString("\n    at ") { it.toString() }
        val threadName = Thread.currentThread().name
        
        Log.e(
            TAG,
            """
            ====================== AUDIO ROUTE CHANGE ALERT ======================
            Speaker/Route state changed!
            
            Previous: $previousRoute
            Current:  $newRoute
            Caller:   $callerComponent
            Thread:   $threadName
            
            Stack trace:
                at $stackTrace
            =======================================================================
            """.trimIndent()
        )

        lastRecordedRoute = newRoute
    }

    private fun logCurrentAudioState(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        val modeStr = when (audioManager.mode) {
            AudioManager.MODE_NORMAL -> "MODE_NORMAL"
            AudioManager.MODE_RINGTONE -> "MODE_RINGTONE"
            AudioManager.MODE_IN_CALL -> "MODE_IN_CALL"
            AudioManager.MODE_IN_COMMUNICATION -> "MODE_IN_COMMUNICATION"
            else -> "MODE_UNKNOWN(${audioManager.mode})"
        }

        val routeStr = AudioRoutingManager.currentRoute.value.name

        val commDeviceStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val device = audioManager.communicationDevice
            if (device != null) "${device.productName} (Type: ${getDeviceTypeName(device.type)})" else "DEFAULT / NONE"
        } else {
            @Suppress("DEPRECATION")
            if (audioManager.isSpeakerphoneOn) "LOUDSPEAKER" else "EARPIECE"
        }

        val callStateStr = CallManager.callState.value.status
        val isMutedStr = if (CallManager.callState.value.isMuted) "MUTED" else "UNMUTED"
        val formattedTime = String.format("%02d:%02d", secondsCounter / 60, secondsCounter % 60)

        Log.d(
            TAG,
            "[$formattedTime] Mode: $modeStr | Route: $routeStr | Device: $commDeviceStr | WebRTC: $callStateStr | Mic: $isMutedStr | Owner: CallForegroundService"
        )
    }

    private fun getDeviceTypeName(type: Int): String {
        return when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "EARPIECE"
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "SPEAKER"
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "WIRED_HEADSET"
            AudioDeviceInfo.TYPE_USB_HEADSET -> "USB_HEADSET"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "BLUETOOTH_SCO"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "BLUETOOTH_A2DP"
            else -> "TYPE_$type"
        }
    }
}
