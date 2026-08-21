package com.example.kinetic

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import com.example.kinetic.ui.theme.JetBrainsMono
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WelcomeScreen(
    userName: String,
    strings: LanguageManager.Strings,
    isDark: Boolean,
    onFinished: () -> Unit
) {
    var phase by remember { mutableIntStateOf(0) }

    // Theme-aware colors
    val bgColor = if (isDark) Color(0xFF121212) else LightBackground
    val textColorPrimary = if (isDark) Color.White else LightTextPrimary
    val textColorSecondary = if (isDark) Color.White.copy(alpha = 0.7f) else LightTextSecondary
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color.White
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.12f)
    val gridLineVertical = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val gridLineHorizontal = if (isDark) AccentRed.copy(alpha = 0.12f) else AccentRed.copy(alpha = 0.15f)
    val dnaStrand1 = if (isDark) Color(0xFFCC001A) else AccentRed
    val dnaStrand2 = if (isDark) Color(0xFFDDDDDD) else Color(0xFF888888)
    val dnaRungWhite = if (isDark) Color.White else Color(0xFF333333)
    val dnaRungRed = if (isDark) Color(0xFFCC001A) else AccentRed
    val dnaNodeRed = if (isDark) Color(0xFFAA0018) else AccentRed
    val dnaNodeRedInner = if (isDark) Color(0xFFFF3355) else Color(0xFFFF6680)
    val dnaNodeGrey = if (isDark) Color(0xFFBBBBBB) else Color(0xFF666666)
    val dnaNodeWhite = if (isDark) Color.White else Color(0xFF333333)

    val message = remember {
        val mottos = strings.mottoMessages.filter { it.isNotBlank() }
        if (mottos.isEmpty()) "" else mottos[(0 until mottos.size).random()]
    }

    // Main fade in/out
    val contentAlpha by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0f; 1 -> 1f; else -> 0f
        },
        animationSpec = tween(
            durationMillis = when (phase) { 0 -> 500; 1 -> 800; else -> 600 },
            easing = EaseInOutCubic
        ),
        label = "alpha"
    )

    val contentScale by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0.6f; 1 -> 1f; else -> 1.08f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    // KINETIC typewriter
    val kineticText = "KINETIC"
    var visibleChars by remember { mutableIntStateOf(0) }

    // Staggered reveal states
    var dnaVisible by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        phase = 1
        dnaVisible = true
        cardVisible = true
        for (i in 1..kineticText.length) {
            visibleChars = i
            delay(260)
        }
        delay(2600)
        phase = 2
        onFinished()
    }

    // DNA fade + slide
    val dnaAlpha by animateFloatAsState(
        targetValue = if (dnaVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "dnaAlpha"
    )
    val dnaOffsetY by animateFloatAsState(
        targetValue = if (dnaVisible) 0f else 14f,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "dnaOffsetY"
    )

    // Card fade + slide (appears last)
    val cardAlpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = EaseInOutCubic),
        label = "cardAlpha"
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (cardVisible) 0f else 18f,
        animationSpec = tween(durationMillis = 900, easing = EaseInOutCubic),
        label = "cardOffsetY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(contentAlpha)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        // 1. Grila animata rosie (doar in centru)
        AnimatedRedGrid(isDark = isDark, gridLineVertical = gridLineVertical, gridLineHorizontal = gridLineHorizontal)

        // 2. Vigneta subtila pentru adancime
        Canvas(modifier = Modifier.fillMaxSize()) {
            val vignetteColor = if (isDark) Color.Black else Color(0x22000000)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, vignetteColor.copy(alpha = 0.13f), vignetteColor.copy(alpha = 0.6f)),
                    center = Offset(size.width / 2, size.height * 0.45f),
                    radius = size.maxDimension * 0.75f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .scale(contentScale)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3. KINETIC letter reveal
            val kineticLetters = "KINETIC"
            val letterDirections = listOf(
                -1f to 0f, 0f to -1f, 1f to 0f, 0f to 1f,
                -1f to 0f, 0f to -1f, 1f to 0f
            )
            val letterAnims = kineticLetters.mapIndexed { index, _ ->
                val isRevealed = visibleChars > index
                val offsetX by animateFloatAsState(
                    targetValue = if (isRevealed) 0f else letterDirections[index].first * 80f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 110f),
                    label = "ox$index"
                )
                val offsetY by animateFloatAsState(
                    targetValue = if (isRevealed) 0f else letterDirections[index].second * 80f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 110f),
                    label = "oy$index"
                )
                val letterAlpha by animateFloatAsState(
                    targetValue = if (isRevealed) 1f else 0f,
                    animationSpec = tween(durationMillis = 350),
                    label = "a$index"
                )
                Triple(offsetX, offsetY, letterAlpha)
            }
            Row {
                kineticLetters.forEachIndexed { index, char ->
                    val (ox, oy, a) = letterAnims[index]
                    Text(
                        text = char.toString(),
                        fontFamily = Varien,
                        fontSize = 64.sp,
                        letterSpacing = 18.sp,
                        color = if (index == 0) AccentRed else textColorPrimary,
                        modifier = Modifier
                            .offset(x = ox.dp, y = oy.dp)
                            .alpha(a)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // 4. Logo — ADN 3D (apare cu fade dupa KINETIC)
            Box(
                modifier = Modifier
                    .alpha(dnaAlpha)
                    .offset(y = dnaOffsetY.dp)
            ) {
                DnaHelix(isDark = isDark, dnaStrand1 = dnaStrand1, dnaStrand2 = dnaStrand2, dnaRungWhite = dnaRungWhite, dnaRungRed = dnaRungRed, dnaNodeRed = dnaNodeRed, dnaNodeRedInner = dnaNodeRedInner, dnaNodeGrey = dnaNodeGrey, dnaNodeWhite = dnaNodeWhite)
            }

            Spacer(Modifier.height(24.dp))

            // 5. Card glassmorphism (apare ultimul)
            GlassWelcomeCard(
                userName = userName,
                welcomeLabel = strings.welcome.ifEmpty { "WELCOME" },
                cardAlpha = cardAlpha,
                cardOffsetY = cardOffsetY,
                quote = message,
                isDark = isDark,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textColorPrimary = textColorPrimary,
                textColorSecondary = textColorSecondary
            )
        }
    }
}

@Composable
private fun AnimatedRedGrid(isDark: Boolean, gridLineVertical: Color, gridLineHorizontal: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "redGrid")

    // Deplasare spre dreapta-jos (diagonala)
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 192f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "gridOffsetX"
    )
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 192f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "gridOffsetY"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 192f
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Linii verticale (gri)
        var x = -offsetX
        while (x < size.width + gridSize) {
            val dist = kotlin.math.abs(x - cx) / cx
            val alphaFactor = ((1f - dist) * (1f - dist)).coerceIn(0f, 1f)
            drawLine(
                color = gridLineVertical.copy(alpha = gridLineVertical.alpha * alphaFactor),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSize
        }

        // Linii orizontale (rosii)
        var y = -offsetY
        while (y < size.height + gridSize) {
            val dist = kotlin.math.abs(y - cy) / cy
            val alphaFactor = ((1f - dist) * (1f - dist)).coerceIn(0f, 1f)
            drawLine(
                color = gridLineHorizontal.copy(alpha = gridLineHorizontal.alpha * alphaFactor),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSize
        }

        // A doua trecere decalata (verticale gri)
        var x2 = -offsetX + gridSize
        while (x2 < size.width + gridSize) {
            val dist = kotlin.math.abs(x2 - cx) / cx
            val alphaFactor = ((1f - dist) * (1f - dist)).coerceIn(0f, 1f)
            drawLine(
                color = gridLineVertical.copy(alpha = gridLineVertical.alpha * alphaFactor),
                start = Offset(x2, 0f),
                end = Offset(x2, size.height),
                strokeWidth = 1f
            )
            x2 += gridSize
        }

        // A doua trecere decalata (orizontale rosii)
        var y2 = -offsetY + gridSize
        while (y2 < size.height + gridSize) {
            val dist = kotlin.math.abs(y2 - cy) / cy
            val alphaFactor = ((1f - dist) * (1f - dist)).coerceIn(0f, 1f)
            drawLine(
                color = gridLineHorizontal.copy(alpha = gridLineHorizontal.alpha * alphaFactor),
                start = Offset(0f, y2),
                end = Offset(size.width, y2),
                strokeWidth = 1f
            )
            y2 += gridSize
        }
    }
}

@Composable
private fun DnaHelix(isDark: Boolean, dnaStrand1: Color, dnaStrand2: Color, dnaRungWhite: Color, dnaRungRed: Color, dnaNodeRed: Color, dnaNodeRedInner: Color, dnaNodeGrey: Color, dnaNodeWhite: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "dna")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dnaRotation"
    )

    Canvas(
        modifier = Modifier
            .width(140.dp)
            .height(300.dp)
    ) {
        drawDNAHelix(rotation = rotation, dnaStrand1 = dnaStrand1, dnaStrand2 = dnaStrand2, dnaRungWhite = dnaRungWhite, dnaRungRed = dnaRungRed, dnaNodeRed = dnaNodeRed, dnaNodeRedInner = dnaNodeRedInner, dnaNodeGrey = dnaNodeGrey, dnaNodeWhite = dnaNodeWhite)
    }
}

fun DrawScope.drawDNAHelix(rotation: Float, dnaStrand1: Color, dnaStrand2: Color, dnaRungWhite: Color, dnaRungRed: Color, dnaNodeRed: Color, dnaNodeRedInner: Color, dnaNodeGrey: Color, dnaNodeWhite: Color) {
    val numRungs = 20
    val helixRadiusX = size.width * 0.42f
    val helixRadiusZ = size.width * 0.15f
    val helixHeight = size.height * 0.9f
    val twist = 2 * PI.toFloat()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val fov = 900f

    fun perspective(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
        val scale = fov / (fov + z)
        return Triple(x * scale, y * scale, scale)
    }

    fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    val stepsSmooth = numRungs * 4
    val strand1 = mutableListOf<Offset>()
    val strand2 = mutableListOf<Offset>()

    for (i in 0..stepsSmooth) {
        val t = i.toFloat() / stepsSmooth
        val angle = rotation + t * twist
        val y = lerp(-helixHeight / 2f, helixHeight / 2f, t)

        val x1 = cos(angle) * helixRadiusX
        val z1 = sin(angle) * helixRadiusZ
        val (px1, py1, _) = perspective(x1, y, z1)
        strand1.add(Offset(cx + px1, cy + py1))

        val x2 = cos(angle + PI.toFloat()) * helixRadiusX
        val z2 = sin(angle + PI.toFloat()) * helixRadiusZ
        val (px2, py2, _) = perspective(x2, y, z2)
        strand2.add(Offset(cx + px2, cy + py2))
    }

    val pathStrand1 = Path().apply {
        strand1.forEachIndexed { i, pt ->
            if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
    }
    val pathStrand2 = Path().apply {
        strand2.forEachIndexed { i, pt ->
            if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
    }

    drawPath(pathStrand1, color = dnaStrand1, style = Stroke(width = 8f, cap = StrokeCap.Round))
    drawPath(pathStrand2, color = dnaStrand2, style = Stroke(width = 8f, cap = StrokeCap.Round))

    data class Rung(
        val px1: Float, val py1: Float, val scale1: Float,
        val px2: Float, val py2: Float, val scale2: Float,
        val depth: Float, val isBase: Boolean
    )

    val rungs = (0..numRungs).map { i ->
        val t = i.toFloat() / numRungs
        val angle = rotation + t * twist
        val y = lerp(-helixHeight / 2f, helixHeight / 2f, t)

        val x1 = cos(angle) * helixRadiusX
        val z1 = sin(angle) * helixRadiusZ
        val (px1, py1, sc1) = perspective(x1, y, z1)

        val x2 = cos(angle + PI.toFloat()) * helixRadiusX
        val z2 = sin(angle + PI.toFloat()) * helixRadiusZ
        val (px2, py2, sc2) = perspective(x2, y, z2)

        Rung(
            px1 = cx + px1, py1 = cy + py1, scale1 = sc1,
            px2 = cx + px2, py2 = cy + py2, scale2 = sc2,
            depth = (z1 + z2) / 2f,
            isBase = i % 2 == 0
        )
    }.sortedBy { it.depth }

    for (rung in rungs) {
        val depthFactor = (rung.depth + helixRadiusZ) / (2f * helixRadiusZ)
        val opacity = lerp(0.2f, 0.75f, depthFactor)
        val strokeW = lerp(2.5f, 6f, depthFactor)
        val rungColor = if (rung.isBase)
            dnaRungWhite.copy(alpha = opacity * 0.7f)
        else
            dnaRungRed.copy(alpha = opacity * 0.7f)

        drawLine(
            color = rungColor,
            start = Offset(rung.px1, rung.py1),
            end = Offset(rung.px2, rung.py2),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        val nodeAlpha = lerp(0.4f, 1f, depthFactor)
        val r1 = lerp(5f, 11f, rung.scale1 * 0.9f)
        val r2 = lerp(5f, 11f, rung.scale2 * 0.9f)

        drawCircle(
            color = dnaNodeRed.copy(alpha = nodeAlpha),
            radius = r1,
            center = Offset(rung.px1, rung.py1)
        )
        drawCircle(
            color = dnaNodeRedInner.copy(alpha = nodeAlpha),
            radius = r1 * 0.5f,
            center = Offset(rung.px1, rung.py1)
        )

        drawCircle(
            color = dnaNodeGrey.copy(alpha = nodeAlpha * 0.85f),
            radius = r2,
            center = Offset(rung.px2, rung.py2)
        )
        drawCircle(
            color = dnaNodeWhite.copy(alpha = nodeAlpha),
            radius = r2 * 0.5f,
            center = Offset(rung.px2, rung.py2)
        )
    }
}

@Composable
private fun GlassWelcomeCard(
    userName: String,
    welcomeLabel: String,
    cardAlpha: Float,
    cardOffsetY: Float,
    quote: String,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textColorPrimary: Color,
    textColorSecondary: Color
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 28.dp)
            .alpha(cardAlpha)
            .offset(y = cardOffsetY.dp)
            .shadow(elevation = 18.dp, spotColor = AccentRed.copy(alpha = 0.30f), shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF242427), cardBg)
                    } else {
                        listOf(Color.White, Color(0xFFF2F2F4))
                    }
                )
            )
            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 28.dp, vertical = 30.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (userName.isNotBlank()) {
                Text(
                    text = welcomeLabel + ",",
                    fontSize = 24.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColorPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = userName,
                    fontSize = 32.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentRed,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = welcomeLabel,
                    fontSize = 28.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Black,
                    color = textColorPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "\u201C$quote\u201D",
                fontSize = 14.sp,
                fontFamily = JetBrainsMono,
                lineHeight = 20.sp,
                letterSpacing = 1.sp,
                color = textColorSecondary,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
