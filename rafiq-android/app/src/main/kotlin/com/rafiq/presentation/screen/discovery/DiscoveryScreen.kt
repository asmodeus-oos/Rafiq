package com.rafiq.presentation.screen.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.presentation.theme.BackgroundPrimary
import com.rafiq.presentation.theme.TextPrimary
import com.rafiq.presentation.components.common.LiquidGlassCard
import com.rafiq.presentation.components.common.PillButton

@Composable
fun DiscoveryScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundPrimary).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassCard(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "Discovery Hub", fontSize = 32.sp, color = TextPrimary)
                Text(text = "Swipe left or right to connect!", fontSize = 18.sp, color = TextPrimary)
                PillButton(text = "Back to Home", onClick = onNavigateBack)
            }
        }
    }
}
