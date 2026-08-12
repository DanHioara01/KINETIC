package com.example.kinetic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.kinetic.ThemeMode

/**
 * Progres global al temei (0f = light, 1f = dark). Valoare STATICĂ — culorile comută
 * instant, fără animație per-frame. Tranziția vizuală e făcută de un singur fade subtil
 * pe conținut în MainActivity (alpha), care nu provoacă recompuneri. Astfel switch-ul
 * nu mai are lag chiar și pe device-uri mid-range.
 */
val LocalThemeProgress = staticCompositionLocalOf { 0f }

/** Tema globală rezolvată (dark = true), pentru componentele care primesc isDark separat. */
val LocalThemeIsDark = staticCompositionLocalOf { false }

@Composable
fun KineticTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Culori directe, fără lerp animat: instant, zero recompuneri la comutare.
    val colorScheme = lightColorScheme(
        primary = Volcanico,
        secondary = Purple,
        tertiary = Purple,
        background = if (isDark) DarkBackground else LightBackground,
        surface = if (isDark) DarkSurface else LightSurface,
        surfaceVariant = if (isDark) DarkCard else LightCard,
        onBackground = if (isDark) TextWarmWhite else LightTextPrimary,
        onSurface = if (isDark) TextWarmWhite else LightTextPrimary,
        onSurfaceVariant = if (isDark) TextGrayRed else LightTextSecondary,
        outline = if (isDark) DarkDivider else LightDividerGray,
        error = if (isDark) Volcanico else VolcanicoDark
    )

    CompositionLocalProvider(
        LocalThemeProgress provides if (isDark) 1f else 0f,
        LocalThemeIsDark provides isDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KineticTypography,
            content = content
        )
    }
}
