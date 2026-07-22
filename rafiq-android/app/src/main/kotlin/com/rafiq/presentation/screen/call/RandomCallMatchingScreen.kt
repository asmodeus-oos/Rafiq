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
import com.rafiq.presentation.theme.BackgroundPrimary
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.components.common.PillButton

@Composable
fun RandomCallMatchingScreen(
    onCancel: () -> Unit,
    onCallConnected: (roomId: String) -> Unit,
    viewModel: RandomCallViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate when matched
    LaunchedEffect(state.state, state.roomId) {
        if (state.state == MatchState.MATCHED && state.roomId != null) {
            kotlinx.coroutines.delay(1500) // Brief "Match Found!" display
            onCallConnected(state.roomId!!)
        }
        if (state.state == MatchState.CANCELLED) {
            onCancel()
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            initialStartOffset = StartOffset(700),
            repeatMode = RepeatMode.Restart
        )
    )
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            initialStartOffset = StartOffset(1400),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        when (state.state) {
            MatchState.LIMIT_REACHED -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_lock),
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text("Daily Limit Reached", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    Text(
                        "You've used all ${state.maxAttempts} attempt${if (state.maxAttempts == 1) "" else "s"} for today.\nUpgrade to Diamond for 10 daily calls!",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    PillButton(text = "Go Back", onClick = onCancel, isPrimary = false)
                }
            }

            MatchState.MATCHED -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier.size(120.dp).clip(CircleShape).background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!state.matchedUser?.avatar.isNullOrBlank()) {
                            AsyncImage(
                                model = state.matchedUser?.avatar,
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
                    Text("Match Found! 🎉", color = Color(0xFF4CAF50), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Connecting you with ${state.matchedUser?.name ?: "someone"}...",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    LinearProgressIndicator(
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.width(200.dp).clip(RoundedCornerShape(4.dp))
                    )
                }
            }

            MatchState.ERROR -> {
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
                    Text("Connection Error", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(state.errorMessage ?: "Something went wrong", color = Color.Gray, textAlign = TextAlign.Center)
                    PillButton(text = "Try Again", onClick = { viewModel.startSearch() })
                    PillButton(text = "Cancel", onClick = onCancel, isPrimary = false)
                }
            }

            MatchState.IDLE -> {
                // Entry screen — show rules and start button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(PrimaryAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_phone),
                            contentDescription = null,
                            tint = PrimaryAccent,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Text("Random Voice Call", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                    
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryAccent.copy(alpha = 0.06f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("• Paired with opposite gender only", color = Color.Gray)
                            Text("• Free tier: ${state.attemptsRemaining}/${state.maxAttempts} attempt${if (state.maxAttempts == 1) "" else "s"} remaining today", color = Color.Gray)
                            Text("• Diamond tier: 10 calls/day", color = Color(0xFFFFD700))
                        }
                    }

                    if (state.attemptsRemaining > 0) {
                        PillButton(text = "Start Search", onClick = { viewModel.startSearch() })
                    } else {
                        PillButton(text = "No Attempts Left", onClick = {}, isPrimary = false)
                    }
                    PillButton(text = "Cancel", onClick = onCancel, isPrimary = false)
                }
            }

            MatchState.SEARCHING, MatchState.CANCELLED -> {
                // Radar animation while searching
                Box(contentAlignment = Alignment.Center) {
                    listOf(wave1, wave2, wave3).forEach { progress ->
                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .graphicsLayer {
                                    scaleX = progress
                                    scaleY = progress
                                    alpha = 1f - progress
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
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Looking for: ${if (state.myGender == "male") "a woman" else "a man"}",
                        color = PrimaryAccent,
                        fontSize = 14.sp
                    )
                    PillButton(text = "Cancel Search", onClick = { viewModel.cancelSearch() }, isPrimary = false)
                }
            }
        }
    }
}
