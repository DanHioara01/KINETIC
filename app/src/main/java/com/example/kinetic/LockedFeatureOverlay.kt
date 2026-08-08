package com.example.kinetic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.theme.GoldPR
import com.example.kinetic.ui.theme.RecoveryGreen
import kotlinx.coroutines.delay

/**
 * Bottom sheet offering the free user two ways to access a premium feature:
 * watch a rewarded ad (temporary unlock) or upgrade to a paid plan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumUnlockSheet(
    feature: PremiumFeature,
    strings: LanguageManager.Strings,
    isAdReady: Boolean,
    onWatchAd: () -> Unit,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = GoldPR, modifier = Modifier.size(44.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                strings.premiumFeature.ifBlank { "Premium Feature" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                strings.subscribersOnly.replace("\$feature", "").trim().ifBlank { strings.upgradeToUnlock },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onWatchAd,
                enabled = isAdReady,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PlayCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isAdReady) strings.watchAdToUnlock else strings.adNotReady)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPR),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.subscribeNow, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Small live countdown showing how long an ad-based unlock remains active.
 * Recomputes every minute via a LaunchedEffect.
 */
@Composable
fun UnlockCountdown(
    unlockedUntil: Long,
    strings: LanguageManager.Strings,
    modifier: Modifier = Modifier
) {
    var remaining by remember(unlockedUntil) { mutableLongStateOf(unlockedUntil - System.currentTimeMillis()) }

    LaunchedEffect(unlockedUntil) {
        while (remaining > 0) {
            delay(60_000L)
            remaining = unlockedUntil - System.currentTimeMillis()
        }
    }

    if (remaining > 0) {
        val hours = (remaining / 3_600_000L).toInt()
        val minutes = ((remaining % 3_600_000L) / 60_000L).toInt()
        val timeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        Text(
            text = strings.unlockedForMinutes.ifBlank { "Unlocked: %s left" }.replace("%s", timeText),
            color = RecoveryGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = modifier
        )
    }
}
