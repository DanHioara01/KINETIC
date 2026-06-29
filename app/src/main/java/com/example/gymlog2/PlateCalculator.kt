package com.example.gymlog2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*

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
    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed

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
            TopAppBar(
                title = { Text("Plate Calculator", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Target Weight ($unit)", color = textSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputWeight,
                            onValueChange = { inputWeight = it.filter { c -> c.isDigit() || c == '.' } },
                            placeholder = { Text("e.g. ${String.format("%.0f", 100.0 * conversionFactor)}", color = textSecondary.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            suffix = { Text(unit, color = textSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accent,
                                unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                                cursorColor = accent,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
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
                                Text("Bar Weight ($unit)", color = textSecondary, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = barWeight,
                                    onValueChange = { barWeight = it.filter { c -> c.isDigit() || c == '.' } },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    suffix = { Text(unit, color = textSecondary) },
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
                            Column {
                                Spacer(Modifier.height(18.dp))
                                FilterChip(
                                    selected = isLbs,
                                    onClick = { isLbs = !isLbs },
                                    label = { Text("lbs/kg") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accent.copy(alpha = 0.2f),
                                        selectedLabelColor = accent
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
                        "Plates per side",
                        color = textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                items(plates) { plate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(plate.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${plate.weightKg}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${plate.weightKg} $unit plate",
                                color = textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "${plate.count}x each side (${plate.count * 2} total)",
                                color = textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total", color = textPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                "${String.format("%.1f", targetWeight)} $unit",
                                color = accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            } else if (targetWeight > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Weight too light for plates (only bar: $bar $unit)",
                            modifier = Modifier.padding(16.dp),
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
