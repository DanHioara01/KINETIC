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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.*

data class BodyFatResult(
    val method: String,
    val bodyFatPercent: Double,
    val category: String,
    val description: String
)

fun calculateBodyFatNavy(
    gender: String,
    waistCm: Double,
    neckCm: Double,
    heightCm: Double,
    hipsCm: Double = 0.0
): Double? {
    if (waistCm <= 0 || neckCm <= 0 || heightCm <= 0) return null
    return when (gender.lowercase()) {
        "male" -> {
            val logVal = (waistCm - neckCm) / (1.0324 - 0.19077 * (waistCm / heightCm) * 100 + 0.15456 * (neckCm / heightCm) * 100)
            495 / (1.29579 - 0.35004 * logVal) - 450
        }
        "female" -> {
            if (hipsCm <= 0) return null
            val logVal = (waistCm + hipsCm - neckCm) / (1.29579 - 0.35004 * ((waistCm + hipsCm - neckCm) / heightCm) * 100)
            495 / (1.29579 - 0.35004 * logVal) - 450
        }
        else -> null
    }
}

fun calculateBodyFatBMIZ(gender: String, age: Int, bmi: Double): Double? {
    if (age <= 0 || bmi <= 0) return null
    return when (gender.lowercase()) {
        "male" -> 1.20 * bmi + 0.23 * age - 16.2
        "female" -> 1.20 * bmi + 0.23 * age - 5.4
        else -> null
    }
}

fun getBodyFatCategory(gender: String, percent: Double): String {
    return when (gender.lowercase()) {
        "male" -> when {
            percent < 6 -> "Essential Fat"
            percent < 14 -> "Athletes"
            percent < 18 -> "Fitness"
            percent < 25 -> "Average"
            else -> "Obese"
        }
        "female" -> when {
            percent < 14 -> "Essential Fat"
            percent < 21 -> "Athletes"
            percent < 25 -> "Fitness"
            percent < 32 -> "Average"
            else -> "Obese"
        }
        else -> "Unknown"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyFatCalculatorScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    latestWeight: Double = 0.0,
    heightCm: Float = 0f,
    onBack: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val p = appPalette(isDark)

    var gender by remember { mutableStateOf("male") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf(if (heightCm > 0) String.format("%.0f", heightCm) else "") }
    var weight by remember { mutableStateOf(if (latestWeight > 0) String.format("%.1f", latestWeight) else "") }
    var waist by remember { mutableStateOf("") }
    var neck by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableIntStateOf(0) }
    var showResult by remember { mutableStateOf(false) }

    val ageVal = age.toIntOrNull() ?: 0
    val heightVal = height.toDoubleOrNull() ?: 0.0
    val weightVal = weight.toDoubleOrNull() ?: 0.0
    val waistVal = waist.toDoubleOrNull() ?: 0.0
    val neckVal = neck.toDoubleOrNull() ?: 0.0
    val hipsVal = hips.toDoubleOrNull() ?: 0.0

    val bmi = if (heightVal > 0 && weightVal > 0) weightVal / ((heightVal / 100) * (heightVal / 100)) else 0.0

    val navyResult = if (selectedMethod == 0) calculateBodyFatNavy(gender, waistVal, neckVal, heightVal, hipsVal) else null
    val bmiResult = if (selectedMethod == 1) calculateBodyFatBMIZ(gender, ageVal, bmi) else null

    val result = if (showResult) navyResult ?: bmiResult else null
    val category = result?.let { getBodyFatCategory(gender, it) } ?: ""

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
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(strings.method ?: "Method", color = p.ts, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedMethod == 0,
                            onClick = { selectedMethod = 0; showResult = false },
                            label = { Text(strings.navy, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = p.ac.copy(alpha = 0.25f),
                                selectedLabelColor = p.ac
                            )
                        )
                        FilterChip(
                            selected = selectedMethod == 1,
                            onClick = { selectedMethod = 1; showResult = false },
                            label = { Text(strings.bmi, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = p.ac.copy(alpha = 0.25f),
                                selectedLabelColor = p.ac
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = p.ts.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(strings.gender ?: "Gender", color = p.ts, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = gender == "male",
                            onClick = { gender = "male"; showResult = false },
                            label = { Text(strings.male ?: "Male", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = p.ac.copy(alpha = 0.25f),
                                selectedLabelColor = p.ac
                            )
                        )
                        FilterChip(
                            selected = gender == "female",
                            onClick = { gender = "female"; showResult = false },
                            label = { Text(strings.female ?: "Female", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = p.ac.copy(alpha = 0.25f),
                                selectedLabelColor = p.ac
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = p.ts.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.age ?: "Age", color = p.ts, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it.filter { c -> c.isDigit() }; showResult = false },
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.heightCm ?: "Height (cm)", color = p.ts, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it.filter { c -> c.isDigit() || c == '.' }; showResult = false },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                suffix = { Text("cm", color = p.ts) },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.weightKgLabel ?: "Weight (kg)", color = p.ts, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' }; showResult = false },
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
                            Text(strings.waistCm ?: "Waist (cm)", color = p.ts, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = waist,
                                onValueChange = { waist = it.filter { c -> c.isDigit() || c == '.' }; showResult = false },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                suffix = { Text("cm", color = p.ts) },
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

                    if (selectedMethod == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.neckCm ?: "Neck (cm)", color = p.ts, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = neck,
                                    onValueChange = { neck = it.filter { c -> c.isDigit() || c == '.' }; showResult = false },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    suffix = { Text("cm", color = p.ts) },
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
                            if (gender == "female") {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(strings.hipsCm ?: "Hips (cm)", color = p.ts, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = hips,
                                        onValueChange = { hips = it.filter { c -> c.isDigit() || c == '.' }; showResult = false },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        suffix = { Text("cm", color = p.ts) },
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
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { showResult = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.calculate ?: "Calculate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (showResult && result != null) {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(strings.estimatedBodyFat ?: "Estimated Body Fat", color = p.tp, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${String.format("%.1f", result)}%",
                            color = p.ac,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        Text(
                            category,
                            color = when {
                                category.contains("Athlete", true) || category.contains("Fitness", true) -> Color(0xFF2E7D32)
                                category.contains("Average", true) -> Color(0xFFFFA000)
                                else -> Color(0xFFE53935)
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            strings.navyMethodInfo ?: "Based on the ${if (selectedMethod == 0) "Navy" else "BMI"} method",
                            color = p.ts,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppConstants.BOTTOM_NAV_PADDING))
        }
    }
}
