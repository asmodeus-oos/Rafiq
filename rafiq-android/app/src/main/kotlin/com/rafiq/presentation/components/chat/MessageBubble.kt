package com.rafiq.presentation.components.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.domain.model.Message
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.GlassBackground
import com.rafiq.presentation.theme.TextPrimary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    currentUserId: String? = null,
    senderAvatarUrl: String? = null,
    repliedMessage: Message? = null,
    onReply: (Message) -> Unit = {},
    onDelete: (Message) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val isMe = currentUserId != null && message.senderId == currentUserId
    val isVoiceMsg = message.isVoice == true
    val backgroundColor = if (isVoiceMsg) androidx.compose.ui.graphics.Color.Transparent else if (isMe) PrimaryAccent.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color(0xFFF1F3F4)
    val textColor = TextPrimary
    
    // Mix of ME Gallery massive radius + Apple HIG chat bubble logic
    val shape = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = if (isMe) 24.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 24.dp
    )

    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            if (!senderAvatarUrl.isNullOrBlank()) {
                coil3.compose.AsyncImage(
                    model = senderAvatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(28.dp).clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (message.deletedForEveryone != true) {
                                coroutineScope.launch {
                                    val newOffset = (offsetX.value + dragAmount).coerceIn(-150f, 150f)
                                    offsetX.snapTo(newOffset)
                                    if (kotlin.math.abs(newOffset) == 150f && kotlin.math.abs(offsetX.value) < 150f) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onReply(message)
                                    }
                                }
                            }
                        }
                    )
                }
                .widthIn(max = 280.dp)
                .clip(shape)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (message.deletedForEveryone != true) {
                            showMenu = true
                        }
                    }
                )
                .background(if (message.deletedForEveryone == true) GlassBackground else backgroundColor)
                .padding(if (isVoiceMsg && message.textContent.isNullOrBlank()) 0.dp else 16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Reply") },
                    onClick = {
                        showMenu = false
                        onReply(message)
                    }
                )
                if (isMe) {
                    DropdownMenuItem(
                        text = { Text("Delete for everyone", color = androidx.compose.ui.graphics.Color.Red) },
                        onClick = {
                            showMenu = false
                            onDelete(message)
                        }
                    )
                }
            }
            Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                if (message.deletedForEveryone == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_ban),
                            contentDescription = "Deleted",
                            tint = androidx.compose.ui.graphics.Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("This message was deleted.", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                } else {
                    if (message.replyToId != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f))
                                .padding(start = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().background(if (isMe) PrimaryAccent.copy(alpha = 0.8f) else GlassBackground).padding(8.dp)) {
                                Column {
                                    Text(if (repliedMessage?.senderId == currentUserId) "You" else "Other User", color = textColor, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    Text(repliedMessage?.textContent ?: "Media message", color = textColor.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (message.isVoice == true && message.mediaUrl != null) {
                        com.rafiq.presentation.screen.profile.AudioPlayerComponent(
                            audioUrl = message.mediaUrl,
                            containerColor = androidx.compose.ui.graphics.Color(0xFFF4F0FB), // Match Image 2 exactly
                            contentColor = PrimaryAccent,
                            onSurfaceColor = androidx.compose.ui.graphics.Color.Black
                        )
                    } else if (message.isVoice == true) {
                        // Fallback if mediaUrl is missing
                        Text("Voice Message Loading...", color = textColor, fontSize = 12.sp)
                    } else if (message.mediaUrl != null) {
                        coil3.compose.AsyncImage(
                            model = message.mediaUrl,
                            contentDescription = "Image message",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        if (!message.textContent.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (!message.textContent.isNullOrBlank()) {
                        Text(
                            text = message.textContent ?: "",
                            color = textColor,
                            fontSize = 16.sp
                        )
                    }
                    
                    if (isMe) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val isRead = message.status == "READ" || message.isRead == true
                        val isDelivered = message.status == "DELIVERED"
                        
                        androidx.compose.material3.Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                id = if (isRead || isDelivered) com.composables.icons.lucide.R.drawable.lucide_ic_check_check else com.composables.icons.lucide.R.drawable.lucide_ic_check
                            ),
                            contentDescription = if (isRead) "Read" else if (isDelivered) "Delivered" else "Sent",
                            tint = if (isRead) androidx.compose.ui.graphics.Color(0xFF34B7F1) else textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            if (!senderAvatarUrl.isNullOrBlank()) {
                coil3.compose.AsyncImage(
                    model = senderAvatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(28.dp).clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(PrimaryAccent),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
