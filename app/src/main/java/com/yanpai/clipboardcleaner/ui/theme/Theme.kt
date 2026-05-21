package com.yanpai.clipboardcleaner.ui.theme

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

val DefaultLightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = CardBackground,
    primaryContainer = Blue50,
    secondary = Color(0xFFE3F2FD),
    tertiary = Blue500,
    background = SurfaceLight,
    surface = CardBackground,
    error = Red500
)

val DefaultDarkColorScheme = darkColorScheme(
    primary = DarkYellow,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurface,
    secondary = Color(0xFF2A2A2A),
    tertiary = DarkYellow,
    background = DarkBackground,
    surface = DarkSurface,
    error = Red500,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White
)

@Composable
fun BackgroundImage(
    path: String?,
    scaleMode: Int = 1,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    scale: Float = 1f
) {
    if (path == null) return
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(path) {
        bitmap = try {
            BitmapFactory.decodeFile(path)
        } catch (_: Exception) { null }
    }
    bitmap?.let {
        val contentScale = when (scaleMode) {
            0 -> ContentScale.Fit
            2 -> ContentScale.FillBounds
            3 -> ContentScale.Fit  // 也使用 Fit 作为基础，变换叠加在上
            else -> ContentScale.Crop
        }
        val modifier = if (scaleMode == 3) {
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    translationX = offsetX; translationY = offsetY
                }
        } else {
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 64.dp)
        }
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun ClipboardCleanerTheme(
    colorScheme: ColorScheme? = null,
    darkTheme: Boolean = false,
    backgroundImagePath: String? = null,
    backgroundScaleMode: Int = 1,
    backgroundOffsetX: Float = 0f,
    backgroundOffsetY: Float = 0f,
    backgroundScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val baseScheme = colorScheme ?: if (darkTheme) DefaultDarkColorScheme else DefaultLightColorScheme
    val scheme = if (backgroundImagePath != null) {
        baseScheme.copy(
            background = androidx.compose.ui.graphics.Color.Transparent,
            surface = baseScheme.surface.copy(alpha = 0.88f)
        )
    } else baseScheme

    MaterialTheme(
        colorScheme = scheme,
        typography = AppTypography
    ) {
        Box {
            BackgroundImage(backgroundImagePath, backgroundScaleMode, backgroundOffsetX, backgroundOffsetY, backgroundScale)
            content()
        }
    }
}
