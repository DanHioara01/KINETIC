package com.example.kinetic

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kinetic.ui.theme.JetBrainsMono
import androidx.compose.ui.unit.sp

@Composable
fun WeeklySummaryCard(
    weekWorkoutCount: Int,
    weekVolume: Double,
    weekWorkoutDurationMs: Long,
    workoutGoal: Int,
    lastWeekWorkoutCount: Int,
    lastWeekVolume: Double,
    currentStreak: Int,
    bestStreak: Int,
    weeklyTopExercise: String?,
    isDark: Boolean,
    isLbs: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    accent: Color,
    iconBg: Color,
    strings: LanguageManager.Strings,
    weightLabel: (Double) -> String
) {
    val borderColor = if (isDark) Color(0x0FFFFFFF) else Color(0x14000000)
    val trackColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)

    // Volum (kg sau lbs) — parsam numărul și unitatea din weightLabel
    val volumeValue = weightLabel(weekVolume)
    val volumeNum = volumeValue.split(" ")[0].replace(",", "").toFloatOrNull() ?: weekVolume.toFloat()
    val volumeUnit = if (volumeValue.contains(" ")) {
        volumeValue.split(" ").drop(1).joinToString(" ")
    } else ""

    // Durată totală săptămânală (ore cu 1 zecimală)
    val totalHours = weekWorkoutDurationMs / 3600000.0
    val hoursText = if (totalHours >= 10) String.format(java.util.Locale.ROOT, "%.0f", totalHours) else String.format(java.util.Locale.ROOT, "%.1f", totalHours)

    // Progresuri relative (semnificative, nu statice)
    val workoutProgress = if (workoutGoal > 0) {
        (weekWorkoutCount.toFloat() / workoutGoal).coerceIn(0f, 1f)
    } else if (weekWorkoutCount > 0) 1f else 0f

    val volumeProgress = if (lastWeekVolume > 0) {
        (weekVolume.toFloat() / lastWeekVolume.toFloat()).coerceIn(0f, 1f)
    } else if (weekVolume > 0) 0.6f else 0f

    val timeProgress = (totalHours.toFloat() / 6f).coerceIn(0f, 1f)

    val streakProgress = (bestStreak / 30f).coerceIn(0f, 1f)

    val workoutsColor = accent
    val volumeColor = Color(0xFF2DD4A0)
    val timeColor = Color(0xFF4E8CFF)
    val streakColor = Color(0xFFF5A623)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.weeklySummary.uppercase(),
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                if (currentStreak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("\uD83D\uDD25", fontSize = 13.sp)
                        Text(
                            "$currentStreak ${strings.daysConsecutive}",
                            fontSize = 12.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Grid 2×2 cu progress bars animate ─────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryProgressCard(
                    modifier = Modifier.weight(1f),
                    icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.LocalFireDepartment),
                    iconTint = workoutsColor,
                    iconBg = workoutsColor.copy(alpha = 0.15f),
                    label = strings.workoutsLabel.uppercase(),
                    value = weekWorkoutCount.toString(),
                    unit = if (workoutGoal > 0) "/ $workoutGoal" else "",
                    progress = workoutProgress,
                    progressColor = workoutsColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    trackColor = trackColor,
                    borderColor = borderColor
                )
                SummaryProgressCard(
                    modifier = Modifier.weight(1f),
                    icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.MonitorWeight),
                    iconTint = volumeColor,
                    iconBg = volumeColor.copy(alpha = 0.15f),
                    label = strings.volumeLabel.uppercase(),
                    value = String.format(java.util.Locale.ROOT, "%,.0f", volumeNum),
                    unit = volumeUnit,
                    progress = volumeProgress,
                    progressColor = volumeColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    trackColor = trackColor,
                    borderColor = borderColor
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryProgressCard(
                    modifier = Modifier.weight(1f),
                    icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Outlined.Schedule),
                    iconTint = timeColor,
                    iconBg = timeColor.copy(alpha = 0.15f),
                    label = strings.duration.uppercase(),
                    value = hoursText,
                    unit = "h",
                    progress = timeProgress,
                    progressColor = timeColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    trackColor = trackColor,
                    borderColor = borderColor
                )
                SummaryProgressCard(
                    modifier = Modifier.weight(1f),
                    icon = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.EmojiEvents),
                    iconTint = streakColor,
                    iconBg = streakColor.copy(alpha = 0.15f),
                    label = strings.bestStreakLabel.uppercase(),
                    value = bestStreak.toString(),
                    unit = strings.days,
                    progress = streakProgress,
                    progressColor = streakColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    trackColor = trackColor,
                    borderColor = borderColor
                )
            }

            if (weeklyTopExercise != null) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color(0xFF1E1E1E) else iconBg,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("\u2B50", fontSize = 16.sp)
                    Column {
                        Text(
                            strings.topExerciseLabel,
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                        Text(
                            weeklyTopExercise,
                            fontSize = 14.sp,
                            color = textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryProgressCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.painter.Painter,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String,
    unit: String,
    progress: Float,
    progressColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    trackColor: Color,
    borderColor: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "summaryProgress"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp, 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = textSecondary,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = textPrimary,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                    fontFamily = JetBrainsMono
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = " $unit",
                        fontSize = 11.sp,
                        color = textSecondary,
                        modifier = Modifier.padding(bottom = 3.dp),
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(progressColor)
                )
            }
        }
    }
}
