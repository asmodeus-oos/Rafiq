package com.rafiq.designsystem.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset

/**
 * Custom Transitions & Better Motion guidelines as requested.
 * Uses spring physics for a more natural feel.
 */
object RafiqTransitions {
    
    // Spring physics with low stiffness, natural damping, and overshoot
    val naturalSpring = spring<Float>(
        dampingRatio = 0.85f,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val naturalOffsetSpring = spring<IntOffset>(
        dampingRatio = 0.85f,
        stiffness = Spring.StiffnessMediumLow
    )

    // iOS push/pop style transitions
    fun iosPushEnter(): EnterTransition =
        slideInHorizontally(animationSpec = naturalOffsetSpring) { it } +
        fadeIn(animationSpec = tween(300))
        
    fun iosPushExit(): ExitTransition =
        slideOutHorizontally(animationSpec = naturalOffsetSpring) { -it / 3 } +
        fadeOut(animationSpec = tween(300))
        
    fun iosPopEnter(): EnterTransition =
        slideInHorizontally(animationSpec = naturalOffsetSpring) { -it / 3 } +
        fadeIn(animationSpec = tween(300))
        
    fun iosPopExit(): ExitTransition =
        slideOutHorizontally(animationSpec = naturalOffsetSpring) { it } +
        fadeOut(animationSpec = tween(300))
        
    // Shared Axis Transition (Z-axis)
    fun sharedAxisEnter(): EnterTransition = 
        fadeIn(tween(300)) + scaleIn(initialScale = 0.8f, animationSpec = tween(300))
        
    fun sharedAxisExit(): ExitTransition = 
        fadeOut(tween(300)) + scaleOut(targetScale = 1.1f, animationSpec = tween(300))

    // Fade Through Transition
    fun fadeThroughEnter(): EnterTransition = 
        fadeIn(tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90))

    fun fadeThroughExit(): ExitTransition = 
        fadeOut(tween(90))
        
    // Scale Transition
    fun scaleEnter(): EnterTransition =
        scaleIn(initialScale = 0.8f, animationSpec = naturalSpring) + fadeIn(tween(200))
        
    fun scaleExit(): ExitTransition =
        scaleOut(targetScale = 0.8f, animationSpec = naturalSpring) + fadeOut(tween(200))
}
