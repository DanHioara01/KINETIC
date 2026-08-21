package com.example.kinetic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.theme.JetBrainsMono
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.overlay
import kotlin.math.roundToInt

/**
 * Readiness score: somn (manual) + pași + recuperare musculară + volum de azi
 * → scor 0-100 și verdictul zilei: HEAVY / MODERATE / LIGHT.
 */
@Composable
fun ReadinessScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context, UserProfileManager(context)) }
    val p = appPalette(isDark)
    val S1 = p.cr
    val Bdr = p.bd

    var sleepHours by remember { mutableDoubleStateOf(prefs.getSleepHours()) }
    var sleepQuality by remember { mutableIntStateOf(prefs.getSleepQuality()) }
    var stepsToday by remember { mutableIntStateOf(0) }
    var recoveryAvg by remember { mutableFloatStateOf(0f) }
    var todayVolume by remember { mutableDoubleStateOf(0.0) }
    var waterMl by remember { mutableIntStateOf(0) }
    var waterGoal by remember { mutableIntStateOf(2000) }
    var history7 by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    LaunchedEffect(Unit) {
        stepsToday = prefs.getTodaySteps()
        waterMl = prefs.getTodayWaterMl()
        waterGoal = prefs.getWaterGoalMl().coerceAtLeast(1)
        history7 = prefs.getReadinessHistory7Days()
        try {
            val repo = AntrenamentRepository(AppDatabase.getDatabase(context))
            val userId = UserProfileManager(context).getOwnUserId()
            val rec = repo.getToateRecuperarile(userId)
            recoveryAvg = if (rec.isEmpty()) 0f else rec.map { it.second }.average().toFloat()
            todayVolume = repo.getVolumeSummary(userId).azi
        } catch (_: Exception) {}
    }

    fun onSleepChanged(hours: Double, quality: Int) {
        sleepHours = hours
        sleepQuality = quality
        prefs.setSleepHours(hours)
        prefs.setSleepQuality(quality)
    }

    val sleepScore = (((sleepHours - 4.5) / 4.0) * 100.0).coerceIn(0.0, 100.0)
    val stepsScore = (stepsToday / 10000.0 * 100.0).coerceIn(0.0, 100.0)
    val recoveryScore = ((1.0 - recoveryAvg) * 100.0).coerceIn(0.0, 100.0)
    val volumeScore = ((1.0 - (todayVolume / 10000.0).coerceAtMost(1.0)) * 100.0).coerceIn(0.0, 100.0)
    val hydrationScore = (waterMl.toDouble() / waterGoal * 100.0).coerceIn(0.0, 100.0)
    val readiness = (0.30 * sleepScore + 0.25 * recoveryScore + 0.15 * stepsScore + 0.15 * hydrationScore + 0.15 * volumeScore)
        .roundToInt().coerceIn(0, 100)

    LaunchedEffect(readiness) { prefs.saveReadinessScore(readiness) }
    val verdict = when {
        readiness >= 70 -> strings.readinessHeavy
        readiness >= 40 -> strings.readinessModerate
        else -> strings.readinessLight
    }
    val verdictColor = when {
        readiness >= 70 -> p.gn
        readiness >= 40 -> p.ac
        else -> Color(0xFFFFB74D)
    }

    val factors = listOf(
        Triple(strings.readinessSleep, sleepScore, strings.readinessActionSleep),
        Triple(strings.readinessSteps, stepsScore, strings.readinessActionSteps),
        Triple(strings.readinessRecovery, recoveryScore, strings.readinessActionRecovery),
        Triple(strings.readinessVolume, volumeScore, strings.readinessActionVolume),
        Triple(strings.readinessHydration, hydrationScore, strings.readinessActionHydration),
    )
    val weakest = factors.minByOrNull { it.second }

    // ── 15 readiness recommendations ──
    val allRecommendations = listOf(
        Triple(sleepScore < 60, strings.readinessActionSleep, Color(0xFF7C4DFF)),
        Triple(hydrationScore < 50, strings.readinessActionHydration, Color(0xFF2196F3)),
        Triple(recoveryScore < 40, strings.readinessActionRecovery, Color(0xFFFF9800)),
        Triple(stepsScore < 30, strings.readinessActionSteps, Color(0xFF4CAF50)),
        Triple(volumeScore < 30, strings.readinessActionVolume, Color(0xFFE91E63)),
        Triple(sleepHours < 6.0, "Try to sleep 7-9 hours for optimal muscle recovery", Color(0xFF7C4DFF)),
        Triple(sleepQuality <= 2, "Low sleep quality — avoid screens 1 hour before bed", Color(0xFF7C4DFF)),
        Triple(hydrationScore < 30, "Drink at least 500ml water in the next hour", Color(0xFF2196F3)),
        Triple(recoveryScore < 25, "Do 10 min of light stretching before training", Color(0xFFFF9800)),
        Triple(readiness >= 80, "Great readiness! Push for a new PR today", Color(0xFF4CAF50)),
        Triple(readiness in 60..79, "Good shape — stick to your regular program", Color(0xFF4CAF50)),
        Triple(readiness < 40, "Consider a rest day or very light activity only", Color(0xFFFFB74D)),
        Triple(stepsScore > 80 && recoveryScore < 50, "Good steps but muscles are tired — foam roll tonight", Color(0xFFFF9800)),
        Triple(sleepScore > 80 && hydrationScore < 40, "Well rested but dehydrated — drink water before training", Color(0xFF2196F3)),
        Triple(readiness in 40..59, "Moderate day — reduce volume by 20% if feeling fatigue", Color(0xFFFFB74D)),
    )
    val activeRecommendations = allRecommendations.filter { it.first }.take(15)

    val intensityText = when {
        readiness >= 70 -> strings.readinessIntensityHeavy
        readiness >= 40 -> strings.readinessIntensityModerate
        else -> strings.readinessIntensityLight
    }
    val intensityColor = when {
        readiness >= 70 -> p.gn
        readiness >= 40 -> p.ac
        else -> Color(0xFFFFB74D)
    }

    val validHistory = history7.filter { it.second >= 0 }
    val trendText = if (validHistory.size >= 2) {
        val recent = validHistory.takeLast(3).map { it.second }.average()
        val older = validHistory.dropLast(3).map { it.second }.average().let { if (validHistory.size <= 3) recent else it }
        when {
            recent > older + 3 -> "↑"
            recent < older - 3 -> "↓"
            else -> "→"
        }
    } else ""

    Column(modifier = modifier.fillMaxSize().background(p.bg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── Score card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(S1)
                    .border(1.dp, Bdr, RoundedCornerShape(22.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(strings.readinessScore, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.ts, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier.size(96.dp).clip(RoundedCornerShape(48.dp))
                        .background(Brush.verticalGradient(listOf(p.ac.copy(alpha = 0.25f), p.ac.copy(alpha = 0.08f))))
                        .border(2.dp, p.ac.copy(alpha = 0.45f), RoundedCornerShape(48.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$readiness",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = p.tp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    verdict,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = verdictColor,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(intensityText, fontSize = 11.sp, color = intensityColor, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            // ── 7-day trend chart ──
            if (validHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(S1)
                        .border(1.dp, p.bd, RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Text(strings.readinessTrend, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.tp)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        history7.forEach { (day, score) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                val barH = if (score >= 0) (score / 100f * 60f).dp else 2.dp
                                val barC = when { score < 0 -> p.overlay(0.08f); score >= 70 -> p.gn; score >= 40 -> p.ac; else -> Color(0xFFFFB74D) }
                                Box(modifier = Modifier.width(20.dp).height(barH).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(barC.copy(alpha = if (score >= 0) 0.8f else 0.3f)))
                                Spacer(Modifier.height(4.dp))
                                Text(day, fontSize = 9.sp, color = p.ts, fontWeight = FontWeight.Medium)
                                if (score >= 0) Text("$score", fontSize = 8.sp, color = p.ts, fontFamily = JetBrainsMono)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Factor bars ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(S1)
                    .border(1.dp, Bdr, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                ReadinessBar(strings.readinessSleep, sleepScore, p)
                ReadinessBar(strings.readinessSteps, stepsScore, p)
                ReadinessBar(strings.readinessRecovery, recoveryScore, p)
                ReadinessBar(strings.readinessHydration, hydrationScore, p)
                ReadinessBar(strings.readinessVolume, volumeScore, p)
            }

            Spacer(Modifier.height(12.dp))

            // ── Recommendations card ──
            if (activeRecommendations.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(S1)
                        .border(1.dp, p.bd, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Text(strings.readinessHint, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.tp, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(10.dp))
                    activeRecommendations.forEachIndexed { index, rec ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(rec.third)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(rec.second, fontSize = 11.sp, color = p.ts, modifier = Modifier.weight(1f))
                        }
                        if (index < activeRecommendations.lastIndex) {
                            Spacer(Modifier.height(2.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Sleep input card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(S1)
                    .border(1.dp, Bdr, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Bedtime, null, tint = p.ac, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.readinessSleep, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.tp)
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.readinessSleepHours, fontSize = 12.sp, color = p.ts, modifier = Modifier.weight(1f))
                    Text(
                        String.format("%.1f h", sleepHours),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        color = p.ac
                    )
                }
                Slider(
                    value = sleepHours.toFloat(),
                    onValueChange = { onSleepChanged(it.toDouble(), sleepQuality) },
                    valueRange = 0f..12f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = p.ac,
                        activeTrackColor = p.ac,
                        inactiveTrackColor = p.overlay(0.10f)
                    )
                )
                Spacer(Modifier.height(10.dp))
                Text(strings.readinessQuality, fontSize = 12.sp, color = p.ts)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { q ->
                        val selected = q <= sleepQuality
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) p.ac.copy(alpha = 0.18f) else p.overlay(0.05f))
                                .border(1.dp, if (selected) p.ac.copy(alpha = 0.5f) else p.bd, RoundedCornerShape(10.dp))
                                .clickable { onSleepChanged(sleepHours, q) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$q", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) p.ac else p.ts, fontFamily = JetBrainsMono)
                        }
                    }
                }
            }

            Spacer(Modifier.height(AppConstants.BOTTOM_NAV_PADDING))
        }
    }
}

@Composable
private fun ReadinessBar(label: String, score: Double, p: com.example.kinetic.ui.theme.AppPalette) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, fontSize = 12.sp, color = p.ts, modifier = Modifier.width(120.dp))
        Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(6.dp)).background(p.overlay(0.08f))) {
            Box(Modifier.fillMaxWidth((score / 100.0).toFloat().coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(p.ac))
        }
        Spacer(Modifier.width(10.dp))
        Text(score.roundToInt().toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.tp, fontFamily = JetBrainsMono, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
    }
}
