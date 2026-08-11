package com.example.kinetic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightGoalScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    db: AppDatabase,
    userId: String,
    latestWeight: Double,
    heightCm: Float,
    bodyFatPercent: Double = 0.0,
    waistCm: Double = 0.0,
    onBack: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val p = appPalette(isDark)

    var activeGoal by remember { mutableStateOf<WeightGoalEntity?>(null) }
    var goalHistory by remember { mutableStateOf<List<WeightGoalEntity>>(emptyList()) }
    var showSetGoal by remember { mutableStateOf(false) }
    var weightHistory by remember { mutableStateOf<List<Pair<String, Double>>>(emptyList()) }

    LaunchedEffect(userId) {
        activeGoal = db.weightGoalDao().getActiveGoal(userId)
        goalHistory = db.weightGoalDao().getAllForUser(userId)
        val entries = db.biometricDao().getAllForUser(userId)
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val bioHistory = entries
            .filter { it.weightKg > 0 }
            .sortedBy { it.timestamp }
            .map { dateFormat.format(Date(it.timestamp)) to it.weightKg }
        weightHistory = if (bioHistory.size >= 2) {
            bioHistory
        } else if (bioHistory.size == 1 && latestWeight > 0) {
            listOf(
                "Start" to latestWeight,
                bioHistory[0].first to bioHistory[0].second
            )
        } else if (latestWeight > 0) {
            val cal = java.util.Calendar.getInstance()
            val now = dateFormat.format(cal.time)
            cal.add(java.util.Calendar.MONTH, -1)
            val monthAgo = dateFormat.format(cal.time)
            listOf(monthAgo to latestWeight, now to latestWeight)
        } else {
            emptyList()
        }
    }

    val bmi = if (heightCm > 0 && latestWeight > 0) latestWeight / ((heightCm / 100) * (heightCm / 100)) else 0.0
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            KineticAppBar(onBack = onBack)
        },
        containerColor = p.bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            AppGlassCard(
                modifier = Modifier.fillMaxWidth(),
                p = p,
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column {
                    Text(strings.currentWeight ?: "Current weight", color = p.ts, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "${String.format("%.1f", latestWeight)} kg",
                            color = p.tp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        if (heightCm > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(strings.heightCm ?: "Height", color = p.ts, fontSize = 12.sp)
                                Text(
                                    "${String.format("%.0f", heightCm)} cm",
                                    color = p.tp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column {
                            Text(strings.bmi, color = p.ts, fontSize = 12.sp)
                            Text(
                                if (bmi > 0) "${String.format("%.1f", bmi)}" else "--",
                                color = p.tp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Column {
                            Text(strings.bodyFat ?: "Body fat", color = p.ts, fontSize = 12.sp)
                            Text(
                                if (bodyFatPercent > 0) "${String.format("%.1f", bodyFatPercent)}%" else "--",
                                color = p.tp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            if (weightHistory.isNotEmpty()) {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column {
                        Text(
                            strings.weightEvolution ?: "Weight Evolution",
                            color = p.tp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "${weightHistory.size} ${strings.measurements ?: "measurements"}",
                            color = p.ts,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LineChart(
                            data = weightHistory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            lineColor = Color(0xFF6C63FF),
                            dotColor = Color(0xFF6C63FF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${String.format("%.1f", weightHistory.first().second)} kg",
                                color = p.ts,
                                fontSize = 12.sp
                            )
                            Text(
                                "${String.format("%.1f", weightHistory.last().second)} kg",
                                color = p.tp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (activeGoal != null) {
                val goal = activeGoal!!
                val progress = if (goal.startWeightKg != goal.targetWeightKg) {
                    val currentProgress = (latestWeight - goal.startWeightKg) / (goal.targetWeightKg - goal.startWeightKg)
                    currentProgress.coerceIn(0.0, 1.0)
                } else 0.0

                val remainingDays = ((goal.deadlineTimestamp - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                val remainingWeight = latestWeight - goal.targetWeightKg

                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${strings.startWeight ?: "Start"} ${String.format("%.1f", goal.startWeightKg)} kg",
                                color = p.ts,
                                fontSize = 13.sp
                            )
                            Text(
                                "${strings.targetWeight ?: "Target"} ${String.format("%.1f", goal.targetWeightKg)} kg",
                                color = p.ts,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                                            .background(p.ac.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress.toFloat())
                                                            .background(p.ac, RoundedCornerShape(4.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(strings.currentWeight ?: "Progress", color = p.ts, fontSize = 12.sp)
                                Text(
                                    "${String.format("%.1f", latestWeight)} kg",
                                    color = p.tp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.remaining ?: "Remaining", color = p.ts, fontSize = 12.sp)
                                Text(
                                    "${String.format("%.1f", kotlin.math.abs(remainingWeight))} kg",
                                    color = p.ac,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(strings.deadline ?: "Deadline", color = p.ts, fontSize = 12.sp)
                                Text(
                                    "$remainingDays ${strings.days ?: "days"}",
                                    color = p.tp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column {
                        Text(strings.goalDetails ?: "Goal details", color = p.tp, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.startedOn ?: "Started on", color = p.ts, fontSize = 14.sp)
                            Text(dateFormat.format(Date(goal.createdAt)), color = p.tp, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = p.ts.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(strings.deadline ?: "Deadline", color = p.ts, fontSize = 14.sp)
                            Text(dateFormat.format(Date(goal.deadlineTimestamp)), color = p.tp, fontSize = 14.sp)
                        }
                    }
                }

                Button(
                    onClick = { showSetGoal = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.editGoal ?: "Edit goal", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            tint = p.ac.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(strings.noActiveGoal ?: "No active goal", color = p.ts, fontSize = 15.sp)
                        Text(strings.setGoalToTrack ?: "Set a goal to track your progress", color = p.ts, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = { showSetGoal = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.setGoal ?: "Set Goal", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (goalHistory.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(strings.pastGoals ?: "Past Goals", color = p.tp, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                goalHistory.filter { !it.isActive }.forEach { goal ->
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 12.dp,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${String.format("%.1f", goal.startWeightKg)} → ${String.format("%.1f", goal.targetWeightKg)} kg",
                                    color = p.tp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(goal.createdAt)),
                                    color = p.ts,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppConstants.BOTTOM_NAV_PADDING))
        }
    }

    if (showSetGoal) {
        SetGoalDialog(
            isDark = isDark,
            strings = strings,
            currentWeight = latestWeight,
            heightCm = heightCm,
            bmi = bmi,
            existingGoal = activeGoal,
            onDismiss = { showSetGoal = false },
            onSave = { target, days ->
                MainScope().launch {
                    db.weightGoalDao().deactivateAll(userId)
                    db.weightGoalDao().insert(
                        WeightGoalEntity(
                            userId = userId,
                            targetWeightKg = target,
                            startWeightKg = latestWeight,
                            deadlineTimestamp = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L),
                            isActive = true
                        )
                    )
                    activeGoal = db.weightGoalDao().getActiveGoal(userId)
                    goalHistory = db.weightGoalDao().getAllForUser(userId)
                    showSetGoal = false
                }
            }
        )
    }
}

@Composable
fun SetGoalDialog(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    currentWeight: Double,
    heightCm: Float,
    bmi: Double,
    existingGoal: WeightGoalEntity?,
    onDismiss: () -> Unit,
    onSave: (Double, Int) -> Unit
) {
    val p = appPalette(isDark)
    val cardBg = p.card
    val textPrimary = p.tp
    val textSecondary = p.ts
    val accent = p.ac

    var targetWeight by remember { mutableStateOf(existingGoal?.targetWeightKg?.let { String.format("%.1f", it) } ?: String.format("%.1f", currentWeight)) }
    var days by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        title = { Text(strings.setGoal ?: "Set Goal", fontWeight = FontWeight.Bold, color = textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(strings.currentWeight ?: "Current Weight", color = textSecondary, fontSize = 13.sp)
                        Text(
                            "${String.format("%.1f", currentWeight)} kg",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        if (heightCm > 0) {
                            Text(strings.heightCm ?: "Height", color = textSecondary, fontSize = 12.sp)
                            Text("${String.format("%.0f", heightCm)} cm", color = textPrimary, fontWeight = FontWeight.Bold)
                        }
                        if (bmi > 0) {
                            Text(strings.bmi, color = textSecondary, fontSize = 12.sp)
                            Text("${String.format("%.1f", bmi)}", color = textPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(strings.targetWeight ?: "Target Weight (kg)", color = textSecondary, fontSize = 13.sp)
                OutlinedTextField(
                    value = targetWeight,
                    onValueChange = { targetWeight = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text("kg", color = textSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                        cursorColor = accent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(strings.deadline ?: "Deadline (days)", color = textSecondary, fontSize = 13.sp)
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    suffix = { Text(strings.days ?: "days", color = textSecondary) },
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
                    val target = targetWeight.toDoubleOrNull() ?: currentWeight
                    val daysInt = days.toIntOrNull() ?: 30
                    onSave(target, daysInt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.save ?: "Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel ?: "Cancel", color = accent)
            }
        }
    )
}
