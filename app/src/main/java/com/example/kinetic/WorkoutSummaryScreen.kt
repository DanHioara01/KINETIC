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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.theme.*

@Composable
fun WorkoutSummaryScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    workout: AntrenamentEntity,
    exercises: List<ExercitiuEntity>,
    prs: List<PersonalRecordEntity>,
    onDismiss: () -> Unit
) {
    val p = appPalette(isDark)
    val accent = p.ac

    val totalVolume = exercises.sumOf { it.greutateKg * it.repetari }
    val totalSets = exercises.size
    val totalReps = exercises.sumOf { it.repetari }
    val maxWeight = exercises.maxOfOrNull { it.greutateKg } ?: 0.0
    val uniqueExercises = exercises.map { it.numeExercitiu }.distinct().size
    val durationMin = workout.durationMs / 60000
    val durationSec = (workout.durationMs % 60000) / 1000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.8f),
                            accent.copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    strings.workoutCompleted ?: "Workout Completed!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    LanguageManager.translateMuscleGroup(workout.grupaMusculara, strings),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryStatCard(
                isDark = isDark,
                icon = Icons.Default.Timer,
                label = strings.duration ?: "Duration",
                value = if (workout.durationMs > 0) "${durationMin}:${String.format("%02d", durationSec)}" else "-",
                accent = accent,
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                isDark = isDark,
                icon = Icons.Default.FitnessCenter,
                label = strings.volume ?: "Volume",
                value = "${String.format("%.0f", totalVolume)} kg",
                accent = accent,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryStatCard(
                isDark = isDark,
                icon = Icons.Default.Repeat,
                label = strings.sets ?: "Sets",
                value = "$totalSets",
                accent = accent,
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                isDark = isDark,
                icon = Icons.Default.Numbers,
                label = strings.reps ?: "Reps",
                value = "$totalReps",
                accent = accent,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryStatCard(
                isDark = isDark,
                icon = Icons.Default.ArrowUpward,
                label = strings.maxWeight ?: "Max Weight",
                value = "${String.format("%.1f", maxWeight)} kg",
                accent = accent,
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                isDark = isDark,
                icon = Icons.Default.List,
                label = strings.exercises ?: "Exercises",
                value = "$uniqueExercises",
                accent = accent,
                modifier = Modifier.weight(1f)
            )
        }

        if (prs.isNotEmpty()) {
            AppGlassCard(
                modifier = Modifier.fillMaxWidth(),
                p = p,
                cornerRadius = 16.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        androidx.compose.ui.res.painterResource(R.drawable.trophy_star),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${strings.newPRs ?: "New PRs"}: ${prs.size}",
                        color = p.tp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                prs.forEach { pr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pr.exerciseName, color = p.tp, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(
                            "${String.format("%.1f", pr.weight)} kg x ${pr.reps}",
                            color = Color(0xFFFFC107),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        AppGlassCard(
            modifier = Modifier.fillMaxWidth(),
            p = p,
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(strings.exerciseBreakdown ?: "Exercise Breakdown", color = p.tp, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            exercises.groupBy { it.numeExercitiu }.forEach { (name, sets) ->
                val exerciseVolume = sets.sumOf { it.greutateKg * it.repetari }
                val exerciseMax = sets.maxOfOrNull { it.greutateKg } ?: 0.0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = p.tp, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${sets.size} ${strings.sets ?: "sets"} | ${String.format("%.0f", exerciseVolume)} kg vol",
                            color = p.ts,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        "${String.format("%.1f", exerciseMax)} kg",
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .background(RedButtonGradient, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(strings.done ?: "Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SummaryStatCard(
    isDark: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val p = appPalette(isDark)

    AppGlassCard(
        modifier = modifier,
        p = p,
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                color = p.tp,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                label,
                color = p.ts,
                fontSize = 11.sp
            )
        }
    }
}
