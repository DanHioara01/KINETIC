package com.example.kinetic.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

data class AppPalette(
    val bg: Color,
    val sf: Color,
    val cr: Color,
    val card: Color,
    val bd: Color,
    val tp: Color,
    val ts: Color,
    val tt: Color,
    val ac: Color,
    val acg: Color,
    val acs: Color,
    val gn: Color,
    val gns: Color,
    val am: Color,
    val ams: Color,
    val bl: Color,
    val bls: Color,
    val pu: Color,
    val pus: Color,
    val rs: Color,
    val rss: Color
)

/**
 * Paleta animată: la schimbarea temei, toate culorile se amestecă lin (lerp) între
 * light și dark, deci orice ecran care folosește paleta are o tranziție globală de
 * temă (≈ crossfade) în loc de schimbare instantanee.
 *
 * Paleta e STATICĂ: culorile comută instant la schimbarea temei (zero animație
 * per-frame → zero jank). Tranziția vizuală e un singur fade subtil pe conținut
 * în MainActivity, care nu provoacă recompuneri.
 */
@Composable
fun appPalette(isDark: Boolean): AppPalette {
    return buildPalette(isDark = isDark)
}

private fun buildPalette(isDark: Boolean): AppPalette {
    val red = if (isDark) Color(0xFFFF3C3C) else LightPrimaryRed
    return AppPalette(
        bg = if (isDark) DarkBackground else LightBackground,
        sf = if (isDark) DarkBackground else LightBackground,
        cr = if (isDark) Color.White.copy(alpha = 0.025f) else Color.Black.copy(alpha = 0.04f),
        card = if (isDark) DarkCard else LightCard,
        bd = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.08f),
        tp = if (isDark) Color(0xFFF0F0F5) else LightTextPrimary,
        ts = if (isDark) Color(0xFFF0F0F5).copy(alpha = 0.48f) else LightTextSecondary,
        tt = if (isDark) Color(0xFFF0F0F5).copy(alpha = 0.18f) else LightTextSecondary.copy(alpha = 0.45f),
        ac = red,
        acg = red.copy(alpha = 0.2f),
        acs = red.copy(alpha = 0.07f),
        gn = Color(0xFF2DD4A0),
        gns = Color(0xFF2DD4A0).copy(alpha = 0.07f),
        am = Color(0xFFF5A623),
        ams = Color(0xFFF5A623).copy(alpha = 0.07f),
        bl = Color(0xFF4E8CFF),
        bls = Color(0xFF4E8CFF).copy(alpha = 0.07f),
        pu = Color(0xFFA855F7),
        pus = Color(0xFFA855F7).copy(alpha = 0.07f),
        rs = Color(0xFFFB7185),
        rss = Color(0xFFFB7185).copy(alpha = 0.07f)
    )
}

/**
 * Overlay/"track" culoare care se adaptează la temă:
 * alb cu alpha pe dark, negru cu alpha pe light.
 * Folosită pentru fundaluri interne, linii de grid, track-uri de progres ș.a.
 */
fun AppPalette.overlay(alpha: Float): Color {
    val darkBg = bg.luminance() < 0.5f
    return if (darkBg) Color.White.copy(alpha = alpha) else Color.Black.copy(alpha = alpha)
}
