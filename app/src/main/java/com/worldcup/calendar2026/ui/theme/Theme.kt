package com.worldcup.calendar2026.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF00873E)
private val GreenDark = Color(0xFF005C29)
private val Gold = Color(0xFFE2B33C)
private val Red = Color(0xFFD32F2F)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = Gold,
    onSecondary = Color(0xFF1B1B1B),
    error = Red,
    background = Color(0xFFF6F7F4),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CC07E),
    onPrimary = Color(0xFF00210F),
    secondary = Gold,
    error = Color(0xFFEF5350),
    background = Color(0xFF101411),
    surface = Color(0xFF1A201C)
)

@Composable
fun WorldCupTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
