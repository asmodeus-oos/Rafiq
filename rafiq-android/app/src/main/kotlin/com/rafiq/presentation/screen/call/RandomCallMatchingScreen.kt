package com.rafiq.presentation.screen.call

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.rafiq.domain.model.VoiceMatchState
import com.rafiq.presentation.components.common.PillButton
import com.rafiq.presentation.theme.BackgroundPrimary
import com.rafiq.presentation.theme.PrimaryAccent
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun RandomCallMatchingScreen(
    onCancel: () -> Unit,
    onCallConnected: (roomId: String) -> Unit,
    viewModel: VoiceMatchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate when matched
    LaunchedEffect(state.matchState, state.roomId) {
        if (state.matchState is VoiceMatchState.Matched && state.roomId != null) {
            delay(1500)
            onCallConnected(state.roomId!!)
        }
        if (state.matchState is VoiceMatchState.Cancelled) {
            onCancel()
        }
    }

    // Radar animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "w1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart, StartOffset(700)),
        label = "w2"
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart, StartOffset(1400)),
        label = "w3"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        when (val matchState = state.matchState) {

            // ── IDLE: Entry screen ────────────────────────────────────────────
            is VoiceMatchState.Idle, is VoiceMatchState.CheckingLimits -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                            .background(PrimaryAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_phone),
                            contentDescription = null,
                            tint = PrimaryAccent,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        "Random Voice Call",
                        fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryAccent.copy(alpha = 0.06f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("• Paired with opposite gender only", color = Color.Gray)
                            val attemptsText = if (state.maxAttempts == Int.MAX_VALUE) "Unlimited"
                            else "${state.attemptsRemaining}/${state.maxAttempts}"
                            Text("• ${state.myTier.name} tier: $attemptsText attempts today", color = Color.Gray)
                            Text("• Diamond tier: 10 calls/day", color = Color(0xFFFFD700))
                            Text("• FREE tier: 15 minute call limit", color = Color.Gray)
                        }
                    }

                    val canSearch = state.attemptsRemaining > 0 || state.maxAttempts == Int.MAX_VALUE
                    if (canSearch) {
                        PillButton(
                            text = if (matchState is VoiceMatchState.CheckingLimits) "Checking..." else "Start Search",
                            onClick = { viewModel.startSearch() },
                            isPrimary = true
                        )
                    } else {
                        PillButton(text = "No Attempts Left", onClick = {}, isPrimary = false)
                    }
                    PillButton(text = "Cancel", onClick = onCancel, isPrimary = false)
                }
            }

            // ── SEARCHING: Radar animation ────────────────────────────────────
            is VoiceMatchState.Searching, is VoiceMatchState.Cancelled -> {
                Box(contentAlignment = Alignment.Center) {
                    listOf(wave1, wave2, wave3).forEach { progress ->
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer {
                                    scaleX = progress; scaleY = progress; alpha = 1f - progress
                                }
                                .clip(CircleShape)
                                .background(PrimaryAccent.copy(alpha = 0.3f))
                        )
                    }
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(PrimaryAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_search),
                            contentDescription = "Searching",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Finding someone awesome...",
                        color = Color.Gray, fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Looking for: ${if (state.myGender == "MALE") "a woman" else "a man"}",
                        color = PrimaryAccent, fontSize = 14.sp
                    )
                    PillButton(
                        text = "Cancel Search",
                        onClick = { viewModel.cancelSearch() },
                        isPrimary = false
                    )
                }
            }

            // ── MATCHED: Show partner ─────────────────────────────────────────
            is VoiceMatchState.Matched -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!matchState.partner.avatar.isNullOrBlank()) {
                            AsyncImage(
                                model = matchState.partner.avatar,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_check),
                                contentDescription = "Matched",
                                tint = Color.White,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                    Text(
                        "Match Found! 🎉",
                        color = Color(0xFF4CAF50), fontSize = 28.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Connecting you with ${matchState.partner.name.takeIf { it.isNotBlank() } ?: "someone"}...",
                        color = Color.Gray, fontSize = 16.sp
                    )
                    LinearProgressIndicator(
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.width(200.dp).clip(RoundedCornerShape(4.dp))
                    )
                }
            }

            // ── LIMIT REACHED ─────────────────────────────────────────────────
            is VoiceMatchState.LimitReached -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_lock),
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        "Daily Limit Reached",
                        fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
                    )
                    Text(
                        "You've used all ${matchState.max} attempt${if (matchState.max == 1) "" else "s"} today " +
                            "(${matchState.tier} tier).\n" +
                            "Upgrade to Diamond for 10 daily calls!",
                        color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center
                    )
                    PillButton(text = "Go Back", onClick = onCancel, isPrimary = false)
                }
            }

            // ── COOLDOWN ──────────────────────────────────────────────────────
            is VoiceMatchState.OnCooldown -> {
                // Live countdown using LaunchedEffect
                var remainingMs by remember { mutableLongStateOf(matchState.remainingMs) }
                LaunchedEffect(matchState.cooldownUntilMs) {
                    while (remainingMs > 0) {
                        delay(1000L)
                        remainingMs = (matchState.cooldownUntilMs - System.currentTimeMillis())
                            .coerceAtLeast(0L)
                    }
                    // Cooldown expired — go back to idle
                    viewModel.resetToIdle()
                }

                val minutes = (remainingMs / 60000).toInt()
                val seconds = ((remainingMs % 60000) / 1000).toInt()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                            .background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_clock),
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text(
                        "Cooldown Active",
                        fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E)
                    )
                    Text(
                        "You left the queue too many times.\nYou can search again in:",
                        color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center
                    )
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    PillButton(text = "Go Back", onClick = onCancel, isPrimary = false)
                }
            }

            // ── ERROR ─────────────────────────────────────────────────────────
            is VoiceMatchState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_wifi_off),
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "Connection Error",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        matchState.message,
                        color = Color.Gray, textAlign = TextAlign.Center
                    )
                    PillButton(text = "Try Again", onClick = { viewModel.startSearch() })
                    PillButton(text = "Cancel", onClick = onCancel, isPrimary = false)
                }
            }
        }
    }
}
