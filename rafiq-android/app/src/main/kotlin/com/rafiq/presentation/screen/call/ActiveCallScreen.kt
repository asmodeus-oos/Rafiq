package com.rafiq.presentation.screen.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import org.webrtc.SurfaceViewRenderer
import org.webrtc.EglBase

@Composable
fun ActiveCallScreen(
    onCallEnded: () -> Unit,
    viewModel: ActiveCallViewModel = hiltViewModel()
) {
    val callState by viewModel.callState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.initializeWebrtc()
        onDispose {
            viewModel.endCall()
        }
    }

    LaunchedEffect(callState.isEnded) {
        if (callState.isEnded) {
            onCallEnded()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Remote Video (Full Screen)
        if (callState.remoteVideoTrack != null) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        init(viewModel.eglBaseContext, null)
                        setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
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
                CircularProgressIndicator(color = Color.White)
                Text("Connecting...", color = Color.White, modifier = Modifier.padding(top = 64.dp))
            }
        }

        // Local Video (Picture in Picture)
        if (callState.localVideoTrack != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp, end = 16.dp)
                    .size(width = 100.dp, height = 150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            ) {
                AndroidView(
                    factory = { context ->
                        SurfaceViewRenderer(context).apply {
                            init(viewModel.eglBaseContext, null)
                            setScalingType(org.webrtc.RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                            setMirror(true)
                            callState.localVideoTrack?.addSink(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Call Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleMute() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (callState.isMuted) Color.White else Color.DarkGray.copy(alpha = 0.5f))
            ) {
                Icon(
                    painter = painterResource(id = if (callState.isMuted) com.composables.icons.lucide.R.drawable.lucide_ic_mic_off else com.composables.icons.lucide.R.drawable.lucide_ic_mic),
                    contentDescription = "Mute",
                    tint = if (callState.isMuted) Color.Black else Color.White
                )
            }

            IconButton(
                onClick = { viewModel.endCall() },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF385C))
            ) {
                Icon(
                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_phone_off),
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = { viewModel.switchCamera() },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray.copy(alpha = 0.5f))
            ) {
                Icon(
                    painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_switch_camera),
                    contentDescription = "Switch Camera",
                    tint = Color.White
                )
            }
        }
    }
}
