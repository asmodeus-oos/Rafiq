package com.rafiq.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rafiq.MainActivity
import com.rafiq.R

class CallForegroundService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var ringtonePlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CallForegroundService onCreate executed (Service started)")
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_STOP_CALL_SERVICE -> {
                Log.d(TAG, "CallForegroundService stopping via ACTION_STOP_CALL_SERVICE")
                stopRingtone()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_RINGING -> {
                startRingtone()
                return START_STICKY
            }
            ACTION_STOP_RINGING -> {
                stopRingtone()
                return START_STICKY
            }
        }

        val callerName = intent?.getStringExtra(EXTRA_CALLER_NAME) ?: "Rafiq Voice Call"
        val notification = createNotification(callerName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            try {
                startForeground(NOTIFICATION_ID, notification, serviceType)
                Log.d(TAG, "CallForegroundService promoted to foreground (type: $serviceType)")
            } catch (e: Exception) {
                startForeground(NOTIFICATION_ID, notification)
                Log.d(TAG, "CallForegroundService promoted to foreground fallback")
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "CallForegroundService promoted to foreground legacy")
        }

        return START_STICKY
    }

    // Ringtone playback managed by service, NOT by Compose screens
    // Uses STREAM_VOICE_CALL so it never interferes with AudioManager routing
    private fun startRingtone() {
        if (ringtonePlayer != null) return
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            ringtonePlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(this@CallForegroundService, ringtoneUri)
                isLooping = true
                prepare()
                start()
            }
            Log.d(TAG, "Ringtone started (USAGE_VOICE_COMMUNICATION_SIGNALLING)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ringtone", e)
        }
    }

    private fun stopRingtone() {
        try {
            ringtonePlayer?.stop()
            ringtonePlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop ringtone", e)
        }
        ringtonePlayer = null
        Log.d(TAG, "Ringtone stopped")
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Rafiq:CallWakeLock")
            wakeLock?.acquire(3 * 60 * 60 * 1000L)
            Log.d(TAG, "WakeLock acquired (PARTIAL_WAKE_LOCK)")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d(TAG, "WakeLock released")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Voice Call",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing Rafiq voice/video call audio session"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(callerName: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rafiq Call - $callerName")
            .setContentText("Tap to return to call screen • Audio Active")
            .setSmallIcon(R.drawable.logo)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        Log.d(TAG, "CallForegroundService onDestroy executed (Service destroyed)")
        stopRingtone()
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ServiceDiagnostic"
        const val CHANNEL_ID = "rafiq_active_call_channel"
        const val NOTIFICATION_ID = 99881
        const val EXTRA_CALLER_NAME = "extra_caller_name"
        const val ACTION_STOP_CALL_SERVICE = "action_stop_call_service"
        const val ACTION_START_RINGING = "action_start_ringing"
        const val ACTION_STOP_RINGING = "action_stop_ringing"

        fun startService(context: Context, callerName: String) {
            try {
                val intent = Intent(context, CallForegroundService::class.java).apply {
                    putExtra(EXTRA_CALLER_NAME, callerName)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun startRinging(context: Context) {
            try {
                val intent = Intent(context, CallForegroundService::class.java).apply {
                    action = ACTION_START_RINGING
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopRinging(context: Context) {
            try {
                val intent = Intent(context, CallForegroundService::class.java).apply {
                    action = ACTION_STOP_RINGING
                }
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, CallForegroundService::class.java).apply {
                    action = ACTION_STOP_CALL_SERVICE
                }
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
