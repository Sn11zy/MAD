package com.example.sportsorganizer.ui.theme

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

/**
 * Dark color scheme for the Sports Organizer app.
 *
 * Uses a green color palette with light green primary color
 * and dark background for comfortable viewing in low-light conditions.
 */
private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF80DA9D),
        onPrimary = Color(0xFF00391D),
        background = Color(0xFF191C1A),
        onSurface = Color(0xFFE1E3DF),
    )

/**
 * Light color scheme for the Sports Organizer app.
 *
 * Uses a green color palette with dark green primary color
 * and light background for optimal readability in bright conditions.
 */
private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF006D3D),
        onPrimary = Color.White,
        background = Color(0xFFFBFDF8),
        onSurface = Color(0xFF191C1A),
    )

/**
 * Main theme composable for the Sports Organizer app.
 *
 * Applies Material Design 3 theming with support for light and dark modes.
 * Automatically adjusts system UI elements like the status bar to match the theme.
 *
 * @param darkTheme Whether to use dark theme (defaults to system preference)
 * @param content The composable content to wrap with the theme
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun SportsOrganizerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
