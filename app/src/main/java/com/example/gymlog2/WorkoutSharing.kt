package com.example.gymlog2

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*

object WorkoutShareFormatter {

    fun formatWorkoutShare(
        workout: AntrenamentEntity,
        exercises: List<ExercitiuEntity>,
        prs: List<PersonalRecordEntity>,
        context: Context
    ): String {
        val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val dateStr = dateFormat.format(java.util.Date(workout.data))
        val unit = "kg"

        val sb = StringBuilder()
        sb.appendLine("Workout - ${workout.grupaMusculara}")
        sb.appendLine("$dateStr")
        sb.appendLine("Total Volume: ${String.format("%.0f", workout.totalWeight)} $unit")
        if (workout.notes.isNotBlank()) {
            sb.appendLine("Notes: ${workout.notes}")
        }
        sb.appendLine()
        sb.appendLine("---")

        val grouped = exercises.groupBy { it.numeExercitiu }
        for ((name, sets) in grouped) {
            sb.appendLine()
            sb.appendLine(name)
            val bestSet = sets.maxByOrNull { it.greutateKg }
            sets.forEachIndexed { idx, set ->
                sb.appendLine("  Set ${idx + 1}: ${String.format("%.1f", set.greutateKg)} $unit x ${set.repetari}")
            }
            if (bestSet != null) {
                val vol = bestSet.greutateKg * bestSet.repetari
                sb.appendLine("  Best: ${String.format("%.1f", bestSet.greutateKg)} $unit x ${bestSet.repetari} (${String.format("%.0f", vol)} $unit vol)")
            }
        }

        if (prs.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine("Personal Records Set:")
            prs.forEach { pr ->
                sb.appendLine("  ${pr.exerciseName}: ${String.format("%.1f", pr.weight)} $unit x ${pr.reps}")
            }
        }

        sb.appendLine()
        sb.appendLine("Trained with Kinetic")

        return sb.toString()
    }

    fun shareWorkout(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "My Workout")
        }
        context.startActivity(Intent.createChooser(intent, "Share workout"))
    }
}

@Composable
fun ShareWorkoutDialog(
    isDark: Boolean,
    workout: AntrenamentEntity,
    exercises: List<ExercitiuEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cardBg = if (isDark) cardColor() else LightCard
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val accent = if (isDark) accentColor() else LightPrimaryRed

    val shareText = remember(workout, exercises) {
        WorkoutShareFormatter.formatWorkoutShare(workout, exercises, emptyList(), context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        titleContentColor = textPrimary,
        title = { Text("Share Workout", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Preview:", color = textSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = textPrimary.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        shareText,
                        modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()).heightIn(max = 300.dp),
                        color = textPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    WorkoutShareFormatter.shareWorkout(context, shareText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Share", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = accent)
            }
        }
    )
}
