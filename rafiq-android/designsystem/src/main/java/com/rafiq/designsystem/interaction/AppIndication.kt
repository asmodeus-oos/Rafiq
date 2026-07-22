package com.rafiq.designsystem.interaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.launch

class AppIndicationNode(
    private val interactionSource: InteractionSource,
    private val theme: AppInteractionTheme
) : Modifier.Node(), DrawModifierNode {

    private val animatable = Animatable(0f)

    override fun onAttach() {
        coroutineScope.launch {
            var currentPress: PressInteraction.Press? = null
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        currentPress = interaction
                        animatable.animateTo(1f, theme.animationSpec)
                    }
                    is PressInteraction.Release -> {
                        if (currentPress == interaction.press) {
                            animatable.animateTo(0f, theme.animationSpec)
                        }
                    }
                    is PressInteraction.Cancel -> {
                        if (currentPress == interaction.press) {
                            animatable.animateTo(0f, theme.animationSpec)
                        }
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val progress = animatable.value
        val scale = 1f - ((1f - theme.scalePressed) * progress)

        scale(scale) {
            this@draw.drawContent()
            if (progress > 0f) {
                val cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                drawRoundRect(
                    color = theme.pressColor,
                    alpha = theme.pressAlpha * progress,
                    size = size,
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}

class AppIndication(private val theme: AppInteractionTheme) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return AppIndicationNode(interactionSource, theme)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppIndication) return false
        return theme == other.theme
    }

    override fun hashCode(): Int = theme.hashCode()
}

@Composable
fun rememberAppIndication(
    theme: AppInteractionTheme = LocalAppInteractionTheme.current
): AppIndication {
    return remember(theme) { AppIndication(theme) }
}

