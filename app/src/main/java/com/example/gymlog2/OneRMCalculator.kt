package com.example.gymlog2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*

data class OneRMResult(
    val formula: String,
    val weight: Double
)

fun calculate1RM(weightKg: Double, reps: Int): List<OneRMResult> {
    if (reps <= 0 || weightKg <= 0) return emptyList()
    if (reps == 1) return listOf(OneRMResult("Actual 1RM", weightKg))

    return listOf(
        OneRMResult("Epley", weightKg * (1 + reps / 30.0)),
        OneRMResult("Brzycki", weightKg * (36.0 / (37.0 - reps))),
        OneRMResult("Lombardi", weightKg * Math.pow(reps.toDouble(), 0.10)),
        OneRMResult("O'Conner", weightKg * (1 + reps * 0.025)),
        OneRMResult("Mayhew", weightKg * (100.0 / (52.2 + 41.9 * Math.exp(-0.055 * reps)))),
        OneRMResult("Wathen", weightKg * (100.0 / (48.8 + 53.8 * Math.exp(-0.075 * reps)))),
        OneRMResult("Lander", weightKg * (100.0 / (101.3 - 2.67123 * reps)))
    )
}

fun getRepsForPercent(oneRm: Double, percent: Double): Int {
    val targetWeight = oneRm * percent
    return when {
        percent >= 0.95 -> 1
        percent >= 0.90 -> 2
        percent >= 0.85 -> 3
        percent >= 0.80 -> 4
        percent >= 0.75 -> 6
        percent >= 0.70 -> 8
        percent >= 0.65 -> 10
        percent >= 0.60 -> 12
        percent >= 0.55 -> 15
        else -> 20
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneRMCalculatorScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    onBack: () -> Unit
) {
    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed

    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    val weightVal = weight.toDoubleOrNull() ?: 0.0
    val repsVal = reps.toIntOrNull() ?: 0
    val results = remember(weightVal, repsVal) { calculate1RM(weightVal, repsVal) }
    val best1RM = results.maxByOrNull { it.weight }?.weight ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("1RM Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceBg)
            )
        },
        containerColor = surfaceBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Weight (kg)", color = textSecondary, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                                placeholder = { Text("e.g. 100", color = textSecondary.copy(alpha = 0.5f)) },
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
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reps", color = textSecondary, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = reps,
                                onValueChange = { reps = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("e.g. 5", color = textSecondary.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
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
                    }
                }
            }

            if (results.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Estimated 1RM", color = textSecondary, fontSize = 13.sp)
                        Text(
                            "${String.format("%.1f", best1RM)} kg",
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }
                }

                Text("Formulas", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                results.forEach { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(result.formula, color = textPrimary, fontWeight = FontWeight.Medium)
                            Text(
                                "${String.format("%.1f", result.weight)} kg",
                                color = accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Intensity Zones", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                val zones = listOf(
                    0.95 to "Strength (1-2 reps)",
                    0.85 to "Strength-Hypertrophy (3-5 reps)",
                    0.75 to "Hypertrophy (6-8 reps)",
                    0.65 to "Hypertrophy-Endurance (10-12 reps)",
                    0.55 to "Endurance (15+ reps)"
                )

                zones.forEach { (pct, label) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${(pct * 100).toInt()}%", color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(label, color = textSecondary, fontSize = 12.sp)
                            }
                            Text(
                                "${String.format("%.1f", best1RM * pct)} kg",
                                color = textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
