package com.example.kinetic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class RestActivity(
    val name: String,
    val icon: ImageVector,
    val duration: String,
    val description: String,
    val category: String,
    val targetedGroups: List<String> = emptyList()
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
    val p = appPalette(isDark)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var restDays by remember { mutableStateOf<List<RestDayEntity>>(emptyList()) }
    var nextRestDay by remember { mutableStateOf<RestDayEntity?>(null) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var activeDeload by remember { mutableStateOf<DeloadWeekEntity?>(null) }
    var weeksSinceDeload by remember { mutableIntStateOf(0) }
    var deloadTrigger by remember { mutableStateOf<DeloadTrigger?>(null) }
    var deloadHistory by remember { mutableStateOf<List<DeloadWeekEntity>>(emptyList()) }
    var autoDeloadEnabled by remember { mutableStateOf(false) }
    var deloadIntervalWeeks by remember { mutableIntStateOf(4) }
    var showDeloadPreview by remember { mutableStateOf(false) }
    var deloadReductions by remember { mutableStateOf<List<DeloadExerciseReduction>>(emptyList()) }
    var showRecoveryGuide by remember { mutableStateOf<RestActivity?>(null) }
    var avgRecoveryPct by remember { mutableIntStateOf(100) }
    var tiredCount by remember { mutableIntStateOf(0) }

    val stretchingExercises = remember(strings) { listOf(
        RestActivity(strings.lightYoga, Icons.Default.SelfImprovement, "15 min", strings.yogaDescription, "yoga", listOf("Piept", "Spate", "Umeri", "Picioare")),
        RestActivity(strings.stretching, Icons.Default.Accessibility, "10-15 min", strings.stretchingDescription, "stretching", emptyList()),
        RestActivity(strings.lightWalk, Icons.Default.DirectionsWalk, "20-30 min", strings.lissDescription, "active", listOf("Picioare", "Fese", "Gambe")),
        RestActivity(strings.foamRolling, Icons.Default.Circle, "8 min", strings.foamRollingDescription, "foam", emptyList())
    ) }

    fun getMusclesNeedingRest(): List<Pair<String, Double>> {
        return recoveryMap.filter { it.value > 0.5 }
            .map { (key, value) -> key to value }
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

    fun getTargetedActivities(): List<RestActivity> {
        val tiredMuscles = getMusclesNeedingRest().map { it.first }
        if (tiredMuscles.isEmpty()) return stretchingExercises

        val sorted = stretchingExercises.map { activity ->
            val relevance = when {
                activity.targetedGroups.isEmpty() && (activity.category == "stretching" || activity.category == "foam") -> tiredMuscles.size
                activity.targetedGroups.isNotEmpty() -> activity.targetedGroups.count { it in tiredMuscles }
                else -> 0
            }
            activity to relevance
        }.sortedByDescending { it.second }

        return sorted.map { it.first }
    }

    fun getMostFatiguedGroup(): String? {
        return getMusclesNeedingRest().firstOrNull()?.first
    }

    LaunchedEffect(userId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            restDays = db.restDayDao().getAllForUser(userId)
            val today = System.currentTimeMillis()
            nextRestDay = db.restDayDao().getNextRestDay(userId, today)

            val prefs = PreferencesManager(context, UserProfileManager(context))
            autoDeloadEnabled = prefs.isAutoDeloadEnabled()
            deloadIntervalWeeks = prefs.getDeloadIntervalWeeks()

            val repo = AntrenamentRepository(db)
            activeDeload = repo.getActiveDeload(userId)
            weeksSinceDeload = repo.weeksSinceLastDeload(userId)
            deloadTrigger = repo.shouldTriggerDeload(userId, deloadIntervalWeeks)
            deloadHistory = repo.getDeloadHistory(userId)
            tiredCount = repo.getTiredMusclesCount(recoveryMap)
            avgRecoveryPct = repo.getAvgRecoveryPercent(recoveryMap)
        }
    }

    BackHandler { onBack() }

    if (showRecoveryGuide != null) {
        val activity = showRecoveryGuide!!
        AlertDialog(
            onDismissRequest = { showRecoveryGuide = null },
            containerColor = p.cr,
            titleContentColor = p.tp,
            title = { Text(activity.name, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(activity.icon, contentDescription = null, tint = p.ac, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(activity.duration, fontSize = 13.sp, color = p.ac, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(activity.description, fontSize = 13.sp, color = p.ts, lineHeight = 18.sp)
                    if (activity.targetedGroups.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(strings.recoveryTargeted.uppercase(), fontSize = 10.sp, letterSpacing = 1.sp, color = p.ac, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        activity.targetedGroups.forEach { group ->
                            val level = recoveryMap[group] ?: 0.0
                            val color = getRecoveryColor(level)
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text(LanguageManager.translateMuscleGroup(group, strings), fontSize = 12.sp, color = p.tp)
                                Spacer(Modifier.width(4.dp))
                                val pct = ((1.0 - level) * 100).toInt().coerceIn(0, 100)
                                Text("($pct%)", fontSize = 10.sp, color = p.ts)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecoveryGuide = null }) {
                    Text(strings.confirm, color = p.ac)
                }
            }
        )
    }

    if (showDeloadPreview) {
        AlertDialog(
            onDismissRequest = { showDeloadPreview = false },
            containerColor = p.cr,
            titleContentColor = p.tp,
            title = { Text(strings.deloadPreview, fontWeight = FontWeight.Bold) },
            text = {
                if (deloadReductions.isEmpty()) {
                    Text(strings.noDataYet, color = p.ts, fontSize = 13.sp)
                } else {
                    LazyColumn {
                        items(deloadReductions) { reduction ->
                            AppGlassCard(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                p = p,
                                cornerRadius = 10.dp,
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(reduction.exerciseName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.tp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        if (reduction.isCompound) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = RecoveryOrange.copy(alpha = 0.15f)
                                            ) {
                                                Text("CMP", fontSize = 9.sp, color = RecoveryOrange, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(strings.deloadNormalValue, fontSize = 10.sp, color = p.ts)
                                            Text("${String.format("%.1f", reduction.originalWeight)} kg x ${reduction.originalSets}", fontSize = 12.sp, color = p.ts)
                                        }
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = p.ac, modifier = Modifier.size(16.dp).align(Alignment.CenterVertically))
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(strings.deloadNewValue, fontSize = 10.sp, color = RecoveryGreen)
                                            Text("${String.format("%.1f", reduction.newWeight)} kg x ${reduction.newSets}", fontSize = 12.sp, color = RecoveryGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = RecoveryRed.copy(alpha = 0.12f)) {
                                            Text("-${reduction.weightReductionPercent}% ${strings.weight}", fontSize = 9.sp, color = RecoveryRed, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                        if (reduction.setsReduction > 0) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = RecoveryOrange.copy(alpha = 0.12f)) {
                                                Text("-${reduction.setsReduction} ${strings.sets}", fontSize = 9.sp, color = RecoveryOrange, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeloadPreview = false }) {
                    Text(strings.confirm, color = p.ac)
                }
            }
        )
    }

    if (showScheduleDialog) {
        var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
        var selectedType by remember { mutableStateOf("rest") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            containerColor = p.cr,
            titleContentColor = p.tp,
            title = { Text(strings.restDayRecommendation, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(strings.selectDay, color = p.ts, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = remember(strings) { listOf("rest" to strings.recovery, "deload" to strings.deloadWeek, "stretching" to strings.stretching) }
                        types.forEach { (type, label) ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = p.ac.copy(alpha = 0.15f),
                                    selectedLabelColor = p.ac
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text(strings.notes, color = p.ts.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.ac,
                            unfocusedBorderColor = p.ts.copy(alpha = 0.3f),
                            cursorColor = p.ac,
                            focusedTextColor = p.tp,
                            unfocusedTextColor = p.tp
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
                                val prefs = PreferencesManager(context, UserProfileManager(context))
                                val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                                val restDay = RestDayEntity(
                                    userId = userId,
                                    date = selectedDate,
                                    type = selectedType,
                                    notes = notes.trim(),
                                    activities = stretchingExercises.filter { it.category == selectedType || selectedType == "rest" }
                                        .joinToString(",") { it.name }
                                )
                                syncRepo.saveRestDay(restDay)
                                restDays = db.restDayDao().getAllForUser(userId)
                                nextRestDay = db.restDayDao().getNextRestDay(userId, System.currentTimeMillis())
                            }
                        }
                        showScheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.confirm, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) {
                    Text(strings.cancel, color = p.ac)
                }
            }
        )
    }

    Scaffold(
        containerColor = p.bg,
        topBar = {
            KineticAppBar(onBack = onBack)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(strings.restDaysSubtitle, color = p.ts, fontSize = 13.sp)
            }

            // Auto-Detection Card
            item {
                val tiredMuscles = getMusclesNeedingRest()
                val isDeloadActive = activeDeload != null
                val shouldDeload = deloadTrigger != null && autoDeloadEnabled

                AppGlassCard(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    p = p,
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.deloadInfo.uppercase(), fontSize = 11.sp, letterSpacing = 2.sp, color = p.ac, fontWeight = FontWeight.Bold)
                            if (isDeloadActive) {
                                Surface(shape = RoundedCornerShape(4.dp), color = RecoveryOrange.copy(alpha = 0.15f)) {
                                    Text(strings.deloadActive, fontSize = 9.sp, color = RecoveryOrange, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                                                        .background(
                                        when {
                                            isDeloadActive -> RecoveryOrange.copy(alpha = 0.12f)
                                            shouldDeload -> RecoveryYellow.copy(alpha = 0.12f)
                                            tiredCount >= 5 -> RecoveryRed.copy(alpha = 0.12f)
                                            tiredCount >= 3 -> RecoveryOrange.copy(alpha = 0.12f)
                                            tiredCount >= 1 -> RecoveryYellow.copy(alpha = 0.12f)
                                            else -> RecoveryGreen.copy(alpha = 0.12f)
                                        }
                                    , RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when {
                                        isDeloadActive -> Icons.Default.Warning
                                        shouldDeload -> Icons.Default.Info
                                        tiredCount >= 5 -> Icons.Default.Warning
                                        tiredCount >= 3 -> Icons.Default.Info
                                        tiredCount >= 1 -> Icons.Default.Lightbulb
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isDeloadActive -> RecoveryOrange
                                        shouldDeload -> RecoveryYellow
                                        tiredCount >= 5 -> RecoveryRed
                                        tiredCount >= 3 -> RecoveryOrange
                                        tiredCount >= 1 -> RecoveryYellow
                                        else -> RecoveryGreen
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    when {
                                        isDeloadActive -> strings.deloadActive
                                        shouldDeload -> strings.timeForDeload
                                        tiredCount >= 5 -> "${strings.muscleNeedsRest} ($tiredCount ${strings.musclesTiredCount})"
                                        tiredCount >= 3 -> "${strings.activeRecovery} — $tiredCount ${strings.musclesTiredCount}"
                                        tiredCount >= 1 -> strings.activeRecovery
                                        else -> strings.allGood
                                    },
                                    fontSize = 13.sp,
                                    color = p.tp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    when {
                                        isDeloadActive -> strings.deloadActiveThisWeek
                                        shouldDeload -> "${strings.timeForDeload} — ${strings.weeksSinceLastDeload}: $weeksSinceDeload"
                                        tiredCount >= 5 -> "${strings.muscleNeedsRest} — ${strings.weeksSinceLastDeload}: $weeksSinceDeload"
                                        tiredCount >= 3 -> "${strings.activeRecovery} — ${strings.weeksSinceLastDeload}: $weeksSinceDeload"
                                        else -> "${strings.allGood} — ${strings.avgRecovery}: $avgRecoveryPct%"
                                    },
                                    fontSize = 11.sp,
                                    color = p.ts
                                )
                            }
                        }

                        if (tiredMuscles.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(strings.muscleNeedsRest.uppercase(), fontSize = 10.sp, letterSpacing = 1.sp, color = p.ts, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            tiredMuscles.take(6).forEach { (muscle, level) ->
                                val recoveryPct = ((1.0 - level) * 100).toInt().coerceIn(0, 100)
                                val color = getRecoveryColor(level)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        LanguageManager.translateMuscleGroup(muscle, strings),
                                        fontSize = 12.sp,
                                        color = p.tp,
                                        modifier = Modifier.width(90.dp)
                                    )
                                    Box(
                                        modifier = Modifier.weight(1f).height(6.dp).background(RecoveryTrack, RoundedCornerShape(3.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = level.toFloat().coerceIn(0f, 1f)).background(color, RoundedCornerShape(3.dp))
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text("$recoveryPct%", fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                                }
                            }
                        }

                        // Deload controls
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = p.ts.copy(alpha = 0.1f))
                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.autoDeloadEnabled, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.tp)
                                Text("${strings.deloadInterval}: $deloadIntervalWeeks ${strings.weeks}", fontSize = 11.sp, color = p.ts)
                            }
                            Switch(
                                checked = autoDeloadEnabled,
                                onCheckedChange = { enabled ->
                                    autoDeloadEnabled = enabled
                                    scope.launch {
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            PreferencesManager(context, UserProfileManager(context)).setAutoDeloadEnabled(enabled)
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = p.ac)
                            )
                        }

                        if (autoDeloadEnabled) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(3, 4, 5, 6).forEach { weeks ->
                                    FilterChip(
                                        selected = deloadIntervalWeeks == weeks,
                                        onClick = {
                                            deloadIntervalWeeks = weeks
                                            scope.launch {
                                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                    PreferencesManager(context, UserProfileManager(context)).setDeloadIntervalWeeks(weeks)
                                                    val db = AppDatabase.getDatabase(context)
                                                    deloadTrigger = AntrenamentRepository(db).shouldTriggerDeload(userId, weeks)
                                                }
                                            }
                                        },
                                        label = { Text("${weeks}w", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = p.ac.copy(alpha = 0.15f),
                                            selectedLabelColor = p.ac
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                }
                            }
                        }

                        if (autoDeloadEnabled && activeDeload == null && deloadIntervalWeeks > 0) {
                            Spacer(Modifier.height(12.dp))
                            val progress = (weeksSinceDeload.toFloat() / deloadIntervalWeeks.toFloat()).coerceIn(0f, 1f)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "$weeksSinceDeload / $deloadIntervalWeeks ${strings.weeks}",
                                        fontSize = 11.sp,
                                        color = p.ts,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "${(progress * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        color = if (progress >= 1f) RecoveryRed else p.ac,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                                                                .background(RecoveryTrack, RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = progress)
                                                                                        .background(if (progress >= 1f) RecoveryRed else p.ac, RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (activeDeload == null && shouldDeload) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                val db = AppDatabase.getDatabase(context)
                                                val repo = AntrenamentRepository(db)
                                                val reason = repo.getDeloadReason(deloadTrigger)
                                                val deloadId = repo.startDeload(userId, reason, 0.65)
                                                activeDeload = repo.getActiveDeload(userId)
                                                val now = System.currentTimeMillis()
                                                PreferencesManager(context, UserProfileManager(context)).apply {
                                                    setDeloadActive(true)
                                                    setDeloadStartDate(now)
                                                    setDeloadEndDate(now + 7L * 24 * 60 * 60 * 1000)
                                                    setDeloadReason(reason)
                                                    setLastDeloadTimestamp(now)
                                                }
                                                // Generate preview
                                                val topExercises = listOf("Bench Press", "Squat", "Deadlift", "Barbell Row", "Overhead Press", "Barbell Curl", "Tricep Pushdown")
                                                val reductions = mutableListOf<DeloadExerciseReduction>()
                                                for (ex in topExercises) {
                                                    val history = db.exercitiuDao().getHistoryForExerciseSimple(userId, ex)
                                                    val lastSets = history.take(5)
                                                    if (lastSets.isNotEmpty()) {
                                                        val maxWeight = lastSets.maxOfOrNull { it.greutateKg } ?: continue
                                                        val avgReps = lastSets.map { it.repetari }.average().toInt().coerceAtLeast(8)
                                                        val setCount = lastSets.groupBy { it.antrenamentId }.size.coerceIn(3, 5)
                                                        reductions.add(repo.applyDeloadReduction(maxWeight, setCount, avgReps, ex))
                                                    }
                                                }
                                                deloadReductions = reductions
                                                showDeloadPreview = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.startDeload, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (activeDeload != null) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                val db = AppDatabase.getDatabase(context)
                                                val repo = AntrenamentRepository(db)
                                                repo.endDeload(activeDeload!!.id)
                                                activeDeload = null
                                                PreferencesManager(context, UserProfileManager(context)).apply {
                                                    setDeloadActive(false)
                                                    setDeloadStartDate(0L)
                                                    setDeloadEndDate(0L)
                                                    setDeloadReason("")
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = RecoveryGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.endDeload, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            val db = AppDatabase.getDatabase(context)
                                            val repo = AntrenamentRepository(db)
                                            val topExercises = listOf("Bench Press", "Squat", "Deadlift", "Barbell Row", "Overhead Press", "Barbell Curl", "Tricep Pushdown")
                                            val reductions = mutableListOf<DeloadExerciseReduction>()
                                            for (ex in topExercises) {
                                                val history = db.exercitiuDao().getHistoryForExerciseSimple(userId, ex)
                                                val lastSets = history.take(5)
                                                if (lastSets.isNotEmpty()) {
                                                    val maxWeight = lastSets.maxOfOrNull { it.greutateKg } ?: continue
                                                    val avgReps = lastSets.map { it.repetari }.average().toInt().coerceAtLeast(8)
                                                    val setCount = lastSets.groupBy { it.antrenamentId }.size.coerceIn(3, 5)
                                                    reductions.add(repo.applyDeloadReduction(maxWeight, setCount, avgReps, ex))
                                                }
                                            }
                                            deloadReductions = reductions
                                        }
                                        showDeloadPreview = true
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, p.ac.copy(alpha = 0.3f))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp), tint = p.ac)
                                        Spacer(Modifier.width(4.dp))
                                        Text(strings.deloadPreview, fontSize = 12.sp, color = p.ac, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(strings.deloadPreviewSubtitle, fontSize = 9.sp, color = p.ts, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Next Rest Day Card
            item {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth().clickable { if (nextRestDay == null) showScheduleDialog = true },
                    p = p,
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.nextRestDay.uppercase(), fontSize = 11.sp, letterSpacing = 2.sp, color = p.ac, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showScheduleDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = strings.add, tint = p.ac)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (nextRestDay != null) {
                            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(44.dp).background(p.ac.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = p.ac, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(sdf.format(Date(nextRestDay!!.date)), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.tp)
                                    Text(nextRestDay!!.type.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = p.ts)
                                }
                            }
                        } else {
                            AppGlassCard(
                                modifier = Modifier.fillMaxWidth().clickable { showScheduleDialog = true },
                                p = p,
                                cornerRadius = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = p.ac, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.tapToSchedule, color = p.ac, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Suggested Activities
            item {
                Text(strings.suggestedActivities.uppercase(), fontSize = 11.sp, letterSpacing = 2.sp, color = p.ac, fontWeight = FontWeight.Bold)
            }

            itemsIndexed(getTargetedActivities()) { index, activity ->
                val isRecommended = index == 0 && getMusclesNeedingRest().isNotEmpty()
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth().clickable { showRecoveryGuide = activity },
                    p = p,
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).background(p.ac.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(activity.icon, contentDescription = null, tint = p.ac, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activity.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.tp)
                            Spacer(Modifier.height(2.dp))
                            Text(activity.description, fontSize = 11.sp, color = p.ts, lineHeight = 15.sp, maxLines = 2)
                            if (activity.targetedGroups.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    activity.targetedGroups.take(3).forEach { group ->
                                        val level = recoveryMap[group] ?: 0.0
                                        val color = getRecoveryColor(level)
                                        Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.12f)) {
                                            Text(
                                                LanguageManager.translateMuscleGroup(group, strings),
                                                fontSize = 9.sp,
                                                color = color,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (isRecommended) {
                                Surface(shape = RoundedCornerShape(4.dp), color = p.ac.copy(alpha = 0.15f)) {
                                    Text(
                                        strings.recommendedForYou,
                                        fontSize = 8.sp,
                                        color = p.ac,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = p.ts, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.height(2.dp))
                            Text(activity.duration, fontSize = 10.sp, color = p.ts, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Deload History
            if (deloadHistory.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(strings.deloadHistory.uppercase(), fontSize = 11.sp, letterSpacing = 2.sp, color = p.ac, fontWeight = FontWeight.Bold)
                }
                items(deloadHistory.take(5)) { deload ->
                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).background(
                                    if (deload.completed) RecoveryGreen.copy(alpha = 0.12f) else RecoveryOrange.copy(alpha = 0.12f)
                                , RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (deload.completed) Icons.Default.CheckCircle else Icons.Default.Pending,
                                    contentDescription = null,
                                    tint = if (deload.completed) RecoveryGreen else RecoveryOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${sdf.format(Date(deload.startDate))} - ${sdf.format(Date(deload.endDate))}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.tp
                                )
                                Text(deload.reason, fontSize = 11.sp, color = p.ts, maxLines = 1)
                            }
                            Text(
                                "${((1.0 - deload.reductionFactor) * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RecoveryOrange
                            )
                        }
                    }
                }
            }

            // Upcoming Rest Days
            if (restDays.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(strings.recoverySchedule.uppercase(), fontSize = 11.sp, letterSpacing = 2.sp, color = p.ac, fontWeight = FontWeight.Bold)
                }
                items(restDays.take(10)) { restDay ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val isCompleted = restDay.completed

                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
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
                                colors = CheckboxDefaults.colors(checkedColor = p.ac)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sdf.format(Date(restDay.date)), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isCompleted) p.ts else p.tp)
                                Text(restDay.type.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = if (isCompleted) p.ts.copy(alpha = 0.6f) else p.ts)
                                if (restDay.notes.isNotBlank()) {
                                    Text(restDay.notes, fontSize = 11.sp, color = p.ts.copy(alpha = 0.7f))
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
                            containerColor = p.cr,
                            titleContentColor = p.tp,
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
                                    Text(strings.cancel, color = p.ac)
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
