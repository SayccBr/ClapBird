package com.example.clapbird.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val ClapBirdColorPalette = Colors(
    primary = Color(0xFFFFEB3B),          // Yellow for the bird
    primaryVariant = Color(0xFFFFC107),   // Amber variant
    secondary = Color(0xFF4CAF50),        // Green for pipes
    secondaryVariant = Color(0xFF388E3C), // Darker green
    error = Color(0xFFF44336),            // Red for game over
    onPrimary = Color.Black,              // Text on primary color
    onSecondary = Color.White,            // Text on secondary color
    onError = Color.White,                // Text on error color
    background = Color(0xFF87CEEB),       // Sky blue background
    onBackground = Color.Black,           // Text on background
    surface = Color(0xFF964B00),          // Brown for ground
    onSurface = Color.White               // Text on surface
)

@Composable
fun ClapBirdTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = ClapBirdColorPalette,
        content = content
    )
}