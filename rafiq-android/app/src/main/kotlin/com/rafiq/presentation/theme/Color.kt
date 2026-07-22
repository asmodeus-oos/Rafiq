package com.rafiq.presentation.theme

import androidx.compose.ui.graphics.Color

// ME Gallery Design System Colors
val PrimaryAccent = Color(0xFF8B5CF6) // #8B5CF6
val BackgroundPrimary = Color(0xFFFAF9FF) // #FAF9FF
val BackgroundSecondary = Color(0xFFFFFFFF) // #FFFFFF
val TextPrimary = Color(0xFF1F1F28) // #1F1F28
val TextTertiary = Color(0xFF94A3B8) // #94a3b8
val BorderLight = Color(0xFFEEEAFB) // #EEEAFB

val AccentSurface = PrimaryAccent.copy(alpha = 0.08f)
val AccentSurfaceStrong = PrimaryAccent.copy(alpha = 0.12f)
val AccentSurfaceSelected = PrimaryAccent.copy(alpha = 0.18f)
val AccentSurfacePressed = PrimaryAccent.copy(alpha = 0.20f)
val AccentBadge = PrimaryAccent.copy(alpha = 0.15f)

// Glassmorphism Tokens
val GlassBackground = Color(0x8CFFFFFF) // rgba(255, 255, 255, 0.55)
val GlassBorder = Color(0x80FFFFFF) // rgba(255, 255, 255, 0.5)
val GlassHighlight = Color(0xE6FFFFFF) // rgba(255, 255, 255, 0.9)
