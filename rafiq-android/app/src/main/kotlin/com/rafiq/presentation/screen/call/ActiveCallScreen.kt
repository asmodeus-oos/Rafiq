package com.rafiq.presentation.screen.call

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.rafiq.domain.model.GlobalCallState
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import kotlin.math.roundToInt

@Composable
fun ActiveCallScreen(
    onCallEnded: () -> Unit,
    viewModel: ActiveCallViewModel = hiltViewModel()
) {
    val callState by viewModel.callState.collectAsState()
    val context = LocalContext.current
    
    // Draggable self avatar card offset
    var topAvatarOffsetX by remember { mutableFloatStateOf(0f) }
    var topAvatarOffsetY by remember { mutableFloatStateOf(0f) }

    val handleMinimize = {
        GlobalCallState.minimize()
        onCallEnded() // Pop back to standard app navigation without destroying call connection
    }

    // Intercept Android System Back press/gesture -> Minimize to normal app screens
    BackHandler(enabled = !callState.isEnded) {
        handleMinimize()
    }

    DisposableEffect(Unit) {
        viewModel.initializeWebrtc()
        onDispose {
            // Keep WebRTC call alive in CallManager singleton when minimized!
        }
    }

    LaunchedEffect(callState.isEnded) {
        if (callState.isEnded) {
            onCallEnded()
        }
    }

    if (callState.isVideoCall) {
        // VIDEO CALL INTERFACE (White Theme & Draggable PIP Alignment)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Remote Video or Camera Off View
            if (callState.remoteVideoTrack != null && callState.isCameraEnabled) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            init(viewModel.eglBaseContext, null)
                            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                            setEnableHardwareScaler(true)
                            callState.remoteVideoTrack?.addSink(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = Color(0xFF6C5CE7).copy(alpha = 0.08f),
                            shape = CircleShape,
                            modifier = Modifier.size(140.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6C5CE7).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (callState.callerAvatar.isNotBlank()) {
                                    AsyncImage(
                                        model = callState.callerAvatar,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                                        contentDescription = null,
                                        tint = Color(0xFF6C5CE7),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = callState.callerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (!callState.isCameraEnabled) "Camera is off. Tap camera button to enable video." else callState.status,
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Top Bar with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(onClick = { handleMinimize() }) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_chevron_left),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video Call",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            // Local Video (Draggable PIP)
            if (callState.localVideoTrack != null && callState.isCameraEnabled) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(topAvatarOffsetX.roundToInt(), topAvatarOffsetY.roundToInt()) }
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 72.dp, end = 20.dp)
                        .size(width = 110.dp, height = 160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.DarkGray)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                topAvatarOffsetX += dragAmount.x
                                topAvatarOffsetY += dragAmount.y
                            }
                        }
                ) {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceViewRenderer(ctx).apply {
                                init(viewModel.eglBaseContext, null)
                                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                                setMirror(true)
                                callState.localVideoTrack?.addSink(this)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Bottom Dual-Tone Light Mode Action Cards
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 40.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute Dual-Tone Card
                Surface(
                    onClick = { viewModel.toggleMute() },
                    color = if (callState.isMuted) Color(0xFFFEE2E2) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 64.dp, height = 64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                id = if (callState.isMuted) com.composables.icons.lucide.R.drawable.lucide_ic_mic_off
                                else com.composables.icons.lucide.R.drawable.lucide_ic_mic
                            ),
                            contentDescription = "Mute",
                            tint = if (callState.isMuted) Color(0xFFEF4444) else Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Camera Toggle Dual-Tone Card
                Surface(
                    onClick = { viewModel.toggleCamera() },
                    color = if (callState.isCameraEnabled) Color(0xFFE0E7FF) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 64.dp, height = 64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                id = if (callState.isCameraEnabled) com.composables.icons.lucide.R.drawable.lucide_ic_video
                                else com.composables.icons.lucide.R.drawable.lucide_ic_video_off
                            ),
                            contentDescription = "Camera",
                            tint = if (callState.isCameraEnabled) Color(0xFF6C5CE7) else Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // End Call Card
                Surface(
                    onClick = { viewModel.endCall() },
                    color = Color(0xFFEF4444),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(width = 80.dp, height = 64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_phone_off),
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Speaker / Earphone Dual-Tone Card (headphones icon for earphone, volume_2 icon for speaker)
                Surface(
                    onClick = { viewModel.toggleSpeaker() },
                    color = if (callState.isSpeakerOn) Color(0xFFE0E7FF) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 64.dp, height = 64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                id = if (callState.isSpeakerOn) com.composables.icons.lucide.R.drawable.lucide_ic_volume_2
                                else com.composables.icons.lucide.R.drawable.lucide_ic_headphones
                            ),
                            contentDescription = "Audio Output",
                            tint = if (callState.isSpeakerOn) Color(0xFF6C5CE7) else Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    } else {
        // VOICE CALL INTERFACE (Light Mode White Background, Dual-tone Cards, Draggable Avatar, Headphones / Speaker Icons)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top Bar with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(onClick = { handleMinimize() }) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_chevron_left),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Voice Call",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            // Draggable Top Self Avatar Card
            Surface(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .offset { IntOffset(topAvatarOffsetX.roundToInt(), topAvatarOffsetY.roundToInt()) }
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 72.dp, start = 20.dp)
                    .size(80.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            topAvatarOffsetX += dragAmount.x
                            topAvatarOffsetY += dragAmount.y
                        }
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (callState.myAvatar.isNotBlank()) {
                        AsyncImage(
                            model = callState.myAvatar,
                            contentDescription = "Me",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                            contentDescription = "Me",
                            tint = Color(0xFF6C5CE7),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Center Partner Avatar & Info
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFF6C5CE7).copy(alpha = 0.08f),
                    shape = CircleShape,
                    modifier = Modifier.size(160.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6C5CE7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (callState.callerAvatar.isNotBlank()) {
                            AsyncImage(
                                model = callState.callerAvatar,
                                contentDescription = "Partner Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                                contentDescription = null,
                                tint = Color(0xFF6C5CE7),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = callState.callerName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (callState.isSpeakerOn) "🔊 Speaker On" else "🎧 Earphone Mode",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            // Bottom Dual-Tone Light Mode Action Cards
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 40.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Mute Dual-Tone Card
                Surface(
                    onClick = { viewModel.toggleMute() },
                    color = if (callState.isMuted) Color(0xFFFEE2E2) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 72.dp, height = 72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                id = if (callState.isMuted) com.composables.icons.lucide.R.drawable.lucide_ic_mic_off
                                else com.composables.icons.lucide.R.drawable.lucide_ic_mic
                            ),
                            contentDescription = "Mute",
                            tint = if (callState.isMuted) Color(0xFFEF4444) else Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // End Call Card
                Surface(
                    onClick = { viewModel.endCall() },
                    color = Color(0xFFEF4444),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(width = 88.dp, height = 72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_phone_off),
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Speaker / Earphone Dual-Tone Card
                Surface(
                    onClick = { viewModel.toggleSpeaker() },
                    color = if (callState.isSpeakerOn) Color(0xFFE0E7FF) else Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 72.dp, height = 72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                id = if (callState.isSpeakerOn) com.composables.icons.lucide.R.drawable.lucide_ic_volume_2
                                else com.composables.icons.lucide.R.drawable.lucide_ic_headphones
                            ),
                            contentDescription = "Audio Mode",
                            tint = if (callState.isSpeakerOn) Color(0xFF6C5CE7) else Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
