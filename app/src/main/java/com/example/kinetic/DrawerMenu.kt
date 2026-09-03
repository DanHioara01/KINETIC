package com.example.kinetic

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kinetic.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

enum class DrawerPage { STATS, CALENDAR, FOOD_JOURNAL, AI_TRAINER, FRIENDS, GPS_CARDIO, REST_DAYS, PLATE_CALCULATOR, ONE_RM_CALCULATOR, SAVED_EXERCISES, WEIGHT_GOAL, BODY_FAT_CALCULATOR, WORKOUT_ANALYTICS, READINESS, MESSAGES, WORKOUT_PLANS }

fun DrawerPage.toDrawerScreen(): DrawerScreen = when (this) {
    DrawerPage.STATS -> DrawerScreen.Calendar
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
    DrawerPage.READINESS -> DrawerScreen.Calendar
    DrawerPage.MESSAGES -> DrawerScreen.Messages
    DrawerPage.WORKOUT_PLANS -> DrawerScreen.WorkoutPlans
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
    DrawerScreen.Messages -> DrawerPage.MESSAGES
    DrawerScreen.WorkoutPlans -> DrawerPage.WORKOUT_PLANS
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
    unreadMessagesCount: Int = 0,
    onNavigate: (DrawerPage) -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
    onLogout: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onOpenLanguageDialog: () -> Unit,
    onOpenUnitsDialog: () -> Unit,
    strings: LanguageManager.Strings,
    onClose: () -> Unit,
    onToggleTheme: () -> Unit = {},
    onOpenServerSettings: () -> Unit = {},
    onOpenPricing: () -> Unit = {},
    isPremium: Boolean = false
) {
    val bg = if (isDark) DarkBackground else LightBackground
    // ── Tema comută instant (fără animație per-frame → zero jank). ──
    val themeProgress = if (isDark) 1f else 0f
    val textPrimary = lerp(LightTextPrimary, WhiteText, themeProgress)
    val textSecondary = lerp(LightTextSecondary, GrayText, themeProgress)
    val divider = lerp(LightDividerGray, DividerGray, themeProgress)
    val accent = lerp(LightPrimaryRed, LightRed, themeProgress)
    val selectedBg = lerp(DrawerItemSelectedLight, DrawerItemSelectedDark, themeProgress)
    val iconBg = lerp(LightIconBackground, IconBackground, themeProgress)

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
                    colors = listOf(
                        lerp(Color(0xFFE8E8E8), Color(0xFF1A1A1A), themeProgress),
                        lerp(Color(0xFFE0E0E0), Color(0xFF121212), themeProgress),
                        lerp(Color(0xFFD8D8D8), Color(0xFF0D0D0D), themeProgress)
                    )
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
                                Icon(
                                    androidx.compose.ui.res.painterResource(R.drawable.trophy_star),
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
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
                    // Switch dark/light — la dreapta, în dreptul imaginii de profil
                    Spacer(Modifier.weight(1f))
                    AnimatedThemeSwitch(
                        isDark = isDark,
                        onToggle = onToggleTheme
                    )
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
                    label = strings.plateCalculatorTitle,
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
                    label = strings.oneRmCalculator,
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
                DrawerNavItem(
                    icon = Icons.Default.FavoriteBorder,
                    label = strings.readinessTitle.ifBlank { "Readiness" },
                    selected = currentPage == DrawerPage.READINESS,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.READINESS); onClose() }
                )
            }

            // Workout Plans section
            GlassmorphismDrawerCard(
                isDark = isDark,
                headerLabel = "Training",
                textSecondary = textSecondary,
                hazeState = hazeState
            ) {
                DrawerNavItem(
                    icon = Icons.Default.FitnessCenter,
                    label = strings.workoutPlans.ifBlank { "Workout Plans" },
                    selected = currentPage == DrawerPage.WORKOUT_PLANS,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    onClick = { onNavigate(DrawerPage.WORKOUT_PLANS); onClose() }
                )
            }

            // Messages section
            GlassmorphismDrawerCard(
                isDark = isDark,
                headerLabel = "Messages",
                textSecondary = textSecondary,
                hazeState = hazeState
            ) {
                DrawerNavItem(
                    icon = Icons.Default.MailOutline,
                    label = "Messages",
                    selected = currentPage == DrawerPage.MESSAGES,
                    accent = accent,
                    selectedBg = selectedBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    iconBg = iconBg,
                    badge = unreadMessagesCount,
                    onClick = { onNavigate(DrawerPage.MESSAGES); onClose() }
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

            // Versiunea aplicației — se actualizează automat din BuildConfig la fiecare release
            Text(
                "KINETIC v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = textSecondary.copy(alpha = 0.5f),
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
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
    val p = appPalette(isDark)
    val cardBg = p.card
    val textPrimary = p.tp
    val accent = p.ac

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
    val p = appPalette(isDark)
    val cardBg = p.card
    val textPrimary = p.tp
    val textSecondary = p.ts
    val accent = p.ac

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
    val p = appPalette(isDark)
    val cardBg = p.card
    val textPrimary = p.tp
    val textSecondary = p.ts
    val accent = p.ac

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
private fun AnimatedThemeSwitch(
    isDark: Boolean,
    onToggle: () -> Unit
) {
    // ══ Culori Volcanic per mod — noapte de lavă ↔ zori de cenușă ══
    // Culori directe (comută instant, fără animație per-frame → zero jank).
    val trackBorderStart = if (isDark) Color(0xFFFF7A3C) else Color(0xFFFFB84D)
    val trackBorderEnd = if (isDark) Color(0xFFD61B30) else Color(0xFFF97316)
    val thumbStart = if (isDark) Color(0xFFFF7A3C) else Color(0xFFFFB84D)
    val thumbEnd = if (isDark) Color(0xFFD61B30) else Color(0xFFF97316)
    val trackFill = (if (isDark) Color(0xFF170B09) else Color(0xFFFFF2E4)).copy(alpha = 0.85f)
    val iconColor = if (isDark) Color(0xFF2B0E06) else Color(0xFF5C2600)

    // ══ Knob — spring bounce ≈ cubic-bezier(0.34, 1.56, 0.64, 1), dar RAPID ══
    // StiffnessMediumLow (200 N/m) lăsa thumb-ul să se târască ~1s → părea lag.
    // StiffnessMedium (1500 N/m) păstrează bounce-ul dar se așază în ~250ms.
    val knobOffset by animateDpAsState(
        targetValue = if (isDark) 24.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.5f,           // bounce discret (nu exagerat)
            stiffness = Spring.StiffnessMedium
        ),
        label = "themeKnob"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isDark) 0f else 360f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "themeRotation"
    )

    val pillShape = RoundedCornerShape(50)

    // Umbra folosește o culoare STATICĂ per mod (nu glowColor animat): altfel umbra
    // se re-rasterizează la fiecare frame al tranziției → jank. Efectul glow rămâne
    // vizibil prin border + thumb-ul animat.
    val shadowColor = if (isDark) Color(0xFFFF5A36) else Color(0xFFF97316)

    Box(
        modifier = Modifier
            .size(width = 58.dp, height = 34.dp)
            .shadow(
                elevation = 10.dp,
                shape = pillShape,
                ambientColor = shadowColor.copy(alpha = 0.55f),
                spotColor = shadowColor.copy(alpha = 0.55f)
            )
            .clip(pillShape)
            .background(trackFill)
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(trackBorderStart, trackBorderEnd)),
                shape = pillShape
            )
            .semantics {
                role = Role.Switch
                contentDescription = if (isDark) "Dark mode" else "Light mode"
            }
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.CenterStart
    ) {
        // Knob cu iconița desenată (lună + stele / soare)
        Box(
            modifier = Modifier
                .size(27.dp)
                .offset(x = 3.5.dp + knobOffset)
                .clip(CircleShape)
                .graphicsLayer { shadowElevation = 2f }
                .background(Brush.linearGradient(listOf(thumbStart, thumbEnd)))
                .border(1.dp, Color.White.copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = iconRotation }
            ) {
                if (isDark) drawMoonIcon(iconColor) else drawSunIcon(iconColor)
            }
        }
    }
}

private fun DrawScope.drawMoonIcon(color: Color) {
    val c = Offset(size.width * 0.5f, size.height * 0.5f)
    val r = size.minDimension * 0.30f

    // Semilună: cerc minus un cerc decalat spre stânga → deschiderea spre stânga
    val body = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
    val cutR = r * 0.90f
    val cut = Path().apply {
        addOval(
            Rect(
                c.x - r * 0.45f - cutR, c.y - r * 0.10f - cutR,
                c.x - r * 0.45f + cutR, c.y - r * 0.10f + cutR
            )
        )
    }
    drawPath(Path.combine(PathOperation.Difference, body, cut), color)

    // Două steluțe mici sus-dreapta
    drawSparkle(Offset(c.x + r * 0.95f, c.y - r * 0.85f), r * 0.20f, color)
    drawSparkle(Offset(c.x + r * 1.30f, c.y - r * 0.30f), r * 0.13f, color)
}

private fun DrawScope.drawSunIcon(color: Color) {
    val c = Offset(size.width * 0.5f, size.height * 0.5f)
    val coreR = size.minDimension * 0.20f
    val rayLen = size.minDimension * 0.24f

    // Disc central + raze triunghiulare
    drawCircle(color = color, radius = coreR, center = c)

    val count = 8
    for (i in 0 until count) {
        val mid = i * (PI / 4.0) - PI / 2.0
        val spread = PI / 14.0
        val apex = Offset(
            c.x + cos(mid).toFloat() * (coreR + rayLen),
            c.y + sin(mid).toFloat() * (coreR + rayLen)
        )
        val b1 = Offset(
            c.x + cos(mid - spread).toFloat() * coreR,
            c.y + sin(mid - spread).toFloat() * coreR
        )
        val b2 = Offset(
            c.x + cos(mid + spread).toFloat() * coreR,
            c.y + sin(mid + spread).toFloat() * coreR
        )
        val ray = Path().apply {
            moveTo(b1.x, b1.y)
            lineTo(apex.x, apex.y)
            lineTo(b2.x, b2.y)
            close()
        }
        drawPath(ray, color)
    }
}

private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    val inner = radius * 0.42f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + inner, center.y - inner)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + inner, center.y + inner)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - inner, center.y + inner)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - inner, center.y - inner)
        close()
    }
    drawPath(path = path, color = color)
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
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
    // Rulează DOAR pentru item-ul selectat. Înainte fiecare din cele ~20 de rânduri
    // din drawer porneau un infiniteTransition cu 2 animații la 60fps, chiar și
    // neselectate → CPU/GPU încărcate permanent → switch-ul și drawer-ul păreau laggy.
    // Ritmul e aliniat cu CURRENT STREAK de pe pagina principală (tween 2000ms, scale 1.08).
    val pulseScale: Float
    val pulseAlpha: Float
    if (selected) {
        val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
        pulseScale = infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        ).value
        pulseAlpha = infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        ).value
    } else {
        pulseScale = 1f
        pulseAlpha = 0.15f
    }

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
            if (iconPainter != null) {
                Icon(iconPainter, contentDescription = label, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(24.dp))
            } else {
                Icon(icon, contentDescription = null, tint = if (selected) accent else textSecondary, modifier = Modifier.size(20.dp))
            }
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
    strings: LanguageManager.Strings,
    currentUrl: String,
    currentApiKey: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val p = appPalette(isDark)
    val cardBg = p.card
    val textPrimary = p.tp
    val textSecondary = p.ts
    val accent = p.ac

    var url by remember { mutableStateOf(currentUrl) }
    var apiKey by remember { mutableStateOf(currentApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        titleContentColor = textPrimary,
        title = { Text(strings.serverSettings, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    strings.backendServerAddress,
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
                    strings.aiApiKeyOptional,
                    color = textSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    placeholder = { Text(strings.leaveEmptyIfAuthDisabled, color = textSecondary.copy(alpha = 0.5f)) },
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
                    strings.leaveEmptyForDefaultServer,
                    color = textSecondary,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(url.trim(), apiKey.trim()) },
                modifier = Modifier.background(RedButtonGradient, RoundedCornerShape(10.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(strings.save, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
                TextButton(onClick = onDismiss) {
                Text(strings.close, color = accent)
            }
        }
    )
}
