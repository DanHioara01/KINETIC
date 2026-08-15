package com.example.kinetic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kinetic.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreen(
    isDark: Boolean,
    currentTier: SubscriptionTier,
    pricingOptions: List<PricingOption>,
    strings: LanguageManager.Strings,
    onSelectPlan: (SubscriptionTier) -> Unit,
    onRestore: () -> Unit,
    onBack: () -> Unit,
    devMode: Boolean = false
) {
    val accent = LightPrimaryRed
    val gold = GoldPR
    val scroll = rememberScrollState()

    val surfaceBg = if (isDark) DarkBackground else LightBackground
    val textPrimary = if (isDark) WhiteText else LightTextPrimary
    val textSecondary = if (isDark) GrayText else LightTextSecondary

    val monthly = pricingOptions.firstOrNull { it.tier == SubscriptionTier.PREMIUM_MONTHLY }
    val annual = pricingOptions.firstOrNull { it.tier == SubscriptionTier.PREMIUM_ANNUAL }
    val lifetime = pricingOptions.firstOrNull { it.tier == SubscriptionTier.PRO_LIFETIME }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.subscription, fontWeight = FontWeight.Bold, color = textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = textPrimary)
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
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = gold, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(10.dp))
            Text(
                strings.unlockPremiumTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                strings.unlockPremiumSubtitle,
                color = textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // FREE
            PricingCard(
                title = strings.freePlan,
                price = strings.free,
                period = null,
                badge = null,
                features = listOf(
                    strings.workouts,
                    strings.dashboard.ifBlank { strings.acasa },
                    strings.calendarView,
                    strings.waterIntake
                ),
                highlighted = currentTier == SubscriptionTier.FREE,
                accent = accent,
                isDark = isDark,
                buttonText = if (currentTier == SubscriptionTier.FREE) strings.currentPlan else "",
                buttonEnabled = false,
                onClick = {}
            )
            Spacer(Modifier.height(14.dp))

            // PREMIUM MONTHLY
            PricingCard(
                title = strings.premium,
                price = monthly?.priceText ?: "4.99€",
                period = strings.perMonth,
                badge = null,
                features = listOf(
                    strings.aiTrainer,
                    strings.workoutAnalytics.ifBlank { "Analytics" },
                    strings.foodJournal,
                    strings.gpsCardioMap.ifBlank { "GPS Cardio" },
                    strings.friends,
                    strings.restDaysTitle.ifBlank { "Rest Days" }
                ),
                highlighted = currentTier == SubscriptionTier.PREMIUM_MONTHLY,
                accent = accent,
                isDark = isDark,
                buttonText = if (currentTier == SubscriptionTier.PREMIUM_MONTHLY) strings.currentPlan else strings.subscribe,
                buttonEnabled = (monthly != null || devMode) && currentTier != SubscriptionTier.PREMIUM_MONTHLY,
                onClick = { onSelectPlan(SubscriptionTier.PREMIUM_MONTHLY) }
            )
            Spacer(Modifier.height(14.dp))

            // PREMIUM ANNUAL
            PricingCard(
                title = "${strings.premium} • ${strings.perYear.trimStart('/')}",
                price = annual?.priceText ?: "39.99€",
                period = strings.perYear,
                badge = strings.bestValue,
                badgeColor = RecoveryGreen,
                features = listOf(
                    strings.aiTrainer,
                    strings.workoutAnalytics.ifBlank { "Analytics" },
                    strings.foodJournal,
                    strings.gpsCardioMap.ifBlank { "GPS Cardio" },
                    strings.friends
                ),
                highlighted = currentTier == SubscriptionTier.PREMIUM_ANNUAL,
                accent = accent,
                isDark = isDark,
                buttonText = if (currentTier == SubscriptionTier.PREMIUM_ANNUAL) strings.currentPlan else strings.subscribe,
                buttonEnabled = (annual != null || devMode) && currentTier != SubscriptionTier.PREMIUM_ANNUAL,
                onClick = { onSelectPlan(SubscriptionTier.PREMIUM_ANNUAL) }
            )
            Spacer(Modifier.height(14.dp))

            // PRO LIFETIME
            PricingCard(
                title = strings.permanentPlan,
                price = lifetime?.priceText ?: "49.99€",
                period = strings.oneTimePayment,
                badge = strings.mostPopular,
                badgeColor = gold,
                features = listOf(
                    strings.lifetimeAccess,
                    strings.aiTrainer,
                    strings.workoutAnalytics.ifBlank { "Analytics" },
                    strings.foodJournal,
                    strings.gpsCardioMap.ifBlank { "GPS Cardio" }
                ),
                highlighted = currentTier == SubscriptionTier.PRO_LIFETIME,
                accent = gold,
                isDark = isDark,
                buttonText = if (currentTier == SubscriptionTier.PRO_LIFETIME) strings.currentPlan else strings.buyNow,
                buttonEnabled = (lifetime != null || devMode) && currentTier != SubscriptionTier.PRO_LIFETIME,
                onClick = { onSelectPlan(SubscriptionTier.PRO_LIFETIME) }
            )

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = onRestore) {
                Text(strings.restorePurchase, color = textSecondary)
            }
            Text(
                strings.cancelAnytime,
                color = textSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PricingCard(
    title: String,
    price: String,
    period: String?,
    badge: String?,
    badgeColor: Color = LightPrimaryRed,
    features: List<String>,
    highlighted: Boolean,
    accent: Color,
    isDark: Boolean,
    buttonText: String,
    buttonEnabled: Boolean,
    onClick: () -> Unit
) {
    val bg = if (highlighted) accent.copy(alpha = 0.10f) else if (isDark) DarkCard else LightCard
    val cardTitle = if (isDark) WhiteText else LightTextPrimary
    val cardSecondary = if (isDark) GrayText else LightTextSecondary
    val cardBorder = if (isDark) DividerGray else LightDividerGray
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = if (highlighted) BorderStroke(2.dp, accent) else BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (badge != null && badge.isNotBlank()) {
                Box(
                    modifier = Modifier
                                                .background(badgeColor.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(badge, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
            }
            Text(title, color = cardTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(price, color = accent, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (!period.isNullOrBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(period, color = cardSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 5.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            features.forEach { f ->
                if (f.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(f, color = cardTitle.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }
            }
            if (buttonText.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    enabled = buttonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RedButtonGradient, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = cardBorder,
                        disabledContentColor = if (isDark) Color.White.copy(alpha = 0.6f) else LightTextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
