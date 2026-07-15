package com.rafiq.designsystem.interaction

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppInteractionTheme(
    val pressColor: Color = Color.Black.copy(alpha = 0.1f),
    val pressAlpha: Float = 0.15f,
    val scalePressed: Float = 0.97f,
    val animationSpec: AnimationSpec<Float> = spring(stiffness = 400f, dampingRatio = 0.6f),
    val glow: Boolean = false,
    val blur: Dp = 0.dp,
    val elevationPressed: Dp = 0.dp
)

val LocalAppInteractionTheme: ProvidableCompositionLocal<AppInteractionTheme> = staticCompositionLocalOf { AppInteractionTheme() }
