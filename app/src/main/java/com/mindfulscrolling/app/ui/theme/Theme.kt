package com.mindfulscrolling.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Teal400,
    onPrimary = Color.White,
    primaryContainer = Teal900,
    onPrimaryContainer = Teal200,
    secondary = Cyan400,
    onSecondary = Color.White,
    secondaryContainer = DarkSurface3,
    onSecondaryContainer = Cyan200,
    tertiary = Amber400,
    onTertiary = Color.Black,
    tertiaryContainer = DarkSurface3,
    onTertiaryContainer = Amber200,
    error = CoralRed,
    onError = Color.White,
    errorContainer = Color(0xFF3B1C1C),
    onErrorContainer = CoralRed,
    background = DarkBackground,
    onBackground = TextWhite,
    surface = DarkSurface,
    onSurface = TextWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextGray,
    outline = Color(0xFF3A5068),
    inverseSurface = LightSurface,
    inverseOnSurface = TextDark
)

private val LightColorScheme = lightColorScheme(
    primary = Teal500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Teal900,
    secondary = Cyan700,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF004D56),
    tertiary = Color(0xFFF9A825),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFF5D4037),
    error = SoftRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextDarkSecondary,
    outline = Color(0xFFB0BEC5),
    inverseSurface = DarkSurface,
    inverseOnSurface = TextWhite
)

@Composable
fun MindfulScrollingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
