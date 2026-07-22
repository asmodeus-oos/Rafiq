package com.rafiq.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // TODO: Define custom branded colors for light theme
)

private val DarkColorScheme = darkColorScheme(
    // TODO: Define custom branded colors for dark theme
)

@Composable
fun RafiqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Enforcing dynamicColor = false as requested for a branded social app
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.LaunchedEffect(view) {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Defined in Type.kt
    ) {
        val appIndication = com.rafiq.designsystem.interaction.rememberAppIndication()
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.foundation.LocalIndication provides appIndication,
            content = content
        )
    }
}
