package com.example.kinetic

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.GlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.*
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.AppPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarWorkoutScreen(
    isDark: Boolean,
    onBackClick: () -> Unit,
    onWorkoutDeleted: () -> Unit = {},
    userId: String
) {
    val p = appPalette(isDark)
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val strings = LanguageManager.getStrings(context)
    val scope = rememberCoroutineScope()

    var currentMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedDay by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var workoutDays by remember { mutableStateOf(setOf<Int>()) }
    var selectedDayWorkouts by remember { mutableStateOf<List<AntrenamentEntity>>(emptyList()) }
    var workoutGroupMap by remember { mutableStateOf<Map<Int, Set<String>>>(emptyMap()) }
    var deleteTrigger by remember { mutableIntStateOf(0) }

    // Pulse animation for workout days
    val pulseTransition = rememberInfiniteTransition(label = "dayPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val monthNames = remember(strings) { listOf(
        strings.jan, strings.feb, strings.mar, strings.apr, strings.may, strings.jun,
        strings.jul, strings.aug, strings.sep, strings.oct, strings.nov, strings.dec
    ) }

    LaunchedEffect(currentMonth, currentYear, deleteTrigger) {
        withContext(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            cal.set(currentYear, currentMonth, 1, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val monthStart = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val monthEnd = cal.timeInMillis

            val workouts = db.antrenamentDao().getWorkoutsInPeriod(userId, monthStart, monthEnd)
            val days = mutableSetOf<Int>()
            val groupMap = mutableMapOf<Int, MutableSet<String>>()
            for (w in workouts) {
                val dayCal = Calendar.getInstance().apply { timeInMillis = w.data }
                val day = dayCal.get(Calendar.DAY_OF_MONTH)
                days.add(day)
                groupMap.getOrPut(day) { mutableSetOf() }.add(w.grupaMusculara)
            }
            workoutDays = days
            workoutGroupMap = groupMap.mapValues { it.value.toSet() }
        }
    }

    LaunchedEffect(selectedDay, currentMonth, currentYear, deleteTrigger) {
        withContext(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            cal.set(currentYear, currentMonth, selectedDay, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val dayEnd = cal.timeInMillis
            selectedDayWorkouts = db.antrenamentDao().getWorkoutsInPeriod(userId, dayStart, dayEnd)
        }
    }

    Scaffold(
        containerColor = p.bg,
        topBar = {
            KineticAppBar(onBack = onBackClick)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            contentPadding = PaddingValues(bottom = AppConstants.BOTTOM_NAV_PADDING),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (currentMonth == 0) { currentMonth = 11; currentYear-- }
                                else currentMonth--
                            }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = p.ac)
                            }
                            Text(
                                text = "${monthNames[currentMonth]} $currentYear",
                                style = MaterialTheme.typography.titleLarge,
                                color = p.tp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                if (currentMonth == 11) { currentMonth = 0; currentYear++ }
                                else currentMonth++
                            }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = p.ac)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf(strings.mon, strings.tue, strings.wed, strings.thu, strings.fri, strings.sat, strings.sun).forEach { dayName ->
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = p.ts,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        val cal = Calendar.getInstance().apply {
                            set(currentYear, currentMonth, 1)
                        }
                        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val today = Calendar.getInstance()
                        val isCurrentMonth = today.get(Calendar.MONTH) == currentMonth && today.get(Calendar.YEAR) == currentYear
                        val todayDay = today.get(Calendar.DAY_OF_MONTH)

                        var dayCounter = 1
                        for (week in 0..5) {
                            if (dayCounter > daysInMonth) break
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (dow in 0..6) {
                                    if (week == 0 && dow < firstDayOfWeek || dayCounter > daysInMonth) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    } else {
                                        val day = dayCounter
                                        val hasWorkout = workoutDays.contains(day)
                                        val isSelected = day == selectedDay && isCurrentMonth
                                        val isToday = day == todayDay && isCurrentMonth

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .graphicsLayer {
                                                    scaleX = if (hasWorkout && !isSelected) pulseScale else 1f
                                                    scaleY = if (hasWorkout && !isSelected) pulseScale else 1f
                                                }
                                                .clip(CircleShape)
                                                .then(
                                                    if (isSelected) Modifier.background(p.ac)
                                                    else if (hasWorkout) Modifier.background(AccentRed.copy(alpha = 0.12f)).border(1.5.dp, AccentRed, CircleShape)
                                                    else if (isToday) Modifier.background(p.ac.copy(alpha = 0.1f))
                                                    else Modifier
                                                )
                                                .clickable {
                                                    selectedDay = day
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$day",
                                                    fontSize = 14.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (hasWorkout) AccentRed else p.tp,
                                                    fontWeight = if (isToday || hasWorkout) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                        dayCounter++
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "${selectedDay} ${monthNames[currentMonth]}",
                    style = MaterialTheme.typography.titleMedium,
                    color = p.tp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (selectedDayWorkouts.isEmpty()) {
                item {
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 12.dp
                    ) {
                        EmptyState(
                            icon = Icons.Default.Event,
                            title = strings.noWorkouts,
                            subtitle = "",
                            textPrimary = p.ac,
                            textSecondary = p.ts,
                            accent = p.ac
                        )
                    }
                }
            } else {
                items(selectedDayWorkouts) { workout ->
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            containerColor = Color(0xFF1A1A1A),
                            title = { Text(strings.delete ?: "Delete", color = p.tp, fontWeight = FontWeight.Bold) },
                            text = { Text(strings.confirm ?: "Confirm?", color = p.ts) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDialog = false
                                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            db.exercitiuDao().deleteForAntrenament(workout.id)
                                            db.antrenamentDao().delete(workout)
                                        }
                                        deleteTrigger++
                                        onWorkoutDeleted()
                                    }
                                }) {
                                    Text(strings.delete ?: "Delete", color = AccentRed, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text(strings.cancel ?: "Cancel", color = p.ts)
                                }
                            }
                        )
                    }

                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                                                                .background(AccentRed.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            when (workout.grupaMusculara) {
                                                "Piept", "chest" -> R.drawable.ic_piept
                                                "Spate", "back" -> R.drawable.ic_spate
                                                "Umeri", "shoulders" -> R.drawable.ic_umeri
                                                "Biceps", "biceps", "arms" -> R.drawable.ic_biceps
                                                "Triceps", "triceps" -> R.drawable.ic_triceps
                                                "Abdomen", "abs", "core" -> R.drawable.ic_abdomen
                                                "Picioare", "legs", "thighs" -> R.drawable.ic_picioare
                                                "Fese", "glutes" -> R.drawable.ic_fese
                                                "Gambe", "calves" -> R.drawable.ic_gambe
                                                "Antebrate", "forearms" -> R.drawable.ic_antebrat
                                                "Gat & Trapezi", "neck", "traps" -> R.drawable.ic_gat
                                                else -> R.drawable.ic_piept
                                            }
                                        ),
                                        contentDescription = null,
                                        tint = AccentRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = LanguageManager.translateMuscleGroup(workout.grupaMusculara, strings),
                                        color = p.tp,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "${String.format("%.0f", workout.totalWeight)} kg total",
                                        color = p.ts,
                                        fontSize = 13.sp
                                    )
                                    if (workout.durationMs > 0) {
                                        val durationMin = workout.durationMs / 60000
                                        val durationSec = (workout.durationMs % 60000) / 1000
                                        Text(
                                            text = "${durationMin}:${String.format("%02d", durationSec)}",
                                            color = p.ts,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = strings.delete,
                                        tint = AccentRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            var exercises by remember(workout.id) { mutableStateOf(emptyList<ExercitiuEntity>()) }
                            LaunchedEffect(workout.id) {
                                withContext(Dispatchers.IO) {
                                    exercises = db.exercitiuDao().getForAntrenament(workout.id)
                                }
                            }
                            if (exercises.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = p.bd)
                                Spacer(Modifier.height(8.dp))
                                exercises.forEach { ex ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = ex.numeExercitiu,
                                            fontSize = 13.sp,
                                color = p.tp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${ex.setIndex + 1}x${ex.repetari} @ ${String.format("%.0f", ex.greutateKg)}kg",
                                            fontSize = 12.sp,
                                            color = AccentRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (workoutDays.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 12.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column {
                            Text(
                                text = strings.monthlyDetails,
                                color = p.tp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            val daysInMonth = java.util.Calendar.getInstance().apply {
                                set(currentYear, currentMonth, 1)
                            }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                            Text(
                                text = "${workoutDays.size} / $daysInMonth ${strings.days.toLowerCase(java.util.Locale.getDefault())}",
                                color = p.ts,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            val barFraction = workoutDays.size.toFloat() / daysInMonth
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                                                        .background(p.bd, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = barFraction.coerceIn(0f, 1f))
                                                                                .background(RecoveryGreen, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
