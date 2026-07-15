package com.rafiq.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: Replace with actual Inter or Plus Jakarta Sans FontFamily definitions.
// val InterFontFamily = FontFamily(...)

val Typography = Typography(
    bodyLarge = TextStyle(
        // fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    // Add more typography styles aligned with the modern UI guidelines
)
