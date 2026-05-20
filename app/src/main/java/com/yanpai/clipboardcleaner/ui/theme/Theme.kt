package com.yanpai.clipboardcleaner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = CardBackground,
    primaryContainer = Blue50,
    secondary = VideoColor,
    tertiary = VideoColor,
    background = SurfaceLight,
    surface = CardBackground,
    error = Red500
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkYellow,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurface,
    secondary = VideoColor,
    tertiary = DarkYellow,
    background = DarkBackground,
    surface = DarkSurface,
    error = Red500,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White
)

@Composable
fun ClipboardCleanerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
