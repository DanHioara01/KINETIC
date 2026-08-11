package com.example.kinetic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.LanguageManager
import com.example.kinetic.ui.theme.*

/**
 * Paleta de bază pentru efectul glass — ajustează la roșu/negru din tema Kinetic.
 */
object GlassPalette {
    val darkBackgroundTint = Color(0xFF1A0000)
    val darkGlassBase = Color(0xFFB33A3A)
    val darkBorderHighlight = Color(0xFFFF5A5A)
    val lightGlassBase = Color(0xFFDC2626)       // Red base for light mode gradient
    val lightBorderHighlight = Color(0xFFEF4444) // Softer red border for light mode
    val lightCardTop = Color(0xFFFECACA)         // Light red tint at top (red-100)
    val lightCardBottom = Color(0xFFFFFFFF)      // White at bottom
}

/**
 * Fundal pentru întreaga pagină — setează culoarea de fundal în funcție de tema curentă.
 * Folosit de DashboardScreen, StatsScreen etc.
 */
@Composable
fun GlassBackground(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable BoxScope.() -> Unit
) {
    val p = appPalette(isDark)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg),
        content = content
    )
}

/**
 * Card cu efect de glassmorphism premium.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    isDark: Boolean = isSystemInDarkTheme(),
    cornerRadius: Dp = 20.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    tint: Color? = null,
    borderColor: Color? = null,
    content: @Composable () -> Unit
) {
    val (defaultTint, defaultBorder) = GlassPalette.darkGlassBase to GlassPalette.darkBorderHighlight
    val lightTint = GlassPalette.lightGlassBase
    val lightBorder = GlassPalette.lightBorderHighlight

    val resolvedTint = tint ?: if (isDark) defaultTint else lightTint
    val resolvedBorder = borderColor ?: if (isDark) defaultBorder else lightBorder

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            resolvedTint.copy(alpha = 0.25f),
                            resolvedTint.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.20f)
                        )
                    } else {
                        listOf(
                            GlassPalette.lightCardTop,         // Subtle red tint at top
                            Color.White.copy(alpha = 0.97f),   // Nearly white in middle
                            GlassPalette.lightCardBottom        // Pure white at bottom
                        )
                    }
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                color = resolvedBorder.copy(alpha = if (isDark) 0.35f else 0.40f),
                shape = shape
            )
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * Variantă cu blur real în spatele card-ului (API 31+ / Android 12+).
 */
@Composable
fun BlurredGlassBackground(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 24.dp,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.blur(radius = blurRadius)
    ) {
        content()
    }
}

@Composable
fun AppGlassCard(
    modifier: Modifier = Modifier,
    p: AppPalette = appPalette(isSystemInDarkTheme()),
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    containerColor: Color = p.cr,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, p.bd, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun AppSectionLabel(
    text: String,
    p: AppPalette = appPalette(isSystemInDarkTheme()),
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.5.sp,
        color = p.tt,
        modifier = modifier.padding(start = 2.dp, bottom = 10.dp)
    )
}

@Composable
fun AppBg(
    isDark: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val p = appPalette(isDark)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(p.bg)
    ) {
        content()
    }
}

/**
 * Acțiuni globale ale header-ului: deschiderea drawer-ului și comutarea temei.
 * Acțiunile sunt furnizate la nivelul root (MainActivity) prin CompositionLocal,
 * astfel încât KineticAppBar să funcționeze identic pe orice ecran.
 */
class KineticHeaderController(
    val isDark: Boolean,
    val onOpenMenu: () -> Unit
)

val LocalKineticHeader = staticCompositionLocalOf<KineticHeaderController?> { null }

/**
 * Header-ul global Kinetic: iconiță (back sau meniu) + textul „KINETIC" +
 * comutatorul dark/light. Folosit pe TOATE ecranele aplicației.
 */
@Composable
fun KineticAppBar(
    onBack: (() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    val controller = LocalKineticHeader.current
    val isDark = controller?.isDark ?: isSystemInDarkTheme()
    val p = appPalette(isDark)

    // Header construit manual (fără TopAppBar) ca textul „KINETIC" să fie EXACT pe mijlocul
    // ecranului, indiferent de lățimea iconițelor laterale sau a acțiunilor.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(p.bg)
    ) {
        // Titlul — plasat într-un Box care acoperă toată lățimea, centrat absolut
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (title != null) {
                title()
            } else {
                Text(
                    "KINETIC",
                    fontFamily = Varien,
                    fontSize = 26.sp,
                    letterSpacing = 6.sp,
                    // letterSpacing adaugă spațiu și după ultima literă; compensăm ca
                    // centrul VIZUAL al textului să fie exact pe mijlocul ecranului.
                    modifier = Modifier.padding(end = 3.dp),
                    color = p.tp
                )
            }
        }
        // Iconița de navigare — stânga
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = p.tp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                IconButton(onClick = controller?.onOpenMenu ?: {}) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = strings.menu,
                        tint = p.tp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        // Acțiuni — dreapta
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions()
        }
    }
}

@Composable
fun GradientNextExerciseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = appPalette(isSystemInDarkTheme())
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(brush = Brush.horizontalGradient(listOf(p.ac, Color(0xFFFF6B4A))))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White
            )
            Icon(
                Icons.Filled.SkipNext,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
