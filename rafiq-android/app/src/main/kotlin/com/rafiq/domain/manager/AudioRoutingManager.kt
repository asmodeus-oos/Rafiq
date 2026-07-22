package com.rafiq.domain.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioRoute {
    EARPIECE,
    SPEAKER,
    BLUETOOTH,
    WIRED_HEADSET
}

object AudioRoutingManager {
    private const val TAG = "AudioRoutingManager"

    private val _currentRoute = MutableStateFlow(AudioRoute.EARPIECE)
    val currentRoute: StateFlow<AudioRoute> = _currentRoute.asStateFlow()

    private var audioFocusRequest: AudioFocusRequest? = null
    private var isCallActive = false

    fun startCallAudio(context: Context) {
        isCallActive = true
        val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        try {
            // 1. Force Communication Mode
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isMicrophoneMute = false

            // 2. Request Audio Focus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener { focusChange ->
                        Log.d(TAG, "Audio focus changed: $focusChange")
                    }
                    .build()

                audioManager.requestAudioFocus(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }

            // 3. Force Initial Route to EARPIECE by Default
            val previous = _currentRoute.value
            setRoute(context, AudioRoute.EARPIECE, isUserExplicit = true)
            
            // 4. Start 1-second Periodic Debug Logging
            AudioDebugManager.startPeriodicLogging(context)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting call audio", e)
        }
    }

    fun setUserRoute(context: Context, newRoute: AudioRoute) {
        setRoute(context, newRoute, isUserExplicit = true)
    }

    fun handleHardwareHeadsetEvent(context: Context, isConnected: Boolean, isBluetooth: Boolean = false) {
        if (!isCallActive) return
        val targetRoute = if (isConnected) {
            if (isBluetooth) AudioRoute.BLUETOOTH else AudioRoute.WIRED_HEADSET
        } else {
            AudioRoute.EARPIECE
        }
        setRoute(context, targetRoute, isUserExplicit = false)
    }

    private fun setRoute(context: Context, route: AudioRoute, isUserExplicit: Boolean) {
        val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val devices = audioManager.availableCommunicationDevices
                when (route) {
                    AudioRoute.SPEAKER -> {
                        val speaker = devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                        if (speaker != null) {
                            audioManager.setCommunicationDevice(speaker)
                        } else {
                            @Suppress("DEPRECATION")
                            audioManager.isSpeakerphoneOn = true
                        }
                    }
                    AudioRoute.EARPIECE -> {
                        val earpiece = devices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                        if (earpiece != null) {
                            audioManager.setCommunicationDevice(earpiece)
                        } else {
                            audioManager.clearCommunicationDevice()
                            @Suppress("DEPRECATION")
                            audioManager.isSpeakerphoneOn = false
                        }
                    }
                    AudioRoute.BLUETOOTH -> {
                        val btDevice = devices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        }
                        if (btDevice != null) {
                            audioManager.setCommunicationDevice(btDevice)
                        }
                    }
                    AudioRoute.WIRED_HEADSET -> {
                        val headset = devices.firstOrNull {
                            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                            it.type == AudioDeviceInfo.TYPE_USB_HEADSET
                        }
                        if (headset != null) {
                            audioManager.setCommunicationDevice(headset)
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = (route == AudioRoute.SPEAKER)
            }

            val prevRoute = _currentRoute.value
            _currentRoute.value = route
            if (prevRoute != route) {
                AudioDebugManager.logRouteStateChange(context, prevRoute, route, "AudioRoutingManager.setRoute(explicit=$isUserExplicit)")
            }
            Log.d(TAG, "Audio route set to: $route (explicit: $isUserExplicit)")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting audio route to $route", e)
        }
    }

    fun stopCallAudio(context: Context) {
        isCallActive = false
        val audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = false
            }
            audioManager.mode = AudioManager.MODE_NORMAL

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping call audio", e)
        }

        _currentRoute.value = AudioRoute.EARPIECE
        audioFocusRequest = null
        AudioDebugManager.stopPeriodicLogging()
    }
}
