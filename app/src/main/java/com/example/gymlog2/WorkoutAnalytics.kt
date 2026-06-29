package com.example.gymlog2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*
import java.util.*

data class MuscleGroupVolume(
    val group: String,
    val volume: Double,
    val sessions: Int,
    val percentage: Double
)

data class WeeklyProgress(
    val weekLabel: String,
    val volume: Double,
    val sessions: Int
)

data class ExerciseProgress(
    val name: String,
    val currentMax: Double,
    val previousMax: Double,
    val changePercent: Double
)

fun computeMuscleGroupAnalytics(
    workouts: List<AntrenamentEntity>,
    exercises: List<ExercitiuEntity>
): List<MuscleGroupVolume> {
    val groupVolumes = mutableMapOf<String, Double>()
    val groupSessions = mutableMapOf<String, Int>()

    for (w in workouts) {
        val workoutExercises = exercises.filter { it.antrenamentId == w.id }
        val vol = workoutExercises.sumOf { it.greutateKg * it.repetari }
        groupVolumes[w.grupaMusculara] = (groupVolumes[w.grupaMusculara] ?: 0.0) + vol
        groupSessions[w.grupaMusculara] = (groupSessions[w.grupaMusculara] ?: 0) + 1
    }

    val totalVolume = groupVolumes.values.sum()
    return groupVolumes.map { (group, volume) ->
        MuscleGroupVolume(
            group = group,
            volume = volume,
            sessions = groupSessions[group] ?: 0,
            percentage = if (totalVolume > 0) (volume / totalVolume * 100) else 0.0
        )
    }.sortedByDescending { it.volume }
}

fun computeWeeklyProgress(
    workouts: List<AntrenamentEntity>,
    exercises: List<ExercitiuEntity>,
    weeks: Int = 12
): List<WeeklyProgress> {
    val cal = Calendar.getInstance()
    val result = mutableListOf<WeeklyProgress>()

    for (i in weeks - 1 downTo 0) {
        val weekCal = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -i)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = weekCal.timeInMillis
        weekCal.add(Calendar.WEEK_OF_YEAR, 1)
        val weekEnd = weekCal.timeInMillis

        val weekWorkouts = workouts.filter { it.data in weekStart until weekEnd }
        val weekVolume = weekWorkouts.sumOf { w ->
            exercises.filter { it.antrenamentId == w.id }.sumOf { it.greutateKg * it.repetari }
        }

        val labelCal = Calendar.getInstance().apply { timeInMillis = weekStart }
        result.add(WeeklyProgress(
            weekLabel = "W${weeks - i}",
            volume = weekVolume,
            sessions = weekWorkouts.size
        ))
    }
    return result
}

fun computeExerciseProgress(
    allExercises: List<ExercitiuEntity>,
    workouts: List<AntrenamentEntity>
): List<ExerciseProgress> {
    val cal = Calendar.getInstance()
    val now = cal.timeInMillis
    cal.add(Calendar.WEEK_OF_YEAR, -4)
    val fourWeeksAgo = cal.timeInMillis
    cal.add(Calendar.WEEK_OF_YEAR, -4)
    val eightWeeksAgo = cal.timeInMillis

    val recentWorkoutIds = workouts.filter { it.data >= fourWeeksAgo }.map { it.id }.toSet()
    val prevWorkoutIds = workouts.filter { it.data in eightWeeksAgo until fourWeeksAgo }.map { it.id }.toSet()

    val recentExercises = allExercises.filter { it.antrenamentId in recentWorkoutIds }
    val prevExercises = allExercises.filter { it.antrenamentId in prevWorkoutIds }

    val recentMaxes = recentExercises.groupBy { it.numeExercitiu }
        .mapValues { (_, sets) -> sets.maxOfOrNull { it.greutateKg } ?: 0.0 }
    val prevMaxes = prevExercises.groupBy { it.numeExercitiu }
        .mapValues { (_, sets) -> sets.maxOfOrNull { it.greutateKg } ?: 0.0 }

    return recentMaxes.map { (name, currentMax) ->
        val prevMax = prevMaxes[name] ?: 0.0
        val change = if (prevMax > 0) ((currentMax - prevMax) / prevMax * 100) else 0.0
        ExerciseProgress(name, currentMax, prevMax, change)
    }.sortedByDescending { it.changePercent }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutAnalyticsScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    db: AppDatabase,
    userId: String,
    onBack: () -> Unit
) {
    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed

    var workouts by remember { mutableStateOf<List<AntrenamentEntity>>(emptyList()) }
    var exercises by remember { mutableStateOf<List<ExercitiuEntity>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(userId) {
        workouts = db.antrenamentDao().getAllForUser(userId)
        val allEx = mutableListOf<ExercitiuEntity>()
        for (w in workouts) {
            allEx.addAll(db.exercitiuDao().getForAntrenament(w.id))
        }
        exercises = allEx
    }

    val muscleData = remember(workouts, exercises) { computeMuscleGroupAnalytics(workouts, exercises) }
    val weeklyData = remember(workouts, exercises) { computeWeeklyProgress(workouts, exercises) }
    val exerciseData = remember(exercises, workouts) { computeExerciseProgress(exercises, workouts) }
    val totalVolume = muscleData.sumOf { it.volume }
    val totalSessions = workouts.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceBg)
            )
        },
        containerColor = surfaceBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(isDark, "Total Volume", "${String.format("%.0f", totalVolume)} kg", accent, Modifier.weight(1f))
                    StatCard(isDark, "Workouts", "$totalSessions", accent, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Muscle Groups", "Weekly", "Progress").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.2f),
                                selectedLabelColor = accent
                            )
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    items(muscleData) { data ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(data.group, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${String.format("%.0f", data.volume)} kg",
                                        color = accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { (data.percentage / 100f).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = accent,
                                    trackColor = accent.copy(alpha = 0.15f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${String.format("%.1f", data.percentage)}%", color = textSecondary, fontSize = 12.sp)
                                    Text("${data.sessions} sessions", color = textSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    items(weeklyData) { week ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(week.weekLabel, color = textPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(40.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    LinearProgressIndicator(
                                        progress = {
                                            val maxVol = weeklyData.maxOfOrNull { it.volume } ?: 1.0
                                            (week.volume / maxVol).toFloat()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = accent,
                                        trackColor = accent.copy(alpha = 0.15f)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${String.format("%.0f", week.volume)} kg | ${week.sessions} sessions",
                                        color = textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    if (exerciseData.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Need at least 8 weeks of data to show progress comparison",
                                    modifier = Modifier.padding(24.dp),
                                    color = textSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    items(exerciseData) { ep ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ep.name, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(
                                        "${String.format("%.1f", ep.previousMax)} -> ${String.format("%.1f", ep.currentMax)} kg",
                                        color = textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                val color = if (ep.changePercent > 0) Color(0xFF2E7D32) else if (ep.changePercent < 0) Color(0xFFD32F2F) else textSecondary
                                Text(
                                    "${if (ep.changePercent > 0) "+" else ""}${String.format("%.1f", ep.changePercent)}%",
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(isDark: Boolean, label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val cardBg = if (isDark) cardColor() else LightCard
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = textSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}
