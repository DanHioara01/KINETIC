package com.example.kinetic

/**
 * OneRM Calculator calculates your One Repetition Maximum (1RM) using multiple formulas.
 *
 * 1RM represents the maximum weight you can lift for one repetition.
 * The calculator provides estimates using 7 different formulas:
 * - Epley: weight * (1 + reps/30)
 * - Brzycki: weight * (36/(37 - reps))
 * - Lombardi: weight * reps^0.10
 * - O'Conner: weight * (1 + reps * 0.025)
 * - Mayhew: weight * (100 / (52.2 + 41.9 * exp(-0.055 * reps)))
 * - Wathen: weight * (100 / (48.8 + 53.8 * exp(-0.075 * reps)))
 * - Lander: weight * (100 / (101.3 - 2.67123 * reps))
 *
 * Usage: Input weight and reps to estimate 1RM and calculate intensity zones
 * for strength, hypertrophy, and endurance training.
 */

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.*
import com.example.kinetic.ui.theme.JetBrainsMono
import java.util.Locale

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
    val p = appPalette(isDark)

    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    val weightVal = weight.toDoubleOrNull() ?: 0.0
    val repsVal = reps.toIntOrNull() ?: 0
    val results = remember(weightVal, repsVal) { calculate1RM(weightVal, repsVal) }
    val best1RM = results.maxByOrNull { it.weight }?.weight ?: 0.0

    Scaffold(
        topBar = {
            KineticAppBar(onBack = onBack)
        },
        containerColor = p.bg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            AppGlassCard(
                modifier = Modifier.fillMaxWidth(),
                p = p,
                cornerRadius = 16.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.weightKgLabel, color = p.ts, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                                placeholder = { Text("e.g. 100", color = p.ts.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                suffix = { Text("kg", color = p.ts) },
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.repsLabel, color = p.ts, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = reps,
                                onValueChange = { reps = it.filter { c -> c.isDigit() } },
                                placeholder = { Text("e.g. 5", color = p.ts.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
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
                    }
                }
            }

            if (results.isNotEmpty()) {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(strings.estimated1rm, color = p.ts, fontSize = 13.sp)
                        Text(
                            "${String.format(Locale.US, "%.1f", best1RM)} kg",
                            color = p.ac,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            fontFamily = JetBrainsMono
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(strings.intensityZones, color = p.tp, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                val zones = listOf(
                    0.95 to strings.zoneStrength,
                    0.85 to strings.zoneStrengthHypertrophy,
                    0.75 to strings.zoneHypertrophy,
                    0.65 to strings.zoneHypertrophyEndurance,
                    0.55 to strings.zoneEndurance
                )

                zones.forEach { (pct, label) ->
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${(pct * 100).toInt()}%", color = p.ac, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = JetBrainsMono)
                                Text(label, color = p.ts, fontSize = 12.sp)
                            }
                            Text(
                                "${String.format(Locale.US, "%.1f", best1RM * pct)} kg",
                                color = p.tp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = JetBrainsMono
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(AppConstants.BOTTOM_NAV_PADDING))
        }
    }
}
