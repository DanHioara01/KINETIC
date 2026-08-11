package com.example.kinetic.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.lerp
import com.example.kinetic.ThemeMode

@Composable
fun KineticTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Tranziție globală de temă: culorile MaterialTheme se amestecă lin (≈ crossfade)
    // între light și dark, deci fundalurile/texturile care citesc colorScheme animă.
    val progress by animateFloatAsState(
        targetValue = if (isDark) 1f else 0f,
        animationSpec = tween(450),
        label = "themeSchemeProgress"
    )

    val colorScheme = lightColorScheme(
        primary = Volcanico,
        secondary = Purple,
        tertiary = Purple,
        background = lerp(LightBackground, DarkBackground, progress),
        surface = lerp(LightSurface, DarkSurface, progress),
        surfaceVariant = lerp(LightCard, DarkCard, progress),
        onBackground = lerp(LightTextPrimary, TextWarmWhite, progress),
        onSurface = lerp(LightTextPrimary, TextWarmWhite, progress),
        onSurfaceVariant = lerp(LightTextSecondary, TextGrayRed, progress),
        outline = lerp(LightDividerGray, DarkDivider, progress),
        error = lerp(VolcanicoDark, Volcanico, progress)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KineticTypography,
        content = content
    )
}
