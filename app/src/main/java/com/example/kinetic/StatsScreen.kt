package com.example.kinetic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.theme.*
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.AppSectionLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

private val GoldPR = Color(0xFFF5B942)

// ── Modele ──────────────────────────────────────────────────
private data class ChartData(
    val values: List<Double>,
    val labels: List<String>,
    val label: String,
    val unit: String,
    val delta: String,
    val hasData: Boolean = values.any { it > 0 }
)

private data class MuscleVolumeUi(
    val name: String,
    val kg: Int,
    val pct: Float,
    val color: Color,
    val iconRes: Int
)

private data class PersonalBestUi(
    val name: String,
    val weight: String,
    val reps: String,
    val date: String,
    val color: Color,
    val bg: Color
)

private data class WeeklyGoalUi(
    val name: String,
    val pct: Int,
    val current: String,
    val target: String,
    val color: Color
)

// ── Screen ──────────────────────────────────────────────────
@Composable
fun StatsScreen(
    isDark: Boolean,
    isLbs: Boolean,
    strings: LanguageManager.Strings,
    weeklyTopExercise: String?,
    weeklyTotalKg: Double,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onExerciseHistoryClick: (String) -> Unit = {},
    userId: String,
    currentStreak: Int = 0,
    bestStreak: Int = 0,
    badgeCount: Int = 0,
    recentBadges: List<BadgeEntity> = emptyList(),
    allExerciseNames: List<String> = emptyList()
) {
    val context = LocalContext.current
    val palette = appPalette(isDark)
    val surfaceBg = palette.bg
    val textPrimary = palette.tp
    val textSecondary = palette.ts
    val textTertiary = palette.tt
    val cardBg = palette.cr
    val cardBorder = palette.bd
    val accent = palette.ac
    val dividerBg = palette.bd

    var selectedPeriod by remember { mutableIntStateOf(0) }
    var chartData by remember { mutableStateOf<Map<Int, ChartData>>(emptyMap()) }
    var selectedExercise by remember { mutableStateOf<String?>(null) }
    var showExerciseDropdown by remember { mutableStateOf(false) }

    var allWorkouts by remember { mutableStateOf<List<AntrenamentEntity>>(emptyList()) }
    var allExercisesList by remember { mutableStateOf<List<ExercitiuEntity>>(emptyList()) }

    LaunchedEffect(userId) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val workouts = db.antrenamentDao().getAllForUser(userId)
                val exList = mutableListOf<ExercitiuEntity>()
                for (w in workouts) {
                    exList.addAll(db.exercitiuDao().getForAntrenament(w.id))
                }
                allWorkouts = workouts
                allExercisesList = exList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val muscleData = remember(allWorkouts, allExercisesList) { computeMuscleGroupAnalytics(allWorkouts, allExercisesList) }
    val weeklyData = remember(allWorkouts, allExercisesList) { computeWeeklyProgress(allWorkouts, allExercisesList) }
    val exerciseData = remember(allExercisesList, allWorkouts) { computeExerciseProgress(allExercisesList, allWorkouts) }
    val personalBests = remember(allExercisesList, allWorkouts) { computePersonalBests(allExercisesList, allWorkouts) }
    val totalVolume = muscleData.sumOf { it.volume }
    val totalSessions = allWorkouts.size
    val newPBs = personalBests.count { it.isNew }
    val totalSets = allExercisesList.size
    val totalMinutes = allWorkouts.sumOf { it.durationMs } / 60000.0

    LaunchedEffect(selectedExercise) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val cal = Calendar.getInstance()
                val now = cal.timeInMillis
                val result = mutableMapOf<Int, ChartData>()
                for (periodIdx in 0..2) {
                    val days = when (periodIdx) { 0 -> 7; 1 -> 30; else -> 90 }
                    cal.timeInMillis = now
                    cal.add(Calendar.DAY_OF_YEAR, -days)
                    val startTime = cal.timeInMillis
                    val periodWorkouts = if (selectedExercise != null) {
                        db.exercitiuDao().getWorkoutsWithExercise(userId, selectedExercise!!, startTime, now)
                    } else {
                        db.antrenamentDao().getWorkoutsInPeriod(userId, startTime, now)
                    }
                    val workoutIds = periodWorkouts.map { it.id }
                    val setCountMap = if (workoutIds.isNotEmpty()) {
                        db.exercitiuDao().getSetCountsForWorkouts(workoutIds).associate { it.antrenamentId to it.cnt }
                    } else emptyMap()
                    val dateFmt = if (days <= 7) SimpleDateFormat("EEE", Locale.getDefault())
                        else SimpleDateFormat("dd", Locale.getDefault())
                    val volumeValues = mutableListOf<Double>()
                    val volumeLabels = mutableListOf<String>()

                    if (days <= 7) {
                        for (i in 0 until 7) {
                            val dayCal = Calendar.getInstance().apply {
                                timeInMillis = now
                                add(Calendar.DAY_OF_YEAR, -(6 - i))
                            }
                            val dayWorkouts = periodWorkouts.filter {
                                val wc = Calendar.getInstance().apply { timeInMillis = it.data }
                                wc.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR) &&
                                        wc.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR)
                            }
                            volumeValues.add(dayWorkouts.sumOf { it.totalWeight })
                            volumeLabels.add(dateFmt.format(dayCal.time).take(3))
                        }
                    } else if (days <= 30) {
                        val weeks = (days / 7) + 1
                        for (w in 0 until weeks) {
                            val weekStartCal = Calendar.getInstance().apply {
                                timeInMillis = now
                                add(Calendar.DAY_OF_YEAR, -((weeks - 1 - w) * 7))
                            }
                            val weekEndCal = Calendar.getInstance().apply {
                                timeInMillis = weekStartCal.timeInMillis
                                add(Calendar.DAY_OF_YEAR, 7)
                            }
                            val weekWorkouts = periodWorkouts.filter { it.data >= weekStartCal.timeInMillis && it.data < weekEndCal.timeInMillis }
                            volumeValues.add(weekWorkouts.sumOf { it.totalWeight })
                            volumeLabels.add("S${w + 1}")
                        }
                    } else {
                        val months = 3
                        for (m in 0 until months) {
                            val monthCal = Calendar.getInstance().apply {
                                timeInMillis = now
                                add(Calendar.MONTH, -(months - 1 - m))
                            }
                            val mStart = Calendar.getInstance().apply {
                                timeInMillis = monthCal.timeInMillis
                                set(Calendar.DAY_OF_MONTH, 1)
                                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                            }
                            val mEnd = Calendar.getInstance().apply {
                                timeInMillis = mStart.timeInMillis
                                add(Calendar.MONTH, 1)
                            }
                            val monthWorkouts = periodWorkouts.filter { it.data >= mStart.timeInMillis && it.data < mEnd.timeInMillis }
                            volumeValues.add(monthWorkouts.sumOf { it.totalWeight })
                            val monthFmt = SimpleDateFormat("MMM", Locale.getDefault())
                            volumeLabels.add(monthFmt.format(mStart.time).take(3))
                        }
                    }

                    fun computeDelta(values: List<Double>): String {
                        if (values.size < 2) return ""
                        val prev = values[values.size - 2]
                        val curr = values.last()
                        if (prev == 0.0 && curr == 0.0) return ""
                        if (prev == 0.0 && curr > 0.0) return "New"
                        val pct = ((curr - prev) / prev * 100).toInt()
                        return if (pct >= 0) "+$pct% ${strings.vsPrevious}" else "$pct% ${strings.vsPrevious}"
                    }

                    result[periodIdx] = ChartData(
                        volumeValues, volumeLabels, strings.volume, if (isLbs) "lbs" else "kg",
                        computeDelta(volumeValues)
                    )
                }
                chartData = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val currentData = chartData[selectedPeriod]

    // Heatmap: 28 days of real workout activity
    val heatmapData = remember(allWorkouts) { buildHeatmapData(allWorkouts) }

    val periods = listOf("7D", "30D", "ALL")

    // Radar from real muscle data (top 8)
    val radarGroups = muscleData.take(8)
    val radarColors = listOf(
        accent, palette.bl, palette.pu, palette.am, palette.rs, Color(0xFF22D3EE), Color(0xFFF97316), Color(0xFF34D399), Color(0xFFE879F9)
    )

    // Weekly goals from real data
    val weekVolume = weeklyData.lastOrNull()?.volume ?: 0.0
    val weekSessions = weeklyData.lastOrNull()?.sessions ?: 0
    val goals = listOf(
        WeeklyGoalUi(strings.workoutsLabel, ((weekSessions.toFloat() / 5f) * 100).toInt().coerceIn(0, 100), "$weekSessions ${strings.sessions}", "5", accent),
        WeeklyGoalUi(strings.volumeLabel, ((weekVolume / 5000.0) * 100).toInt().coerceIn(0, 100), "${formatKg(weekVolume)} kg", "5,000 kg", palette.gn),
        WeeklyGoalUi(strings.currentStreakLabel, ((currentStreak.toFloat() / maxOf(bestStreak, 7)) * 100).toInt().coerceIn(0, 100), "$currentStreak ${strings.days}", "${maxOf(bestStreak, 7)} ${strings.days}", palette.am)
    )

    // Volume-by-muscle UI list from real data
    val muscleUi = muscleData.take(9).mapIndexed { idx, md ->
        MuscleVolumeUi(
            name = LanguageManager.translateMuscleGroup(md.group, strings),
            kg = md.volume.toInt(),
            pct = (md.percentage / 100.0).toFloat().coerceIn(0f, 1f),
            color = muscleColors[idx % muscleColors.size],
            iconRes = muscleIconRes(md.group)
        )
    }

    // Personal bests UI
    val pbUi = personalBests.map { pb ->
        PersonalBestUi(
            name = pb.exerciseName,
            weight = if (isLbs) "%.1f".format(pb.maxWeight * 2.20462) else "%.0f".format(pb.maxWeight),
            reps = "${pb.reps}",
            date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(pb.achievedAt)),
            color = if (pb.isNew) GoldPR else palette.bl,
            bg = if (pb.isNew) palette.ams else palette.bls
        )
    }

    StatsScreenContent(
        isDark = isDark,
        strings = strings,
        palette = palette,
        surfaceBg = surfaceBg,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textTertiary = textTertiary,
        cardBg = cardBg,
        cardBorder = cardBorder,
        accent = accent,
        dividerBg = dividerBg,
        paddingValues = paddingValues,
        periods = periods,
        selectedPeriod = selectedPeriod,
        onPeriodSelect = { selectedPeriod = it },
        selectedExercise = selectedExercise,
        allExerciseNames = allExerciseNames,
        showExerciseDropdown = showExerciseDropdown,
        onToggleDropdown = { showExerciseDropdown = !showExerciseDropdown },
        onSelectExercise = { selectedExercise = it; showExerciseDropdown = false },
        totalVolume = totalVolume,
        isLbs = isLbs,
        currentChart = currentData,
        totalSessions = totalSessions,
        totalMinutes = totalMinutes,
        totalSets = totalSets,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        badgeCount = badgeCount,
        heatmapData = heatmapData,
        radarGroups = radarGroups,
        radarColors = radarColors,
        muscleUi = muscleUi,
        weeklyTopExercise = weeklyTopExercise,
        onExerciseHistoryClick = onExerciseHistoryClick,
        pbUi = pbUi,
        goals = goals
    )
}

@Composable
private fun StatsScreenContent(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    palette: AppPalette,
    surfaceBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    cardBg: Color,
    cardBorder: Color,
    accent: Color,
    dividerBg: Color,
    paddingValues: PaddingValues,
    periods: List<String>,
    selectedPeriod: Int,
    onPeriodSelect: (Int) -> Unit,
    selectedExercise: String?,
    allExerciseNames: List<String>,
    showExerciseDropdown: Boolean,
    onToggleDropdown: () -> Unit,
    onSelectExercise: (String?) -> Unit,
    totalVolume: Double,
    isLbs: Boolean,
    currentChart: ChartData?,
    totalSessions: Int,
    totalMinutes: Double,
    totalSets: Int,
    currentStreak: Int,
    bestStreak: Int,
    badgeCount: Int,
    heatmapData: List<Int>,
    radarGroups: List<MuscleGroupVolume>,
    radarColors: List<Color>,
    muscleUi: List<MuscleVolumeUi>,
    weeklyTopExercise: String?,
    onExerciseHistoryClick: (String) -> Unit,
    pbUi: List<PersonalBestUi>,
    goals: List<WeeklyGoalUi>
) {
    var visibleItems by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..16) {
            delay(80L)
            visibleItems = i
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = AppConstants.BOTTOM_NAV_PADDING
        )
    ) {
        // Header
        item {
            AnimatedVisibility(
                visible = visibleItems > 0,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(strings.stats, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-1).sp)
                    Text(periodSubtitle(selectedPeriod, strings), fontSize = 11.sp, color = textSecondary)
                }
                PeriodTabs(periods, selectedPeriod, onPeriodSelect, accent, cardBg, cardBorder, textSecondary, textTertiary)
            }
            }
        }

        // Exercise filter dropdown
        if (allExerciseNames.isNotEmpty()) {
            item {
                AnimatedVisibility(
                    visible = visibleItems > 1,
                    enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
                ) {
                Box(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dividerBg, RoundedCornerShape(10.dp))
                            .clickable { onToggleDropdown() }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedExercise ?: strings.allExercises,
                            color = if (selectedExercise != null) textPrimary else textSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showExerciseDropdown, onDismissRequest = { onToggleDropdown() }) {
                        DropdownMenuItem(text = { Text(strings.allExercises) }, onClick = { onSelectExercise(null) })
                        allExerciseNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = { onSelectExercise(name) }
                            )
                        }
                    }
                }
                }
            }
        }

        // Hero volume
        item {
            AnimatedVisibility(
                visible = visibleItems > 2,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                HeroVolumeCard(
                    totalVolume = totalVolume,
                    isLbs = isLbs,
                    currentChart = currentChart,
                    totalSessions = totalSessions,
                    totalMinutes = totalMinutes,
                    totalSets = totalSets,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    textTertiary = textTertiary,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    accent = accent,
                    strings = strings,
                    p = palette
                )
            }
            Spacer(Modifier.height(14.dp))
            }
        }

        // Ring trio
        item {
            AnimatedVisibility(
                visible = visibleItems > 3,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                RingTrioRow(
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    badgeCount = badgeCount,
                    strings = strings,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    accent = accent,
                    p = palette
                )
            }
            Spacer(Modifier.height(14.dp))
            }
        }

        // Heatmap
        item {
            AnimatedVisibility(
                visible = visibleItems > 4,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { SectionTitle(strings.thisMonth, textSecondary) }
            }
        }
        item {
            AnimatedVisibility(
                visible = visibleItems > 5,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                HeatmapCard(heatmapData, strings, cardBg, cardBorder, accent, textTertiary, textSecondary, palette)
            }
            Spacer(Modifier.height(14.dp))
            }
        }

        // Radar
        item {
            AnimatedVisibility(
                visible = visibleItems > 6,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { SectionTitle(strings.muscleGroups, textSecondary) }
            }
        }
        item {
            AnimatedVisibility(
                visible = visibleItems > 7,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            RadarCard(radarGroups, radarColors, cardBg, cardBorder, textSecondary, strings, palette)
            Spacer(Modifier.height(14.dp))
            }
        }

        // Volume bars
        item {
            AnimatedVisibility(
                visible = visibleItems > 8,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { SectionTitle(strings.volumeLabel, textSecondary) }
            }
        }
        item {
            AnimatedVisibility(
                visible = visibleItems > 9,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                VolumeByMuscleCard(muscleUi, cardBg, cardBorder, textPrimary, textSecondary, palette)
            }
            Spacer(Modifier.height(14.dp))
            }
        }

        // Top exercise
        item {
            AnimatedVisibility(
                visible = visibleItems > 10,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { SectionTitle(strings.mostTrained, textSecondary) }
            }
        }
        item {
            AnimatedVisibility(
                visible = visibleItems > 11,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                TopExerciseCard(weeklyTopExercise, onExerciseHistoryClick, strings, cardBg, cardBorder, textPrimary, textSecondary, textTertiary, accent, palette)
            }
            Spacer(Modifier.height(14.dp))
            }
        }

        // PBs
        item {
            AnimatedVisibility(
                visible = visibleItems > 12,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { SectionTitle(strings.pbsTab, textSecondary) }
            }
        }
        item {
            AnimatedVisibility(
                visible = visibleItems > 13,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                PersonalBestsCard(pbUi, cardBg, cardBorder, textPrimary, textSecondary, textTertiary, palette)
            }
            Spacer(Modifier.height(14.dp))
            }
        }

        // Goals
        item {
            AnimatedVisibility(
                visible = visibleItems > 14,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) { SectionTitle(strings.weeklyTab, textSecondary) }
            }
        }
        item {
            AnimatedVisibility(
                visible = visibleItems > 15,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                WeeklyGoalsCard(goals, cardBg, cardBorder, textPrimary, textSecondary, palette)
            }
            Spacer(Modifier.height(14.dp))
            }
        }
    }
}

private fun periodSubtitle(period: Int, strings: LanguageManager.Strings): String = when (period) {
    0 -> "7 ${strings.days}"
    1 -> "30 ${strings.days}"
    else -> strings.thisMonth
}

// ── Period Tabs ─────────────────────────────────────────────
@Composable
private fun PeriodTabs(
    periods: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    accent: Color,
    cardBg: Color,
    cardBorder: Color,
    textSecondary: Color,
    textTertiary: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        periods.forEachIndexed { i, label ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (i == selected) accent.copy(alpha = 0.14f) else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(horizontal = 15.dp, vertical = 7.dp)
            ) {
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (i == selected) accent else textTertiary
                )
            }
        }
    }
}

// ── Section Title ───────────────────────────────────────────
@Composable
private fun SectionTitle(text: String, textSecondary: Color) {
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = textSecondary
    )
}

// ── Hero Volume Card ────────────────────────────────────────
@Composable
private fun HeroVolumeCard(
    totalVolume: Double,
    isLbs: Boolean,
    currentChart: ChartData?,
    totalSessions: Int,
    totalMinutes: Double,
    totalSets: Int,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    cardBg: Color,
    cardBorder: Color,
    accent: Color,
    strings: LanguageManager.Strings,
    p: AppPalette
) {
    AppGlassCard(p = p, cornerRadius = 18.dp) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(strings.totalVolumeLabel.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = textSecondary)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    formatChartValue(totalVolume),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 56.sp,
                    letterSpacing = (-3).sp,
                    color = accent
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isLbs) "lbs" else "kg",
                    fontSize = 14.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            // Trend
            val delta = currentChart?.delta
            if (!delta.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(p.gns)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.TrendingUp, null, tint = p.gn, modifier = Modifier.size(10.dp))
                    Text(delta, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.gn)
                }
            }

            // Mini stats
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniStat(p.gn, strings.workoutsLabel, "$totalSessions", textPrimary, textTertiary, Modifier.weight(1f), p)
                MiniStat(p.bl, strings.duration, String.format("%.1f", totalMinutes / 60.0), textPrimary, textTertiary, Modifier.weight(1f), p)
                MiniStat(p.pu, strings.sets, "$totalSets", textPrimary, textTertiary, Modifier.weight(1f), p)
            }

            // Sparkline
            Spacer(Modifier.height(16.dp))
            val sparkValues = currentChart?.values?.map { it } ?: emptyList()
            Canvas(modifier = Modifier.fillMaxWidth().height(52.dp)) {
                val w = size.width
                val h = size.height
                if (sparkValues.size < 2 || sparkValues.maxOrNull() == 0.0) {
                    drawLine(
                        color = textTertiary.copy(alpha = 0.3f),
                        start = Offset(0f, h * 0.7f),
                        end = Offset(w, h * 0.7f),
                        strokeWidth = 2.dp.toPx()
                    )
                } else {
                    val maxV = sparkValues.maxOrNull() ?: 1.0
                    val minV = sparkValues.minOrNull() ?: 0.0
                    val range = (maxV - minV).coerceAtLeast(1.0)
                    val step = w / (sparkValues.size - 1)
                    val path = Path().apply {
                        sparkValues.forEachIndexed { i, v ->
                            val x = i * step
                            val y = h - ((v - minV) / range * (h - 6.dp.toPx())).toFloat() - 3.dp.toPx()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }
                    drawPath(path, accent, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
                    val lastX = (sparkValues.size - 1) * step
                    val lastY = h - ((sparkValues.last() - minV) / range * (h - 6.dp.toPx())).toFloat() - 3.dp.toPx()
                    drawCircle(accent, 4.dp.toPx(), Offset(lastX, lastY))
                }
            }
        }
    }
}

@Composable
private fun MiniStat(color: Color, label: String, value: String, textPrimary: Color, textTertiary: Color, modifier: Modifier = Modifier, p: AppPalette) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(p.overlay(0.025f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Column {
            Text(label, fontSize = 9.sp, letterSpacing = 0.5.sp, color = textTertiary)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        }
    }
}

// ── Ring Trio ───────────────────────────────────────────────
@Composable
private fun RingTrioRow(
    currentStreak: Int,
    bestStreak: Int,
    badgeCount: Int,
    strings: LanguageManager.Strings,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color,
    accent: Color,
    p: AppPalette
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RingItem(
            modifier = Modifier.weight(1f),
            label = strings.currentStreakLabel,
            value = "$currentStreak",
            pct = (currentStreak.toFloat() / (bestStreak + 3).coerceAtLeast(1)).coerceIn(0f, 1f),
            color = accent,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            cardBg = cardBg,
            cardBorder = cardBorder,
            p = p
        )
        RingItem(
            modifier = Modifier.weight(1f),
            label = strings.bestStreakLabel,
            value = "$bestStreak",
            pct = if (bestStreak > 0) (bestStreak.toFloat() / (bestStreak + 3).coerceAtLeast(1)).coerceIn(0f, 1f) else 0f,
            color = p.gn,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            cardBg = cardBg,
            cardBorder = cardBorder,
            p = p
        )
        RingItem(
            modifier = Modifier.weight(1f),
            label = strings.badges,
            value = "$badgeCount",
            pct = (badgeCount / 10f).coerceIn(0f, 1f),
            color = p.am,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            cardBg = cardBg,
            cardBorder = cardBorder,
            p = p
        )
    }
}

@Composable
private fun RingItem(
    modifier: Modifier,
    label: String,
    value: String,
    pct: Float,
    color: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color,
    p: AppPalette
) {
    val animatedPct by animateFloatAsState(pct.coerceIn(0f, 1f), tween(1500, easing = EaseOutCubic), label = label)
    AppGlassCard(
        modifier = modifier,
        p = p,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val r = 26.dp.toPx()
                    val c = Offset(size.width / 2, size.height / 2)
                    drawCircle(p.overlay(0.05f), r, c, style = Stroke(5.dp.toPx()))
                    if (animatedPct > 0.001f) {
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedPct,
                            useCenter = false,
                            topLeft = Offset(c.x - r, c.y - r),
                            size = Size(r * 2, r * 2),
                            style = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 9.sp, letterSpacing = 0.8.sp, color = textSecondary)
        }
    }
}

// ── Heatmap ─────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeatmapCard(
    data: List<Int>,
    strings: LanguageManager.Strings,
    cardBg: Color,
    cardBorder: Color,
    accent: Color,
    textTertiary: Color,
    textSecondary: Color,
    p: AppPalette
) {
    val colors = listOf(
        p.overlay(0.025f),
        accent.copy(alpha = 0.08f),
        accent.copy(alpha = 0.2f),
        accent.copy(alpha = 0.38f),
        accent.copy(alpha = 0.6f),
        accent
    )
    AppGlassCard(p = p, cornerRadius = 18.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach {
                    Text(
                        it,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = textTertiary,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cellSize = (maxWidth - 3.dp * 6) / 7
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    data.forEach { v ->
                        val ci = if (v == 0) 0 else minOf((v / 1.1).toInt(), 5)
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(5.dp))
                                .background(colors[ci])
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${data.count { it > 0 }} ${strings.workoutsLabel}", fontSize = 9.sp, color = textTertiary)
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Low", fontSize = 7.sp, color = textTertiary)
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(accent.copy(alpha = 0.06f)))
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(accent.copy(alpha = 0.18f)))
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(accent.copy(alpha = 0.4f)))
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(accent))
                    Text("High", fontSize = 7.sp, color = textTertiary)
                }
            }
        }
    }
}

// ── Radar Chart ─────────────────────────────────────────────
// Arată ce grupă musculară a fost cel mai mult lucrată, pe baza
// volumului real logat (greutate × repetări), scalat față de grupa dominantă.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RadarCard(
    groups: List<MuscleGroupVolume>,
    colors: List<Color>,
    cardBg: Color,
    cardBorder: Color,
    textSecondary: Color,
    strings: LanguageManager.Strings,
    p: AppPalette
) {
    if (groups.isEmpty()) return
    val maxVolume = groups.maxOfOrNull { it.volume }?.takeIf { it > 0 } ?: return
    // Volumul fiecărei grupe raportat la grupa dominantă (0..1)
    val axes = groups.map { LanguageManager.translateMuscleGroup(it.group, strings) }
    val values = groups.map { (it.volume / maxVolume).toFloat().coerceIn(0f, 1f) }
    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        p = p,
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Radarul folosește toată lățimea cardului (limită rezonabilă pe tablete)
                val radarSize = minOf(maxWidth, 360.dp)
                Box(
                    modifier = Modifier.size(radarSize).aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = minOf(size.width, size.height) * 0.36f
                        val n = axes.size
                        for (i in 1..4) {
                            val ri = r * i / 4
                            val path = Path()
                            for (j in 0 until n) {
                                val a = (2 * Math.PI * j / n - Math.PI / 2).toFloat()
                                val x = cx + ri * cos(a); val y = cy + ri * sin(a)
                                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            path.close()
                            drawPath(path, p.overlay(0.04f), style = Stroke(1.dp.toPx()))
                        }
                        for (j in 0 until n) {
                            val a = (2 * Math.PI * j / n - Math.PI / 2).toFloat()
                            drawLine(
                                p.overlay(0.06f),
                                Offset(cx, cy),
                                Offset(cx + r * cos(a), cy + r * sin(a)),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        // Poligonul datelor — cu cât grupa a fost mai antrenată, cu atât mai aproape de inelul exterior
                        val dataPath = Path()
                        values.forEachIndexed { j, v ->
                            val a = (2 * Math.PI * j / n - Math.PI / 2).toFloat()
                            val x = (cx + r * v * cos(a)).toFloat(); val y = (cy + r * v * sin(a)).toFloat()
                            if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                        }
                        dataPath.close()
                        drawPath(dataPath, p.overlay(0.1f), style = Stroke(1.5.dp.toPx()))
                        values.forEachIndexed { j, v ->
                            val a = (2 * Math.PI * j / n - Math.PI / 2).toFloat()
                            drawCircle(
                                colors[j % colors.size],
                                3.dp.toPx(),
                                Offset((cx + r * v * cos(a)).toFloat(), (cy + r * v * sin(a)).toFloat())
                            )
                        }
                    }
                }
            }
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groups.forEachIndexed { i, g ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(8.dp).clip(RoundedCornerShape(3.dp)).background(colors[i % colors.size]))
                        Column {
                            Text(axes[i], fontSize = 9.sp, color = textSecondary)
                            Text(
                                "${formatKg(g.volume)} kg · ${g.percentage.toInt()}%",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors[i % colors.size]
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Volume by Muscle ────────────────────────────────────────
@Composable
private fun VolumeByMuscleCard(
    muscles: List<MuscleVolumeUi>,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    p: AppPalette
) {
    if (muscles.isEmpty()) return
    AppGlassCard(p = p, cornerRadius = 18.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            muscles.forEachIndexed { index, m ->
                val animatedPct by animateFloatAsState(m.pct.coerceIn(0f, 1f), tween(1400, easing = EaseOutCubic), label = m.name)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(m.color.copy(alpha = 0.07f)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(m.iconRes),
                            contentDescription = null,
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(m.color),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(m.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                            Text("${m.kg} kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = m.color)
                        }
                        Spacer(Modifier.height(5.dp))
                        Box(
                            Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp))
                                .background(p.overlay(0.04f))
                        ) {
                            Box(
                                Modifier.fillMaxHeight().fillMaxWidth(animatedPct).clip(RoundedCornerShape(4.dp))
                                    .background(m.color)
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "${(m.pct * 100).toInt()}%",
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp,
                            color = m.color.copy(alpha = 0.6f)
                        )
                    }
                }
                if (index != muscles.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// ── Top Exercise ────────────────────────────────────────────
@Composable
private fun TopExerciseCard(
    exercise: String?,
    onHistory: (String) -> Unit,
    strings: LanguageManager.Strings,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    accent: Color,
    p: AppPalette
) {
    AppGlassCard(p = p, cornerRadius = 18.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(p.pus),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.EmojiEvents, null, tint = p.pu, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(strings.mostTrained.uppercase(), fontSize = 9.sp, letterSpacing = 1.sp, color = textTertiary)
                Spacer(Modifier.height(2.dp))
                Text(exercise ?: "--", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (exercise != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        strings.exerciseHistory,
                        fontSize = 10.sp,
                        color = accent,
                        modifier = Modifier.clickable { onHistory(exercise) }
                    )
                }
            }
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(p.pus).padding(6.dp)) {
                Text("TOP", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, color = p.pu)
            }
        }
    }
}

// ── Personal Bests ──────────────────────────────────────────
@Composable
private fun PersonalBestsCard(
    pbs: List<PersonalBestUi>,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    p: AppPalette
) {
    if (pbs.isEmpty()) return
    AppGlassCard(p = p, cornerRadius = 18.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            pbs.take(5).forEachIndexed { index, pb ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.overlay(0.015f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(pb.bg),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.EmojiEvents, null, tint = pb.color, modifier = Modifier.size(15.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(pb.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(1.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(pb.weight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = pb.color)
                            Text("× ${pb.reps}", fontSize = 11.sp, color = textSecondary)
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.CalendarMonth, null, tint = textTertiary, modifier = Modifier.size(9.dp))
                            Text(pb.date, fontSize = 9.sp, color = textTertiary)
                        }
                    }
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(pb.bg).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("PB", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, color = pb.color)
                    }
                }
                if (index != pbs.take(5).lastIndex) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Weekly Goals ────────────────────────────────────────────
@Composable
private fun WeeklyGoalsCard(
    goals: List<WeeklyGoalUi>,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    p: AppPalette
) {
    AppGlassCard(p = p, cornerRadius = 18.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            goals.forEach { g ->
                val animatedPct by animateFloatAsState((g.pct / 100f).coerceIn(0f, 1f), tween(1400, easing = EaseOutCubic), label = g.name)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(g.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textPrimary)
                    Text("${g.pct}%", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = g.color)
                }
                Spacer(Modifier.height(7.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(p.overlay(0.04f))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(animatedPct).clip(RoundedCornerShape(5.dp)).background(g.color))
                }
                Row(Modifier.fillMaxWidth().padding(top = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(g.current, fontSize = 9.sp, color = textSecondary)
                    Text(g.target, fontSize = 9.sp, color = textSecondary)
                }
                if (g != goals.last()) Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ===HELPERS===
private val muscleColors = listOf(
    Color(0xFF2DD4A0), Color(0xFF4E8CFF), Color(0xFF9C6ADE), Color(0xFFF5A623), Color(0xFFFB7185), Color(0xFF22D3EE), Color(0xFFF97316), Color(0xFF34D399), Color(0xFFE879F9)
)

// Iconițe din onboarding pentru fiecare grupă musculară (PNG monocrom, tinted cu culoarea grupei)
private fun muscleIconRes(group: String): Int = when (group) {
    "Piept" -> R.drawable.onboarding_chest
    "Spate" -> R.drawable.onboarding_back
    "Umeri" -> R.drawable.onboarding_shoulders
    "Triceps", "Biceps", "Antebrat", "Antebrate" -> R.drawable.onboarding_arms
    "Abdomen" -> R.drawable.onboarding_core
    "Fese", "Fesieri" -> R.drawable.onboarding_glutes
    "Gambe" -> R.drawable.onboarding_legs
    "Picioare" -> R.drawable.onboarding_legs
    "Cardio" -> R.drawable.onboarding_cardio
    "Gat & Trapezi" -> R.drawable.onboarding_back
    else -> R.drawable.onboarding_strength
}

private fun buildHeatmapData(workouts: List<AntrenamentEntity>): List<Int> {
    val now = Calendar.getInstance()
    val dayStarts = (27 downTo 0).map { offset ->
        Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, -offset)
        }
    }
    return dayStarts.map { day ->
        val count = workouts.count {
            val wc = Calendar.getInstance().apply { timeInMillis = it.data }
            wc.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR) &&
                    wc.get(Calendar.YEAR) == day.get(Calendar.YEAR)
        }
        count.coerceIn(0, 5)
    }
}

private fun formatKg(value: Double): String {
    return if (value >= 1000) "%.1fK".format(value / 1000) else "%.0f".format(value)
}

private fun formatChartValue(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else String.format("%.1f", value)
}
