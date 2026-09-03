package com.example.kinetic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.AppPalette
import com.example.kinetic.ui.theme.appPalette
import com.example.kinetic.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricInputScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    latestEntry: BiometricEntity?,
    onSave: (
        Double, Double, Double, Double, Double, Double, Double
    ) -> Unit,
    onBack: () -> Unit
) {
    val p = appPalette(isDark)

    var weight by remember { mutableStateOf(latestEntry?.weightKg?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else String.format(java.util.Locale.ROOT, "%.1f", it) } ?: "") }
    var bodyFat by remember { mutableStateOf(latestEntry?.bodyFatPercent?.let { if (it > 0) String.format(java.util.Locale.ROOT, "%.1f", it) else "" } ?: "") }
    var waist by remember { mutableStateOf(latestEntry?.waistCm?.let { if (it > 0) String.format(java.util.Locale.ROOT, "%.1f", it) else "" } ?: "") }
    var hips by remember { mutableStateOf(latestEntry?.hipsCm?.let { if (it > 0) String.format(java.util.Locale.ROOT, "%.1f", it) else "" } ?: "") }
    var thighs by remember { mutableStateOf(latestEntry?.thighsCm?.let { if (it > 0) String.format(java.util.Locale.ROOT, "%.1f", it) else "" } ?: "") }
    var chest by remember { mutableStateOf(latestEntry?.chestCm?.let { if (it > 0) String.format(java.util.Locale.ROOT, "%.1f", it) else "" } ?: "") }
    var arms by remember { mutableStateOf(latestEntry?.armsCm?.let { if (it > 0) String.format(java.util.Locale.ROOT, "%.1f", it) else "" } ?: "") }

    Scaffold(
        topBar = {
            KineticAppBar(
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        val w = weight.toDoubleOrNull() ?: 0.0
                        val bf = bodyFat.toDoubleOrNull() ?: 0.0
                        val wa = waist.toDoubleOrNull() ?: 0.0
                        val hi = hips.toDoubleOrNull() ?: 0.0
                        val th = thighs.toDoubleOrNull() ?: 0.0
                        val ch = chest.toDoubleOrNull() ?: 0.0
                        val ar = arms.toDoubleOrNull() ?: 0.0
                        onSave(w, bf, wa, hi, th, ch, ar)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = p.ac, modifier = Modifier.size(28.dp))
                    }
                }
            )
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

            BiometricInputCard(
                p = p,
                title = strings.weight,
                value = weight,
                onValueChange = { weight = it },
                unit = strings.kg,
                keyboardType = KeyboardType.Decimal,
                strings = strings
            )

            BiometricInputCard(
                p = p,
                title = strings.bodyFat,
                value = bodyFat,
                onValueChange = { bodyFat = it },
                unit = strings.percent,
                keyboardType = KeyboardType.Decimal,
                strings = strings
            )

            BiometricInputCard(
                p = p,
                title = strings.waistCirc,
                value = waist,
                onValueChange = { waist = it },
                unit = strings.cm,
                keyboardType = KeyboardType.Decimal,
                strings = strings
            )

            BiometricInputCard(
                p = p,
                title = strings.hipsCirc,
                value = hips,
                onValueChange = { hips = it },
                unit = strings.cm,
                keyboardType = KeyboardType.Decimal,
                strings = strings
            )

            BiometricInputCard(
                p = p,
                title = strings.thighsCirc,
                value = thighs,
                onValueChange = { thighs = it },
                unit = strings.cm,
                keyboardType = KeyboardType.Decimal,
                strings = strings
            )

            BiometricInputCard(
                p = p,
                title = strings.chestCirc,
                value = chest,
                onValueChange = { chest = it },
                unit = strings.cm,
                keyboardType = KeyboardType.Decimal,
                strings = strings
            )

            BiometricInputCard(
                p = p,
                title = strings.armsCirc,
                value = arms,
                onValueChange = { arms = it },
                unit = strings.cm,
                keyboardType = KeyboardType.Decimal,
                strings = strings
            )

            Spacer(modifier = Modifier.height(AppConstants.BOTTOM_NAV_PADDING))
        }
    }
}

@Composable
private fun BiometricInputCard(
    p: AppPalette,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    keyboardType: KeyboardType,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("0.0", color = p.ts.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = p.ac,
                        unfocusedBorderColor = p.ts.copy(alpha = 0.3f),
                        cursorColor = p.ac,
                        focusedTextColor = p.tp,
                        unfocusedTextColor = p.tp
                    )
                )
                Text(
                    unit,
                    color = p.ts,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
