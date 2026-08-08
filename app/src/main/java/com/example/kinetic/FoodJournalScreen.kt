package com.example.kinetic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.*
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.AppPalette
import java.text.SimpleDateFormat
import java.util.*

data class DailyMacros(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodJournalScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    entries: List<FoodEntity>,
    onDelete: (FoodEntity) -> Unit,
    onScanBarcode: () -> Unit,
    onAddManual: () -> Unit,
    onBack: () -> Unit,
    preferencesManager: PreferencesManager
) {
    val p = appPalette(isDark)

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val tomorrow = remember { today + 86400000L }

    val todayEntries = remember(entries) {
        entries.filter { it.timestamp in today until tomorrow }
    }

    val todayMacros = remember(todayEntries) {
        DailyMacros(
            calories = todayEntries.sumOf { it.calories },
            protein = todayEntries.sumOf { it.proteinG },
            carbs = todayEntries.sumOf { it.carbsG },
            fat = todayEntries.sumOf { it.fatG },
            fiber = todayEntries.sumOf { it.fiberG }
        )
    }

    val nutritionTargets = remember { NutritionCalculator.getTargets(preferencesManager) }

    val mealGroups = remember(todayEntries) {
        todayEntries.groupBy { it.mealType }
    }

    Scaffold(
        topBar = {
            KineticAppBar(onBack = onBack)
        },
        containerColor = p.bg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MacrosOverviewCard(
                    isDark = isDark,
                    strings = strings,
                    macros = todayMacros,
                    targets = nutritionTargets,
                    p = p
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onScanBarcode,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = p.ac)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.scan, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onAddManual,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = p.ac),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.add, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (mealGroups.isNotEmpty()) {
                val mealOrder = listOf("breakfast", "lunch", "dinner", "snack", "drinks")
                mealOrder.forEach { mealType ->
                    val meals = mealGroups[mealType]
                    if (!meals.isNullOrEmpty()) {
                        item {
                            MealSection(
                                isDark = isDark,
                                strings = strings,
                                mealType = mealType,
                                entries = meals,
                                dateFormat = dateFormat,
                                timeFormat = timeFormat,
                                p = p,
                                onDelete = onDelete
                            )
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            strings.noFoodEntries,
                            color = p.ts,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacrosOverviewCard(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    macros: DailyMacros,
    targets: NutritionTargets,
    p: AppPalette
) {
    val remainingCalories = (targets.calories - macros.calories).coerceAtLeast(0.0)
    val hasTargets = targets.calories > 0

    // Tema: dark → card negru premium; light → card alb cu border subtil
    val cardBg = if (isDark) Color(0xFF1A1A1A) else LightCard
    val primaryText = if (isDark) Color.White else LightTextPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .then(
                if (isDark) Modifier
                else Modifier.border(1.dp, LightDividerGray, RoundedCornerShape(16.dp))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(p.ac)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
            ) {
                Text(
                    "DAILY INTAKE",
                    color = p.ts,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        String.format("%.0f", macros.calories),
                        color = primaryText,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "KCAL",
                        color = p.ac,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                if (hasTargets) {
                    Text(
                        "${String.format("%.1f", remainingCalories)} remaining",
                        color = p.ts,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                MacroRow(
                    isDark = isDark,
                    icon = R.drawable.ic_calories_macro,
                    label = strings.caloriesLabel,
                    value = String.format("%.0f", macros.calories),
                    target = if (hasTargets) String.format("%d", targets.calories) else "",
                    progress = if (hasTargets && targets.calories > 0) (macros.calories / targets.calories).coerceIn(0.0, 1.0) else 0.0,
                    barColor = p.ac
                )

                Spacer(modifier = Modifier.height(12.dp))

                MacroRow(
                    isDark = isDark,
                    icon = R.drawable.ic_protein_macro,
                    label = strings.proteinLabel,
                    value = String.format("%.0f", macros.protein),
                    target = if (hasTargets) String.format("%.0f", targets.proteinG) else "",
                    progress = if (hasTargets && targets.proteinG > 0) (macros.protein / targets.proteinG).coerceIn(0.0, 1.0) else 0.0,
                    // Cafeniu deschis — aceeași culoare în dark și light mode
                    barColor = Color(0xFFC29A6D)
                )

                Spacer(modifier = Modifier.height(12.dp))

                MacroRow(
                    isDark = isDark,
                    icon = R.drawable.ic_carbs_macro,
                    label = strings.carbsLabel,
                    value = String.format("%.0f", macros.carbs),
                    target = if (hasTargets) String.format("%.0f", targets.carbsG) else "",
                    progress = if (hasTargets && targets.carbsG > 0) (macros.carbs / targets.carbsG).coerceIn(0.0, 1.0) else 0.0,
                    barColor = Color(0xFFFF9800)
                )

                Spacer(modifier = Modifier.height(12.dp))

                MacroRow(
                    isDark = isDark,
                    icon = R.drawable.ic_fat_macro,
                    label = strings.fatLabel,
                    value = String.format("%.0f", macros.fat),
                    target = if (hasTargets) String.format("%.0f", targets.fatG) else "",
                    progress = if (hasTargets && targets.fatG > 0) (macros.fat / targets.fatG).coerceIn(0.0, 1.0) else 0.0,
                    barColor = Color(0xFFFFD600)
                )
            }
        }
    }
}

@Composable
private fun MacroRow(
    isDark: Boolean,
    icon: Int,
    label: String,
    value: String,
    target: String,
    progress: Double,
    barColor: Color
) {
    val primaryText = if (isDark) Color.White else LightTextPrimary
    val secondaryText = if (isDark) Color(0xFF888888) else LightTextSecondary
    val trackColor = if (isDark) Color(0xFF333333) else LightDividerGray

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Container colorat în culoarea macro-ului, cu iconiță mare tintuită
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(barColor.copy(alpha = if (isDark) 0.16f else 0.13f))
                    .border(
                        width = 1.dp,
                        color = barColor.copy(alpha = if (isDark) 0.45f else 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = barColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                label,
                color = primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = primaryText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (target.isNotBlank()) {
                    Text(" / $target", color = secondaryText, fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 58.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun MealSection(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    mealType: String,
    entries: List<FoodEntity>,
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat,
    p: AppPalette,
    onDelete: (FoodEntity) -> Unit
) {
    val mealName = when (mealType) {
        "breakfast" -> strings.breakfast
        "lunch" -> strings.lunch
        "dinner" -> strings.dinner
        "drinks" -> strings.drinks
        else -> strings.snack
    }

    val mealIcon = when (mealType) {
        "breakfast" -> Icons.Default.WbSunny
        "lunch" -> Icons.Default.WbCloudy
        "dinner" -> Icons.Default.NightsStay
        "drinks" -> Icons.Default.LocalBar
        else -> Icons.Default.Cookie
    }

    var showDeleteDialog by remember { mutableStateOf<FoodEntity?>(null) }

    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        p = p,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(mealIcon, contentDescription = null, tint = p.ac, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(mealName, color = p.tp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    "${entries.sumOf { it.calories }} kcal",
                    color = p.ac,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = entry }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.name, color = p.tp, fontWeight = FontWeight.Medium)
                        if (entry.brand.isNotBlank()) {
                            Text(entry.brand, color = p.ts, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            "${timeFormat.format(Date(entry.timestamp))} · ${String.format("%.0f", entry.calories)} kcal · P:${String.format("%.1f", entry.proteinG)} · C:${String.format("%.1f", entry.carbsG)} · F:${String.format("%.1f", entry.fatG)}",
                            color = p.ts,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = entry }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = p.ts, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { entry ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(strings.delete) },
            text = { Text(strings.confirm + "?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(entry)
                    showDeleteDialog = null
                }) {
                    Text(strings.delete, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(strings.cancel, color = p.ac)
                }
            }
        )
    }
}
