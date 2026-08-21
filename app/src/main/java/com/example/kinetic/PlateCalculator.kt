package com.example.kinetic

/**
 * Plate Calculator helps determine which barbell plates are needed for a target weight.
 *
 * The standard barbell setup includes:
 * - Bar weight: 20kg (default)
 * - Available plates: 25kg, 20kg, 15kg, 10kg, 5kg, 2.5kg, 1.25kg, 0.5kg, 0.25kg
 * - Each plate is placed on both sides of the bar
 *
 * Usage: Enter target weight and optional bar weight to see plate breakdown
 * for building the desired total weight. Shows plates per side with colors
 * corresponding to standard plate colors (red=25kg, blue=20kg, yellow=15kg, etc.).
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.*
import com.example.kinetic.ui.theme.JetBrainsMono
import java.util.Locale

data class PlateWeight(
    val weightKg: Double,
    val color: Color,
    val count: Int
)

fun calculatePlates(targetWeightKg: Double, barWeightKg: Double = 20.0): List<PlateWeight> {
    val plateWeights = listOf(
        25.0 to Color(0xFFD32F2F),
        20.0 to Color(0xFF1565C0),
        15.0 to Color(0xFFFFCA28),
        10.0 to Color(0xFF2E7D32),
        5.0 to Color(0xFF616161),
        2.5 to Color(0xFF9E9E9E),
        1.25 to Color(0xFFBDBDBD),
        0.5 to Color(0xFFE0E0E0),
        0.25 to Color(0xFFF5F5F5)
    )

    var remaining = (targetWeightKg - barWeightKg) / 2.0
    if (remaining < 0) return emptyList()

    val plates = mutableListOf<PlateWeight>()
    for ((weight, color) in plateWeights) {
        if (remaining >= weight - 0.001) {
            val count = (remaining / weight).toInt()
            if (count > 0) {
                plates.add(PlateWeight(weight, color, count))
                remaining -= count * weight
            }
        }
    }
    return plates
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateCalculatorScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    onBack: () -> Unit
) {
    val p = appPalette(isDark)

    var inputWeight by remember { mutableStateOf("") }
    var barWeight by remember { mutableStateOf("20") }
    var isLbs by remember { mutableStateOf(false) }
    val targetWeight = inputWeight.toDoubleOrNull() ?: 0.0
    val bar = barWeight.toDoubleOrNull() ?: 20.0
    val plates = remember(targetWeight, bar) { calculatePlates(targetWeight, bar) }
    val unit = if (isLbs) "lbs" else "kg"
    val conversionFactor = if (isLbs) 2.20462 else 1.0

    Scaffold(
        topBar = {
            KineticAppBar(onBack = onBack)
        },
        containerColor = p.bg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = AppConstants.BOTTOM_NAV_PADDING),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp
                ) {
                    Text(
                        strings.plateCalcNote,
                        color = p.ts,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            item {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    p = p,
                    cornerRadius = 16.dp
                ) {
                    Column {
                        Text("${strings.targetWeightLabel} ($unit)", color = p.ts, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputWeight,
                            onValueChange = { inputWeight = it.filter { c -> c.isDigit() || c == '.' } },
                            placeholder = { Text("e.g. ${String.format(Locale.US, "%.0f", 100.0 * conversionFactor)}", color = p.ts.copy(alpha = 0.5f), fontFamily = JetBrainsMono) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            suffix = { Text(unit, color = p.ts) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = p.ac,
                                unfocusedBorderColor = p.ts.copy(alpha = 0.3f),
                                cursorColor = p.ac,
                                focusedTextColor = p.tp,
                                unfocusedTextColor = p.tp
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${strings.barWeightLabel} ($unit)", color = p.ts, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = barWeight,
                                    onValueChange = { barWeight = it.filter { c -> c.isDigit() || c == '.' } },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    suffix = { Text(unit, color = p.ts) },
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
                            Column {
                                Spacer(Modifier.height(18.dp))
                                FilterChip(
                                    selected = isLbs,
                                    onClick = { isLbs = !isLbs },
                                    label = { Text(strings.lbsKg) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = p.ac.copy(alpha = 0.2f),
                                        selectedLabelColor = p.ac
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (plates.isNotEmpty()) {
                item {
                        Text(
                        strings.platesPerSide,
                        color = p.tp,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                items(plates) { plate ->
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(plate.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${plate.weightKg}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = JetBrainsMono
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${plate.weightKg} $unit ${strings.plateUnit}",
                                    color = p.tp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = JetBrainsMono
                                )
                                Text(
                                    "${plate.count}x ${strings.eachSide} (${plate.count * 2} total)",
                                    color = p.ts,
                                    fontSize = 13.sp,
                                    fontFamily = JetBrainsMono
                                )
                            }
                        }
                    }
                }

                item {
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
                            Text(strings.total, color = p.tp, fontWeight = FontWeight.Bold)
                            Text(
                                "${String.format(Locale.US, "%.1f", targetWeight)} $unit",
                                color = p.ac,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                fontFamily = JetBrainsMono
                            )
                        }
                    }
                }
            } else if (targetWeight > 0) {
                item {
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        p = p,
                        cornerRadius = 12.dp
                    ) {
                        Text(
                            "${strings.weightTooLight} $bar $unit)",
                            color = p.ts,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
