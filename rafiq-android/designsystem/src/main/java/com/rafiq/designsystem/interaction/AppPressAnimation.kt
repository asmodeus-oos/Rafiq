package com.rafiq.designsystem.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.flow.collectLatest

@Composable
fun rememberPressState(interactionSource: InteractionSource): State<PressState> {
    val pressState = remember { mutableStateOf(PressState.IDLE) }
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(interactionSource) {
        var currentPress: PressInteraction.Press? = null
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    currentPress = interaction
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // Light haptic on press down
                    pressState.value = PressState.PRESSED
                }
                is PressInteraction.Release -> {
                    if (currentPress == interaction.press) {
                        pressState.value = PressState.IDLE
                    }
                }
                is PressInteraction.Cancel -> {
                    if (currentPress == interaction.press) {
                        pressState.value = PressState.IDLE
                    }
                }
            }
        }
    }
    return pressState
}

@Composable
fun rememberPressAnimation(
    pressState: PressState,
    theme: AppInteractionTheme = LocalAppInteractionTheme.current
): State<Float> {
    val animatable = remember { Animatable(if (pressState == PressState.PRESSED) 1f else 0f) }
    
    LaunchedEffect(pressState) {
        animatable.animateTo(
            targetValue = if (pressState == PressState.PRESSED) 1f else 0f,
            animationSpec = theme.animationSpec
        )
    }
    
    return animatable.asState()
}
