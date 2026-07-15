@file:Suppress("DEPRECATION")
package com.rafiq.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import com.rafiq.designsystem.interaction.LocalAppInteractionTheme
import com.rafiq.designsystem.interaction.InteractionDefaults
import com.rafiq.designsystem.interaction.rememberAppIndication

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAccent,
    secondary = SecondaryAccent,
    tertiary = TertiaryAccent,
    background = BackgroundPrimary,
    surface = BackgroundSecondary,
    onPrimary = BackgroundSecondary,
    onSecondary = TextPrimary,
    onTertiary = BackgroundSecondary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderLight
)

@Composable
fun RafiqTheme(
    darkTheme: Boolean = false, // Detached from system
    dynamicColor: Boolean = false, // Disabled to use our own colors
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    // We strictly use our custom LightColorScheme regardless of Android 12+ or system settings
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    val appIndication = rememberAppIndication()
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    CompositionLocalProvider(
        androidx.compose.material3.LocalRippleConfiguration provides androidx.compose.material3.RippleConfiguration(
            color = colorScheme.primary,
            rippleAlpha = androidx.compose.material.ripple.RippleAlpha(0.15f, 0.15f, 0.15f, 0.15f)
        ),
        LocalAppInteractionTheme provides InteractionDefaults.defaultInteractionTheme()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = RafiqShapes,
            content = content
        )
    }
}
