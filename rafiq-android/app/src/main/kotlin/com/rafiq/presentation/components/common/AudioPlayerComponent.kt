package com.rafiq.presentation.components.common

import android.media.MediaPlayer
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Random

@Composable
fun AudioPlayerComponent(
    audioUrl: String,
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
    contentColor: Color = MaterialTheme.colorScheme.primary,
    onSurfaceColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0) }
    var currentPosition by remember { mutableStateOf(0) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
    DisposableEffect(audioUrl) {
        val player = MediaPlayer()
        mediaPlayer = player
        
        try {
            val finalUrl = if (audioUrl.startsWith("file://")) audioUrl.replace("file://", "") else audioUrl
            player.setDataSource(finalUrl)
            player.prepareAsync()
            player.setOnPreparedListener { mp ->
                duration = mp.duration
            }
            player.setOnCompletionListener {
                isPlaying = false
                currentPosition = 0
                progress = 0f
                it.seekTo(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        onDispose {
            player.release()
            mediaPlayer = null
        }
    }
    
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val player = mediaPlayer
            if (player != null && player.isPlaying) {
                currentPosition = player.currentPosition
                if (duration > 0) {
                    progress = currentPosition.toFloat() / duration.toFloat()
                }
            }
            delay(100)
        }
    }
    
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    val player = mediaPlayer ?: return@Surface
                    if (isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.start()
                        isPlaying = true
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                player.playbackParams = player.playbackParams.setSpeed(playbackSpeed)
                            }
                        } catch (e: Exception) {}
                    }
                },
                color = contentColor,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                val icon = if (isPlaying) com.composables.icons.lucide.R.drawable.lucide_ic_pause else com.composables.icons.lucide.R.drawable.lucide_ic_play
                Icon(painter = painterResource(id = icon), contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.padding(12.dp))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                onClick = {
                    playbackSpeed = when (playbackSpeed) {
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                    }
                    if (isPlaying) {
                        try {
                            val player = mediaPlayer
                            if (player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                player.playbackParams = player.playbackParams.setSpeed(playbackSpeed)
                            }
                        } catch (e: Exception) {}
                    }
                },
                color = contentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(text = "${playbackSpeed}x", color = contentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            WaveformSlider(
                progress = progress,
                onProgressChange = { newProgress ->
                    progress = newProgress
                    val seekToPos = (progress * duration).toInt()
                    mediaPlayer?.seekTo(seekToPos)
                    currentPosition = seekToPos
                },
                audioUrl = audioUrl,
                primaryColor = contentColor,
                modifier = Modifier.weight(1f).height(32.dp).padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            val currStr = String.format("%02d:%02d", (currentPosition / 1000) / 60, (currentPosition / 1000) % 60)
            val durStr = String.format("%02d:%02d", (duration / 1000) / 60, (duration / 1000) % 60)
            Text(text = "$currStr / $durStr", fontSize = 11.sp, color = onSurfaceColor)
        }
    }
}

@Composable
fun WaveformSlider(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    audioUrl: String,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val seed = audioUrl.hashCode().toLong()
    val random = remember(audioUrl) { Random(seed) }
    
    val numBars = 35
    val barHeights = remember(audioUrl) {
        FloatArray(numBars) {
            0.2f + random.nextFloat() * 0.8f
        }
    }
    
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onProgressChange(newProgress)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    onProgressChange(newProgress)
                }
            }
    ) {
        val barWidth = size.width / numBars
        val barSpacing = 4.dp.toPx()
        val actualBarWidth = (barWidth - barSpacing).coerceAtLeast(1f)
        
        for (i in 0 until numBars) {
            val barProgress = i.toFloat() / numBars
            val color = if (barProgress <= progress) {
                primaryColor
            } else {
                primaryColor.copy(alpha = 0.3f)
            }
            
            val barHeight = size.height * barHeights[i]
            val x = i * barWidth + (barSpacing / 2)
            val y = (size.height - barHeight) / 2
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(actualBarWidth, barHeight),
                cornerRadius = CornerRadius(actualBarWidth / 2, actualBarWidth / 2)
            )
        }
        
        val handleX = progress * size.width
        val handleRadius = 6.dp.toPx()
        drawCircle(
            color = Color.Black,
            radius = handleRadius,
            center = Offset(handleX, size.height / 2)
        )
    }
}
