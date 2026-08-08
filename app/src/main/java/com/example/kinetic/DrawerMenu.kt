package com.example.kinetic

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kinetic.ui.theme.*
import kotlinx.coroutines.delay
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

enum class DrawerPage { CALENDAR, FOOD_JOURNAL, AI_TRAINER, FRIENDS, GPS_CARDIO, REST_DAYS, PLATE_CALCULATOR, ONE_RM_CALCULATOR, SAVED_EXERCISES, WEIGHT_GOAL, BODY_FAT_CALCULATOR, WORKOUT_ANALYTICS }

fun DrawerPage.toDrawerScreen(): DrawerScreen = when (this) {
    DrawerPage.CALENDAR -> DrawerScreen.Calendar
    DrawerPage.FOOD_JOURNAL -> DrawerScreen.FoodJournal
    DrawerPage.AI_TRAINER -> DrawerScreen.AiTrainer
    DrawerPage.FRIENDS -> DrawerScreen.Friends
    DrawerPage.GPS_CARDIO -> DrawerScreen.GpsCardio
    DrawerPage.REST_DAYS -> DrawerScreen.RestDays
    DrawerPage.PLATE_CALCULATOR -> DrawerScreen.Calendar
    DrawerPage.ONE_RM_CALCULATOR -> DrawerScreen.Calendar
    DrawerPage.SAVED_EXERCISES -> DrawerScreen.SavedExercises
    DrawerPage.WEIGHT_GOAL -> DrawerScreen.WeightGoal
    DrawerPage.BODY_FAT_CALCULATOR -> DrawerScreen.BodyFatCalculator
    DrawerPage.WORKOUT_ANALYTICS -> DrawerScreen.Calendar
}

fun DrawerScreen.toDrawerPage(): DrawerPage = when (this) {
    DrawerScreen.Calendar -> DrawerPage.CALENDAR
    DrawerScreen.FoodJournal -> DrawerPage.FOOD_JOURNAL
    DrawerScreen.AiTrainer -> DrawerPage.AI_TRAINER
    DrawerScreen.Friends -> DrawerPage.FRIENDS
    DrawerScreen.GpsCardio -> DrawerPage.GPS_CARDIO
    DrawerScreen.RestDays -> DrawerPage.REST_DAYS
    DrawerScreen.SavedExercises -> DrawerPage.SAVED_EXERCISES
    DrawerScreen.WeightGoal -> DrawerPage.WEIGHT_GOAL
    DrawerScreen.BodyFatCalculator -> DrawerPage.BODY_FAT_CALCULATOR
}

data class LanguageOption(
    val code: String,
    val name: String,
    val flag: String
)

fun getLanguageOptions(strings: LanguageManager.Strings) = listOf(
    LanguageOption("en", strings.englishUS, "\uD83C\uDDEC\uD83C\uDDE7"),
    LanguageOption("ro", strings.romana, "\uD83C\uDDF7\uD83C\uDDF4"),
    LanguageOption("ru", strings.russkiy, "\uD83C\uDDF7\uD83C\uDDFA"),
    LanguageOption("uk", strings.ukrainska, "\uD83C\uDDFA\uD83C\uDDE6"),
    LanguageOption("fr", strings.francais, "\uD83C\uDDEB\uD83C\uDDF7"),
    LanguageOption("de", strings.deutsch, "\uD83C\uDDE9\uD83C\uDDEA"),
    LanguageOption("es", strings.espanol, "\uD83C\uDDEA\uD83C\uDDF8"),
    LanguageOption("it", strings.italiano, "\uD83C\uDDEE\uD83C\uDDF9"),
    LanguageOption("tr", strings.turkce, "\uD83C\uDDF9\uD83C\uDDF7"),
    LanguageOption("pt", strings.portugues, "\uD83C\uDDE7\uD83C\uDDF7"),
    LanguageOption("pl", strings.polski, "\uD83C\uDDF5\uD83C\uDDF1")
)

@Composable
fun DrawerMenu(
    profileName: String,
    profilePhotoUri: String,
    profilePhotoVersion: Int = 0,
    userId: String,
    shortId: String = userId,
    currentPage: DrawerPage?,
    isLbs: Boolean,
    isDark: Boolean,
    currentLanguage: String,
    badgeCount: Int = 0,
    currentStreak: Int = 0,
    pendingRequestsCount: Int = 0,
    onNavigate: (DrawerPage) -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
    onLogout: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onOpenLanguageDialog: () -> Unit,
    onOpenUnitsDialog: () -> Unit,
    strings: LanguageManager.Strings,
    onClose: () -> Unit,
    onOpenServerSettings: () -> Unit = {},
    onOpenPricing: () -> Unit = {},
    isPremium: Boolean = false
) {
    val bg = if (isDark) DarkBackground else LightBackground
    val textPrimary = if (isDark) WhiteText else LightTextPrimary
    val textSecondary = if (isDark) GrayText else LightTextSecondary
    val divider = if (isDark) DividerGray else LightDividerGray
    val accent = if (isDark) LightRed else LightPrimaryRed
    val selectedBg = if (isDark) DrawerItemSelectedDark else DrawerItemSelectedLight
    val iconBg = if (isDark) IconBackground else LightIconBackground

    // ── Scroll state for depth effect ──
    val scrollState = rememberScrollState()
    val scrollProgress = (scrollState.value.toFloat() / (scrollState.maxValue.coerceAtLeast(1))).coerceIn(0f, 1f)

    // ── Haze state for frosted glass blur ──
    val hazeState = remember { HazeState() }

    ModalDrawerSheet(
        drawerContainerColor = Color.Transparent,
        modifier = Modifier
            .width(320.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF121212),
                            Color(0xFF0D0D0D)
                        )
                    } else {
                        listOf(
                            Color(0xFFE8E8E8),
                            Color(0xFFE0E0E0),
                            Color(0xFFD8D8D8)
                        )
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .hazeSource(state = hazeState)
                .padding(top = 16.dp)
        ) {
            // Profile Header (depth effect: fades based on scroll)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .graphicsLayer {
                        alpha = 1f - scrollProgress * 0.4f
                        translationY = scrollProgress * -8f
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (profilePhotoUri.isNotBlank()) {
                        AsyncImage(
                            // Sufix de cache-busting sigur pentru orice tip de URL (Firebase Storage / file://)
                            model = cacheBustedPhotoUrl(profilePhotoUri, profilePhotoVersion),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            if (isDark) Color(0xFFFF3C3C) else LightPrimaryRed,
                                            Color(0xFFFF6B4A)
                                        ),
                                        start = Offset.Zero,
                                        end = Offset(64f, 64f)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                profileName.split(" ").mapNotNull { it.firstOrNull() }
                                    .take(2).joinToString("").uppercase().ifBlank { "K" },
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            profileName.ifBlank { strings.guest },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "#$shortId",
                            style = MaterialTheme.typography.bodySmall,
                            color = textSecondary,
                            fontSize = 12.sp
                        )
                        if (badgeCount > 0) {
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\uD83C\uDFC6", fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "$badgeCount",
                                    color = accent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = divider, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(4.dp))

            // Features section
            GlassmorphismDrawerCard(
                isDark = isDark,
                headerLabel = strings.features,
                textSecondary = textSecondary,
                hazeState = hazeState
            ) {
                DrawerNavItem(
                    icon = Icons.Default.CalendarMonth,
                    label = strings.calendarView,
                    selected = currentPage == DrawerPage.CALENDAR,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.CALENDAR); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.Restaurant,
                    label = strings.foodJournal,
                    selected = currentPage == DrawerPage.FOOD_JOURNAL,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.FOOD_JOURNAL); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.Psychology,
                    label = strings.aiTrainer,
                    selected = currentPage == DrawerPage.AI_TRAINER,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.AI_TRAINER); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.People,
                    label = strings.friends,
                    selected = currentPage == DrawerPage.FRIENDS,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    badge = pendingRequestsCount,
                    onClick = { onNavigate(DrawerPage.FRIENDS); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.Bookmark,
                    label = strings.savedExercises,
                    selected = currentPage == DrawerPage.SAVED_EXERCISES,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.SAVED_EXERCISES); onClose() }
                )
            }

            // Activity section
            GlassmorphismDrawerCard(
                isDark = isDark,
                headerLabel = strings.activity,
                textSecondary = textSecondary,
                hazeState = hazeState
            ) {
                DrawerNavItem(
                    icon = Icons.Default.DirectionsRun,
                    label = strings.gpsCardioMap,
                    selected = currentPage == DrawerPage.GPS_CARDIO,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.GPS_CARDIO); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.Bedtime,
                    label = strings.restDaysTitle,
                    selected = currentPage == DrawerPage.REST_DAYS,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.REST_DAYS); onClose() }
                )
            }

            // Tools & Calculators
            GlassmorphismDrawerCard(
                isDark = isDark,
                headerLabel = strings.tools,
                textSecondary = textSecondary,
                hazeState = hazeState
            ) {
                DrawerNavItem(
                    icon = Icons.Default.Calculate,
                    label = "Plate Calculator",
                    selected = currentPage == DrawerPage.PLATE_CALCULATOR,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.PLATE_CALCULATOR); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "1RM Calculator",
                    selected = currentPage == DrawerPage.ONE_RM_CALCULATOR,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.ONE_RM_CALCULATOR); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.Flag,
                    label = strings.weightGoal ?: "Weight Goal",
                    selected = currentPage == DrawerPage.WEIGHT_GOAL,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.WEIGHT_GOAL); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.MonitorWeight,
                    label = strings.bodyFatCalculator ?: "Body Fat Calculator",
                    selected = currentPage == DrawerPage.BODY_FAT_CALCULATOR,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.BODY_FAT_CALCULATOR); onClose() }
                )
            }

            // Settings & More section
            GlassmorphismDrawerCard(
                isDark = isDark,
                headerLabel = strings.settingsAndMore,
                textSecondary = textSecondary,
                hazeState = hazeState
            ) {
                DrawerNavItem(
                    icon = Icons.Default.WorkspacePremium,
                    label = if (isPremium) strings.youAreSubscribed.ifBlank { strings.premium } else strings.subscription.ifBlank { "Premium" },
                    selected = false,
                    accent = Color(0xFFFF9800),
                    selectedBg = Color(0xFFFF9800).copy(alpha = 0.12f),
                    textPrimary = Color(0xFFFF9800),
                    textSecondary = Color(0xFFFF9800).copy(alpha = 0.7f),
                    iconBg = Color(0xFFFF9800).copy(alpha = 0.12f),
                    onClick = { onOpenPricing(); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.FileDownload,
                    label = strings.exportCsv,
                    selected = false,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onExportCsv(); onClose() }
                )
                DrawerNavItem(
                    icon = Icons.Default.FileUpload,
                    label = strings.importCsv,
                    selected = false,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onImportCsv(); onClose() }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Logout with glassmorphism
            GlassmorphismDrawerCard(
                isDark = isDark,
                textSecondary = RecoveryRed.copy(alpha = 0.7f),
                hazeState = hazeState
            ) {
                DrawerNavItem(
                    icon = Icons.Default.Logout,
                    label = strings.logout,
                    selected = false,
                    accent = RecoveryRed,
                    selectedBg = RecoveryRed.copy(alpha = 0.1f),
                    textPrimary = RecoveryRed,
                    textSecondary = RecoveryRed.copy(alpha = 0.7f),
                    iconBg = RecoveryRed.copy(alpha = 0.1f),
                    onClick = { onLogout(); onClose() }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    isDark: Boolean,
    currentLanguage: String,
    strings: LanguageManager.Strings,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = if (isDark) cardColor() else LightCard
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val accent = if (isDark) accentColor() else LightPrimaryRed

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        titleContentColor = textPrimary,
        title = { Text(strings.selectLanguage, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                getLanguageOptions(strings).forEach { lang ->
                    val isSelected = lang.code == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) accent.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .clickable { onSelect(lang.code) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(lang.flag, fontSize = 22.sp)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            lang.name,
                            color = if (isSelected) accent else textPrimary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = accent)
            }
        }
    )
}

@Composable
fun ThemeSelectionDialog(
    isDark: Boolean,
    currentThemeMode: ThemeMode,
    strings: LanguageManager.Strings,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = if (isDark) cardColor() else LightCard
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val accent = if (isDark) accentColor() else LightPrimaryRed

    val themeOptions = remember(strings) { listOf(
        ThemeMode.LIGHT to strings.light,
        ThemeMode.DARK to strings.dark,
        ThemeMode.SYSTEM to strings.system
    ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        titleContentColor = textPrimary,
        title = { Text(strings.selectTheme, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                themeOptions.forEach { (mode, label) ->
                    val isSelected = mode == currentThemeMode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) accent.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .clickable { onSelect(mode) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (mode) {
                                ThemeMode.DARK -> Icons.Default.DarkMode
                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                            },
                            contentDescription = null,
                            tint = if (isSelected) accent else textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            label,
                            color = if (isSelected) accent else textPrimary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = accent)
            }
        }
    )
}

@Composable
fun UnitsSelectionDialog(
    isDark: Boolean,
    isLbs: Boolean,
    strings: LanguageManager.Strings,
    onSelect: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = if (isDark) cardColor() else LightCard
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val accent = if (isDark) accentColor() else LightPrimaryRed

    val unitOptions = remember(strings) { listOf(
        false to strings.kg,
        true to strings.lbs
    ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        titleContentColor = textPrimary,
        title = { Text(strings.units, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                unitOptions.forEach { (lbsValue, label) ->
                    val isSelected = lbsValue == isLbs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) accent.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .clickable { onSelect(lbsValue) }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = if (isSelected) accent else textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            label,
                            color = if (isSelected) accent else textPrimary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = accent)
            }
        }
    )
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    accent: Color,
    selectedBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    iconBg: Color,
    badge: Int = 0,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) selectedBg else Color.Transparent,
        animationSpec = tween(200),
        label = "bg"
    )

    // ── Pulse animation for selected icon ──
    val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (selected) 0.3f else 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .background(
                    if (selected) accent.copy(alpha = pulseAlpha) else iconBg
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) accent else textSecondary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            color = if (selected) accent else textPrimary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .background(Volcanico, RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+$badge",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(6.dp))
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(label: String, textSecondary: Color) {
    Text(
        label,
        color = textSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassmorphismDrawerCard(
    isDark: Boolean,
    headerLabel: String? = null,
    textSecondary: Color,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardBg = if (isDark) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.40f)
    }
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.Black.copy(alpha = 0.10f) // more visible border
    }
    val shape = RoundedCornerShape(20.dp)

    // HazeMaterials.thin is @Composable (Haze 1.5.4) — compute it here, pass it to hazeEffect.
    val hazeStyle: HazeStyle? = if (hazeState != null) {
        HazeMaterials.thin(containerColor = cardBg)
    } else {
        null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(shape)
            .then(
                if (hazeState != null && hazeStyle != null) {
                    Modifier.hazeEffect(state = hazeState, style = hazeStyle)
                } else {
                    Modifier
                }
            )
    ) {
        // Layer 1: fundal gradient (frosted glass background)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.03f),
                                Color.White.copy(alpha = 0.02f),
                                Color.Black.copy(alpha = 0.05f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.20f)
                            )
                        }
                    )
                )
        )
        // Layer 2: conținut sharp + border
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, shape)
        ) {
            if (headerLabel != null) {
                DrawerSectionHeader(label = headerLabel, textSecondary = textSecondary)
                content()
            } else {
                content()
            }
        }
    }
}

@Composable
private fun DrawerSettingItem(
    icon: ImageVector,
    label: String,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = textSecondary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, color = textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun ServerUrlDialog(
    isDark: Boolean,
    currentUrl: String,
    currentApiKey: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = if (isDark) cardColor() else LightCard
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val accent = if (isDark) accentColor() else LightPrimaryRed

    var url by remember { mutableStateOf(currentUrl) }
    var apiKey by remember { mutableStateOf(currentApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        titleContentColor = textPrimary,
        title = { Text("Server Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Backend server address:",
                    color = textSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("http://192.168.100.5:4242", color = textSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                        cursorColor = accent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "AI Trainer API Key (optional):",
                    color = textSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text("Leave empty if auth is disabled", color = textSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                        cursorColor = accent,
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Leave empty for default server URL. API key only needed if server has auth enabled.",
                    color = textSecondary,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(url.trim(), apiKey.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
                TextButton(onClick = onDismiss) {
                Text("Close", color = accent)
            }
        }
    )
}
