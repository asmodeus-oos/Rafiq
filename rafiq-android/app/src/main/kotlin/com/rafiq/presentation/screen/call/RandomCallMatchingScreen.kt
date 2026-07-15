package com.rafiq.presentation.screen.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.presentation.theme.BackgroundSecondary
import com.rafiq.presentation.theme.PrimaryAccent
import com.rafiq.presentation.components.common.PillButton

@Composable
fun RandomCallMatchingScreen(
    onCancel: () -> Unit,
    onCallConnected: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundSecondary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = "Searching for a match...", color = PrimaryAccent, fontSize = 24.sp)
            PillButton(text = "Cancel Search", onClick = onCancel, isPrimary = false)
            PillButton(text = "Simulate Match", onClick = onCallConnected)
        }
    }
}
