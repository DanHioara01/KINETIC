package com.example.gymlog2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private val GoldPR = Color(0xFFF5B942)

private data class ChartData(
    val values: List<Double>,
    val labels: List<String>,
    val label: String,
    val unit: String,
    val delta: String,
    val hasData: Boolean = values.any { it > 0 }
)

@Composable
fun StatsScreen(
    isDark: Boolean,
    isLbs: Boolean,
    strings: LanguageManager.Strings,
    weeklyTopExercise: String?,
    weeklyTotalKg: Double,
    lastPR: PersonalRecordEntity?,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onExerciseHistoryClick: (String) -> Unit = {},
    userId: String = "simple",
    currentStreak: Int = 0,
    bestStreak: Int = 0,
    badgeCount: Int = 0,
    recentBadges: List<BadgeEntity> = emptyList(),
    allRecentPRs: List<PersonalRecordEntity> = emptyList(),
    allExerciseNames: List<String> = emptyList()
) {
    val context = LocalContext.current
    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed
    val dividerBg = if (isDark) dividerColor() else LightDividerGray

    var selectedMetric by remember { mutableIntStateOf(0) }
    var selectedPeriod by remember { mutableIntStateOf(0) }
    var chartData by remember { mutableStateOf<Map<Int, Map<Int, ChartData>>>(emptyMap()) }
    var distributionData by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var selectedExercise by remember { mutableStateOf<String?>(null) }
    var showExerciseDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(selectedExercise) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val cal = Calendar.getInstance()
            val now = cal.timeInMillis

            val result = mutableMapOf<Int, MutableMap<Int, ChartData>>()

            for (periodIdx in 0..2) {
                val days = when (periodIdx) { 0 -> 7; 1 -> 30; 2 -> 90; else -> 7 }
                cal.timeInMillis = now
                cal.add(Calendar.DAY_OF_YEAR, -days)
                val startTime = cal.timeInMillis

                val allWorkouts = if (selectedExercise != null) {
                    db.exercitiuDao().getWorkoutsWithExercise(userId, selectedExercise!!, startTime, now)
                } else {
                    db.antrenamentDao().getWorkoutsInPeriod(userId, startTime, now)
                }

                val workoutIds = allWorkouts.map { it.id }
                val setCountMap = if (workoutIds.isNotEmpty()) {
                    db.exercitiuDao().getSetCountsForWorkouts(workoutIds).associate { it.antrenamentId to it.cnt }
                } else emptyMap()

                val dateFmt = if (days <= 7) SimpleDateFormat("EEE", Locale.getDefault())
                    else SimpleDateFormat("dd", Locale.getDefault())

                val volumeValues = mutableListOf<Double>()
                val volumeLabels = mutableListOf<String>()
                val maxWeightValues = mutableListOf<Double>()
                val maxWeightLabels = mutableListOf<String>()
                val setsValues = mutableListOf<Double>()
                val setsLabels = mutableListOf<String>()

                if (days <= 7) {
                    for (i in 0 until 7) {
                        val dayCal = Calendar.getInstance().apply {
                            timeInMillis = now
                            add(Calendar.DAY_OF_YEAR, -(6 - i))
                        }
                        val dayWorkouts = allWorkouts.filter {
                            val wc = Calendar.getInstance().apply { timeInMillis = it.data }
                            wc.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR) &&
                                    wc.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR)
                        }
                        volumeValues.add(dayWorkouts.sumOf { it.totalWeight })
                        volumeLabels.add(dateFmt.format(dayCal.time).take(3))
                        val maxW = dayWorkouts.maxOfOrNull { it.totalWeight } ?: 0.0
                        maxWeightValues.add(maxW)
                        maxWeightLabels.add(dateFmt.format(dayCal.time).take(3))
                        val daySets = dayWorkouts.sumOf { setCountMap[it.id] ?: 0 }
                        setsValues.add(daySets.toDouble())
                        setsLabels.add(dateFmt.format(dayCal.time).take(3))
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
                        val weekWorkouts = allWorkouts.filter { it.data >= weekStartCal.timeInMillis && it.data < weekEndCal.timeInMillis }
                        volumeValues.add(weekWorkouts.sumOf { it.totalWeight })
                        volumeLabels.add("S${w + 1}")
                        val maxW = weekWorkouts.maxOfOrNull { it.totalWeight } ?: 0.0
                        maxWeightValues.add(maxW)
                        maxWeightLabels.add("S${w + 1}")
                        val weekSets = weekWorkouts.sumOf { setCountMap[it.id] ?: 0 }
                        setsValues.add(weekSets.toDouble())
                        setsLabels.add("S${w + 1}")
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
                        val monthWorkouts = allWorkouts.filter { it.data >= mStart.timeInMillis && it.data < mEnd.timeInMillis }
                        volumeValues.add(monthWorkouts.sumOf { it.totalWeight })
                        val monthFmt = SimpleDateFormat("MMM", Locale.getDefault())
                        volumeLabels.add(monthFmt.format(mStart.time).take(3))
                        val maxW = monthWorkouts.maxOfOrNull { it.totalWeight } ?: 0.0
                        maxWeightValues.add(maxW)
                        maxWeightLabels.add(monthFmt.format(mStart.time).take(3))
                        val monthSets = monthWorkouts.sumOf { setCountMap[it.id] ?: 0 }
                        setsValues.add(monthSets.toDouble())
                        setsLabels.add(monthFmt.format(mStart.time).take(3))
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

                result[periodIdx] = mutableMapOf(
                    0 to ChartData(volumeValues, volumeLabels, strings.volume, if (isLbs) "lbs" else "kg",
                        computeDelta(volumeValues)),
                    1 to ChartData(maxWeightValues, maxWeightLabels, strings.maxWeight, if (isLbs) "lbs" else "kg",
                        computeDelta(maxWeightValues)),
                    2 to ChartData(setsValues, setsLabels, strings.sets, "",
                        computeDelta(setsValues))
                )
            }

            chartData = result

            val cal2 = Calendar.getInstance()
            cal2.set(Calendar.DAY_OF_MONTH, 1)
            cal2.set(Calendar.HOUR_OF_DAY, 0); cal2.set(Calendar.MINUTE, 0)
            cal2.set(Calendar.SECOND, 0); cal2.set(Calendar.MILLISECOND, 0)
            val monthStart = cal2.timeInMillis
            val workoutsMonth = db.antrenamentDao().getWorkoutsInPeriod(userId, monthStart, now)
            val counts = mutableMapOf<String, Int>()
            for (w in workoutsMonth) {
                counts[w.grupaMusculara] = (counts[w.grupaMusculara] ?: 0) + 1
            }
            distributionData = counts
        }
    }

    val currentData = chartData[selectedPeriod]?.get(selectedMetric)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 8.dp,
            bottom = paddingValues.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, accent, RoundedCornerShape(24.dp))
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = strings.stats.uppercase(),
                        color = accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp
                    )
                }
            }
        }

        // Exercise filter dropdown
        if (allExerciseNames.isNotEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dividerBg, RoundedCornerShape(10.dp))
                            .clickable { showExerciseDropdown = !showExerciseDropdown }
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
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showExerciseDropdown,
                        onDismissRequest = { showExerciseDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.allExercises) },
                            onClick = {
                                selectedExercise = null
                                showExerciseDropdown = false
                            }
                        )
                        allExerciseNames.forEach { name ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    selectedExercise = name
                                    showExerciseDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Chart card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val metricLabels = remember(strings) { listOf(strings.volume, strings.maxWeight, strings.sets) }
                        metricLabels.forEachIndexed { idx, label ->
                            val isSelected = selectedMetric == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        if (isSelected) accent else dividerBg,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (isSelected) accent.copy(alpha = 0.12f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 7.dp)
                                    .let { mod ->
                                        mod.then(
                                            Modifier.padding(horizontal = 4.dp)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { selectedMetric = idx }) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) accent else textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = currentData?.label?.uppercase() ?: "",
                                color = textSecondary,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formatChartValue(currentData?.values?.lastOrNull() ?: 0.0),
                                    color = textPrimary,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (currentData?.unit?.isNotEmpty() == true) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentData.unit,
                                        color = textSecondary,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                            // #1: Fixed delta - only show when we have real data to compare
                            val deltaText = currentData?.delta ?: ""
                            if (deltaText.isNotEmpty()) {
                                Text(
                                    text = deltaText,
                                    color = accent,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val periodLabels = remember(strings) { listOf("7${strings.days.take(1)}", "30${strings.days.take(1)}", "3l") }
                            periodLabels.forEachIndexed { idx, label ->
                                val isSelected = selectedPeriod == idx
                                Box(
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            if (isSelected) accent else dividerBg,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(
                                            if (isSelected) accent.copy(alpha = 0.12f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TextButton(onClick = { selectedPeriod = idx }) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) accent else textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // #5: Line chart with dashed segments for gaps
                    if (currentData != null && currentData.hasData) {
                        val values = currentData.values
                        val labels = currentData.labels
                        val maxVal = values.max()
                        val minVal = values.min()
                        val range = (maxVal - minVal).coerceAtLeast(1.0)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(78.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val padTop = 10f
                            val padBottom = 15f
                            val usableH = h - padTop - padBottom
                            val step = if (values.size > 1) w / (values.size - 1) else w
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

                            // Draw segments: solid where both endpoints have data, dashed where either is 0
                            for (i in 0 until values.size) {
                                val x = i * step
                                val y = padTop + usableH - ((values[i] - minVal) / range * usableH).toFloat()
                                if (i > 0) {
                                    val prevX = (i - 1) * step
                                    val prevY = padTop + usableH - ((values[i - 1] - minVal) / range * usableH).toFloat()
                                    val bothHaveData = values[i] > 0 && values[i - 1] > 0
                                    val segPath = Path().apply {
                                        moveTo(prevX, prevY)
                                        lineTo(x, y)
                                    }
                                    drawPath(
                                        path = segPath,
                                        color = accent,
                                        style = Stroke(
                                            width = 2.5.dp.toPx(),
                                            pathEffect = if (bothHaveData) null else dashEffect
                                        )
                                    )
                                }
                            }

                            // Draw dots only where there's real data
                            values.forEachIndexed { i, v ->
                                if (v > 0) {
                                    val x = i * step
                                    val y = padTop + usableH - ((v - minVal) / range * usableH).toFloat()
                                    drawCircle(
                                        color = accent,
                                        radius = if (i == values.size - 1) 5.dp.toPx() else 3.dp.toPx(),
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            labels.forEach { label ->
                                Text(
                                    text = label,
                                    color = textSecondary,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(78.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(strings.noChartData, color = textSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // #2: Streak + Badges card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.currentStreakLabel.uppercase(),
                            color = textSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currentStreak ${strings.days}",
                            color = textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.bestStreak.uppercase(),
                            color = textSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$bestStreak ${strings.days}",
                            color = textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = strings.badges.uppercase(),
                            color = textSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$badgeCount",
                            color = GoldPR,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // #3 & #6: Last PR + Most Trained row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Last PR card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(GoldPR.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.lastPR.uppercase(),
                            color = textSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lastPR?.exerciseName ?: "--",
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = if (lastPR != null) "${weightLabel(lastPR.weight, isLbs)} x ${lastPR.reps}" else "",
                            color = GoldPR,
                            fontSize = 13.sp
                        )
                    }
                }

                // #3: Most Trained card (renamed from "Start Here")
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(textSecondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.mostTrained.uppercase(),
                            color = textSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weeklyTopExercise ?: "--",
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        TextButton(
                            onClick = {
                                weeklyTopExercise?.let { onExerciseHistoryClick(it) }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = strings.exerciseHistory,
                                color = textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // #6: PR Timeline (recent PRs history)
        if (allRecentPRs.size > 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = strings.personalRecords.uppercase(),
                            color = textSecondary,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        allRecentPRs.take(5).forEachIndexed { index, pr ->
                            val dateFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                            val dateStr = dateFmt.format(Date(pr.date))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pr.exerciseName,
                                        color = textPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${weightLabel(pr.weight, isLbs)} x ${pr.reps}",
                                    color = GoldPR,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateStr,
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (index < allRecentPRs.size - 1 && index < 4) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = dividerBg,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // #4: Workout Distribution with improvements
        if (distributionData.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.workoutDistribution,
                            color = textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.thisMonth,
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val totalCount = distributionData.values.sum()
                        val maxCount = distributionData.values.maxOrNull() ?: 1
                        val sortedGroups = distributionData.entries.sortedByDescending { it.value }

                        sortedGroups.forEach { (group, count) ->
                            val pct = if (totalCount > 0) (count * 100 / totalCount) else 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LanguageManager.translateMuscleGroup(group, strings),
                                    color = textPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(60.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .background(dividerBg, RoundedCornerShape(6.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = count.toFloat() / maxCount)
                                            .background(accent, RoundedCornerShape(6.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$count",
                                    color = textSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.width(20.dp),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$pct%",
                                    color = textSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalCount ${strings.sessions}",
                            color = textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatChartValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.1f", value)
    }
}
