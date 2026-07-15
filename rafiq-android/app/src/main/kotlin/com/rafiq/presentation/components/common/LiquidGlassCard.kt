package com.rafiq.presentation.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.rafiq.presentation.theme.GlassBackground
import com.rafiq.presentation.theme.GlassBorder
import com.rafiq.presentation.theme.RafiqShapes

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RafiqShapes.large,
                spotColor = androidx.compose.ui.graphics.Color(0x0D000000)
            )
            .clip(RafiqShapes.large)
            .background(GlassBackground)
            .border(
                width = 1.dp,
                color = GlassBorder,
                shape = RafiqShapes.large
            )
            .padding(24.dp),
        content = content
    )
}
