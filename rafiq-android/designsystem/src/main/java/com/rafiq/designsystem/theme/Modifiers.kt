package com.rafiq.designsystem.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Provides better shadows by using graphicsLayer with ambient and spot shadows
 * instead of the standard Modifier.shadow(). This gives more refined depth.
 */
fun Modifier.refinedShadow(
    elevation: Float = 8f,
    ambientColor: Color = Color.Black.copy(alpha = 0.05f),
    spotColor: Color = Color.Black.copy(alpha = 0.15f),
    clip: Boolean = true
) = this.graphicsLayer {
    this.shadowElevation = elevation
    this.ambientShadowColor = ambientColor
    this.spotShadowColor = spotColor
    this.clip = clip
}
