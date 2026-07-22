package com.rafiq.designsystem.interaction

import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object InteractionDefaults {
    @Composable
    fun defaultInteractionTheme(): AppInteractionTheme {
        return AppInteractionTheme(
            pressColor = MaterialTheme.colorScheme.primary,
            pressAlpha = 0.12f,
            scalePressed = 0.97f,
            animationSpec = spring(stiffness = 400f, dampingRatio = 0.7f),
            glow = false,
            blur = 0.dp,
            elevationPressed = 0.dp
        )
    }
}
