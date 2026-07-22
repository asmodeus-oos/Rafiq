package com.rafiq.presentation.screen.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafiq.domain.model.Notification
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPost: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_arrow_left),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (notifications.any { !it.read }) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Mark all as read", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_bell_off),
                        contentDescription = "No notifications",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No notifications yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            if (!notification.read) viewModel.markAsRead(notification.id)
                            when (notification.type) {
                                "follow" -> onNavigateToProfile(notification.senderId)
                                "message", "voice_message" -> onNavigateToChat(notification.senderId)
                                "profile_visit" -> onNavigateToProfile(notification.senderId)
                                else -> notification.postId?.let { onNavigateToPost(it) }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with type icon overlay
            Box(modifier = Modifier.size(52.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .align(Alignment.TopStart)
                ) {
                    if (notification.sender?.avatar.isNullOrBlank()) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        AsyncImage(
                            model = notification.sender?.avatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }

                // Type badge
                val (badgeIcon, badgeColor) = when (notification.type) {
                    "like" -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_heart, Color(0xFFE91E63))
                    "comment" -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_message_square, Color(0xFF2196F3))
                    "reply" -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_corner_down_right, Color(0xFF9C27B0))
                    "follow" -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_user_plus, Color(0xFF4CAF50))
                    "message" -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_message_circle, Color(0xFF00BCD4))
                    "voice_message" -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_mic, Color(0xFFFF9800))
                    "profile_visit" -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_eye, Color(0xFF795548))
                    "mention" -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_at_sign, Color(0xFF3F51B5))
                    else -> Pair(com.composables.icons.lucide.R.drawable.lucide_ic_bell, Color.Gray)
                }

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = badgeIcon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                val actionText = when (notification.type) {
                    "follow" -> "started following you"
                    "mention" -> "mentioned you in a comment"
                    "comment" -> "commented on your post"
                    "reply" -> "replied to your comment"
                    "like" -> "liked your post"
                    "message" -> "sent you a message"
                    "voice_message" -> "sent you a voice message 🎙️"
                    "profile_visit" -> "visited your profile 👁️"
                    else -> "interacted with you"
                }
                Text(
                    text = "${notification.sender?.name ?: "Someone"} $actionText",
                    fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Timestamp
                if (notification.timestamp > 0) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = formatNotificationTime(notification.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!notification.read) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFff385c))
                )
            }
        }
    }
}

private fun formatNotificationTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 7 * 86_400_000 -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
