package com.example.gymlog2

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class RestActivity(
    val name: String,
    val icon: ImageVector,
    val duration: String,
    val description: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestDayScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    userId: String,
    recoveryMap: Map<String, Double>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed

    var restDays by remember { mutableStateOf<List<RestDayEntity>>(emptyList()) }
    var nextRestDay by remember { mutableStateOf<RestDayEntity?>(null) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var selectedWeek by remember { mutableIntStateOf(0) }

    val stretchingExercises = listOf(
        RestActivity(strings.stretching, Icons.Default.FitnessCenter, "10-15 min", "Full body stretching routine to improve flexibility and reduce muscle tension", "stretching"),
        RestActivity(strings.lightYoga, Icons.Default.SelfImprovement, "20-30 min", "Gentle yoga flow focusing on deep breathing and muscle relaxation", "yoga"),
        RestActivity(strings.foamRolling, Icons.Default.Circle, "10-15 min", "Self-myofascial release for tight muscles and improved recovery", "foam"),
        RestActivity(strings.activeRecovery, Icons.Default.DirectionsWalk, "20-30 min", "Light movement to promote blood flow and recovery", "active"),
        RestActivity(strings.lightWalk, Icons.Default.DirectionsWalk, "30-45 min", "Easy pace walk outdoors or on treadmill", "active"),
        RestActivity(strings.mobilityWork, Icons.Default.OpenWith, "15-20 min", "Joint mobility exercises and dynamic stretching", "mobility")
    )

    LaunchedEffect(userId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            restDays = db.restDayDao().getAllForUser(userId)
            val today = System.currentTimeMillis()
            nextRestDay = db.restDayDao().getNextRestDay(userId, today)
        }
    }

    fun getMusclesNeedingRest(): List<Pair<String, Double>> {
        return recoveryMap.filter { it.value > 0.5 }
            .map { it.key to it.value }
            .sortedByDescending { it.second }
    }

    fun getRecoveryColor(level: Double): Color {
        return when {
            level <= 0.2 -> RecoveryGreen
            level <= 0.5 -> RecoveryYellow
            level <= 0.7 -> RecoveryOrange
            else -> RecoveryRed
        }
    }

    fun getDeloadRecommendation(): String {
        val tiredMuscles = getMusclesNeedingRest().size
        return when {
            tiredMuscles >= 5 -> "Consider a full deload week - reduce volume by 40-50%"
            tiredMuscles >= 3 -> "Deload recommended - reduce weight by 20-30%"
            tiredMuscles >= 1 -> "Active recovery day recommended"
            else -> "You're recovered! Train hard today"
        }
    }

    BackHandler { onBack() }

    if (showScheduleDialog) {
        var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
        var selectedType by remember { mutableStateOf("rest") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            containerColor = cardBg,
            titleContentColor = textPrimary,
            title = { Text(strings.restDayRecommendation, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(strings.selectDay, color = textSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf("rest" to strings.recovery, "deload" to strings.deloadWeek, "stretching" to strings.stretching)
                        types.forEach { (type, label) ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent.copy(alpha = 0.15f),
                                    selectedLabelColor = accent
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text(strings.notes, color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                            cursorColor = accent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                val db = AppDatabase.getDatabase(context)
                                val restDay = RestDayEntity(
                                    userId = userId,
                                    date = selectedDate,
                                    type = selectedType,
                                    notes = notes.trim(),
                                    activities = stretchingExercises.filter { it.category == selectedType || selectedType == "rest" }
                                        .joinToString(",") { it.name }
                                )
                                db.restDayDao().insert(restDay)
                                restDays = db.restDayDao().getAllForUser(userId)
                                nextRestDay = db.restDayDao().getNextRestDay(userId, System.currentTimeMillis())
                            }
                        }
                        showScheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.confirm, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text(strings.cancel, color = accent)
                }
            }
        )
    }

    Scaffold(
        containerColor = surfaceBg,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(strings.restDaysTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back, tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceBg,
                    titleContentColor = textPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Subtitle
            item {
                Text(
                    strings.restDaysSubtitle,
                    color = textSecondary,
                    fontSize = 13.sp
                )
            }

            // Next Rest Day Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                strings.nextRestDay.uppercase(),
                                fontSize = 11.sp,
                                letterSpacing = 2.sp,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showScheduleDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = strings.add, tint = accent)
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        if (nextRestDay != null) {
                            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(accent.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        sdf.format(Date(nextRestDay!!.date)),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Text(
                                        nextRestDay!!.type.replaceFirstChar { it.uppercase() },
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }
                            }
                        } else {
                            Text(
                                strings.noRestDays,
                                color = textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Deload Recommendation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            strings.deloadInfo.uppercase(),
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(10.dp))

                        val tiredMuscles = getMusclesNeedingRest()
                        val recommendation = getDeloadRecommendation()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            tiredMuscles.size >= 5 -> RecoveryRed.copy(alpha = 0.12f)
                                            tiredMuscles.size >= 3 -> RecoveryOrange.copy(alpha = 0.12f)
                                            tiredMuscles.size >= 1 -> RecoveryYellow.copy(alpha = 0.12f)
                                            else -> RecoveryGreen.copy(alpha = 0.12f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when {
                                        tiredMuscles.size >= 5 -> Icons.Default.Warning
                                        tiredMuscles.size >= 3 -> Icons.Default.Info
                                        tiredMuscles.size >= 1 -> Icons.Default.Lightbulb
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        tiredMuscles.size >= 5 -> RecoveryRed
                                        tiredMuscles.size >= 3 -> RecoveryOrange
                                        tiredMuscles.size >= 1 -> RecoveryYellow
                                        else -> RecoveryGreen
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                recommendation,
                                fontSize = 13.sp,
                                color = textPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (tiredMuscles.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                strings.muscleNeedsRest.uppercase(),
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(6.dp))
                            tiredMuscles.forEach { (muscle, level) ->
                                val recoveryPct = ((1.0 - level) * 100).toInt().coerceIn(0, 100)
                                val color = getRecoveryColor(level)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        LanguageManager.translateMuscleGroup(muscle, strings),
                                        fontSize = 12.sp,
                                        color = textPrimary,
                                        modifier = Modifier.width(90.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(RecoveryTrack)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fraction = (level).toFloat().coerceIn(0f, 1f))
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(color)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "$recoveryPct%",
                                        fontSize = 11.sp,
                                        color = color,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(36.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Suggested Activities
            item {
                Text(
                    strings.suggestedActivities.uppercase(),
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    color = accent,
                    fontWeight = FontWeight.Bold
                )
            }

            items(stretchingExercises) { activity ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(activity.icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activity.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Spacer(Modifier.height(2.dp))
                            Text(activity.description, fontSize = 11.sp, color = textSecondary, lineHeight = 15.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = textSecondary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.height(2.dp))
                            Text(activity.duration, fontSize = 10.sp, color = textSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Upcoming Rest Days
            if (restDays.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        strings.recoverySchedule.uppercase(),
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(restDays.take(10)) { restDay ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val isCompleted = restDay.completed

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            val db = AppDatabase.getDatabase(context)
                                            if (checked) {
                                                db.restDayDao().markCompleted(restDay.id)
                                            }
                                            restDays = db.restDayDao().getAllForUser(userId)
                                        }
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = accent)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    sdf.format(Date(restDay.date)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) textSecondary else textPrimary
                                )
                                Text(
                                    restDay.type.replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp,
                                    color = if (isCompleted) textSecondary.copy(alpha = 0.6f) else textSecondary
                                )
                                if (restDay.notes.isNotBlank()) {
                                    Text(restDay.notes, fontSize = 11.sp, color = textSecondary.copy(alpha = 0.7f))
                                }
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = RecoveryRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            containerColor = cardBg,
                            titleContentColor = textPrimary,
                            title = { Text(strings.delete, fontWeight = FontWeight.Bold) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                val db = AppDatabase.getDatabase(context)
                                                db.restDayDao().deleteById(restDay.id)
                                                restDays = db.restDayDao().getAllForUser(userId)
                                                nextRestDay = db.restDayDao().getNextRestDay(userId, System.currentTimeMillis())
                                            }
                                        }
                                        showDeleteConfirm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RecoveryRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(strings.delete, color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text(strings.cancel, color = accent)
                                }
                            }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
