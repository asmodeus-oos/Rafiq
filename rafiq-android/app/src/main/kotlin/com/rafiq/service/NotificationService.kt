package com.rafiq.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.Service
import com.rafiq.MainActivity
import com.rafiq.R
import com.rafiq.data.repository.NotificationRepositoryImpl.NotificationDto
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import io.github.jan.supabase.realtime.decodeRecord

@AndroidEntryPoint
class NotificationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Inject
    lateinit var supabaseClient: SupabaseClient

    companion object {
        const val CHANNEL_ID = "rafiq_notifications"
        const val SERVICE_CHANNEL_ID = "rafiq_service"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(1, createServiceNotification())
        
        listenForNotifications()
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Channel for actual push notifications
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Push Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
            
            // Channel for the foreground service
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Background Sync",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createServiceNotification() = NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
        .setContentTitle("Rafiq Background Sync")
        .setContentText("Listening for new notifications...")
        .setSmallIcon(R.drawable.logo)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun listenForNotifications() {
        val currentUser = supabaseClient.auth.currentUserOrNull() ?: return
        
        val channel = supabaseClient.channel("public-notifications-${currentUser.id}")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "notifications"
        }

        flow.onEach { action ->
            try {
                // Decode the record using Supabase Realtime built-in method
                val dto = action.decodeRecord<NotificationDto>()
                if (dto.recipientId == currentUser.id && dto.senderId != currentUser.id) {
                    showNotification(dto)
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Error decoding notification", e)
            }
        }.launchIn(serviceScope)

        serviceScope.launch {
            supabaseClient.realtime.connect()
            channel.subscribe()
        }
    }

    private fun showNotification(dto: NotificationDto) {
        serviceScope.launch {
            // Fetch sender name for a better notification message
            val senderName = try {
                val user = supabaseClient.postgrest["users"]
                    .select(io.github.jan.supabase.postgrest.query.Columns.list("name")) {
                        filter { eq("id", dto.senderId) }
                    }.decodeSingleOrNull<com.rafiq.domain.model.User>()
                user?.name ?: "Someone"
            } catch (e: Exception) {
                "Someone"
            }

            val intent = Intent(this@NotificationService, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (dto.type == "follow") {
                    putExtra("USER_ID", dto.senderId)
                } else if (dto.type == "message") {
                    putExtra("USER_ID", dto.senderId)
                    putExtra("IS_CHAT", true)
                } else {
                    putExtra("POST_ID", dto.postId)
                }
            }
            
            val pendingIntent = PendingIntent.getActivity(
                this@NotificationService, 
                dto.id.hashCode(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val message = when (dto.type) {
                "follow" -> "$senderName started following you!"
                "mention" -> "$senderName mentioned you in a post."
                "tag" -> "$senderName tagged you."
                "comment" -> "$senderName commented on your post."
                "reply" -> "$senderName replied to your comment."
                "like" -> "$senderName liked your post."
                "message" -> "$senderName sent you a message."
                else -> "You have a new notification from $senderName."
            }

            val builder = NotificationCompat.Builder(this@NotificationService, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("RAFIQ")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(dto.id.hashCode(), builder.build())
        }
    }
}
