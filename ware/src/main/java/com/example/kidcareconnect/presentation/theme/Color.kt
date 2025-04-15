package com.example.kidcareconnect.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors

// Primary colors
val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

// App-specific colors
val Primary = Color(0xFF0097B2) // Teal Primary
val Secondary = Color(0xFF4CAF50) // Green Secondary
val Tertiary = Color(0xFFFFA726) // Orange Tertiary
val Error = Color(0xFFBA1A1A)

internal val wearColorPalette: Colors = Colors(
    primary = Primary,
    primaryVariant = Purple700,
    secondary = Secondary,
    secondaryVariant = Teal200,
    error = Error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onError = Color.White
)