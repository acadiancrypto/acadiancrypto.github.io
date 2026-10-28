package com.acadiancrypto.loadcounter.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Amber = Color(0xFFFFB300)
val LoadGreen = Color(0xFF2E7D32)
val DangerRed = Color(0xFFC62828)
val Background = Color(0xFF121212)

private val DarkColors = darkColorScheme(
    primary = Amber,
    background = Background,
    surface = Background,
)

@Composable
fun LoadCounterTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
