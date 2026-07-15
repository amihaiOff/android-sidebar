package com.personal.sidebar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightFallback = lightColorScheme(
    primary = Color(0xFF4C5BD4),
    secondary = Color(0xFF5B6270),
)

private val DarkFallback = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    secondary = Color(0xFFC2C6DD),
)

/**
 * Material 3 theme with Material You dynamic color on Android 12+ (falls back to
 * a fixed indigo scheme below that). Used by both the onboarding activity and
 * the overlay panel so they share one look.
 */
@Composable
fun SidebarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkFallback
        else -> LightFallback
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
