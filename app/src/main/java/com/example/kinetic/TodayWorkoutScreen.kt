package com.example.kinetic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.Varien
import com.example.kinetic.ui.theme.RecoveryOrange
import com.example.kinetic.ui.theme.RecoveryTrack
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun TodayWorkoutScreen(
    todayWorkout: TodayWorkout?,
    strings: LanguageManager.Strings,
    isDark: Boolean,
    onStartExercise: (String, String) -> Unit,
    onSaveExercise: (String, String) -> Unit,
    onBack: () -> Unit,
    onOpenSpotify: () -> Unit = {},
    recoveryMap: Map<String, Double> = emptyMap()
) {
    val p = appPalette(isDark)

    if (todayWorkout == null) {
        val appLang = LanguageManager.getLanguage()
        val todayDayName = java.time.LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE", Locale(appLang))
        ).replaceFirstChar { it.uppercase() }

        RestDayContent(
            strings = strings,
            p = p,
            cycleLabel = "",
            dayName = todayDayName,
            onBack = onBack
        )
        return
    }

    val appLang = LanguageManager.getLanguage()
    val dayName = todayWorkout.date.format(
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale(appLang))
    )

    if (todayWorkout.dayType == GymDayType.REST) {
        val restDayName = todayWorkout.date.format(
            DateTimeFormatter.ofPattern("EEEE", Locale(appLang))
        ).replaceFirstChar { it.uppercase() }

        RestDayContent(
            strings = strings,
            p = p,
            cycleLabel = "${strings.dayLabel} ${todayWorkout.dayInCycle} ${strings.ofCycle} · $restDayName",
            dayName = "",
            onBack = onBack
        )
    } else {
        CycleWorkoutContent(
            todayWorkout = todayWorkout,
            dayName = dayName,
            strings = strings,
            isDark = isDark,
            p = p,
            onStartExercise = onStartExercise,
            onSaveExercise = onSaveExercise,
            onBack = onBack,
            onOpenSpotify = onOpenSpotify,
            recoveryMap = recoveryMap
        )
    }
}

@Composable
private fun RestDayContent(
    strings: LanguageManager.Strings,
    p: AppPalette,
    cycleLabel: String = "",
    dayName: String = "",
    onBack: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            KineticAppBar(onBack = onBack)
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (dayName.isNotEmpty()) {
                    Text(dayName, fontSize = 12.sp, color = p.ts, fontWeight = FontWeight.Medium)
                }
                if (cycleLabel.isNotEmpty()) {
                    Text(cycleLabel, fontSize = 11.sp, color = p.ts)
                }
            }
        }

        item {
            AppGlassCard(
                modifier = Modifier.fillMaxWidth(),
                p = p,
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(32.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = p.ac,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        strings.todayYouRest.uppercase(),
                        fontFamily = Varien,
                        fontSize = 28.sp,
                        color = p.tp,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        strings.restDayMessage,
                        fontSize = 15.sp,
                        color = p.ts,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = p.ac,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                strings.restDayTip,
                                fontSize = 13.sp,
                                color = p.ts,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleWorkoutContent(
    todayWorkout: TodayWorkout,
    dayName: String,
    strings: LanguageManager.Strings,
    isDark: Boolean,
    p: AppPalette,
    onStartExercise: (String, String) -> Unit,
    onSaveExercise: (String, String) -> Unit,
    onBack: () -> Unit,
    onOpenSpotify: () -> Unit = {},
    recoveryMap: Map<String, Double> = emptyMap()
) {
    val regularExercises = remember(todayWorkout) {
        todayWorkout.exercises.filter { !it.isStretch }
    }
    val warmupStretch = remember(todayWorkout) {
        todayWorkout.exercises.firstOrNull { it.isStretch && it.note.contains("Warm-up", ignoreCase = true) }
    }
    val cooldownStretch = remember(todayWorkout) {
        todayWorkout.exercises.firstOrNull { it.isStretch && it.note.contains("Cool-down", ignoreCase = true) }
    }
    val groupedByGroup = remember(regularExercises) {
        regularExercises.groupBy { it.group }
    }
    val totalExercises = todayWorkout.exercises.size
    val estimatedMinutes = totalExercises * 3
    val totalSets = todayWorkout.exercises.sumOf { it.sets }



    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            KineticAppBar(onBack = onBack)
        }
        item {
            Text(
                "${strings.dayLabel} ${todayWorkout.dayInCycle} ${strings.ofCycle}",
                fontSize = 12.sp,
                color = p.ts,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (todayWorkout.isDeloadActive) {
            item {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RecoveryOrange, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                strings.deloadActiveThisWeek,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RecoveryOrange
                            )
                            Text(
                                strings.deloadPreviewSubtitle,
                                fontSize = 12.sp,
                                color = p.ts
                            )
                        }
                    }
                }
            }
        }

        item {
            AppGlassCard(
                modifier = Modifier.fillMaxWidth(),
                p = p,
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column {
                    Text(
                        dayName.replaceFirstChar { it.uppercase() },
                        fontFamily = Varien,
                        fontSize = 22.sp,
                        color = p.tp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        todayWorkout.muscleGroups.joinToString(" + ") { WorkoutCycleGenerator.formatGroupName(it, strings) },
                        fontSize = 15.sp,
                        color = p.ac,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$totalExercises ${strings.exercises} · ~$estimatedMinutes min · $totalSets ${strings.sets}",
                        fontSize = 13.sp,
                        color = p.ts
                    )
                }
            }
        }
        item {
            SpotifyWorkoutCard(onOpenSpotify = onOpenSpotify, strings = strings)
        }

        if (warmupStretch != null) {
            item {
                Text(
                    strings.warmupStretch.uppercase(),
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Text(
                    strings.stretchingDescription,
                    fontSize = 12.sp,
                    color = p.ts,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            item {
                StretchExerciseCard(warmupStretch, 0, isDark, p, Accent, strings, onStartExercise, onSaveExercise)
            }
        }

        for ((group, exercises) in groupedByGroup) {
            if (exercises.isEmpty()) continue

            item {
                Text(
                    WorkoutCycleGenerator.formatGroupName(group, strings).uppercase(),
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = p.ac,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            itemsIndexed(exercises) { index, exercise ->

                AppGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStartExercise(exercise.group, exercise.name) },
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = if (isDark) 0.3f else 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val gifUrl = ExerciseGifs.getGif(exercise.name)
                            if (gifUrl != null) {
                                AsyncImage(
                                    model = gifUrl,
                                    contentDescription = exercise.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = p.ac.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .background(p.ac.copy(alpha = 0.85f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Text(
                                exercise.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.tp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                if (exercise.sets == 1 && exercise.reps.endsWith("s")) exercise.reps else "${exercise.sets}x${exercise.reps}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.ac
                            )
                        }
                        IconButton(onClick = { onSaveExercise(exercise.name, exercise.group) }) {
                            Icon(
                                Icons.Default.BookmarkBorder,
                                contentDescription = strings.saveExercise,
                                tint = p.ac,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        if (cooldownStretch != null) {
            item {
                Text(
                    strings.cooldownStretch.uppercase(),
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Text(
                    strings.stretchingDescription,
                    fontSize = 12.sp,
                    color = p.ts,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            item {
                StretchExerciseCard(cooldownStretch, 0, isDark, p, Accent, strings, onStartExercise, onSaveExercise)
            }
        }
    }
}

@Composable
private fun StretchExerciseCard(
    exercise: ExerciseRecommendation,
    index: Int,
    isDark: Boolean,
    p: AppPalette,
    color: Color,
    strings: LanguageManager.Strings,
    onStartExercise: (String, String) -> Unit,
    onSaveExercise: (String, String) -> Unit
) {
    AppGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartExercise(exercise.group, exercise.name) },
        p = p,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = if (isDark) 0.3f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                val gifUrl = ExerciseGifs.getGif(exercise.name)
                if (gifUrl != null) {
                    AsyncImage(
                        model = gifUrl,
                        contentDescription = exercise.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = p.ac.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .background(p.ac.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${index + 1}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    exercise.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.tp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    if (exercise.sets == 1 && exercise.reps.endsWith("s")) exercise.reps else "${exercise.sets}x${exercise.reps}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.ac
                )
            }
            IconButton(onClick = { onSaveExercise(exercise.name, exercise.group) }) {
                Icon(
                    Icons.Default.BookmarkBorder,
                    contentDescription = strings.saveExercise,
                    tint = p.ac,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SpotifyWorkoutCard(onOpenSpotify: () -> Unit, strings: LanguageManager.Strings) {
    val SpotifyGreen = Color(0xFF1DB954)
    val context = LocalContext.current
    var isTriggered by remember { mutableStateOf(false) }
    var triggerTime by remember { mutableStateOf(0L) }

    val infiniteTransition = rememberInfiniteTransition()
    val eqTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing))
    )

    val eqAlpha by animateFloatAsState(
        targetValue = if (isTriggered) 0.7f else 0f,
        animationSpec = tween(200),
        label = "eqAlpha"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    LaunchedEffect(triggerTime) {
        if (triggerTime > 0) {
            isTriggered = true
            delay(500)
            openSpotifyApp(context)
            delay(1500)
            isTriggered = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                try {
                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(30)
                    }
                } catch (_: Exception) {}
                triggerTime = System.currentTimeMillis()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .alpha(eqAlpha)
                .clip(RoundedCornerShape(16.dp))
        ) {
            drawEqualizer(eqTime)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpotifyGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_spotify),
                    contentDescription = "Spotify",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = if (isTriggered) strings.startingWorkoutLabel else strings.tapToPlayLabel,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = strings.play,
                tint = SpotifyGreen,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun DrawScope.drawEqualizer(time: Float) {
    val SpotifyGreen = Color(0xFF1DB954)
    val numBars = 20
    val barSpacing = size.width / numBars
    val barWidth = barSpacing * 0.6f

    for (i in 0 until numBars) {
        val phase = i * 0.8f
        val heightFraction = 0.3f + 0.6f * (sin(time + phase) * 0.5f + 0.5f)
        val barHeight = size.height * heightFraction

        drawRect(
            color = SpotifyGreen,
            topLeft = Offset(
                x = i * barSpacing + (barSpacing - barWidth) / 2,
                y = size.height - barHeight
            ),
            size = Size(width = barWidth, height = barHeight)
        )
    }
}
