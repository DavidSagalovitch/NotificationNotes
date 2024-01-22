package com.example.notificationnotes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1C1C1E), // Dark grey/black for primary elements
    secondary = Color(0xFFB0B0B0), // Mild grey for text
    tertiary = Color(0xFF8B0000), // Red for buttons
    background = Color(0xFF121212), // Dark background
    surface = Color(0xFF1E1E1E), // Dark surface
    onPrimary = Color.White, // White text/icons on primary color
    onSecondary = Color.Black, // Black text/icons on secondary color
    onTertiary = Color.White, // White text/icons on tertiary color
    onBackground = Color.White, // White text/icons on background
    onSurface = Color.White // White text/icons on surface
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1C1C1E), // Dark grey/black for primary elements
    secondary = Color(0xFFB0B0B0), // Mild grey for text
    tertiary = Color(0xFFD32F2F), // Red for buttons
    background = Color(0xFFF0F0F0), // Light grey background
    surface = Color(0xFFB0B0B0), // White surface
    onPrimary = Color.White, // White text/icons on primary color
    onSecondary = Color.Black, // Black text/icons on secondary color
    onTertiary = Color.White, // White text/icons on tertiary color
    onBackground = Color.Black, // Black text/icons on background
    onSurface = Color.Black // Black text/icons on surface
)

@Composable
fun NotificationNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme, //should change At some Point back to colorScheme
        typography = Typography,
        content = content
    )
}