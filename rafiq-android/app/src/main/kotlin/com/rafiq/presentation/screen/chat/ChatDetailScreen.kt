package com.rafiq.presentation.screen.chat

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.domain.model.Message
import com.rafiq.presentation.components.chat.MessageBubble
import com.rafiq.presentation.theme.BackgroundPrimary
import com.rafiq.presentation.theme.TextPrimary
import com.rafiq.presentation.theme.GlassBackground
import com.rafiq.presentation.theme.RafiqShapes
import com.rafiq.presentation.theme.PrimaryAccent
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    currentUserId: String?,
    targetUser: com.rafiq.domain.model.User?,
    currentUserAvatar: String? = null,
    messages: List<Message>,
    isTyping: Boolean = false,
    onSendMessage: (String?, String?, Boolean, String?) -> Unit,
    onDeleteMessage: (String) -> Unit = {},
    onTyping: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onVideoCallClick: () -> Unit = {},
    onBack: () -> Unit
) {
    val chatViewModel: ChatViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val icebreakers by chatViewModel.icebreakers.collectAsState()

    var text by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var isRecordingLocked by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    val micOffsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val micOffsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var audioFile by remember { mutableStateOf<java.io.File?>(null) }
    var mediaRecorder by remember { mutableStateOf<android.media.MediaRecorder?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition()
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val stopAndSendRecording = {
        isRecording = false
        isRecordingLocked = false
        try { mediaRecorder?.stop() } catch (e: Exception) {}
        mediaRecorder?.release()
        mediaRecorder = null
        
        if (recordingDuration >= 1 && audioFile != null) {
            onSendMessage("", "file://${audioFile!!.absolutePath}", true, replyingToMessage?.id)
        }
        audioFile = null
        replyingToMessage = null
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                audioFile = java.io.File(context.cacheDir, "audio_chat_${System.currentTimeMillis()}.m4a")
                try {
                    mediaRecorder = @Suppress("DEPRECATION") android.media.MediaRecorder().apply {
                        setAudioSource(android.media.MediaRecorder.AudioSource.MIC)
                        setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                        setOutputFile(audioFile!!.absolutePath)
                        prepare()
                        start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                recordingDuration++
            }
        }
    }
    
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            onSendMessage(null, it.toString(), false, replyingToMessage?.id)
            showAttachmentSheet = false
            replyingToMessage = null
        }
    }

    if (showAttachmentSheet) {
        ModalBottomSheet(onDismissRequest = { showAttachmentSheet = false }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Attachments", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { }) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE91E63).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_gift), contentDescription = "Gift", tint = Color(0xFFE91E63))
                        }
                        Text("Gift", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                        imagePickerLauncher.launch("image/*")
                    }) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF2196F3).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_image), contentDescription = "Gallery", tint = Color(0xFF2196F3))
                        }
                        Text("Gallery", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { }) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF4CAF50).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_map_pin), contentDescription = "Location", tint = Color(0xFF4CAF50))
                        }
                        Text("Location", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Header - 3 Cards Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Card: Back & Avatar
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp).clickable { onBack() }) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_chevron_left), contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(24.dp))
                    }
                    Box {
                        val avatarUrl = targetUser?.avatar
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray)) {
                        if (!avatarUrl.isNullOrBlank()) {
                            coil3.compose.AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user), contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(16.dp))
                        }
                    }
                    val statusColor = when (targetUser?.onlineStatus) {
                        com.rafiq.domain.model.OnlineStatus.ONLINE -> Color(0xFF22C55E)
                        com.rafiq.domain.model.OnlineStatus.IN_CALL -> Color(0xFFFF9800)
                        else -> Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(10.dp)
                            .background(Color.White, CircleShape)
                            .padding(2.dp)
                            .background(statusColor, CircleShape)
                        )
                    }
                }
            }

            // Center Card: Title
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(48.dp).weight(1f).padding(horizontal = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    val displayName = targetUser?.name?.takeIf { it.isNotBlank() } ?: "User Name"
                    Text(text = displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (isTyping) {
                        Text(text = "typing...", color = PrimaryAccent, fontSize = 12.sp)
                    }
                }
            }

            // Right Cards: Voice & Video Call
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = { onCallClick() },
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_phone), contentDescription = "Voice Call", modifier = Modifier.padding(12.dp), tint = Color(0xFF4CAF50))
                }
                Surface(
                    onClick = { onVideoCallClick() },
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_video), contentDescription = "Video Call", modifier = Modifier.padding(12.dp), tint = PrimaryAccent)
                }
            }
        }
        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)
        ) {
            items(messages.reversed()) { msg ->
                val repliedMessage = msg.replyToId?.let { replyId -> messages.find { it.id == replyId } }
                MessageBubble(
                    message = msg,
                    currentUserId = currentUserId,
                    senderAvatarUrl = if (msg.senderId == currentUserId) currentUserAvatar else targetUser?.avatar,
                    repliedMessage = repliedMessage,
                    onReply = { replyingToMessage = it },
                    onDelete = { msg.id?.let { onDeleteMessage(it) } }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Icebreakers for new chats
            if (messages.isEmpty() && icebreakers.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Start the conversation with an icebreaker!",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        icebreakers.forEach { icebreaker ->
                            Surface(
                                onClick = { onSendMessage(icebreaker, null, false, null) },
                                shape = RoundedCornerShape(16.dp),
                                color = PrimaryAccent.copy(alpha = 0.05f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                            ) {
                                Text(
                                    text = icebreaker,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    fontSize = 14.sp,
                                    color = PrimaryAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Input Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassBackground)
        ) {
            if (replyingToMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Replying to a message...", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryAccent)
                        Text(replyingToMessage?.textContent ?: "Media message", fontSize = 12.sp, color = TextPrimary, maxLines = 1)
                    }
                    IconButton(onClick = { replyingToMessage = null }) {
                        Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_x), contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            // Input
            if (isRecordingLocked) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Pink Waveform Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFBE9E7))
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Waveform
                            Row(
                                modifier = Modifier.height(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                val heights = listOf(6, 10, 16, 24, 16, 8, 4, 18, 22, 16, 12, 18, 10, 6, 14, 20, 16)
                                heights.forEach { h ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(h.dp)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(Color(0xFFE57373))
                                    )
                                }
                            }
                            
                            Text(
                                text = String.format("%01d:%02d", recordingDuration / 60, recordingDuration % 60), 
                                color = Color.Black, 
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Bottom Row Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE53935)).alpha(dotAlpha))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Recording", color = Color(0xFFE53935), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    onClick = { 
                                        isRecording = false
                                        isRecordingLocked = false
                                        try { mediaRecorder?.stop() } catch (e: Exception) {}
                                        mediaRecorder?.release()
                                        mediaRecorder = null
                                        audioFile = null
                                        recordingDuration = 0
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFBE9E7)
                                ) {
                                    Text("Cancel", color = Color(0xFFE53935), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
                                }
                                
                                Surface(
                                    onClick = { stopAndSendRecording() },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black
                                ) {
                                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAttachmentSheet = true }) {
                        Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_plus), contentDescription = "Add", tint = Color.Gray)
                    }
                    IconButton(onClick = { /* Open Camera */ }) {
                        Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_camera), contentDescription = "Camera", tint = Color.Gray)
                    }
                    
                    if (isRecording) {
                        Row(
                            modifier = Modifier.weight(1f).height(56.dp).padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color.Red).alpha(dotAlpha))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    String.format("%02d:%02d", recordingDuration / 60, recordingDuration % 60), 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 16.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_chevron_up), contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Text("Lock", color = Color.Gray, fontSize = 12.sp)
                            }
                            Text("< Slide to cancel", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { 
                            text = it
                            onTyping()
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp, max = 120.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Message...", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF1F3F4),
                            unfocusedContainerColor = Color(0xFFF1F3F4)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { /* Toggle Emoji Keyboard */ }) {
                                Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_smile), contentDescription = "Emoji", tint = Color.Gray)
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (text.isNotBlank()) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(PrimaryAccent.copy(alpha = 0.15f)).clickable { 
                            onSendMessage(text, null, false, replyingToMessage?.id)
                            text = ""
                            replyingToMessage = null
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_send), contentDescription = "Send", tint = PrimaryAccent, modifier = Modifier.size(24.dp))
                    }
                } else if (!isRecordingLocked) {
                    Box(
                        modifier = Modifier
                            .offset { androidx.compose.ui.unit.IntOffset(micOffsetX.value.roundToInt(), micOffsetY.value.roundToInt()) }
                            .size(if (isRecording) 72.dp else 56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isRecording) Color.Red else PrimaryAccent.copy(alpha = 0.15f))
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown()
                                        if (text.isBlank() && !isRecordingLocked) {
                                            isRecording = true
                                            coroutineScope.launch { micOffsetX.snapTo(0f); micOffsetY.snapTo(0f) }
                                        }
                                        
                                        var isDragging = true
                                        while (isDragging && isRecording && !isRecordingLocked) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull()
                                            if (change != null) {
                                                val dragX = change.position.x - down.position.x
                                                val dragY = change.position.y - down.position.y
                                                
                                                coroutineScope.launch {
                                                    micOffsetX.snapTo(if (dragX > 0f) 0f else dragX)
                                                    micOffsetY.snapTo(if (dragY > 0f) 0f else dragY)
                                                }
                                                
                                                if (dragY < -150f) {
                                                    isRecordingLocked = true
                                                    coroutineScope.launch { micOffsetX.animateTo(0f); micOffsetY.animateTo(0f) }
                                                    isDragging = false
                                                } else if (dragX < -200f) {
                                                    isRecording = false
                                                    coroutineScope.launch { micOffsetX.animateTo(0f); micOffsetY.animateTo(0f) }
                                                    isDragging = false
                                                }
                                                
                                                if (!change.pressed) {
                                                    isDragging = false
                                                    if (isRecording && !isRecordingLocked) {
                                                        stopAndSendRecording()
                                                    }
                                                    coroutineScope.launch { micOffsetX.animateTo(0f); micOffsetY.animateTo(0f) }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_mic), 
                            contentDescription = "Voice Note", 
                            tint = if (isRecording) Color.White else PrimaryAccent, 
                            modifier = Modifier.size(if (isRecording) 32.dp else 24.dp)
                        )
                    }
                }
            }
        }
    }
    }
}
