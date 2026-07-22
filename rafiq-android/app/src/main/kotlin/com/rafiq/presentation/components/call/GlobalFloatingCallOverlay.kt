package com.rafiq.presentation.components.call

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rafiq.domain.model.GlobalCallState
import kotlin.math.roundToInt

@Composable
fun GlobalFloatingCallOverlay(
    onReopenCall: (String, Boolean) -> Unit
) {
    if (!GlobalCallState.isCallActive || !GlobalCallState.isMinimized) return

    var offsetX by remember { mutableFloatStateOf(100f) }
    var offsetY by remember { mutableFloatStateOf(300f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Surface(
            color = Color.White,
            shape = CircleShape,
            shadowElevation = 12.dp,
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(68.dp)
                .border(2.5.dp, Color(0xFF6C5CE7), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .clip(CircleShape)
                .clickable {
                    GlobalCallState.restore()
                    onReopenCall(GlobalCallState.roomId, GlobalCallState.isVideoCall)
                }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (GlobalCallState.partnerAvatar.isNotBlank()) {
                    AsyncImage(
                        model = GlobalCallState.partnerAvatar,
                        contentDescription = "Partner Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF6C5CE7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_user),
                            contentDescription = null,
                            tint = Color(0xFF6C5CE7),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                // Green active call indicator dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(16.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .background(Color(0xFF22C55E), CircleShape)
                )
            }
        }
    }
}
