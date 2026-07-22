package com.rafiq.presentation.screen.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.domain.model.VoiceRoom
import com.rafiq.domain.model.RoomParticipant
import com.rafiq.domain.model.ParticipantRole
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRoomDetailScreen(
    room: VoiceRoom,
    onLeaveRoom: () -> Unit
) {
    var isMuted by remember { mutableStateOf(true) }
    var handRaised by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(room.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${room.participantCount} listening • ${room.activeSpeakers} speaking", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLeaveRoom) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_chevron_down),
                            contentDescription = "Minimize"
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = onLeaveRoom,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text("Leave quietly ✌️", color = Color(0xFFE53935), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color(0xFFF1F3F4) else PrimaryAccent)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isMuted) com.composables.icons.lucide.R.drawable.lucide_ic_mic_off
                                else com.composables.icons.lucide.R.drawable.lucide_ic_mic
                            ),
                            contentDescription = "Mute",
                            tint = if (isMuted) TextPrimary else Color.White
                        )
                    }

                    // Raise Hand
                    IconButton(
                        onClick = { handRaised = !handRaised },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (handRaised) Color(0xFFFFE082) else Color(0xFFF1F3F4))
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_hand),
                            contentDescription = "Raise Hand",
                            tint = if (handRaised) Color(0xFFFF8F00) else TextPrimary
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8FB))
                .padding(16.dp)
        ) {
            Text("Speakers", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            // Speakers Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(room.activeSpeakers.coerceAtLeast(1)) { index ->
                    SpeakerBubble(
                        name = if (index == 0) "Host" else "Speaker ${index + 1}",
                        isSpeaking = index == 0,
                        pulseAlpha = pulseAlpha
                    )
                }
            }
        }
    }
}

@Composable
fun SpeakerBubble(name: String, isSpeaking: Boolean, pulseAlpha: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isSpeaking) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .graphicsLayer { alpha = pulseAlpha }
                        .clip(CircleShape)
                        .background(PrimaryAccent.copy(alpha = 0.3f))
                )
            }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = TextPrimary)
    }
}
