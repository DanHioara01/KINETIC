package com.example.kinetic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.*
import kotlinx.coroutines.launch

data class MuscleRiskInfo(
    val muscleGroup: String,
    val muscleGroupKey: String,
    val riskLevel: Double,
    val fatigueLevel: Double,
    val reason: String,
    val recommendation: String
)

private val romanianToEnglish = mapOf(
    "Piept" to "chest",
    "Spate" to "back",
    "Picioare" to "legs",
    "Umeri" to "shoulders",
    "Biceps" to "biceps",
    "Triceps" to "triceps",
    "Abdomen" to "abs",
    "Fese" to "glutes",
    "Gambe" to "calves",
    "Antebrate" to "forearms",
    "Gat & Trapezi" to "neck"
)

private val recoveryHours = mapOf(
    "chest" to 48L,
    "back" to 72L,
    "legs" to 72L,
    "shoulders" to 48L,
    "biceps" to 36L,
    "triceps" to 48L,
    "abs" to 48L,
    "glutes" to 72L,
    "calves" to 36L,
    "forearms" to 36L,
    "neck" to 48L
)

fun assessInjuryRisk(
    workouts: List<AntrenamentEntity>,
    exercises: List<ExercitiuEntity>,
    recovery: List<MuscleRecoveryEntity>
): List<MuscleRiskInfo> {
    val muscleGroups = listOf("chest", "back", "legs", "shoulders", "biceps", "triceps", "abs")
    val risks = mutableListOf<MuscleRiskInfo>()

    val now = System.currentTimeMillis()

    for (group in muscleGroups) {
        val romanianName = romanianToEnglish.entries.find { it.value == group }?.key ?: group
        val groupWorkouts = workouts.filter { it.grupaMusculara.equals(group, ignoreCase = true) || it.grupaMusculara.equals(romanianName, ignoreCase = true) }
        val recentWorkouts = groupWorkouts.filter {
            it.data > now - 7 * 24 * 60 * 60 * 1000
        }
        val totalVolume = exercises.filter { ex ->
            groupWorkouts.any { it.id == ex.antrenamentId }
        }.sumOf { it.greutateKg * it.repetari }

        val recoveryEntity = recovery.find {
            it.grupaMusculara.equals(group, ignoreCase = true) ||
            it.grupaMusculara.equals(romanianName, ignoreCase = true)
        }

        val fatigueLevel = if (recoveryEntity != null) {
            val hours = recoveryHours[group] ?: 48L
            val elapsedMs = now - recoveryEntity.lastUpdated
            val recoveryMs = hours * 3_600_000
            val drain = elapsedMs.toDouble() / recoveryMs
            (recoveryEntity.level - drain).coerceIn(0.0, 1.0)
        } else {
            0.0
        }

        var riskLevel = 0.0
        val reasons = mutableListOf<String>()

        if (recentWorkouts.size >= 4) {
            riskLevel += 0.3
            reasons.add("High frequency (${recentWorkouts.size}x/week)")
        } else if (recentWorkouts.size >= 3) {
            riskLevel += 0.15
            reasons.add("Moderate frequency (${recentWorkouts.size}x/week)")
        }

        if (totalVolume > 50000) {
            riskLevel += 0.25
            reasons.add("Very high volume")
        } else if (totalVolume > 30000) {
            riskLevel += 0.15
            reasons.add("High volume")
        }

        if (fatigueLevel > 0.7) {
            riskLevel += 0.3
            reasons.add("High fatigue (${String.format("%.0f", fatigueLevel * 100)}%)")
        } else if (fatigueLevel > 0.4) {
            riskLevel += 0.15
            reasons.add("Moderate fatigue (${String.format("%.0f", fatigueLevel * 100)}%)")
        }

        val daysSinceLast = if (groupWorkouts.isNotEmpty()) {
            (now - groupWorkouts.maxOfOrNull { it.data }!!) / (24 * 60 * 60 * 1000)
        } else Long.MAX_VALUE

        if (daysSinceLast < 2 && recentWorkouts.size >= 2) {
            riskLevel += 0.2
            reasons.add("Insufficient rest between sessions")
        }

        riskLevel = riskLevel.coerceIn(0.0, 1.0)

        val recommendation = when {
            riskLevel > 0.7 -> "High load. Consider a rest day."
            riskLevel > 0.5 -> "Moderate load. Train with care."
            riskLevel > 0.3 -> "Light load. Monitor fatigue."
            else -> "Good recovery. Safe to train."
        }

        risks.add(MuscleRiskInfo(group, group, riskLevel, fatigueLevel, reasons.joinToString("; "), recommendation))
    }

    return risks.sortedByDescending { it.riskLevel }
}

private val RiskRed = Color(0xFFE53935)
private val RiskOrange = Color(0xFFFFA000)
private val RiskGreen = Color(0xFF43A047)
private val RiskGreenLight = Color(0xFF66BB6A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjuryRiskScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    db: AppDatabase,
    userId: String,
    onBack: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val p = appPalette(isDark)
    val scope = rememberCoroutineScope()

    var risks by remember { mutableStateOf<List<MuscleRiskInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        scope.launch {
            val workouts = db.antrenamentDao().getAllForUser(userId)
            val allExercises = mutableListOf<ExercitiuEntity>()
            for (w in workouts) {
                allExercises.addAll(db.exercitiuDao().getForAntrenament(w.id))
            }
            val recovery = db.muscleRecoveryDao().getAll(userId)
            risks = assessInjuryRisk(workouts, allExercises, recovery)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(strings.injuryRisk ?: "Injury Risk", fontWeight = FontWeight.Bold, color = p.tp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = p.tp, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = p.bg)
            )
        },
        containerColor = p.bg
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = p.ac)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                val highRiskCount = risks.count { it.riskLevel > 0.5 }
                val moderateRiskCount = risks.count { it.riskLevel in 0.3..0.5 }
                val lowRiskCount = risks.size - highRiskCount - moderateRiskCount
                val safeCount = risks.count { it.riskLevel <= 0.3 }

                val overallRingColor = when {
                    highRiskCount > 0 -> RiskRed
                    moderateRiskCount > 0 -> RiskOrange
                    else -> RiskGreen
                }

                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 8.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                drawArc(
                                    color = overallRingColor.copy(alpha = 0.15f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                val sweepAngle = if (risks.isNotEmpty()) (safeCount.toFloat() / risks.size) * 360f else 0f
                                drawArc(
                                    color = overallRingColor,
                                    startAngle = -90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$safeCount/${risks.size}",
                                    color = p.tp,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.highRisk ?: "High risk", color = RiskRed, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("$highRiskCount", color = RiskRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.moderateRisk ?: "Moderate", color = RiskOrange, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("$moderateRiskCount", color = RiskOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.lowRisk ?: "Low risk", color = RiskGreen, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("$lowRiskCount", color = RiskGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                risks.forEach { risk ->
                    val fatiguePercent = risk.fatigueLevel * 100
                    val riskColor = when {
                        risk.riskLevel > 0.5 -> RiskRed
                        risk.riskLevel > 0.3 -> RiskOrange
                        else -> RiskGreen
                    }
                    val cardBorder = when {
                        risk.riskLevel > 0.5 -> RiskRed.copy(alpha = 0.4f)
                        risk.riskLevel > 0.3 -> RiskOrange.copy(alpha = 0.3f)
                        else -> Color.Transparent
                    }

                    AppGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (risk.riskLevel > 0.3) Modifier.border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                else Modifier
                            ),
                        p = p,
                        cornerRadius = 14.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                                                                         .background(riskColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        LanguageManager.translateMuscleGroup(risk.muscleGroup, strings),
                                        color = p.tp,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    "${String.format("%.0f", fatiguePercent)}%",
                                    color = riskColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                                            .background(p.ts.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val fillWidth = size.width * risk.fatigueLevel.toFloat()
                                    if (fillWidth > 0f) {
                                        val fillBrush = Brush.horizontalGradient(
                                            colors = listOf(RiskGreen, RiskGreen, RiskOrange, RiskOrange, RiskRed, RiskRed),
                                            startX = 0f,
                                            endX = size.width
                                        )
                                        drawRect(
                                            brush = fillBrush,
                                            size = size.copy(width = fillWidth)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (risk.riskLevel > 0.3) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = riskColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = RiskGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    risk.recommendation,
                                    color = if (risk.riskLevel > 0.3) riskColor else p.ts,
                                    fontSize = 13.sp,
                                    fontWeight = if (risk.riskLevel > 0.3) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
