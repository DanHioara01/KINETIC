package com.example.kinetic

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.kinetic.ui.theme.Ember
import com.example.kinetic.ui.theme.EmberLight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.JetBrainsMono
import com.example.kinetic.ui.theme.overlay
import com.example.kinetic.ui.components.GradientNextExerciseButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val S1 = if (isDark) Color(0xFF0D0D0D) else Color(0xFFFFFFFF)
    val Bdr = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg)
    ) {
        // ── Header (matching ExerciseInputScreen) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(p.cr)
                    .border(1.dp, p.bd, RoundedCornerShape(14.dp))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(R.drawable.ic_back_arrow),
                    contentDescription = null,
                    tint = p.ts,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                if (totalExercises > 0) {
                    Text(
                        "${currentIndex + 1} / $totalExercises",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.tt,
                        letterSpacing = 2.sp
                    )
                }
                Text(
                    exercise.nume.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.tp,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.size(44.dp))
        }

        Spacer(Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── Next Exercise Button ──
            if (onNextExercise != null) {
                GradientNextExerciseButton(
                    text = strings.nextExercise,
                    onClick = { onNextExercise() }
                )
                Spacer(Modifier.height(8.dp))
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

            Spacer(Modifier.height(12.dp))

            // ── Timer Section (matching ExerciseInputScreen) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(S1)
                    .border(1.dp, Bdr, RoundedCornerShape(22.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.duration, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.tt, letterSpacing = 1.8.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf(30, 45, 60).forEach { sec ->
                            val presetMin = sec / 60
                            val presetSec = sec % 60
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sec == selectedDuration) p.acs else p.cr)
                                    .border(
                                        1.dp,
                                        if (sec == selectedDuration) p.ac.copy(alpha = 0.35f) else p.bd,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (timerState != TimerState.RUNNING) {
                                            selectedDuration = sec
                                            remainingMs = sec * 1000f
                                            timerState = TimerState.READY
                                        }
                                    }
                                    .padding(6.dp, 5.dp, 13.dp, 5.dp)
                            ) {
                                Text(
                                    if (presetMin > 0) "$presetMin:${presetSec.toString().padStart(2, '0')}" else "${sec}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (sec == selectedDuration) p.ac else p.tt
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Timer display
                val totalSec = (remainingMs / 1000f).toInt()
                val mins = totalSec / 60
                val secs = totalSec % 60
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        mins.toString(), fontSize = 54.sp, fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono, letterSpacing = (-3).sp,
                        color = if (timerState == TimerState.RUNNING) p.ac else p.tp
                    )
                    Text(":", fontSize = 54.sp, fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono, letterSpacing = (-3).sp,
                        color = if (timerState == TimerState.RUNNING) p.ac else p.tt)
                    Text(
                        secs.toString().padStart(2, '0'), fontSize = 54.sp, fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono, letterSpacing = (-3).sp,
                        color = if (timerState == TimerState.RUNNING) p.ac else p.tp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Progress bar
                val totalMs = selectedDuration * 1000f
                val progress = (remainingMs / totalMs).coerceIn(0f, 1f)
                Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)).background(p.overlay(0.06f))) {
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(Brush.horizontalGradient(listOf(p.ac, p.ac.copy(alpha = 0.7f)))))
                }

                Spacer(Modifier.height(18.dp))

                // Replay + Play/Pause
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(p.cr)
                            .border(1.dp, p.bd, RoundedCornerShape(14.dp))
                            .clickable {
                                remainingMs = selectedDuration * 1000f
                                timerState = TimerState.READY
                            },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Refresh, null, tint = p.tt, modifier = Modifier.size(16.dp)) }
                    Box(
                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp))
                            .background(if (timerState == TimerState.RUNNING) S1 else p.ac)
                            .clickable {
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
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (timerState == TimerState.RUNNING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            null, tint = if (timerState == TimerState.RUNNING) p.ac else Color.White, modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (nextExerciseName != null) {
                NextExerciseHint(
                    p = p,
                    name = nextExerciseName,
                    sets = nextExerciseSets,
                    strings = strings,
                    cardBg = S1,
                    border = Bdr
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(AppConstants.BOTTOM_NAV_PADDING))
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

// ── NEXT EXERCISE HINT ─────────────────────────────────────

@Composable
private fun NextExerciseHint(
    p: AppPalette,
    name: String,
    sets: String,
    strings: LanguageManager.Strings,
    cardBg: Color = p.cr,
    border: Color = p.bd
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
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
