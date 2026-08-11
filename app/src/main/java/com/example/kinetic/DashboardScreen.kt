package com.example.kinetic

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import android.annotation.SuppressLint
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.example.kinetic.ui.theme.Varien
import com.example.kinetic.ui.theme.RecoveryOrange
import com.example.kinetic.ui.theme.RecoveryYellow
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.DarkBackground
import com.example.kinetic.ui.theme.LightBackground
import kotlinx.coroutines.delay
import kotlin.math.sin
import java.util.Random
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Locale

data class DashboardUiState(
    val profileName: String,
    val weekWorkoutCount: Int,
    val weekVolume: Double,
    val weekWorkoutDurationMs: Long,
    val lastWeekWorkoutCount: Int,
    val lastWeekVolume: Double,
    val currentStreak: Int,
    val bestStreak: Int,
    val weeklyTopExercise: String?,
    val todayCardioDistance: Double,
    val todayCardioDuration: Long,
    val todayCardioCalories: Double,
    val totalSteps: Int
)

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DashboardScreen(
    state: DashboardUiState,
    todayWorkout: TodayWorkout?,
    strings: LanguageManager.Strings,
    isDark: Boolean,
    isLbs: Boolean,
    onboardingProfile: UserOnboardingProfile,
    profilePhotoUri: String = "",
    profilePhotoVersion: Int = 0,
    bottomPadding: PaddingValues,
    innerPadding: PaddingValues,
    onStartWorkout: () -> Unit,
    onExerciseClick: (String, String) -> Unit,
    onSetStepGoal: (Int) -> Unit,
    stepGoal: Int = 7000,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    deloadDue: Boolean = false,
    onOpenDeload: () -> Unit = {}
) {
    val p = appPalette(isDark)

    // Mood: 0 = epuizat, 1 = obosit, 2 = normal, 3 = energic
    var selectedMood by remember { mutableIntStateOf(2) }
    val generatedTips = remember(onboardingProfile, selectedMood) {
        FitnessAssistant.generateTips(onboardingProfile, selectedMood)
    }

    val dayOfYear = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    var activeTipIndex by remember(generatedTips) {
        mutableIntStateOf(if (generatedTips.isNotEmpty()) dayOfYear % generatedTips.size else 0)
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> strings.goodMorning
            hour < 18 -> strings.goodAfternoon
            else -> strings.goodEvening
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    // ── Stagger entry animation ──
    var visibleItems by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..8) {
            delay(80L)
            visibleItems = i
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(p.bg)) {
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .pullToRefresh(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = onRefresh
                )
        ) {
            // ── Ambient glow (behind content) ────────────────────────────
            AmbientGlowLayer(accent = p.ac)

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = AppConstants.BOTTOM_NAV_PADDING
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header ────────────────────────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visibleItems > 0,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                    ) {
                        HeaderSection(
                            name = state.profileName,
                            greeting = greeting,
                            accent = p.ac,
                            textPrimary = p.tp,
                            textSecondary = p.ts,
                            isDark = isDark,
                            profilePhotoUri = profilePhotoUri,
                            profilePhotoVersion = profilePhotoVersion
                        )
                    }
                }

                // ── Week strip ───────────────────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visibleItems > 1,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                    ) {
                        WeekStrip(
                            accent = p.ac,
                            textPrimary = p.tp,
                            textSecondary = p.ts,
                            cardBg = p.cr,
                            isDark = isDark
                        )
                    }
                }

                // ── Deload due banner ─────────────────────────────────────
                if (deloadDue) {
                    item {
                        AnimatedVisibility(
                            visible = visibleItems > 1,
                            enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                        ) {
                            DeloadDueBanner(
                                strings = strings,
                                p = p,
                                onClick = onOpenDeload
                            )
                        }
                    }
                }

                // ── Activity rings (existing card with step goal dialog) ─
                item {
                    AnimatedVisibility(
                        visible = visibleItems > 2,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                    ) {
                        DailyActivityCard(
                        strings = strings,
                        isDark = isDark,
                        cardBg = p.cr,
                        textPrimary = p.tp,
                        textSecondary = p.ts,
                        accent = p.ac,
                        iconBg = p.acs,
                        todayDistanceKm = state.todayCardioDistance,
                        todayDurationMs = state.todayCardioDuration,
                        todayCalories = state.todayCardioCalories,
                        stepsEstimate = state.totalSteps,
                        stepGoal = stepGoal,
                        onSetStepGoal = { goal -> onSetStepGoal(goal) }
                        )
                    }
                }

                // ── Streak card (animated flame) ─────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visibleItems > 3,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                    ) {
                        StreakCard(
                            streak = state.currentStreak,
                        bestStreak = state.bestStreak,
                        accent = p.ac,
                        textPrimary = p.tp,
                        textSecondary = p.ts,
                        cardBg = p.cr,
                        isDark = isDark,
                        strings = strings
                        )
                    }
                }

                // ── Mood selector (4 stări) ──────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visibleItems > 4,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                    ) {
                        MoodSelector(
                            selected = selectedMood,
                            onMoodSelected = { selectedMood = it },
                            strings = strings,
                            accent = p.ac,
                            textPrimary = p.tp,
                            textSecondary = p.ts,
                            cardBg = p.cr,
                        isDark = isDark
                        )
                    }
                }

                // ── Tips card (mood + objective based) ───────────────────
                item {
                    AnimatedVisibility(
                        visible = visibleItems > 5,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                    ) {
                    if (generatedTips.isNotEmpty()) {
                        val currentTip = generatedTips.getOrNull(activeTipIndex)
                        if (currentTip != null) {
                            val (tipLabel, tipIcon) = when (currentTip.category) {
                                "performanta" -> Pair(strings.performLabel, Icons.Default.TrendingUp)
                                "sfat_obiectiv" -> Pair(strings.goalLabel, Icons.Default.EmojiEvents)
                                "sfat_tehnic" -> Pair(strings.technicalTip, Icons.Default.Lightbulb)
                                "nutritie" -> Pair(strings.nutritionLabel, Icons.Default.Restaurant)
                                "energie" -> Pair(strings.energizeLabel, Icons.Default.Battery1Bar)
                                "recuperare" -> Pair(strings.recovery, Icons.Default.Accessibility)
                                "motivatie" -> Pair(strings.motivationLabel, Icons.Default.EmojiEvents)
                                "forteaza_te" -> Pair(strings.pushItLabel, Icons.Default.BatteryFull)
                                else -> Pair(strings.performLabel, Icons.Default.TrendingUp)
                            }

                            // Accent bar color per mood (0 exhausted, 1 tired, 2 normal, 3 energic)
                            val accentBarColor = when (selectedMood) {
                                0 -> Color(0xFF9E9E9E) // grey for exhausted
                                1 -> Color(0xFFF59E0B) // warm amber for tired
                                2 -> Color(0xFF10B981) // emerald green for normal
                                else -> Color(0xFFEF4444) // red for energetic
                            }

                            val categoryBadgeColor = when (tipLabel) {
                                strings.recovery -> Color(0xFF4A90D9) // blue
                                strings.nutritionLabel -> Color(0xFF4CAF50) // green
                                strings.technicalTip -> Color(0xFF9C27B0) // purple
                                strings.goalLabel -> Color(0xFFFFB300) // gold
                                strings.motivationLabel -> Color(0xFFFF6D00) // orange
                                strings.energizeLabel -> Color(0xFF4A90D9)
                                strings.performLabel -> Color(0xFF009688) // teal
                                strings.pushItLabel -> Color(0xFFFF5722) // deep orange
                                else -> p.ac
                            }

                            LaunchedEffect(activeTipIndex) {
                                delay(10000)
                                activeTipIndex = (activeTipIndex + 1) % generatedTips.size
                            }

                            AnimatedContent(
                                targetState = selectedMood,
                                transitionSpec = {
                                    slideInVertically(animationSpec = tween(400)) { it / 4 } + fadeIn(tween(300)) togetherWith
                                        slideOutVertically(animationSpec = tween(300)) { -it / 4 } + fadeOut(tween(200))
                                },
                                label = "moodTipEntrance"
                            ) { _ ->
                                AppGlassCard(
                                    modifier = Modifier
                                        .clickable {
                                            activeTipIndex = (activeTipIndex + 1) % generatedTips.size
                                        },
                                    p = p,
                                    cornerRadius = 24.dp,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.height(IntrinsicSize.Min)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = categoryBadgeColor.copy(alpha = 0.12f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        tipIcon,
                                                        contentDescription = null,
                                                        tint = categoryBadgeColor,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        tipLabel,
                                                        fontSize = 11.sp,
                                                        color = categoryBadgeColor,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            Spacer(Modifier.height(10.dp))

                                            AnimatedContent(
                                                targetState = TipsTranslator.translateTip(currentTip.text, LanguageManager.getLanguage()),
                                                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                                                label = "tipTransition"
                                            ) { tipText ->
                                                Text(
                                                    tipText,
                                                    fontSize = 13.sp,
                                                    color = p.ts,
                                                    lineHeight = 19.sp
                                                )
                                            }

                                            Spacer(Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                repeat(generatedTips.size) { i ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(if (i == activeTipIndex) 8.dp else 6.dp)
                                                            .background(
                                                                if (i == activeTipIndex) accentBarColor else p.ts.copy(alpha = 0.2f),
                                                                CircleShape
                                                            )
                                                    )
                                                    if (i < generatedTips.size - 1) {
                                                        Spacer(Modifier.width(6.dp))
                                                    }
                                                }
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .fillMaxHeight()
                                                .background(accentBarColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                }

                // ── Today's workout (hero card) or rest day ─────────────
                item {
                    AnimatedVisibility(
                        visible = visibleItems > 6,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                    ) {
                        val workoutExercises = todayWorkout?.exercises ?: emptyList()

                        if (todayWorkout != null && todayWorkout.dayType == GymDayType.REST) {
                            RestDayCard(
                                todayWorkout = todayWorkout,
                                strings = strings,
                                p = p,
                                isDark = isDark
                            )
                        } else if (workoutExercises.isNotEmpty()) {
                            WorkoutHeroCard(
                                todayWorkout = todayWorkout,
                                onboardingProfile = onboardingProfile,
                                strings = strings,
                                accent = p.ac,
                                textPrimary = p.tp,
                                textSecondary = p.ts,
                                cardBg = p.cr,
                                isDark = isDark,
                                onExerciseClick = onExerciseClick,
                                onStartWorkout = onStartWorkout
                            )
                        }
                    }
                }

                // ── Weekly summary ───────────────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visibleItems > 7,
                        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                    ) {
                    WeeklySummaryCard(
                        weekWorkoutCount = state.weekWorkoutCount,
                        weekVolume = state.weekVolume,
                        weekWorkoutDurationMs = state.weekWorkoutDurationMs,
                        workoutGoal = onboardingProfile.sessionsPerWeek,
                        lastWeekWorkoutCount = state.lastWeekWorkoutCount,
                        lastWeekVolume = state.lastWeekVolume,
                        currentStreak = state.currentStreak,
                        bestStreak = state.bestStreak,
                        weeklyTopExercise = state.weeklyTopExercise,
                        isDark = isDark,
                        isLbs = isLbs,
                        textPrimary = p.tp,
                        textSecondary = p.ts,
                        cardBg = p.cr,
                        accent = p.ac,
                        iconBg = p.acs,
                        strings = strings,
                        weightLabel = { kg -> weightLabel(kg, isLbs) }
                        )
                    }
                }
            }
        }
    }
}

// ── AMBIENT GLOW ───────────────────────────────────────────────────

@Composable
private fun AmbientGlowLayer(accent: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Brush-ul e static — se creează o singură dată, nu la fiecare frame.
    // Alpha se animă pe graphicsLayer (transformare GPU), fără alocări per-frame.
    val glowBrush = remember(accent) {
        Brush.radialGradient(
            colors = listOf(
                accent.copy(alpha = 0.5f),
                Color.Transparent
            )
        )
    }

    Box(modifier = Modifier.fillMaxWidth().height(10.dp)) {
        Canvas(
            modifier = Modifier
                .size(280.dp, 280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-100).dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                    alpha = glowAlpha
                }
        ) {
            drawCircle(
                brush = glowBrush,
                radius = size.width / 2
            )
        }
    }
}

// ── PARTICLE FIELD (hero de la „Antrenamentul de azi”) ────────────

/** O particulă plutitoare cu poziție, viteză, mărime (în px, precalculată) și drift proprii. */
private data class Particle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val radiusPx: Float,
    val alpha: Float,
    val drift: Float,
    val phaseOffset: Float
)

/**
 * Câmp de particule desenat pe Canvas — fluid și ușor pe resurse.
 * Particulele plutesc lent în sus, oscilează orizontal și au alpha animat.
 */
@Composable
private fun ParticleField(
    accent: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val particles = remember(density) {
        List(16) { i ->
            val r = Random(i * 7919L + 13L)
            val sizeDp = 1.5f + r.nextFloat() * 3.0f
            Particle(
                x = r.nextFloat(),
                y = r.nextFloat(),
                speed = 0.02f + r.nextFloat() * 0.04f,
                radiusPx = with(density) { sizeDp.dp.toPx() },
                alpha = 0.10f + r.nextFloat() * 0.25f,
                drift = (r.nextFloat() - 0.5f) * 0.02f,
                phaseOffset = r.nextFloat() * 6.28f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val t by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleTime"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            // Înaintează lent în sus (wrap-around) + oscilație orizontală lentă
            val travel = (t + p.phaseOffset / 6.28f) % 1f
            val py = ((p.y - travel * p.speed * 3f) % 1f + 1f) % 1f
            val px = p.x + sin(t * 6.28f + p.phaseOffset) * p.drift * 2f
            val fade = sin(travel * 3.14f).coerceIn(0f, 1f)
            drawCircle(
                color = accent.copy(alpha = p.alpha * fade),
                radius = p.radiusPx,
                center = Offset(px * w, py * h)
            )
        }
    }
}

// ── HEADER ─────────────────────────────────────────────────────────

@Composable
private fun HeaderSection(
    name: String,
    greeting: String,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean,
    profilePhotoUri: String = "",
    profilePhotoVersion: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary,
                letterSpacing = 2.sp,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = name.uppercase(),
                fontFamily = Varien,
                fontSize = 28.sp,
                color = textPrimary,
                letterSpacing = 3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Avatar — poza de profil dacă există, altfel gradient cu inițială
        Box {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(accent, Color(0xFFFF6B4A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUri.isNotBlank()) {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(
                            // Sufix de cache-busting sigur pentru orice tip de URL (Firebase Storage / file://)
                            model = cacheBustedPhotoUrl(profilePhotoUri, profilePhotoVersion),
                            contentScale = ContentScale.Crop
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = name.firstOrNull()?.uppercase()?.toString() ?: "K",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2DD4A0))
                    .border(2.dp, if (isDark) DarkBackground else LightBackground, CircleShape)
            )
        }
    }
}

// ── WEEK STRIP ─────────────────────────────────────────────────────

@Composable
private fun WeekStrip(
    accent: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    isDark: Boolean
) {
    val today = LocalDate.now()
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val appLang = LanguageManager.getLanguage()
    val dayFmt = DateTimeFormatter.ofPattern("EEE", Locale(appLang))
    val days = (0..6).map { monday.plusDays(it.toLong()) }
    val labels = days.map { it.format(dayFmt).replaceFirstChar { c -> c.uppercaseChar() } }
    val nums = days.map { it.dayOfMonth }
    val todayIndex = today.dayOfWeek.value - 1 // Mon=0
    val completedIndices = (0 until todayIndex).toSet()
    val borderColor = if (isDark) Color(0x0FFFFFFF) else Color(0x14000000)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            days.forEachIndexed { index, _ ->
                val isToday = index == todayIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isToday) accent.copy(alpha = 0.16f) else Color.Transparent)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = labels[index],
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isToday) accent else textSecondary.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = nums[index].toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) accent else textPrimary.copy(alpha = 0.8f)
                    )
                    if (index in completedIndices) {
                        Spacer(Modifier.height(3.dp))
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF2DD4A0)))
                    }
                }
            }
        }
    }
}

// ── STREAK CARD ────────────────────────────────────────────────────

@Composable
private fun StreakCard(
    streak: Int,
    bestStreak: Int,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    isDark: Boolean,
    strings: LanguageManager.Strings
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val borderColor = if (isDark) Color(0x14FF3C3C) else accent.copy(alpha = 0.12f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.streakLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = textPrimary
                )
                Text(
                    text = "${strings.bestStreak}: $bestStreak ${strings.days}",
                    fontSize = 12.sp,
                    color = textSecondary
                )
            }
            Text(
                text = "$streak",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = accent
            )
        }
    }
}

// ── MOOD SELECTOR (4 stări) ────────────────────────────────────────

@Composable
private fun MoodSelector(
    selected: Int,
    onMoodSelected: (Int) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    isDark: Boolean
) {
    val moods = listOf(
        Triple(0, R.drawable.ic_mood_exhausted, strings.exhausted),  // Epuizat
        Triple(1, R.drawable.ic_mood_tired, strings.tiredLabel),     // Obosit
        Triple(2, R.drawable.ic_mood_normal, strings.normalLabel),   // Normal
        Triple(3, R.drawable.ic_mood_energic, strings.energeticLabel) // Energic
    )
    val borderColor = if (isDark) Color(0x0FFFFFFF) else Color(0x14000000)
    val selectedColor = Color(0xFF2DD4A0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = strings.howDoYouFeel.uppercase(),
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = textSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { (mood, iconRes, label) ->
                    val isActive = mood == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) selectedColor.copy(alpha = 0.12f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isActive) selectedColor.copy(alpha = 0.35f) else borderColor,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onMoodSelected(mood) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium,
                            color = if (isActive) selectedColor else textSecondary
                        )
                    }
                }
            }
        }
    }
}

// ── WORKOUT HERO CARD ──────────────────────────────────────────────

@Composable
private fun WorkoutHeroCard(
    todayWorkout: TodayWorkout?,
    onboardingProfile: UserOnboardingProfile,
    strings: LanguageManager.Strings,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    isDark: Boolean,
    onExerciseClick: (String, String) -> Unit,
    onStartWorkout: () -> Unit
) {
    val workoutExercises = todayWorkout?.exercises ?: emptyList()
    val goalLabel = when (onboardingProfile.goal) {
        "strength" -> strings.goalStrength.uppercase()
        "mass" -> strings.goalMass.uppercase()
        "weight_loss" -> strings.goalWeightLoss.uppercase()
        "maintenance" -> strings.goalMaintenance.uppercase()
        else -> ""
    }
    val groupedByGroup = workoutExercises.groupBy { it.group }
    val totalExercises = workoutExercises.size
    val estimatedMinutes = totalExercises * 3
    val totalSets = workoutExercises.sumOf { it.sets }
    val dayLabel = todayWorkout?.let {
        val appLang = LanguageManager.getLanguage()
        val dayName = it.date.format(
            DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale(appLang))
        )
        dayName.replaceFirstChar { c -> c.uppercase() }
    } ?: ""
    val borderColor = if (isDark) Color(0x0FFFFFFF) else Color(0x14000000)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        // Hero gradient (height adapts to content so the deload banner never clips)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            Color(0x14FF6B4A),
                            Color(0xFF4E8CFF).copy(alpha = 0.06f)
                        )
                    )
                )
        ) {
            // Particule plutitoare animate pe Canvas (peste gradient, sub conținut)
            ParticleField(
                accent = accent,
                modifier = Modifier.matchParentSize()
            )
            Column(modifier = Modifier.padding(16.dp)) {
                if (todayWorkout?.isDeloadActive == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RecoveryOrange, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            strings.deloadActiveThisWeek,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RecoveryOrange
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(accent.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = accent, modifier = Modifier.size(11.dp))
                    Text(
                        text = if (goalLabel.isNotBlank()) goalLabel else strings.todaysWorkout.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = accent
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = dayLabel,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = textPrimary
                )
            }
        }

        // Body
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WorkoutMeta(Icons.Filled.FitnessCenter, "$totalExercises ${strings.exercises}")
                WorkoutMeta(Icons.Outlined.Schedule, "~$estimatedMinutes min")
                WorkoutMeta(Icons.Filled.Layers, "$totalSets ${strings.sets}")
            }
            Spacer(Modifier.height(14.dp))

            var rowIndex = 0
            groupedByGroup.forEach { (group, exercises) ->
                Text(
                    text = WorkoutCycleGenerator.formatGroupName(group, strings).uppercase(),
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                exercises.forEach { ex ->
                    rowIndex++
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onExerciseClick(ex.group, ex.name) }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = if (isDark) 0.06f else 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rowIndex",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondary
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = ex.name,
                            fontSize = 13.sp,
                            color = textPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (ex.sets == 1 && ex.reps.endsWith("s")) ex.reps else "${ex.sets}x${ex.reps}",
                            fontSize = 12.sp,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            StartWorkoutButton(
                label = strings.startWorkout.uppercase(),
                accent = accent,
                onClick = onStartWorkout
            )
        }
    }
}

@Composable
private fun WorkoutMeta(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF8E8E93))
        Spacer(Modifier.width(6.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StartWorkoutButton(label: String, accent: Color, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val shineOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(accent, Color(0xFFFF6B4A))))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val shineX = shineOffset * size.width
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    startX = shineX - size.width * 0.3f,
                    endX = shineX + size.width * 0.3f
                ),
                size = size
            )
        }
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
            color = Color.White
        )
    }
}

// ── REST DAY CARD ──────────────────────────────────────────────────

@Composable
private fun RestDayCard(
    todayWorkout: TodayWorkout,
    strings: LanguageManager.Strings,
    p: AppPalette,
    isDark: Boolean
) {
    val appLang = LanguageManager.getLanguage()
    val restDayName = todayWorkout.date.format(
        DateTimeFormatter.ofPattern("EEEE", Locale(appLang))
    ).replaceFirstChar { it.uppercase() }

    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        p = p,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column {
            Text(
                strings.todaysWorkout.uppercase(),
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                color = p.ac,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${strings.dayLabel} ${todayWorkout.dayInCycle} ${strings.ofCycle} · $restDayName",
                fontSize = 13.sp,
                color = p.ts,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "🌙",
                fontSize = 36.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                strings.todayYouRest,
                fontSize = 20.sp,
                color = p.tp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(6.dp))
            Text(
                strings.restDayMessage,
                fontSize = 13.sp,
                color = p.ts,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(p.acs, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = p.ac,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    strings.restDayTip,
                    fontSize = 12.sp,
                    color = p.ts
                )
            }
        }
    }
}

// ── DELOAD DUE BANNER ──────────────────────────────────────────────

@Composable
private fun DeloadDueBanner(
    strings: LanguageManager.Strings,
    p: AppPalette,
    onClick: () -> Unit
) {
    val accent = RecoveryYellow // same theme color RestDayScreen uses for the "time for deload" state
    AppGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        p = p,
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    strings.timeForDeload,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.tp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    strings.deloadDueBanner,
                    fontSize = 11.sp,
                    color = p.ts,
                    lineHeight = 15.sp
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = p.ts,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
