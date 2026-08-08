package com.example.kinetic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.theme.GoldPR
import com.example.kinetic.ui.theme.GrayText
import com.example.kinetic.ui.theme.WhiteText
import com.example.kinetic.ui.theme.appPalette

/**
 * Full-screen paywall shown instead of a locked premium screen.
 * Offers a rewarded-ad temporary unlock and a link to the pricing screen.
 */
@Composable
fun LockedFeatureScreen(
    feature: PremiumFeature,
    featureLabel: String,
    strings: LanguageManager.Strings,
    isAdReady: Boolean,
    onWatchAd: () -> Unit,
    onUpgrade: () -> Unit,
    onBack: () -> Unit
) {
    val p = appPalette(isSystemInDarkTheme())
    Box(modifier = Modifier.fillMaxSize().background(p.bg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            KineticAppBar(onBack = onBack)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldPR, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(18.dp))
            Text(featureLabel, color = p.tp, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                strings.premiumFeature.ifBlank { "Premium Feature" },
                color = GoldPR, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                strings.upgradeToUnlock,
                color = p.ts, fontSize = 13.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPR),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.subscribeNow, color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onWatchAd,
                enabled = isAdReady,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PlayCircle, contentDescription = null, tint = WhiteText, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isAdReady) strings.watchAdToUnlock else strings.adNotReady, color = WhiteText)
            }
        }
    }
    }
}

/**
 * Wraps premium content. When [hasAccess] is false and the feature is not free,
 * the content is blurred/dimmed and a paywall overlay is shown.
 */
@Composable
fun PremiumGate(
    feature: PremiumFeature,
    hasAccess: Boolean,
    strings: LanguageManager.Strings,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (feature.isFree || hasAccess) {
        content()
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.matchParentSize().blur(12.dp)) { content() }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.88f))
                    )
                )
                .clickable(onClick = onUnlockClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldPR, modifier = Modifier.size(52.dp))
                Spacer(Modifier.height(14.dp))
                Text(
                    strings.premiumFeature.ifBlank { "Premium" },
                    color = WhiteText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    strings.upgradeToUnlock,
                    color = WhiteText.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onUnlockClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPR),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.choosePlan.ifBlank { "View plans" }, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
