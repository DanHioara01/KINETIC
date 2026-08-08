package com.example.kinetic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.kinetic.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Volcanico,
    secondary = Purple,
    tertiary = Purple,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = TextWarmWhite,
    onSurface = TextWarmWhite,
    onSurfaceVariant = TextGrayRed,
    outline = DarkDivider,
    error = Volcanico
)

private val LightColorScheme = lightColorScheme(
    primary = Volcanico,
    secondary = Purple,
    tertiary = Purple,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightCard,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDividerGray,
    error = VolcanicoDark
)

@Composable
fun KineticTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
        typography = KineticTypography,
        content = content
    )
}
