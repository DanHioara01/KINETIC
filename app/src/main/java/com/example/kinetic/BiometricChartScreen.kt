package com.example.kinetic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricChartScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    entries: List<BiometricEntity>,
    onDelete: (BiometricEntity) -> Unit,
    onBack: () -> Unit
) {
    val p = appPalette(isDark)

    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf<BiometricEntity?>(null) }

    val sortedEntries = remember(entries) { entries.sortedBy { it.timestamp } }
    val last4 = remember(sortedEntries) { sortedEntries.takeLast(4) }

    val weightData = remember(last4) { last4.map { dateFormat.format(Date(it.timestamp)) to it.weightKg } }
    val bodyFatData = remember(last4) { last4.filter { it.bodyFatPercent > 0 }.map { dateFormat.format(Date(it.timestamp)) to it.bodyFatPercent } }
    val waistData = remember(last4) { last4.filter { it.waistCm > 0 }.map { dateFormat.format(Date(it.timestamp)) to it.waistCm } }
    val hipsData = remember(last4) { last4.filter { it.hipsCm > 0 }.map { dateFormat.format(Date(it.timestamp)) to it.hipsCm } }
    val thighsData = remember(last4) { last4.filter { it.thighsCm > 0 }.map { dateFormat.format(Date(it.timestamp)) to it.thighsCm } }
    val chestData = remember(last4) { last4.filter { it.chestCm > 0 }.map { dateFormat.format(Date(it.timestamp)) to it.chestCm } }
    val armsData = remember(last4) { last4.filter { it.armsCm > 0 }.map { dateFormat.format(Date(it.timestamp)) to it.armsCm } }

    val circumferenceData = remember(last4) {
        last4.filter { it.waistCm > 0 || it.hipsCm > 0 || it.chestCm > 0 || it.armsCm > 0 }
            .map { entry ->
                dateFormat.format(Date(entry.timestamp)) to
                        listOfNotNull(
                            entry.waistCm.takeIf { it > 0 },
                            entry.hipsCm.takeIf { it > 0 },
                            entry.chestCm.takeIf { it > 0 },
                            entry.armsCm.takeIf { it > 0 }
                        ).average()
            }
    }

    Scaffold(
        topBar = {
            KineticAppBar(onBack = onBack)
        },
        containerColor = p.bg
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(strings.noMeasurements, color = p.ts)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (weightData.size >= 2) {
                    item {
                        ChartCard(
                            p = p,
                            title = strings.weightChart,
                            data = weightData,
                            strings = strings
                        )
                    }
                }

                if (bodyFatData.size >= 2) {
                    item {
                        ChartCard(
                            p = p,
                            title = strings.bodyFatChart,
                            data = bodyFatData,
                            strings = strings
                        )
                    }
                }

                if (circumferenceData.size >= 2) {
                    item {
                        ChartCard(
                            p = p,
                            title = strings.circumferenceChart,
                            data = circumferenceData,
                            strings = strings
                        )
                    }
                }

                item {
                    Text(
                        strings.biometricHistory,
                        style = MaterialTheme.typography.titleMedium,
                        color = p.tp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(sortedEntries.reversed()) { entry ->
                    BiometricHistoryCard(
                        p = p,
                        entry = entry,
                        dateFormat = dateFormat,
                        strings = strings,
                        onDelete = { showDeleteDialog = entry }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    showDeleteDialog?.let { entry ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = p.card,
            titleContentColor = p.tp,
            textContentColor = p.ts,
            title = { Text(strings.deleteMeasurement) },
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

@Composable
private fun ChartCard(
    p: AppPalette,
    title: String,
    data: List<Pair<String, Double>>,
    strings: LanguageManager.Strings
) {
    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        p = p,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = p.tp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LineChart(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                lineColor = p.ac,
                dotColor = p.ac
            )
        }
    }
}

@Composable
private fun BiometricHistoryCard(
    p: AppPalette,
    entry: BiometricEntity,
    dateFormat: SimpleDateFormat,
    strings: LanguageManager.Strings,
    onDelete: () -> Unit
) {
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
                Text(
                    dateFormat.format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.titleMedium,
                    color = p.tp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = p.ts)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (entry.weightKg > 0) BiometricRow(p = p, label = strings.weight, value = "${entry.weightKg} kg")
            if (entry.bodyFatPercent > 0) BiometricRow(p = p, label = strings.bodyFat, value = "${entry.bodyFatPercent}%")
            if (entry.waistCm > 0) BiometricRow(p = p, label = strings.waistCirc, value = "${entry.waistCm} cm")
            if (entry.hipsCm > 0) BiometricRow(p = p, label = strings.hipsCirc, value = "${entry.hipsCm} cm")
            if (entry.thighsCm > 0) BiometricRow(p = p, label = strings.thighsCirc, value = "${entry.thighsCm} cm")
            if (entry.chestCm > 0) BiometricRow(p = p, label = strings.chestCirc, value = "${entry.chestCm} cm")
            if (entry.armsCm > 0) BiometricRow(p = p, label = strings.armsCirc, value = "${entry.armsCm} cm")
        }
    }
}

@Composable
private fun BiometricRow(
    p: AppPalette,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = p.ts, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = p.tp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}
