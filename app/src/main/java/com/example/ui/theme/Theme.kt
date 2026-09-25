package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MantisDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001F24),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF8CF7FF),
    secondary = ElectricViolet,
    onSecondary = Color(0xFF380062),
    secondaryContainer = Color(0xFF550090),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = NeonGreen,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF005234),
    onTertiaryContainer = Color(0xFF86F8BF),
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = NeonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Gaming apps are best optimized in immersive dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MantisDarkColorScheme,
        typography = Typography,
        content = content
    )
}
