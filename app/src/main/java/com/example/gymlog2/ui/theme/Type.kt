package com.example.gymlog2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.gymlog2.R

val Oswald = FontFamily(
    Font(R.font.oswald_medium, FontWeight.Medium),
    Font(R.font.oswald_semibold, FontWeight.SemiBold),
)

val GeneralSans = FontFamily(
    Font(R.font.general_sans_regular, FontWeight.Normal),
    Font(R.font.general_sans_medium, FontWeight.Medium),
    Font(R.font.general_sans_semibold, FontWeight.SemiBold),
)

val KineticTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Oswald,
        fontSize = 57.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Oswald,
        fontSize = 45.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Oswald,
        fontSize = 36.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Oswald,
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Oswald,
        fontSize = 28.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Oswald,
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Oswald,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Oswald,
        fontSize = 19.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Oswald,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.04.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = GeneralSans,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = GeneralSans,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = GeneralSans,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = GeneralSans,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = GeneralSans,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = GeneralSans,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
)
