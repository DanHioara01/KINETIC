package com.example.kinetic

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.kinetic.ui.theme.Ember
import com.example.kinetic.ui.theme.EmberLight
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.overlay
import com.example.kinetic.ui.components.GradientNextExerciseButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private enum class TimerState { READY, RUNNING, PAUSED, FINISHED }

// ── CULORI (legacy — folosite și de alte ecrane) ───────────────
val BgDeep = Color(0xFF0A0A0F)
val Accent = Color(0xFFFF3C3C)
val AccentGlow = Color(0x4DFF3C3C)
val AccentSoft = Color(0x1FFF3C3C)
val Green = Color(0xFF2DD4A0)
val GreenSoft = Color(0x1F2DD4A0)
val Amber = Color(0xFFF5A623)
val AmberSoft = Color(0x1FF5A623)
val Blue = Color(0xFF4E8CFF)
val BlueSoft = Color(0x1F4E8CFF)
val TextPrimary = Color(0xFFF0F0F5)
val TextSecondary = Color(0x8CF0F0F5)
val TextTertiary = Color(0x4DF0F0F5)
val Border = Color(0x0FFFFFFF)
val CardBg = Color(0x0AFFFFFF)

@Composable
fun StretchExerciseScreen(
    exercise: ExerciseDefinition,
    isLbs: Boolean = false,
    isDark: Boolean = false,
    onBackClick: () -> Unit,
    onNextExercise: (() -> Unit)? = null,
    onFinishExercise: () -> Unit = {},
    onOpenProgress: (String) -> Unit = {},
    onWorkoutSaved: () -> Unit = {},
    strings: LanguageManager.Strings,
    currentIndex: Int = 0,
    totalExercises: Int = 0,
        nextExerciseName: String? = null,
    nextExerciseSets: String = "",
    phase: String = "WARMUP"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val p = appPalette(isDark)
    var selectedDuration by remember { mutableIntStateOf(60) }
    var remainingMs by remember { mutableFloatStateOf(60000f) }
    var timerState by remember { mutableStateOf(TimerState.READY) }

    BackHandler {
        timerState = TimerState.READY
        remainingMs = selectedDuration * 1000f
        onBackClick()
    }

    // Timer tick
    LaunchedEffect(timerState) {
        if (timerState == TimerState.RUNNING) {
            while (remainingMs > 0f) {
                delay(100)
                remainingMs = (remainingMs - 100f).coerceAtLeast(0f)
            }
            if (remainingMs <= 0f) {
                timerState = TimerState.FINISHED
                vibratePhone(context)
                scope.launch {
                    delay(1600)
                    if (timerState == TimerState.FINISHED) {
                        if (onNextExercise != null) onNextExercise() else onFinishExercise()
                    }
                }
            }
        }
    }

    // Reset on duration change
    LaunchedEffect(selectedDuration) {
        remainingMs = selectedDuration * 1000f
        timerState = TimerState.READY
    }

    val totalMs = selectedDuration * 1000f
    val progress = (remainingMs / totalMs).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        progress,
        animationSpec = tween(100, easing = LinearEasing), label = "animProgress"
    )
    val totalSec = (remainingMs / 1000f).toInt()
    val minutes = totalSec / 60
    val seconds = totalSec % 60

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val finishPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "finishPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // ── Top bar ──
        TopBar(
            p = p,
            exerciseName = exercise.nume,
            phase = phase,
            currentIndex = currentIndex,
            totalExercises = totalExercises,
            onBack = onBackClick
        )

        Spacer(Modifier.height(12.dp))

        // ── Next Exercise Button ──
        if (onNextExercise != null) {
            GradientNextExerciseButton(
                text = strings.nextExercise,
                onClick = { onNextExercise() }
            )
            Spacer(Modifier.height(12.dp))
        }

        // ── GIF Card ──
        GifCard(
            p = p,
            isDark = isDark,
            gifUrl = ExerciseGifs.getGif(exercise.nume),
            exerciseName = exercise.nume,
            muscleGroup = exercise.group,
            strings = strings
        )

        Spacer(Modifier.height(14.dp))

        // ── Circular Timer ──
        Box(
            modifier = Modifier
                .size(170.dp)
                .graphicsLayer {
                    if (timerState == TimerState.FINISHED) {
                        scaleX = finishPulse
                        scaleY = finishPulse
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            CircularTimerCanvas(
                p = p,
                progress = animatedProgress,
                isRunning = timerState == TimerState.RUNNING,
                isFinished = timerState == TimerState.FINISHED
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    letterSpacing = 2.sp,
                    color = if (timerState == TimerState.FINISHED) p.gn else p.tp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (timerState) {
                        TimerState.READY -> strings.start.uppercase()
                        TimerState.RUNNING -> strings.active.uppercase()
                        TimerState.PAUSED -> "PAUSED"
                        TimerState.FINISHED -> strings.done.uppercase()
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = when (timerState) {
                        TimerState.READY -> p.am
                        TimerState.RUNNING -> p.ac
                        TimerState.PAUSED -> p.ts
                        TimerState.FINISHED -> p.gn
                    }
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Duration presets ──
        DurationPresets(
            p = p,
            presets = listOf(30, 45, 60),
            selected = selectedDuration,
            onSelected = { duration ->
                if (timerState != TimerState.RUNNING) {
                    selectedDuration = duration
                    remainingMs = duration * 1000f
                    timerState = TimerState.READY
                }
            }
        )

        Spacer(Modifier.height(14.dp))

        // ── Controls card ──
        ControlsCard(
            p = p,
            timerState = timerState,
            onPlayPause = {
                when (timerState) {
                    TimerState.READY -> {
                        remainingMs = selectedDuration * 1000f
                        timerState = TimerState.RUNNING
                    }
                    TimerState.RUNNING -> timerState = TimerState.PAUSED
                    TimerState.PAUSED -> timerState = TimerState.RUNNING
                    TimerState.FINISHED -> {
                        remainingMs = selectedDuration * 1000f
                        timerState = TimerState.READY
                    }
                }
            },
            onReset = {
                remainingMs = selectedDuration * 1000f
                timerState = TimerState.READY
            }
        )

        if (nextExerciseName != null) {
            Spacer(Modifier.height(10.dp))
            NextExerciseHint(
                p = p,
                name = nextExerciseName,
                sets = nextExerciseSets,
                strings = strings
            )
        }

        Spacer(
            Modifier
                .weight(1f)
                .padding(bottom = AppConstants.BOTTOM_NAV_PADDING)
        )
    }
}

// ── TOP BAR ────────────────────────────────────────────────

@Composable
private fun TopBar(
    p: AppPalette,
    exerciseName: String,
    phase: String = "WARMUP",
    currentIndex: Int,
    totalExercises: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(p.cr)
                .border(1.dp, p.bd, RoundedCornerShape(14.dp))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = phase,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = p.am
            )
            Text(
                text = exerciseName.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = p.tp,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (totalExercises > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(p.cr)
                    .border(1.dp, p.bd, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${currentIndex + 1} / $totalExercises",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.ts
                )
            }
        }
    }
}

// ── GIF CARD ───────────────────────────────────────────────

@Composable
private fun GifCard(
    p: AppPalette,
    isDark: Boolean,
    gifUrl: String?,
    exerciseName: String,
    muscleGroup: String,
    strings: LanguageManager.Strings
) {
    val fadeColor = if (isDark) Color(0xFF0A0A0C) else Color.White
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, p.bd, RoundedCornerShape(20.dp))
    ) {
        if (gifUrl != null) {
            AsyncImage(
                model = gifUrl,
                contentDescription = exerciseName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = p.tt,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = strings.exercise.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = p.tt
                )
            }
        }

        // Fade overlays
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF0A0A0C).copy(alpha = 0.5f), Color.Transparent))
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF0A0A0C).copy(alpha = 0.5f)))
                )
        )

        // Muscle label bottom-left
        Text(
            text = LanguageManager.translateMuscleGroup(muscleGroup, strings).uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = EmberLight.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )

        // Exercise badge top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Ember.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = strings.exercise.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = EmberLight
            )
        }
    }
}

// ── CIRCULAR TIMER CANVAS ──────────────────────────────────

@Composable
private fun CircularTimerCanvas(
    p: AppPalette,
    progress: Float,
    isRunning: Boolean,
    isFinished: Boolean
) {
    val animatedProgress by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        animationSpec = tween(100, easing = LinearEasing),
        label = "circProgress"
    )

    Canvas(modifier = Modifier.size(170.dp)) {
        val strokeWidth = 6.dp.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.width / 2) - 16.dp.toPx()

        // Background ring
        drawCircle(
            p.overlay(0.06f),
            radius,
            center,
            style = Stroke(strokeWidth)
        )

        // Progress arc
        val sweepAngle = 360f * animatedProgress
        val arcColor = when {
            isFinished -> p.gn
            animatedProgress < 0.15f -> p.ac.copy(alpha = 0.9f)
            else -> p.ac
        }

        if (animatedProgress > 0.001f) {
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Glow at the arc head
        if (isRunning && animatedProgress > 0.01f) {
            val endAngleRad = Math.toRadians((-90.0 + sweepAngle).toDouble())
            val endX = center.x + radius * cos(endAngleRad).toFloat()
            val endY = center.y + radius * sin(endAngleRad).toFloat()
            drawCircle(
                brush = Brush.radialGradient(
                    center = Offset(endX, endY),
                    radius = 14.dp.toPx(),
                    colors = listOf(p.ac.copy(alpha = 0.3f), Color.Transparent)
                ),
                radius = 14.dp.toPx(),
                center = Offset(endX, endY)
            )
        }

        // Tick marks (60)
        val tickRadius = radius + 10.dp.toPx()
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6.0 - 90.0))
            val isMajor = i % 5 == 0
            val innerR = if (isMajor) tickRadius - 6.dp.toPx() else tickRadius - 3.dp.toPx()

            val x1 = center.x + innerR * cos(angle).toFloat()
            val y1 = center.y + innerR * sin(angle).toFloat()
            val x2 = center.x + tickRadius * cos(angle).toFloat()
            val y2 = center.y + tickRadius * sin(angle).toFloat()

            val tickProgress = i / 60f
            val isFilled = tickProgress <= animatedProgress
            val tickColor = when {
                isFilled && isFinished -> p.gn.copy(alpha = if (isMajor) 0.5f else 0.25f)
                isFilled -> p.ac.copy(alpha = if (isMajor) 0.5f else 0.2f)
                else -> p.overlay(if (isMajor) 0.1f else 0.04f)
            }

            drawLine(
                tickColor,
                Offset(x1, y1),
                Offset(x2, y2),
                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

// ── DURATION PRESETS ───────────────────────────────────────

@Composable
private fun DurationPresets(
    p: AppPalette,
    presets: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        presets.forEach { duration ->
            val isActive = duration == selected
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isActive) p.acs else p.cr)
                    .border(
                        1.dp,
                        if (isActive) p.ac.copy(alpha = 0.35f) else p.bd,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelected(duration) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${duration}s",
                    fontSize = 14.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isActive) p.ac else p.tt
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isActive) p.ac else Color.Transparent)
                )
            }
        }
    }
}

// ── CONTROLS CARD ──────────────────────────────────────────

@Composable
private fun ControlsCard(
    p: AppPalette,
    timerState: TimerState,
    onPlayPause: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = p.cr),
        border = BorderStroke(1.dp, p.bd)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(p.overlay(0.06f))
                    .border(1.dp, p.bd, RoundedCornerShape(16.dp))
                    .clickable { onReset() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = p.ts,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Play/Pause button
            val isRunning = timerState == TimerState.RUNNING
            Box(
                modifier = Modifier
                    .weight(1.6f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = if (isRunning) {
                            Brush.horizontalGradient(listOf(p.overlay(0.1f), p.overlay(0.1f)))
                        } else {
                            Brush.horizontalGradient(listOf(p.ac, Color(0xFFFF6B4A)))
                        }
                    )
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val btnContent = if (isRunning) p.tp else Color.White
                    Icon(
                        if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = btnContent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isRunning) "PAUSE" else "START",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = btnContent
                    )
                }
            }
        }
    }
}

// ── NEXT EXERCISE HINT ─────────────────────────────────────

@Composable
private fun NextExerciseHint(
    p: AppPalette,
    name: String,
    sets: String,
    strings: LanguageManager.Strings
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(p.cr)
            .border(1.dp, p.bd, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.SkipNext,
            contentDescription = null,
            tint = p.tt,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${strings.next}: ",
            fontSize = 12.sp,
            color = p.tt
        )
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = p.ts,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (sets.isNotBlank()) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = sets,
                fontSize = 11.sp,
                color = p.tt
            )
        }
    }
}

private fun vibratePhone(context: Context) {
    try {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(500)
        }
    } catch (_: Exception) {}
}
