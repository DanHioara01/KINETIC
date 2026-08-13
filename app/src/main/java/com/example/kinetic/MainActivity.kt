package com.example.kinetic

import android.app.PictureInPictureParams
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import androidx.compose.foundation.BorderStroke
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.annotation.RequiresApi
import kotlinx.coroutines.tasks.await
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableDoubleStateOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.kinetic.ui.components.AppGlassCard
import com.example.kinetic.ui.components.GradientNextExerciseButton
import com.example.kinetic.ui.components.GlassCard
import com.example.kinetic.ui.components.KineticAppBar
import com.example.kinetic.ui.components.KineticHeaderController
import com.example.kinetic.ui.components.LocalKineticHeader
import com.example.kinetic.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableLongStateOf
import androidx.glance.appwidget.updateAll

// True once the navbar/WORKOUT entrance animation has played this app session,
// so it does not replay on recomposition or reloadToken refreshes.
private var navbarEntranceAnimationPlayed = false

class MainActivity : ComponentActivity() {
    // Set when the GPS tracking notification is tapped so the app reopens on the Cardio screen.
    var openGpsCardioRequest by mutableStateOf(false)
    // Set when the water widget is tapped so the app reopens on the Water tab.
    var openWaterTabRequest by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        openGpsCardioRequest = intent.getBooleanExtra("open_gps_cardio", false)
        openWaterTabRequest = intent.getBooleanExtra("open_water_tab", false)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        val userProfileManager = UserProfileManager(this)
        val preferencesManager = PreferencesManager(this, userProfileManager)
        preferencesManager.migrateLegacyDataIfNeeded()
        LanguageManager.loadSavedLanguage(this)

        setContent {
            val themeMode = remember { mutableStateOf(preferencesManager.getThemeMode()) }
            var showWelcome by remember { mutableStateOf(preferencesManager.isLoggedIn()) }
            val context = androidx.compose.ui.platform.LocalContext.current
            val strings = LanguageManager.getStrings(context)
            val isDark = when (themeMode.value) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            val userName = remember {
                val profile = UserProfileManager(this).getOwnProfile()
                profile?.name?.takeIf { it.isNotBlank() && it != "Guest" && it != "Facebook User" } ?: ""
            }

            KineticTheme(themeMode = themeMode.value) {
                // ══ Animație de temă UȘOARĂ (crossfade cu scrim) ══
                // Un singur overlay solid acoperă scurt conținutul, DUPĂ care culorile
                // comută (ascunse sub scrim), apoi scrim-ul se dezvăluie → crossfade lin,
                // vizibil pe orice ecran, fără flash de fundal. Un singur nod animat → zero lag.
                val scrimAlpha = remember { Animatable(0f) }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(com.example.kinetic.ui.theme.bgColor())
                ) {
                    if (showWelcome) {
                        WelcomeScreen(
                            userName = userName.ifEmpty { strings.athlete },
                            strings = strings,
                            isDark = isDark,
                            onFinished = { showWelcome = false }
                        )
                    } else {
                        // alpha(1f) creează un hardware layer care asigură că primul frame
                        // e prezentat la pornire (fără el, pe unele device-uri conținutul
                        // rămâne nedesenat până la primul invalidate/scroll). Fără fade.
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(1f),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MuscleGroupList(
                                onThemeChanged = { themeMode.value = it },
                                themeScrim = scrimAlpha
                            )
                        }
                    }
                    // Scrim peste tot (acoperă și drawer-ul): crossfade spre tema țintă.
                    // Composable separat → doar el recompune în timpul animației (290ms),
                    // întregul arbore nu. La repaus e absent complet (alpha = 0).
                    ThemeScrimOverlay(scrimAlpha = scrimAlpha)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("open_gps_cardio", false)) {
            openGpsCardioRequest = true
        }
        if (intent.getBooleanExtra("open_water_tab", false)) {
            openWaterTabRequest = true
        }
    }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isInPictureInPictureMode) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(1, 1))
                .build()
            try {
                enterPictureInPictureMode(params)
            } catch (_: Exception) {
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        GpsTrackingState.isInPipMode = isInPictureInPictureMode
    }
}

    private fun todayKey(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(java.util.Date())
    }

    // ============================================
    // Helper functions for weight conversion
// ============================================
internal fun convertWeight(kg: Double, isLbs: Boolean): Double = if (isLbs) kg * 2.20462 else kg
internal fun weightLabel(kg: Double, isLbs: Boolean): String {
    val value = if (isLbs) kg * 2.20462 else kg
    val unit = if (isLbs) "lbs" else "kg"
    return if (value == value.toLong().toDouble()) "${value.toLong()} $unit" else "${String.format("%.1f", value)} $unit"
}

/** Estimated kcal burned per km, matching the GPS Cardio screen estimates. */
internal fun cardioCalPerKm(type: String): Double = when (type) {
    "running" -> 70.0
    "cycling" -> 30.0
    "walking" -> 40.0
    else -> 50.0
}

// ============================================
// Ecranul 1: Lista de Grupe Musculare
// ============================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutSubTabs(
    subTab: Int,
    onSubTabChange: (Int) -> Unit,
    strings: LanguageManager.Strings,
    accent: Color,
    textSecondary: Color,
    surfaceBg: Color,
    alpha: Float
) {
    // 0 la top → 1 când lista s-a derulat sub header (frosted glass)
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 220),
        label = "workoutTabsAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        surfaceBg.copy(alpha = animatedAlpha * 0.92f),
                        surfaceBg.copy(alpha = animatedAlpha * 0.84f)
                    )
                )
            )
            .drawBehind {
                if (animatedAlpha > 0.02f) {
                    drawLine(
                        color = textSecondary.copy(alpha = animatedAlpha * 0.25f),
                        start = Offset(0f, size.height - 0.5.dp.toPx()),
                        end = Offset(size.width, size.height - 0.5.dp.toPx()),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val tabLabels = remember(strings) { listOf(strings.templates, strings.muscleGroups) }
            tabLabels.forEachIndexed { index, label ->
                val isActive = subTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isActive) Modifier.border(1.5.dp, accent, RoundedCornerShape(12.dp))
                            else Modifier.border(1.dp, textSecondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        )
                        .background(if (isActive) accent.copy(alpha = 0.08f) else Color.Transparent)
                        .clickable { onSubTabChange(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (isActive) accent else textSecondary,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeScrimOverlay(scrimAlpha: Animatable<Float, *>) {
    val a = scrimAlpha.value
    if (a > 0f) {
        // Scrim cu culoare CONSTANTĂ (întunecată), nu legată de temă: la schimbarea
        // culorilor sub el, scrim-ul NU se recolorează → fără flash alb la dark→light.
        // Tranziția trece prin întuneric și dezvăluie tema nouă lin.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.kinetic.ui.theme.DarkBackground)
                .alpha(a)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MuscleGroupList(
    onThemeChanged: (ThemeMode) -> Unit = {},
    // Scrim-ul de crossfade al temei (din MainActivity). Dacă e furnizat, comutarea
    // temei e secvențiată: acoperire solidă → schimbare culori → dezvăluire lină.
    themeScrim: Animatable<Float, *>? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userProfileManager = remember { UserProfileManager(context) }
    val preferencesManager = remember { PreferencesManager(context, userProfileManager) }
    val mainViewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(Unit) {
        val savedUrl = preferencesManager.getServerUrl()
        if (savedUrl.isNotBlank()) {
            NetworkClient.getApi(savedUrl)
        }
    }

    var isLoggedIn by remember { mutableStateOf(preferencesManager.isLoggedIn()) }
    var showOnboarding by remember {
        mutableStateOf(isLoggedIn && !preferencesManager.isOnboardingComplete())
    }
    var showSignUp by remember { mutableStateOf(false) }
    val workoutNavController = rememberNavController()
    val workoutBackStackEntry by workoutNavController.currentBackStackEntryAsState()
    val isWorkoutFlowActive = WorkoutRoutes.isActiveRoute(workoutBackStackEntry?.destination?.route)
    var showCalendar by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showUnitsDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentPage by remember { mutableStateOf<DrawerPage?>(null) }
    var currentDashboardTab by remember { mutableIntStateOf(0) }
    var muscleGroupsSubTab by remember { mutableIntStateOf(0) }
    var selectedTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var showTodayWorkout by remember { mutableStateOf(false) }
    var quickExerciseGrupa by remember { mutableStateOf<String?>(null) }
    var quickExerciseName by remember { mutableStateOf<String?>(null) }
    var quickExerciseIndex by remember { mutableIntStateOf(0) }
    var quickExerciseList by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var selectedProgressFromQuick by remember { mutableStateOf<String?>(null) }
    var isLbs by remember { mutableStateOf(preferencesManager.isLbs()) }
    var currentLanguage by remember { mutableStateOf(LanguageManager.getLanguage()) }
    var currentThemeMode by remember { mutableStateOf(preferencesManager.getThemeMode()) }
    var reloadToken by remember { mutableIntStateOf(0) }
    var badgeCheckTrigger by remember { mutableIntStateOf(0) }
    var profileChangedTrigger by remember { mutableIntStateOf(0) }
    var newBadgeNotifications by remember { mutableStateOf<List<String>>(emptyList()) }
    var updateAvailable by remember { mutableStateOf<UpdateChecker.LatestRelease?>(null) }
    var updateChecked by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val strings = LanguageManager.getStrings(context)
    val isDark = when (currentThemeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val profile = userProfileManager.getOwnProfile()
    var profileName by remember { mutableStateOf(profile?.name?.ifBlank { null } ?: strings.guest) }
    var profilePhoto by remember { mutableStateOf(profile?.photoUri ?: "") }
    // Incrementat la fiecare poză nouă ca să forțeze reload-ul (cache-busting Coil)
    var profilePhotoVersion by remember { mutableIntStateOf(0) }
    val userId = profile?.userId ?: userProfileManager.getOwnUserId()
    val userShortId = remember { userProfileManager.getOwnShortId() }
    val userEmail = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val latestProfile = userProfileManager.getOwnProfile()
            if (latestProfile != null && latestProfile.name.isNotBlank()) {
                profileName = latestProfile.name
            }
            if (!preferencesManager.isOnboardingComplete()) {
                showOnboarding = true
            }
            // Verifică o singură dată pe pornire dacă există o versiune nouă pe GitHub
            if (!updateChecked) {
                updateChecked = true
                runCatching { UpdateChecker.checkForUpdate() }.getOrNull()?.let {
                    updateAvailable = it
                }
            }
        }
    }

    LaunchedEffect(isLoggedIn, userId, profileName, profilePhoto) {
        if (isLoggedIn && userId != "local_user") {
            kotlinx.coroutines.Dispatchers.IO.let { dispatcher ->
                kotlinx.coroutines.withContext(dispatcher) {
                    try {
                        FirestoreHelper().saveFcmToken(userId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        if (profileName.isNotBlank()) {
                            FirestoreHelper().saveUserProfile(userId, profileName, profilePhoto)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        if (profileName.isNotBlank()) {
                            val db = AppDatabase.getDatabase(context)
                            // Dacă poza e un path local (file:// / content://), o urcăm în
                            // Firebase Storage ca să fie vizibilă și pentru ceilalți (leaderboard,
                            // căutare); altfel trimitem URL-ul direct.
                            var photoToSync = profilePhoto
                            if (photoToSync.startsWith("file://") || photoToSync.startsWith("content://")) {
                                photoToSync = try {
                                    FirestoreHelper().uploadProfilePhoto(context, userId, android.net.Uri.parse(photoToSync))
                                } catch (_: Exception) {
                                    photoToSync
                                }
                            }
                            SocialRepository(db).syncUserProfile(userId, profileName, photoToSync)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val totalVolume = db.antrenamentDao().sumVolumeForUser(userId)
                        val workoutCount = db.antrenamentDao().countForUser(userId)
                        SocialRepository(db).syncVolumeToFirestore(userId, totalVolume, workoutCount)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val prefs = PreferencesManager(context, userProfileManager)
                        val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                        syncRepo.initialSync(userId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    var weeklyTopExercise by remember { mutableStateOf<String?>(null) }
    var weeklyTotalKg by remember { mutableDoubleStateOf(0.0) }
    var todayExercises by remember { mutableStateOf<List<String>>(emptyList()) }
    var todayVolume by remember { mutableDoubleStateOf(0.0) }
    var lastPR by remember { mutableStateOf<PersonalRecordEntity?>(null) }
    var recoveryMap by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }
    var badgeCount by remember { mutableIntStateOf(0) }
    var recentBadges by remember { mutableStateOf<List<BadgeEntity>>(emptyList()) }
    var allRecentPRs by remember { mutableStateOf<List<PersonalRecordEntity>>(emptyList()) }
    var allExerciseNames by remember { mutableStateOf<List<String>>(emptyList()) }

    var weekWorkoutCount by remember { mutableIntStateOf(0) }
    var lastWeekWorkoutCount by remember { mutableIntStateOf(0) }
    var weekVolume by remember { mutableDoubleStateOf(0.0) }
    var lastWeekVolume by remember { mutableDoubleStateOf(0.0) }
    var weekWorkoutDurationMs by remember { mutableLongStateOf(0L) }

    var totalAllWorkouts by remember { mutableIntStateOf(0) }
    var totalAllVolume by remember { mutableDoubleStateOf(0.0) }
    var showBiometricInput by remember { mutableStateOf(false) }
    var showBiometricCharts by remember { mutableStateOf(false) }
    var lastBiometric by remember { mutableStateOf<BiometricEntity?>(null) }
    var allBiometrics by remember { mutableStateOf<List<BiometricEntity>>(emptyList()) }
    var weeksSinceMeasurement by remember { mutableIntStateOf(-1) }

    var showFoodJournal by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showAddFood by remember { mutableStateOf(false) }
    var foodEntries by remember { mutableStateOf<List<FoodEntity>>(emptyList()) }
    var pendingFoodProduct by remember { mutableStateOf<FoodProduct?>(null) }
    var showAiTrainer by remember { mutableStateOf(false) }
    var showFriends by remember { mutableStateOf(false) }
    var pendingRequestsCount by remember { mutableIntStateOf(0) }
    var showLeaderboard by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var showPlateCalculator by remember { mutableStateOf(false) }
    var showOneRMCalculator by remember { mutableStateOf(false) }
    var showWorkoutAnalytics by remember { mutableStateOf(false) }
    var showSavedExercises by remember { mutableStateOf(false) }
    var showWeightGoal by remember { mutableStateOf(false) }
    var showBodyFatCalculator by remember { mutableStateOf(false) }

    // Open GPS Cardio when the tracking notification is tapped (service keeps running in background)
    val mainActivity = context as? MainActivity
    LaunchedEffect(mainActivity?.openGpsCardioRequest) {
        val act = mainActivity ?: return@LaunchedEffect
        if (act.openGpsCardioRequest) {
            act.openGpsCardioRequest = false
            act.intent?.removeExtra("open_gps_cardio")
            currentPage = DrawerPage.GPS_CARDIO
            showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false
            workoutNavController.popToWorkoutHome()
        }
    }

    // Open the Water tab (water bottle screen) when the water widget is tapped.
    LaunchedEffect(mainActivity?.openWaterTabRequest) {
        val act = mainActivity ?: return@LaunchedEffect
        if (act.openWaterTabRequest) {
            act.openWaterTabRequest = false
            act.intent?.removeExtra("open_water_tab")
            currentPage = null
            showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false
            workoutNavController.popToWorkoutHome()
            currentDashboardTab = 3
        }
    }

    // ===== Monetization =====
    val gymApp = context.applicationContext as? KineticApplication
    val subscriptionRepo = remember { SubscriptionRepository(AppDatabase.getDatabase(context)) }
    val featureAccess = remember { FeatureAccessManager(subscriptionRepo) }
    val activity = context as? androidx.activity.ComponentActivity
    var showPricing by remember { mutableStateOf(false) }
    var pricingOptions by remember { mutableStateOf<List<PricingOption>>(emptyList()) }
    var unlockSheetFeature by remember { mutableStateOf<PremiumFeature?>(null) }
    val subscription by featureAccess.subscription(userId, email = userEmail).collectAsState(initial = UserSubscription.free(userId))
    val rcOfferings by (gymApp?.revenueCatManager?.offerings ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()
    var subscriptionLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(rcOfferings) {
        gymApp?.revenueCatManager?.let { pricingOptions = it.buildPricingOptions() }
    }

    DisposableEffect(userId, isLoggedIn) {
        val app = gymApp
        var registration: com.google.firebase.firestore.ListenerRegistration? = null
        if (isLoggedIn && userId != "local_user") {
            subscriptionLoaded = false
            registration = subscriptionRepo.startFirestoreListener(userId)
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                // Await RevenueCat login so the entitlement below belongs to THIS user.
                // Without this, the previous account's cached CustomerInfo could be written
                // onto the new account (subscription "contamination" across logins).
                val rc = app?.revenueCatManager
                val info = rc?.logInSuspend(userId)
                subscriptionRepo.cleanupExpiredUnlocks()
                subscriptionRepo.syncFromFirestore(userId)
                // Only overlay a RevenueCat-derived subscription when we actually have a
                // fresh, valid CustomerInfo for this user that is genuinely active.
                // Never downgrade: if Firestore already has a higher tier, keep it.
                if (info != null && rc != null) {
                    if (rc.isKineticProActive()) {
                        subscriptionRepo.updateSubscription(RevenueCatManager.mapToEntity(userId, info))
                    } else {
                        val currentTier = subscriptionRepo.getCurrentTier(userId)
                        if (currentTier == SubscriptionTier.FREE) {
                            subscriptionRepo.resetToFree(userId)
                        }
                    }
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    subscriptionLoaded = true
                }
            }
        }
        onDispose {
            registration?.remove()
            gymApp?.revenueCatManager?.logOut()
        }
    }

    if (isLoggedIn && userId == "local_user") {
        subscriptionLoaded = true
    }

    fun reconcileAfterPurchase() {
        val app = gymApp ?: return
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val info = app.revenueCatManager.customerInfo.value
            if (info != null) {
                subscriptionRepo.updateSubscription(RevenueCatManager.mapToEntity(userId, info))
            }
        }
    }

    fun devSimulatePurchase(tier: SubscriptionTier) {
        scope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val expiry = when (tier) {
                    SubscriptionTier.PRO_LIFETIME, SubscriptionTier.FREE -> null
                    SubscriptionTier.PREMIUM_MONTHLY -> System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
                    SubscriptionTier.PREMIUM_ANNUAL -> System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000
                }
                subscriptionRepo.updateSubscription(
                    UserSubscriptionEntity(
                        userId = userId,
                        subscriptionType = tier.id,
                        subscriptionStatus = "ACTIVE",
                        expiryDate = expiry,
                        isLifetime = tier == SubscriptionTier.PRO_LIFETIME,
                        revenueCatId = "",
                        lastSyncedAt = System.currentTimeMillis()
                    )
                )
            }
            snackbarHostState.showSnackbar(strings.purchaseSuccess)
            showPricing = false
        }
    }

    fun purchasePlan(tier: SubscriptionTier) {
        val option = pricingOptions.firstOrNull { it.tier == tier }
        val pkg = option?.rcPackage
        val act = activity
        val app = gymApp
        if (pkg != null && act != null && app != null) {
            scope.launch {
                when (val result = app.revenueCatManager.purchase(act, pkg)) {
                    is PurchaseResult.Success -> {
                        subscriptionRepo.updateSubscription(RevenueCatManager.mapToEntity(userId, result.customerInfo))
                        reconcileAfterPurchase()
                        snackbarHostState.showSnackbar(strings.purchaseSuccess)
                        showPricing = false
                    }
                    is PurchaseResult.Error -> snackbarHostState.showSnackbar(
                        "${strings.purchaseFailed}: ${result.message ?: ""}".trim()
                    )
                    PurchaseResult.Cancelled -> snackbarHostState.showSnackbar(strings.purchaseCancelled)
                }
            }
        } else if (BuildConfig.DEBUG) {
            // Billing not configured yet -> simulate locally so the flow is testable in debug builds.
            devSimulatePurchase(tier)
        } else {
            scope.launch { snackbarHostState.showSnackbar(strings.purchaseFailed) }
        }
    }

    fun restorePurchases() {
        val app = gymApp ?: return
        scope.launch {
            when (val result = app.revenueCatManager.restore()) {
                is PurchaseResult.Success -> {
                    subscriptionRepo.updateSubscription(RevenueCatManager.mapToEntity(userId, result.customerInfo))
                    reconcileAfterPurchase()
                    snackbarHostState.showSnackbar(strings.restoreSuccess)
                }
                is PurchaseResult.Error -> snackbarHostState.showSnackbar(
                    result.message ?: strings.noPurchasesToRestore
                )
                PurchaseResult.Cancelled -> {}
            }
        }
    }

    fun watchAdToUnlock(feature: PremiumFeature) {
        val act = activity ?: return
        val app = gymApp ?: return
        scope.launch {
            val count = subscriptionRepo.getTodayAdUnlockCount(userId)
            if (count >= BillingProducts.MAX_DAILY_AD_UNLOCKS) {
                snackbarHostState.showSnackbar(strings.dailyAdLimitReached)
                return@launch
            }
            if (!app.adUnlockManager.isAdReady()) {
                snackbarHostState.showSnackbar(strings.adNotReady)
                app.adUnlockManager.loadAd()
                return@launch
            }
            app.adUnlockManager.showAd(
                activity = act,
                onRewarded = {
                    scope.launch {
                        subscriptionRepo.saveAdUnlock(userId, feature.id, BillingProducts.AD_UNLOCK_DURATION_MS)
                        snackbarHostState.showSnackbar(strings.adUnlockSuccess)
                    }
                },
                onUnavailable = {
                    scope.launch { snackbarHostState.showSnackbar(strings.adNotReady) }
                }
            )
        }
    }
    var todayCardioDistance by remember { mutableDoubleStateOf(0.0) }
    var todayCardioDuration by remember { mutableLongStateOf(0L) }
    var todayCardioCalories by remember { mutableDoubleStateOf(0.0) }
    var todayStepsEstimate by remember { mutableIntStateOf(0) }
    // Baseline totals from saved routes (DB). Live GPS session values are added on top while tracking.
    var savedTodayCardioDistance by remember { mutableDoubleStateOf(0.0) }
    var savedTodayCardioDuration by remember { mutableLongStateOf(0L) }
    var savedTodayCardioCalories by remember { mutableDoubleStateOf(0.0) }
    var savedTodayStepsEstimate by remember { mutableIntStateOf(0) }
    var pedometerSteps by remember { mutableIntStateOf(0) }
    var manualSteps by remember { mutableIntStateOf(0) }
    var stepGoal by remember { mutableIntStateOf(preferencesManager.getStepGoal()) }

    fun refreshStepsWidget() {
        try {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                KineticStepsGlanceWidget().updateAll(context)
            }
        } catch (_: Exception) {}
    }

    val onDashboard by remember {
        derivedStateOf {
            !isWorkoutFlowActive && !showCalendar && !showTemplates && !showBiometricInput &&
                !showBiometricCharts && !showFoodJournal && !showBarcodeScanner && !showAddFood &&
                !showAiTrainer && !showPlateCalculator && !showOneRMCalculator && !showWorkoutAnalytics &&
                !showPricing &&
                currentPage != DrawerPage.GPS_CARDIO && currentPage != DrawerPage.REST_DAYS
        }
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val prefs = context.getSharedPreferences("pedometer_prefs", android.content.Context.MODE_PRIVATE)
        var initialSteps = prefs.getFloat("initial_steps_${todayKey()}", -1f).toInt()

        var lastPersistedSteps = -1
        var lastWidgetBroadcast = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val totalSteps = event.values[0].toInt()
                if (initialSteps < 0 || totalSteps < initialSteps) {
                    // First read of the day, or the counter reset on device reboot — re-baseline
                    initialSteps = totalSteps
                    prefs.edit().putFloat("initial_steps_${todayKey()}", initialSteps.toFloat()).apply()
                }
                pedometerSteps = (totalSteps - initialSteps).coerceAtLeast(0)
                // Persist today's steps so the home-screen widget can read them
                if (pedometerSteps != lastPersistedSteps) {
                    lastPersistedSteps = pedometerSteps
                    preferencesManager.setTodaySteps(pedometerSteps)
                }
                // Refresh the steps widget at most once a minute
                val now = System.currentTimeMillis()
                if (now - lastWidgetBroadcast > 60_000L) {
                    lastWidgetBroadcast = now
                    refreshStepsWidget()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (stepCounter != null) {
            sensorManager.registerListener(listener, stepCounter, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val totalSteps by remember(todayStepsEstimate, pedometerSteps, manualSteps) {
        derivedStateOf {
            val base = if (GpsTrackingState.isTracking || GpsTrackingState.isPaused) {
                // GPS already estimates the current session — don't count the same steps twice
                maxOf(todayStepsEstimate, pedometerSteps)
            } else {
                todayStepsEstimate + pedometerSteps
            }
            (base + manualSteps).coerceIn(0, 99999)
        }
    }

    // Live GPS cardio totals — while tracking, the home page shows saved + current session values
    LaunchedEffect(Unit) {
        var wasGpsSessionActive = false
        while (true) {
            val sessionActive = GpsTrackingState.isTracking || GpsTrackingState.isPaused
            if (sessionActive) {
                wasGpsSessionActive = true
                val liveDist = GpsTrackingState.totalDistance
                val liveDur = GpsTrackingState.elapsedTime
                val liveCal = liveDist * cardioCalPerKm(GpsTrackingState.activityType)
                val liveSteps = GpsTrackingState.estimatedSteps
                todayCardioDistance = savedTodayCardioDistance + liveDist
                todayCardioDuration = savedTodayCardioDuration + liveDur
                todayCardioCalories = savedTodayCardioCalories + liveCal
                todayStepsEstimate = (savedTodayStepsEstimate + liveSteps).coerceIn(0, 99999)
            } else {
                if (wasGpsSessionActive) {
                    wasGpsSessionActive = false
                    // Session ended (saved or cancelled) — refresh the saved baseline from the DB
                    withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val cal = java.util.Calendar.getInstance()
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            cal.set(java.util.Calendar.MINUTE, 0)
                            cal.set(java.util.Calendar.SECOND, 0)
                            cal.set(java.util.Calendar.MILLISECOND, 0)
                            val dayStart = cal.timeInMillis
                            val summary = db.cardioRouteDao().getTodaySummary(userId, dayStart, System.currentTimeMillis())
                            savedTodayCardioDistance = summary?.totalDistance ?: 0.0
                            savedTodayCardioDuration = summary?.totalDuration ?: 0L
                            savedTodayCardioCalories = summary?.totalCalories ?: 0.0
                            savedTodayStepsEstimate = (savedTodayCardioDistance * 1312).toInt().coerceIn(0, 99999)
                            // Persist the baseline so the home-screen widget shows the same numbers
                            preferencesManager.setTodayCardioBaseline(
                                savedTodayCardioDistance,
                                savedTodayCardioDuration,
                                savedTodayCardioCalories
                            )
                        } catch (_: Exception) {}
                    }
                    // Refresh the steps widget after the session ends
                    refreshStepsWidget()
                }
                todayCardioDistance = savedTodayCardioDistance
                todayCardioDuration = savedTodayCardioDuration
                todayCardioCalories = savedTodayCardioCalories
                todayStepsEstimate = savedTodayStepsEstimate
            }
            delay(1000)
        }
    }

    LaunchedEffect(isLoggedIn, userId) {
        if (isLoggedIn && userId != "local_user") {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                while (true) {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val requests = SocialRepository(db).getIncomingRequests(userId)
                        pendingRequestsCount = requests.size
                    } catch (_: Exception) {}
                    kotlinx.coroutines.delay(30000)
                }
            }
        }
    }

    LaunchedEffect(badgeCheckTrigger) {
        if (badgeCheckTrigger > 0 && userId != "local_user") {
            kotlinx.coroutines.Dispatchers.IO.let { dispatcher ->
                kotlinx.coroutines.withContext(dispatcher) {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val sm = StreakManager(db)
                        sm.recordWorkout(userId)
                        val be = BadgeEngine(db)
                        val newBadges = be.checkAndAward(userId)
                        if (newBadges.isNotEmpty()) {
                            newBadgeNotifications = newBadges
                            reloadToken++
                        }
                    } catch (_: Exception) { }
                }
            }
        }
    }

    LaunchedEffect(newBadgeNotifications) {
        if (newBadgeNotifications.isNotEmpty()) {
            val badgeNames = newBadgeNotifications.joinToString(", ") { key ->
                LanguageManager.getTranslatedBadge(key).title.ifEmpty { key }
            }
            snackbarHostState.showSnackbar("\uD83C\uDFC6 $badgeNames")
            newBadgeNotifications = emptyList()
        }
    }

    LaunchedEffect(reloadToken, userId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val prefs = PreferencesManager(context, userProfileManager)
                val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                val bm = BiometricManager(db, syncRepo)
                val latest = bm.getLatest(userId)
                lastBiometric = latest
                allBiometrics = bm.getAll(userId)
                weeksSinceMeasurement = if (latest != null) {
                    val diffMs = System.currentTimeMillis() - latest.timestamp
                    (diffMs / (7L * 24 * 60 * 60 * 1000)).toInt()
                } else -1
                val fm = FoodManager(db, syncRepo)
                foodEntries = fm.getRecent(userId, 100)
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(reloadToken, onDashboard, userId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
          try {
            val db = AppDatabase.getDatabase(context)
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val dayEnd = System.currentTimeMillis()
            cal.timeInMillis = dayStart
            cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            val weekStart = cal.timeInMillis
            cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            val weekEnd = cal.timeInMillis

            weeklyTotalKg = db.antrenamentDao().getTotalVolume(userId, weekStart, weekEnd) ?: 0.0
            val mostFrequent = db.exercitiuDao().getMostFrequentExercise(userId, weekStart, weekEnd)
            weeklyTopExercise = mostFrequent?.numeExercitiu

            val weekWorkouts = db.antrenamentDao().getWorkoutsInPeriod(userId, weekStart, weekEnd)
            weekWorkoutCount = weekWorkouts.size
            weekVolume = weekWorkouts.sumOf { it.totalWeight }
            weekWorkoutDurationMs = weekWorkouts.sumOf { it.durationMs }

            val lastWeekStart = weekStart - 7L * 24 * 60 * 60 * 1000
            val lastWeekEnd = weekStart
            val lastWeekWorkouts = db.antrenamentDao().getWorkoutsInPeriod(userId, lastWeekStart, lastWeekEnd)
            lastWeekWorkoutCount = lastWeekWorkouts.size
            lastWeekVolume = lastWeekWorkouts.sumOf { it.totalWeight }

            val todayWorkouts = db.antrenamentDao().getWorkoutsInPeriod(userId, dayStart, dayEnd)
            todayVolume = todayWorkouts.sumOf { it.totalWeight }
            if (todayWorkouts.isNotEmpty()) {
                val allTodayExercises = db.exercitiuDao().getForAntrenaments(todayWorkouts.map { it.id })
                todayExercises = allTodayExercises.map { it.numeExercitiu }.distinct()
            } else {
                todayExercises = emptyList()
            }

            val prs = db.personalRecordDao().getAllForUser(userId)
            lastPR = prs.firstOrNull()
            allRecentPRs = prs.take(5)

            val cal3 = java.util.Calendar.getInstance()
            cal3.timeInMillis = System.currentTimeMillis()
            cal3.add(java.util.Calendar.DAY_OF_YEAR, -365)
            val yearAgo = cal3.timeInMillis
            allExerciseNames = db.exercitiuDao().getDistinctExerciseNames(userId, yearAgo, System.currentTimeMillis())

            recoveryMap = AntrenamentRepository(db).getToateRecuperarile(userId).toMap()

            val streakEntity = db.streakDao().getForUser(userId)
            currentStreak = streakEntity?.currentStreak ?: 0
            bestStreak = streakEntity?.bestStreak ?: 0

            val userBadges = db.userBadgeDao().getForUser(userId)
            badgeCount = userBadges.size
            val allBadges = db.badgeDao().getAll()
            val badgeMap = allBadges.associateBy { it.key }
            recentBadges = userBadges.mapNotNull { badgeMap[it.badgeKey] }

            totalAllWorkouts = db.antrenamentDao().countForUser(userId)
            totalAllVolume = db.antrenamentDao().sumVolumeForUser(userId)

            val cardioSummary = db.cardioRouteDao().getTodaySummary(userId, dayStart, dayEnd)
            savedTodayCardioDistance = cardioSummary?.totalDistance ?: 0.0
            savedTodayCardioDuration = cardioSummary?.totalDuration ?: 0L
            savedTodayCardioCalories = cardioSummary?.totalCalories ?: 0.0
            savedTodayStepsEstimate = (savedTodayCardioDistance * 1312).toInt().coerceIn(0, 99999)
            // Persist the baseline so the home-screen widget shows the same numbers
            preferencesManager.setTodayCardioBaseline(
                savedTodayCardioDistance,
                savedTodayCardioDuration,
                savedTodayCardioCalories
            )
          } catch (_: Exception) { }
        }
    }

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showBarcodeScanner = true
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val notifPerm = android.Manifest.permission.POST_NOTIFICATIONS
            val cameraPerm = android.Manifest.permission.CAMERA
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, notifPerm) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(notifPerm)
            }
        }
    }

    val exportCsvLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { os ->
                scope.launch {
                    val db = AppDatabase.getDatabase(context)
                    val workouts = db.antrenamentDao().getAllForUser(userId)
                    val exercises = mutableMapOf<Long, List<ExercitiuEntity>>()
                    for (w in workouts) {
                        exercises[w.id] = db.exercitiuDao().getForAntrenament(w.id)
                    }
                    os.bufferedWriter().use { w ->
                        w.write("Date,Group,Exercise,Set,WeightKg,Reps")
                        w.newLine()
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        for (aw in workouts) {
                            val exList = exercises[aw.id] ?: emptyList()
                            for (ex in exList) {
                                w.write("${sdf.format(java.util.Date(aw.data))},${aw.grupaMusculara},${ex.numeExercitiu},${ex.setIndex + 1},${ex.greutateKg},${ex.repetari}")
                                w.newLine()
                            }
                        }
                    }
                }
            }
        }
    }

    val importCsvLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val importer = CsvImporter(context)
            val sessions = importer.importWorkouts(it)
            val repo = AntrenamentRepository(AppDatabase.getDatabase(context))
            scope.launch {
                for (session in sessions) {
                    for (ex in session.exercitii) {
                        repo.salveazaAntrenamentSimple(userId, session.grupaMusculara, ex.numeExercitiu, ex.seturi, "")
                    }
                }
            }
            reloadToken++
        }
    }

    val authManager = remember { AuthManager(context) }
    val loginHandler = remember { LoginHandler(context, preferencesManager, userProfileManager, authManager, scope) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }

    val googleSignInClient = remember {
        val clientId = preferencesManager.getGoogleOAuthClientId()
            .ifBlank { context.getString(R.string.default_web_client_id) }
        if (clientId.isNotBlank()) {
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build()
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
        } else null
    }

    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                kotlinx.coroutines.MainScope().launch {
                    val authResult = loginHandler.loginWithGoogle(idToken)
                    authResult.onSuccess {
                        isLoggedIn = true
                    }.onFailure {
                        googleSignInError = it.message
                    }
                }
            }
        } catch (e: Exception) {
            googleSignInError = e.message
        }
    }

    val facebookSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Facebook login callback - handled via CallbackManager
    }

    // Onboarding — shown after sign-up or when onboarding hasn't been completed yet.
    if (showOnboarding) {
        OnboardingScreen(
            strings = strings,
            onProfileComplete = { profile ->
                preferencesManager.setFitnessGoal(profile.goal)
                preferencesManager.setExperienceLevel(profile.experience)
                preferencesManager.setEquipmentAvailable(profile.equipment)
                preferencesManager.setSessionsPerWeek(profile.sessionsPerWeek)
                preferencesManager.setPhysicalLimitations(profile.limitations)
                preferencesManager.setSelectedMuscleGroups(profile.selectedGroups)
                preferencesManager.setUserAge(profile.age)
                preferencesManager.setUserGender(profile.gender)
                preferencesManager.setActivityLevel(profile.activityLevel)
                preferencesManager.setUserWeight(profile.weight)
                preferencesManager.setUserHeight(profile.height)
                preferencesManager.setOnboardingComplete(true)
                preferencesManager.setWorkoutStartDate(java.time.LocalDate.now())
                preferencesManager.setLoggedIn(true)
                preferencesManager.setAutoDeloadEnabled(false)
                showOnboarding = false
                isLoggedIn = true
            }
        )
        return
    }

    if (!isLoggedIn) {
        val isDark = when (currentThemeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        }
        LoginScreen(
            strings = strings,
            isDark = isDark,
            error = googleSignInError,
            isGoogleAvailable = googleSignInClient != null,
            isFacebookAvailable = false,
            onEmailLogin = { email, password ->
                kotlinx.coroutines.MainScope().launch {
                    loginHandler.loginWithEmail(email, password).onSuccess {
                        isLoggedIn = true
                    }.onFailure {
                        googleSignInError = it.message
                    }
                }
            },
            onGoogleLogin = {
                googleSignInError = null
                googleSignInClient?.signInIntent?.let { intent ->
                    googleSignInLauncher.launch(intent)
                }
            },
            onFacebookLogin = {
                googleSignInError = "Facebook login is not yet configured."
            },
            onGuestLogin = {
                kotlinx.coroutines.MainScope().launch {
                    loginHandler.loginAsGuest().onSuccess {
                        isLoggedIn = true
                    }.onFailure {
                        googleSignInError = it.message
                    }
                }
            },
            onSignUpClick = { showSignUp = true },
            onLanguageClick = { showLanguageDialog = true },
            onForgotPassword = { email ->
                if (email.isBlank()) {
                    googleSignInError = "Please enter your email address"
                } else {
                    kotlinx.coroutines.MainScope().launch {
                        authManager.sendPasswordResetEmail(email).onSuccess {
                            googleSignInError = "Password reset email sent. Check your inbox."
                        }.onFailure {
                            googleSignInError = it.message
                        }
                    }
                }
            }
        )

        if (showSignUp) {
            val isDark2 = when (currentThemeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            SignUpScreen(
                strings = strings,
                isDark = isDark2,
                error = googleSignInError,
                onSignUp = { name, email, password, _, _ ->
                    kotlinx.coroutines.MainScope().launch {
                        googleSignInError = null
                        authManager.signUpWithEmail(email, password).onSuccess { firebaseUser ->
                            authManager.updateDisplayName(name)
                            userProfileManager.createOrUpdateProfile(name = name, userId = firebaseUser.uid)
                            preferencesManager.resetOnboarding()
                            preferencesManager.setLoginMethod("email")
                            showOnboarding = true
                        }.onFailure {
                            googleSignInError = it.message
                        }
                    }
                },
                onGoogleSignUp = {},
                onFacebookSignUp = {},
                onLoginClick = { showSignUp = false },
                onLanguageClick = { showLanguageDialog = true },
                emailAlreadyInUseAction = { showSignUp = false }
            )
        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                isDark = isDark,
                currentLanguage = currentLanguage,
                strings = strings,
                onSelect = { code ->
                    LanguageManager.saveLanguage(context, code)
                    currentLanguage = code
                    reloadToken++
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        return
    }

    BackHandler {
        when {
            showPricing -> { showPricing = false }
            quickExerciseGrupa != null -> {
                if (quickExerciseList.isNotEmpty()) showTodayWorkout = true
                quickExerciseGrupa = null; quickExerciseName = null; quickExerciseList = emptyList(); quickExerciseIndex = 0
            }
            showTodayWorkout -> { showTodayWorkout = false }
            showLanguageDialog -> { showLanguageDialog = false }
            showUnitsDialog -> { showUnitsDialog = false }
            showOnboarding -> { showOnboarding = false }
            showSignUp -> { showSignUp = false }
            showServerDialog -> { showServerDialog = false }
            isWorkoutFlowActive -> workoutNavController.popBackStack()
            showTemplates -> { showTemplates = false; currentPage = null }
            showCalendar -> { showCalendar = false; currentPage = null }
            showBiometricInput -> { showBiometricInput = false; currentPage = null }
            showBiometricCharts -> { showBiometricCharts = false; currentPage = null }
            showFoodJournal -> { showFoodJournal = false; currentPage = null }
            showBarcodeScanner -> { showBarcodeScanner = false; showFoodJournal = true }
            showAddFood -> { showAddFood = false; showFoodJournal = true }
            showAiTrainer -> { showAiTrainer = false; currentPage = null }
            showFriends -> { showFriends = false; currentPage = null }
            showLeaderboard -> { showLeaderboard = false; showFriends = true }
            showPlateCalculator -> { showPlateCalculator = false; currentPage = null }
            showOneRMCalculator -> { showOneRMCalculator = false; currentPage = null }
            showWorkoutAnalytics -> { showWorkoutAnalytics = false; currentPage = null }
            showSavedExercises -> { showSavedExercises = false; currentPage = null }
            showWeightGoal -> { showWeightGoal = false; currentPage = null }
            showBodyFatCalculator -> { showBodyFatCalculator = false; currentPage = null }
            currentPage != null -> currentPage = null
            currentDashboardTab != 0 -> currentDashboardTab = 0
            drawerState.isOpen -> { /* let drawer close itself */ }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
                DrawerMenu(
                    profileName = profileName,
                    profilePhotoUri = profilePhoto,
                    profilePhotoVersion = profilePhotoVersion,
                    userId = userId,
                    shortId = userShortId,
                    currentPage = currentPage,
                    isLbs = isLbs,
                    isDark = isDark,
                    currentLanguage = currentLanguage,
                    badgeCount = badgeCount,
                    currentStreak = currentStreak,
                    pendingRequestsCount = pendingRequestsCount,
                    onNavigate = { page ->
                        currentPage = page
                        when (page) {
                            null -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.CALENDAR -> { showCalendar = true; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.FOOD_JOURNAL -> { showCalendar = false; showTemplates = false; showFoodJournal = true; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.AI_TRAINER -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = true; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.FRIENDS -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = true; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.GPS_CARDIO -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.REST_DAYS -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.PLATE_CALCULATOR -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = true; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.ONE_RM_CALCULATOR -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = true; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.WORKOUT_ANALYTICS -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = true; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.SAVED_EXERCISES -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = true; showWeightGoal = false; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.WEIGHT_GOAL -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = true; showBodyFatCalculator = false; workoutNavController.popToWorkoutHome() }
                            DrawerPage.BODY_FAT_CALCULATOR -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; showSavedExercises = false; showWeightGoal = false; showBodyFatCalculator = true; workoutNavController.popToWorkoutHome() }
                        }
                    },
                    onExportCsv = {
                        exportCsvLauncher.launch("kinetic_export.csv")
                    },
                    onImportCsv = {
                        importCsvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/*"))
                    },
                    onLogout = {
                        preferencesManager.setLoggedIn(false)
                        preferencesManager.setLoginMethod("")
                        authManager.signOut()
                        isLoggedIn = false
                    },
                    onLanguageSelected = { code ->
                        LanguageManager.saveLanguage(context, code)
                        currentLanguage = code
                        reloadToken++
                    },
                    onOpenLanguageDialog = { showLanguageDialog = true },
                    onOpenUnitsDialog = { showUnitsDialog = true },
                    strings = strings,
                    onClose = { scope.launch { drawerState.close() } },
                    onToggleTheme = {
                        val performToggle = {
                            val newMode = if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                            preferencesManager.setThemeMode(newMode)
                            currentThemeMode = newMode
                            onThemeChanged(newMode)
                        }
                        val scrim = themeScrim
                        if (scrim != null) {
                            // Crossfade SEMI-TRANSPARENT: scrim-ul atinge doar ~65% opacitate,
                            // deci ecranul NU devine solid — conținutul rămâne vizibil prin el
                            // pe tot parcursul tranziției. Culorile se schimbă sub scrim,
                            // apoi acesta se estompează. Un singur nod animat, zero lag.
                            scope.launch {
                                scrim.snapTo(0f)
                                scrim.animateTo(0.65f, tween(110, easing = FastOutSlowInEasing))
                                performToggle()
                                scrim.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                            }
                        } else {
                            performToggle()
                        }
                    },
                    onOpenServerSettings = { showServerDialog = true },
                    onOpenPricing = { showPricing = true },
                    isPremium = subscription.isPremium || AdminManager.isAdmin(userEmail)
                )
            }
        ) {
        // PiP activ pe GPS Cardio: randÄƒm doar harta, fÄƒrÄƒ Scaffold/bottom bar
        if (GpsTrackingState.isInPipMode && currentPage == DrawerPage.GPS_CARDIO) {
            GpsCardioScreen(
                isDark = isDark,
                strings = strings,
                userId = userId,
                onBack = { currentPage = null }
            )
            return@ModalNavigationDrawer
        }

        // Content inside drawer
        val p = appPalette(isDark)
        val surfaceBg = p.bg
        val textPrimary = p.tp
        val textSecondary = p.ts
        val cardBg = p.card
        val accent = p.ac
        val iconBg = if (isDark) IconBackground else LightIconBackground

        if (!subscriptionLoaded) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(surfaceBg),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = accent)
            }
            return@ModalNavigationDrawer
        }

        CompositionLocalProvider(
            LocalKineticHeader provides KineticHeaderController(
                isDark = isDark,
                onOpenMenu = { scope.launch { drawerState.open() } }
            )
        ) {
        Scaffold(
            containerColor = surfaceBg,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .animateContentSize(tween(300))
            ) {
                if (showTemplates && !subscription.hasAccess(PremiumFeature.TEMPLATES, userEmail)) {
                    LockedFeatureScreen(
                        feature = PremiumFeature.TEMPLATES,
                        featureLabel = strings.templates,
                        strings = strings,
                        isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                        onWatchAd = { watchAdToUnlock(PremiumFeature.TEMPLATES) },
                        onUpgrade = { showTemplates = false; showPricing = true },
                        onBack = { showTemplates = false; currentPage = null }
                    )
                } else if (showTemplates) {
                    TemplateScreen(onBackClick = { showTemplates = false; currentPage = null })
                } else if (showCalendar) {
                    CalendarWorkoutScreen(
                        isDark = isDark,
                        onBackClick = { showCalendar = false; currentPage = null },
                        onWorkoutDeleted = { reloadToken++ },
                        userId = userId
                    )
                } else if (showBiometricInput && subscription.hasAccess(PremiumFeature.BIOMETRICS, userEmail)) {
                    BiometricInputScreen(
                        isDark = isDark,
                        strings = strings,
                        latestEntry = lastBiometric,
                        onSave = { weight, bf, waist, hips, thighs, chest, arms ->
                            scope.launch {
                                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                                val db = AppDatabase.getDatabase(context)
                                                                val prefs = PreferencesManager(context, userProfileManager)
                                                                val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                                                                val bm = BiometricManager(db, syncRepo)
                                                                bm.saveEntry(userId, weight, bf, waist, hips, thighs, chest, arms)
                                                                // Update user weight in preferences
                                                                prefs.setUserWeight(weight.toFloat())
                                                                lastBiometric = bm.getLatest(userId)
                                                                allBiometrics = bm.getAll(userId)
                                                                weeksSinceMeasurement = bm.getWeeksSinceLastMeasurement(userId)
                                                            }
                                                        }
                            showBiometricInput = false
                            reloadToken++
                        },
                        onBack = { showBiometricInput = false }
                    )
                } else if (showBiometricInput) {
                    LockedFeatureScreen(
                        feature = PremiumFeature.BIOMETRICS,
                        featureLabel = strings.biometricTracking,
                        strings = strings,
                        isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                        onWatchAd = { watchAdToUnlock(PremiumFeature.BIOMETRICS) },
                        onUpgrade = { showBiometricInput = false; showPricing = true },
                        onBack = { showBiometricInput = false; currentPage = null }
                    )
                } else if (showBiometricCharts && subscription.hasAccess(PremiumFeature.BIOMETRICS, userEmail)) {
                    BiometricChartScreen(
                        isDark = isDark,
                        strings = strings,
                        entries = allBiometrics,
                        onDelete = { entry ->
                            scope.launch {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val prefs = PreferencesManager(context, userProfileManager)
                                    val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                                    val bm = BiometricManager(db, syncRepo)
                                    bm.delete(entry)
                                    lastBiometric = bm.getLatest(userId)
                                    allBiometrics = bm.getAll(userId)
                                    weeksSinceMeasurement = bm.getWeeksSinceLastMeasurement(userId)
                                }
                            }
                        },
                        onBack = { showBiometricCharts = false }
                    )
                } else if (showBiometricCharts) {
                    LockedFeatureScreen(
                        feature = PremiumFeature.BIOMETRICS,
                        featureLabel = strings.biometricTracking,
                        strings = strings,
                        isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                        onWatchAd = { watchAdToUnlock(PremiumFeature.BIOMETRICS) },
                        onUpgrade = { showBiometricCharts = false; showPricing = true },
                        onBack = { showBiometricCharts = false; currentPage = null }
                    )
                } else if (showFoodJournal && !subscription.hasAccess(PremiumFeature.FOOD_JOURNAL, userEmail)) {
                    LockedFeatureScreen(
                        feature = PremiumFeature.FOOD_JOURNAL,
                        featureLabel = strings.foodJournal,
                        strings = strings,
                        isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                        onWatchAd = { watchAdToUnlock(PremiumFeature.FOOD_JOURNAL) },
                        onUpgrade = { showFoodJournal = false; showPricing = true },
                        onBack = { showFoodJournal = false; currentPage = null }
                    )
                } else if (showFoodJournal) {
                    FoodJournalScreen(
                        isDark = isDark,
                        strings = strings,
                        entries = foodEntries,
                        onDelete = { entry ->
                            scope.launch {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val fm = FoodManager(db)
                                    fm.delete(entry)
                                    foodEntries = fm.getAll(userId)
                                }
                            }
                        },
                        onScanBarcode = {
                            showFoodJournal = false
                            val cameraPerm = android.Manifest.permission.CAMERA
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, cameraPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                showBarcodeScanner = true
                            } else {
                                cameraPermissionLauncher.launch(cameraPerm)
                            }
                        },
                        onAddManual = { showFoodJournal = false; showAddFood = true },
                        onBack = { showFoodJournal = false; currentPage = null },
                        preferencesManager = preferencesManager
                    )
                } else if (showBarcodeScanner) {
                    BarcodeScannerScreen(
                        isDark = isDark,
                        strings = strings,
                        onBarcodeScanned = { barcode ->
                            showBarcodeScanner = false
                            scope.launch {
                                val product = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    OpenFoodFactsApi.getProduct(barcode)
                                }
                                pendingFoodProduct = product
                                showAddFood = true
                                android.util.Log.d("BarcodeScan", "barcode=$barcode, found=${product.found}, name=${product.name}, cal=${product.calories}")
                            }
                        },
                        onBack = { showBarcodeScanner = false; showFoodJournal = true }
                    )
                } else if (showAddFood) {
                    AddFoodScreen(
                        isDark = isDark,
                        strings = strings,
                        prefilledProduct = pendingFoodProduct,
                        onSave = { name, brand, mealType, servingSize, calories, protein, carbs, fat, fiber, servingUnit ->
                            scope.launch {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val prefs = PreferencesManager(context, userProfileManager)
                                    val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                                    val fm = FoodManager(db, syncRepo)
                                    fm.addFood(
                                        userId = userId,
                                        barcode = pendingFoodProduct?.barcode ?: "",
                                        name = name,
                                        brand = brand,
                                        mealType = mealType,
                                        servingSize = servingSize,
                                        servingUnit = servingUnit,
                                        calories = calories,
                                        proteinG = protein,
                                        carbsG = carbs,
                                        fatG = fat,
                                        fiberG = fiber
                                    )
                                    foodEntries = fm.getAll(userId)
                                    pendingFoodProduct = null
                                }
                            }
                            showAddFood = false
                            showFoodJournal = true
                        },
                        onBack = { showAddFood = false; pendingFoodProduct = null; showFoodJournal = true }
                    )
                } else if (showAiTrainer) {
                    if (subscription.hasAccess(PremiumFeature.AI_TRAINER, userEmail)) {
                        val db = remember { AppDatabase.getDatabase(context) }
                        val manager = remember { AiTrainerManager(db) }
                        AiTrainerScreen(
                            aiManager = manager,
                            isDark = isDark,
                            strings = strings,
                            userId = userId,
                            preferencesManager = preferencesManager,
                            onBack = { showAiTrainer = false; currentPage = null },
                            onOpenMenu = { scope.launch { drawerState.open() } }
                        )
                    } else {
                        LockedFeatureScreen(
                            feature = PremiumFeature.AI_TRAINER,
                            featureLabel = strings.aiTrainer,
                            strings = strings,
                            isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                            onWatchAd = { watchAdToUnlock(PremiumFeature.AI_TRAINER) },
                            onUpgrade = { showAiTrainer = false; showPricing = true },
                            onBack = { showAiTrainer = false; currentPage = null }
                        )
                    }
                } else if (showLeaderboard) {
                    LeaderboardScreen(
                        isDark = isDark,
                        isLbs = isLbs,
                        strings = strings,
                        onBackClick = { showLeaderboard = false; showFriends = true }
                    )
                } else if (showFriends) {
                    if (subscription.hasAccess(PremiumFeature.FRIENDS_SOCIAL, userEmail)) {
                        FriendsScreen(
                            isDark = isDark,
                            isLbs = isLbs,
                            strings = strings,
                            onBackClick = { showFriends = false; currentPage = null },
                            onOpenLeaderboard = { showFriends = false; showLeaderboard = true }
                        )
                    } else {
                        LockedFeatureScreen(
                            feature = PremiumFeature.FRIENDS_SOCIAL,
                            featureLabel = strings.friends,
                            strings = strings,
                            isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                            onWatchAd = { watchAdToUnlock(PremiumFeature.FRIENDS_SOCIAL) },
                            onUpgrade = { showFriends = false; showPricing = true },
                            onBack = { showFriends = false; currentPage = null }
                        )
                    }
                } else if (showPlateCalculator) {
                    if (subscription.hasAccess(PremiumFeature.PLATE_CALCULATOR, userEmail)) {
                        PlateCalculatorScreen(
                            isDark = isDark,
                            strings = strings,
                            onBack = { showPlateCalculator = false; currentPage = null }
                        )
                    } else {
                        LockedFeatureScreen(
                            feature = PremiumFeature.PLATE_CALCULATOR,
                            featureLabel = "Plate Calculator",
                            strings = strings,
                            isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                            onWatchAd = { watchAdToUnlock(PremiumFeature.PLATE_CALCULATOR) },
                            onUpgrade = { showPlateCalculator = false; showPricing = true },
                            onBack = { showPlateCalculator = false; currentPage = null }
                        )
                    }
                } else if (showOneRMCalculator) {
                    if (subscription.hasAccess(PremiumFeature.ONE_RM_CALCULATOR, userEmail)) {
                        OneRMCalculatorScreen(
                            isDark = isDark,
                            strings = strings,
                            onBack = { showOneRMCalculator = false; currentPage = null }
                        )
                    } else {
                        LockedFeatureScreen(
                            feature = PremiumFeature.ONE_RM_CALCULATOR,
                            featureLabel = "1RM Calculator",
                            strings = strings,
                            isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                            onWatchAd = { watchAdToUnlock(PremiumFeature.ONE_RM_CALCULATOR) },
                            onUpgrade = { showOneRMCalculator = false; showPricing = true },
                            onBack = { showOneRMCalculator = false; currentPage = null }
                        )
                    }
                } else if (showWorkoutAnalytics) {
                    if (subscription.hasAccess(PremiumFeature.WORKOUT_ANALYTICS, userEmail)) {
                        Scaffold(
                            containerColor = surfaceBg,
                            topBar = { KineticAppBar(onBack = { showWorkoutAnalytics = false }) }
                        ) { pad ->
                            Box(modifier = Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                                Text(strings.workoutAnalytics, color = textPrimary, fontSize = 20.sp)
                            }
                        }
                    } else {
                        LockedFeatureScreen(
                            feature = PremiumFeature.WORKOUT_ANALYTICS,
                            featureLabel = strings.workoutAnalytics,
                            strings = strings,
                            isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                            onWatchAd = { watchAdToUnlock(PremiumFeature.WORKOUT_ANALYTICS) },
                            onUpgrade = { showWorkoutAnalytics = false; showPricing = true },
                            onBack = { showWorkoutAnalytics = false; currentPage = null }
                        )
                    }
                } else if (showSavedExercises) {
                    SavedExercisesScreen(
                        userId = userId,
                        isDark = isDark,
                        isLbs = isLbs,
                        onBackClick = { showSavedExercises = false; currentPage = null },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onWorkoutSaved = { reloadToken++; badgeCheckTrigger++ },
                        strings = strings
                    )
                } else if (showWeightGoal) {
                    val weightGoalDb = AppDatabase.getDatabase(context)
                    val weightForGoal = if (lastBiometric != null && lastBiometric!!.weightKg > 0) {
                        lastBiometric!!.weightKg
                    } else {
                        preferencesManager.getUserWeight().toDouble()
                    }
                    WeightGoalScreen(
                        isDark = isDark,
                        strings = strings,
                        db = weightGoalDb,
                        userId = userId,
                        latestWeight = weightForGoal,
                        heightCm = preferencesManager.getUserHeight(),
                        bodyFatPercent = lastBiometric?.bodyFatPercent ?: 0.0,
                        waistCm = lastBiometric?.waistCm ?: 0.0,
                        onBack = { showWeightGoal = false; currentPage = null },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                } else if (showBodyFatCalculator) {
                    val bfcWeight = if (lastBiometric != null && lastBiometric!!.weightKg > 0) {
                        lastBiometric!!.weightKg
                    } else {
                        preferencesManager.getUserWeight().toDouble()
                    }
                    BodyFatCalculatorScreen(
                        isDark = isDark,
                        strings = strings,
                        latestWeight = bfcWeight,
                        heightCm = preferencesManager.getUserHeight(),
                        onBack = { showBodyFatCalculator = false; currentPage = null },
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                } else if (currentPage == DrawerPage.GPS_CARDIO) {
                    if (subscription.hasAccess(PremiumFeature.GPS_CARDIO, userEmail)) {
                        GpsCardioScreen(
                            isDark = isDark,
                            strings = strings,
                            userId = userId,
                            onBack = { currentPage = null }
                        )
                    } else {
                        LockedFeatureScreen(
                            feature = PremiumFeature.GPS_CARDIO,
                            featureLabel = strings.gpsCardioMap,
                            strings = strings,
                            isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                            onWatchAd = { watchAdToUnlock(PremiumFeature.GPS_CARDIO) },
                            onUpgrade = { currentPage = null; showPricing = true },
                            onBack = { currentPage = null }
                        )
                    }
                } else if (currentPage == DrawerPage.REST_DAYS) {
                    if (subscription.hasAccess(PremiumFeature.REST_DAYS, userEmail)) {
                        RestDayScreen(
                            isDark = isDark,
                            strings = strings,
                            userId = userId,
                            recoveryMap = recoveryMap,
                            onBack = { currentPage = null }
                        )
                    } else {
                        LockedFeatureScreen(
                            feature = PremiumFeature.REST_DAYS,
                            featureLabel = strings.restDaysTitle,
                            strings = strings,
                            isAdReady = gymApp?.adUnlockManager?.isAdReady() == true,
                            onWatchAd = { watchAdToUnlock(PremiumFeature.REST_DAYS) },
                            onUpgrade = { currentPage = null; showPricing = true },
                            onBack = { currentPage = null }
                        )
                    }
                } else if (selectedTemplate != null) {
                    TemplateDetailScreen(
                        template = selectedTemplate!!,
                        isLbs = isLbs,
                        isDark = isDark,
                        onBackClick = { selectedTemplate = null },
                        onBackToMain = { currentDashboardTab = 0; selectedTemplate = null },
                        onWorkoutSaved = { reloadToken++; badgeCheckTrigger++ }
                    )
                } else if (quickExerciseGrupa != null && quickExerciseName != null) {
                    if (selectedProgressFromQuick != null) {
                        CalendarScreen(
                            isLbs = isLbs,
                            initialExercise = selectedProgressFromQuick,
                            isDark = isDark,
                            onBackClick = { selectedProgressFromQuick = null }
                        )
                    } else {
                    ExerciseInputScreen(
                        exercise = ExerciseDefinition(quickExerciseName!!, quickExerciseGrupa!!),
                        grupaMusculara = quickExerciseGrupa!!,
                        isLbs = isLbs,
                        isDark = isDark,
                        onBackClick = {
                            // Back from an exercise always returns to the main Today's Workout page
                            if (quickExerciseList.isNotEmpty()) {
                                showTodayWorkout = true
                            }
                            quickExerciseGrupa = null
                            quickExerciseName = null
                            quickExerciseList = emptyList()
                            quickExerciseIndex = 0
                        },
                        onNextExercise = if (quickExerciseIndex < quickExerciseList.size - 1) {
                            {
                                quickExerciseIndex++
                                quickExerciseGrupa = quickExerciseList[quickExerciseIndex].first
                                quickExerciseName = quickExerciseList[quickExerciseIndex].second
                            }
                        } else {
                            {
                                quickExerciseGrupa = null
                                quickExerciseName = null
                                quickExerciseList = emptyList()
                                quickExerciseIndex = 0
                                reloadToken++
                                badgeCheckTrigger++
                            }
                        },
                        onOpenProgress = { name ->
                            selectedProgressFromQuick = name
                        },
                        onWorkoutSaved = { reloadToken++; badgeCheckTrigger++ },
                        strings = strings,
                        currentIndex = quickExerciseIndex,
                        totalExercises = quickExerciseList.size,
                        nextExerciseName = quickExerciseList.getOrNull(quickExerciseIndex + 1)?.second,
                        nextExerciseSets = ""
                    )
                    }
                } else if (showTodayWorkout) {
                    val equipKey = preferencesManager.getEquipmentAvailable()
                    val onboardingProfile = remember(equipKey, profileChangedTrigger) { preferencesManager.getOnboardingProfile() }
                    val todayWorkoutData = mainViewModel.todayWorkout.collectAsState()
                    val exerciseSummaries by mainViewModel.exerciseSummaries.collectAsState()
                    var isTodayScheduledRest by remember { mutableStateOf(false) }
                    LaunchedEffect(showTodayWorkout, equipKey, profileChangedTrigger) {
                        val db = AppDatabase.getDatabase(context)
                        val today = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        val tomorrow = today + 86400000L
                        val uid = UserProfileManager(context).getOwnUserId()
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val todayRestDay = db.restDayDao().getAllForUser(uid).find { it.date in today until tomorrow }
                            isTodayScheduledRest = todayRestDay != null
                        }
                        mainViewModel.computeTodayWorkout(preferencesManager)
                        val workout = mainViewModel.todayWorkout.value
                        if (workout != null) {
                            val names = workout.exercises.map { it.name }
                            val userId = UserProfileManager(context).getOwnUserId()
                            mainViewModel.loadExerciseSummaries(userId, names)
                        }
                    }
                            TodayWorkoutScreen(
                                todayWorkout = if (isTodayScheduledRest) null else todayWorkoutData.value,
                                strings = strings,
                                isDark = isDark,
                                onStartExercise = { grupa, exerciseName ->
                                    val flatList = (todayWorkoutData.value?.exercises ?: emptyList()).map { Pair(it.group, it.name) }
                                    quickExerciseList = flatList
                                    quickExerciseIndex = flatList.indexOfFirst { it.first == grupa && it.second == exerciseName }.coerceAtLeast(0)
                                    showTodayWorkout = false
                                    quickExerciseGrupa = grupa
                                    quickExerciseName = exerciseName
                                },
                                onSaveExercise = { exerciseName, group ->
                                    val uid = UserProfileManager(context).getOwnUserId()
                                    mainViewModel.toggleFavorite(exerciseName, userId = uid, group = group)
                                },
                                onOpenSpotify = { openSpotifyApp(context) },
                                onBack = { showTodayWorkout = false },
                                exerciseSummaries = exerciseSummaries,
                                recoveryMap = recoveryMap
                            )
                } else if (isWorkoutFlowActive) {
                    WorkoutNavHost(
                        navController = workoutNavController,
                        isLbs = isLbs,
                        isDark = isDark,
                        strings = strings,
                        onWorkoutSaved = { reloadToken++; badgeCheckTrigger++ }
                    )
                } else if (currentPage == DrawerPage.CALENDAR) {
                    CalendarWorkoutScreen(
                        isDark = isDark,
                        onBackClick = { currentPage = null },
                        onWorkoutDeleted = { reloadToken++ },
                        userId = userId
                    )
                } else {
                    Scaffold(
                        containerColor = surfaceBg,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        topBar = {
                            KineticAppBar()
                        }
                    ) { innerPadding ->
                        if (currentDashboardTab == 0) {
                            val equipKey = preferencesManager.getEquipmentAvailable()
                            val onboardingProfile = remember(equipKey, profileChangedTrigger) { preferencesManager.getOnboardingProfile() }
                            val todayWorkoutData = mainViewModel.todayWorkout.collectAsState()
                            var deloadDue by remember(equipKey, profileChangedTrigger) { mutableStateOf(false) }
                            LaunchedEffect(equipKey, profileChangedTrigger) {
                                mainViewModel.computeTodayWorkout(preferencesManager)
                                val uid = UserProfileManager(context).getOwnUserId()
                                val repo = AntrenamentRepository(AppDatabase.getDatabase(context))
                                deloadDue = preferencesManager.isAutoDeloadEnabled() &&
                                    repo.shouldTriggerDeload(uid, preferencesManager.getDeloadIntervalWeeks()) != null &&
                                    !preferencesManager.isDeloadActive()
                            }
                            DashboardScreen(
                                state = DashboardUiState(
                                    profileName = profileName,
                                    weekWorkoutCount = weekWorkoutCount,
                                    weekVolume = weekVolume,
                                    weekWorkoutDurationMs = weekWorkoutDurationMs,
                                    lastWeekWorkoutCount = lastWeekWorkoutCount,
                                    lastWeekVolume = lastWeekVolume,
                                    currentStreak = currentStreak,
                                    bestStreak = bestStreak,
                                    weeklyTopExercise = weeklyTopExercise,
                                    todayCardioDistance = todayCardioDistance,
                                    todayCardioDuration = todayCardioDuration,
                                    todayCardioCalories = todayCardioCalories,
                                    totalSteps = totalSteps
                                ),
                                todayWorkout = todayWorkoutData.value,
                                strings = strings,
                                isDark = isDark,
                                isLbs = isLbs,
                                onboardingProfile = onboardingProfile,
                                profilePhotoUri = profilePhoto,
                                profilePhotoVersion = profilePhotoVersion,
                                bottomPadding = paddingValues,
                                innerPadding = innerPadding,
                                onStartWorkout = {
                                    showTodayWorkout = true
                                },
                                onExerciseClick = { grupa, exerciseName ->
                                    quickExerciseGrupa = grupa
                                    quickExerciseName = exerciseName
                                },
                                onSetStepGoal = { goal ->
                                    stepGoal = goal
                                    preferencesManager.setStepGoal(goal)
                                    refreshStepsWidget()
                                },
                                stepGoal = stepGoal,
                                deloadDue = deloadDue,
                                onOpenDeload = { currentPage = DrawerPage.REST_DAYS }
                            )
                        } else if (currentDashboardTab == 1) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    val listState = rememberLazyListState()

                                    // Tab-urile nu mai sunt sticky — fac parte din hero-ul care se ridică la scroll

                                    // Hero ascuns la scroll în jos, reapare la scroll în sus
                                    var heroHidden by remember { mutableStateOf(false) }
                                    var lastHeroScrollTop by remember { mutableIntStateOf(0) }
                                    var selectedMuscleGroup by remember { mutableStateOf<String?>(null) }
                                    LaunchedEffect(listState) {
                                        snapshotFlow {
                                            listState.firstVisibleItemIndex * 100000 + listState.firstVisibleItemScrollOffset
                                        }
                                            .distinctUntilChanged()
                                            .collect { scrollTop ->
                                                val delta = scrollTop - lastHeroScrollTop
                                                if (delta > 32) heroHidden = true
                                                else if (delta < -32) heroHidden = false
                                                lastHeroScrollTop = scrollTop
                                            }
                                    }

                                    AnimatedVisibility(
                                        // Ascunde hero-ul (butonul Today's Workout + taburile Template/Muscle Groups)
                                        // când utilizatorul vede exercițiile unei grupe musculare selectate
                                        visible = !heroHidden && selectedMuscleGroup == null,
                                        enter = expandVertically(
                                            expandFrom = Alignment.Top,
                                            animationSpec = tween(260)
                                        ) + fadeIn(tween(260)),
                                        exit = shrinkVertically(
                                            shrinkTowards = Alignment.Top,
                                            animationSpec = tween(260)
                                        ) + fadeOut(tween(260))
                                    ) {
                                        Column {
                                            Button(
                                                onClick = { showTodayWorkout = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = accent),
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                                    .height(48.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    strings.todaysWorkout.uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 2.sp,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            // Cardul cu „TEMPLATE / MUSCLE GROUPS" se ridică împreună cu hero-ul
                                            WorkoutSubTabs(
                                                subTab = muscleGroupsSubTab,
                                                onSubTabChange = { muscleGroupsSubTab = it },
                                                strings = strings,
                                                accent = accent,
                                                textSecondary = textSecondary,
                                                surfaceBg = surfaceBg,
                                                alpha = 0f
                                            )
                                        }
                                    }

                                    if (muscleGroupsSubTab == 0) {
                                        LazyColumn(
                                            state = listState,
                                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            items(DataProvider.templateuri) { template ->
                                                val gradientColors = templateGradient(template.nume)
                                                val estimatedDuration = template.exercitii.size * 3
                                                val totalSets = template.exercitii.size * 4
                                                val muscleGroups = templateMuscleGroups(template)

                                                AppGlassCard(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                        .clickable { selectedTemplate = template },
                                                    p = p,
                                                    cornerRadius = 24.dp,
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(
                                                                    Brush.linearGradient(
                                                                        colors = listOf(
                                                                            gradientColors.first().copy(alpha = if (isDark) 0.26f else 0.12f),
                                                                            Color.Transparent,
                                                                            Color.Transparent
                                                                        ),
                                                                        start = Offset(0f, 0f),
                                                                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                                                    )
                                                                )
                                                        )
                                                        Image(
                                                            painter = painterResource(id = templateIcon(template.nume.take(20))),
                                                            contentDescription = null,
                                                            modifier = Modifier
                                                                .fillMaxHeight()
                                                                .width(140.dp)
                                                                .align(Alignment.CenterEnd)
                                                                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                                                                .alpha(0.55f),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(22.dp),
                                                            verticalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Column {
                                                                Text(
                                                                    template.nume.uppercase(),
                                                                    fontFamily = Varien,
                                                                    color = p.tp,
                                                                    fontWeight = FontWeight.Black,
                                                                    fontSize = 24.sp,
                                                                    letterSpacing = 4.sp
                                                                )
                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                Text(
                                                                    "${template.exercitii.size} ${strings.exercises}  ·  ~${estimatedDuration}min  ·  ${totalSets} ${strings.sets}",
                                                                    color = p.ts,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                LazyRow(
                                                                    modifier = Modifier.weight(1f),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    items(muscleGroups) { mg ->
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(20.dp))
                                                                                .background(p.acs)
                                                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                                        ) {
                                                                            Text(
                                                                                LanguageManager.translateMuscleGroup(mg, strings),
                                                                                color = p.ac,
                                                                                fontSize = 11.sp,
                                                                                fontWeight = FontWeight.SemiBold
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(36.dp)
                                                                                                                                                .background(p.acg, CircleShape),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        Icons.Default.ChevronRight,
                                                                        contentDescription = null,
                                                                        tint = p.ac,
                                                                        modifier = Modifier.size(22.dp)
                                                                    )
                        }
                    }
                }
            }
        }
                                            }
                                        }
                                    } else {
                                        var selectedEquipment by remember { mutableStateOf<String?>(null) }
                                        var selectedExerciseFromMG by remember { mutableStateOf<ExerciseDefinition?>(null) }
                                        var selectedProgressFromMG by remember { mutableStateOf<String?>(null) }
                                        var mgSearchQuery by remember { mutableStateOf("") }
                                        var mgIsListening by remember { mutableStateOf(false) }
                                        val mgSpeechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
                                        fun mgStartVoiceListening() {
                                            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                                                android.widget.Toast.makeText(context, strings.voiceSearchError, android.widget.Toast.LENGTH_SHORT).show()
                                                return
                                            }
                                            mgIsListening = true
                                            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                                                putExtra(RecognizerIntent.EXTRA_PROMPT, strings.voiceSearch)
                                            }
                                            mgSpeechRecognizer.setRecognitionListener(object : RecognitionListener {
                                                override fun onResults(results: Bundle?) {
                                                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                                    if (!matches.isNullOrEmpty()) mgSearchQuery = matches[0]
                                                    mgIsListening = false
                                                }
                                                override fun onReadyForSpeech(params: Bundle?) {}
                                                override fun onBeginningOfSpeech() {}
                                                override fun onRmsChanged(rmsdB: Float) {}
                                                override fun onBufferReceived(buffer: ByteArray?) {}
                                                override fun onEndOfSpeech() { mgIsListening = false }
                                                override fun onError(error: Int) {
                                                    mgIsListening = false
                                                    android.widget.Toast.makeText(context, strings.voiceSearchError, android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                                override fun onPartialResults(partialResults: Bundle?) {}
                                                override fun onEvent(eventType: Int, params: Bundle?) {}
                                            })
                                            mgSpeechRecognizer.startListening(recognizerIntent)
                                        }
                                        val mgAudioPermissionLauncher = rememberLauncherForActivityResult(
                                            contract = ActivityResultContracts.RequestPermission()
                                        ) { granted -> if (granted) mgStartVoiceListening() }
                                        DisposableEffect(Unit) {
                                            onDispose { mgSpeechRecognizer.cancel(); mgSpeechRecognizer.destroy() }
                                        }

                                        if (selectedProgressFromMG != null) {
                                            CalendarScreen(
                                                isLbs = isLbs,
                                                initialExercise = selectedProgressFromMG,
                                                isDark = isDark,
                                                onBackClick = { selectedProgressFromMG = null }
                                            )
                                        } else if (selectedExerciseFromMG != null) {
                                            ExerciseInputScreen(
                                                exercise = selectedExerciseFromMG!!,
                                                grupaMusculara = selectedExerciseFromMG!!.group,
                                                isLbs = isLbs,
                                                isDark = isDark,
                                                onBackClick = { selectedExerciseFromMG = null },
                                                onOpenProgress = { name ->
                                                    selectedProgressFromMG = name
                                                },
                                                onWorkoutSaved = { reloadToken++; badgeCheckTrigger++ },
                                                showFinishButton = false,
                                                strings = strings
                                            )
                                        } else {

                                        LazyColumn(
                                            state = listState,
                                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            item {
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    items(DataProvider.grupeMusculare) { group ->
                                                        val isActive = selectedMuscleGroup == group
                                                        val groupRecovery = recoveryMap[group]
                                                        val pillColor = groupRecovery?.let { getRecoveryPillColor(it) } ?: textSecondary
                                                        val iconRes = muscleGroupIcon(group)

                                                        Surface(
                                                            shape = RoundedCornerShape(20.dp),
                                                            color = if (isActive) accent else p.cr,
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(20.dp))
                                                                .then(
                                                                    if (!isActive) Modifier.border(1.dp, p.bd, RoundedCornerShape(20.dp))
                                                                    else Modifier
                                                                )
                                                                .clickable {
                                                                    selectedMuscleGroup = if (selectedMuscleGroup == group) null else group
                                                                }
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                                            ) {
                                                                Image(
                                                                    painter = painterResource(id = iconRes),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(22.dp)
                                                                )
                                                                Spacer(Modifier.width(8.dp))
                                                                Text(
                                                                    LanguageManager.translateMuscleGroup(group, strings),
                                                                    color = if (isActive) Color.White else p.tp,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            item {
                                BodyAnatomyMapSimple(
                                    recoveryMap = recoveryMap,
                                    onGroupClick = { group -> selectedMuscleGroup = group },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp)
                                )
                                            }

                                            item {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val legendItems = remember(strings) { listOf(
                                                        RecoveryGreen to strings.recovered,
                                                        RecoveryYellow to strings.almostRecovered,
                                                        RecoveryRed to strings.tired
                                                    ) }
                                                    legendItems.forEachIndexed { index, pair ->
                                                        val color = pair.first
                                                        val label = pair.second
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 8.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                                                                                        .background(color, CircleShape)
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Text(
                                                                label,
                                                                fontSize = 12.sp,
                                                                color = textSecondary
                                                            )
                                                        }
                                                        if (index < legendItems.lastIndex) {
                                                            Spacer(Modifier.width(12.dp))
                                                        }
                                                    }
                                                }
                                            }

                                            if (selectedMuscleGroup != null) {
                                                val group = selectedMuscleGroup!!
                                                val allExercises = DataProvider.getDeduplicatedExercises(group)
                                                val equipmentTypes = allExercises.map { it.equipment }.distinct()
                                                val filteredExercises = allExercises.filter { exercise ->
                                                    (selectedEquipment == null || exercise.equipment == selectedEquipment) &&
                                                    (mgSearchQuery.isBlank() || exercise.name.contains(mgSearchQuery, ignoreCase = true))
                                                }

                                                item {
                                                    AppGlassCard(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        p = p,
                                                        cornerRadius = 16.dp
                                                    ) {
                                                        Column {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    LanguageManager.translateMuscleGroup(group, strings).uppercase(),
                                                                    fontSize = 16.sp,
                                                                    fontWeight = FontWeight.Black,
                                                                    letterSpacing = 2.sp,
                                                                    color = p.tp
                                                                )
                                                                Text(
                                                                    "${allExercises.size} ${strings.exercises}",
                                                                    fontSize = 12.sp,
                                                                    color = p.ts,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                item {
                                                    RecoveryBarCard(grupaMusculara = group, isDark = isDark)
                                                }

                                                item {
                                                    OutlinedTextField(
                                                        value = mgSearchQuery,
                                                        onValueChange = { mgSearchQuery = it },
                                                        placeholder = { Text(strings.search, color = textSecondary) },
                                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                                                        trailingIcon = {
                                                            Row {
                                                                IconButton(
                                                                    onClick = {
                                                                        if (mgIsListening) {
                                                                            mgSpeechRecognizer.stopListening()
                                                                            mgIsListening = false
                                                                        } else if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                                            mgStartVoiceListening()
                                                                        } else {
                                                                            mgAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                                                        }
                                                                    }
                                                                ) {
                                                                    Icon(
                                                                        imageVector = if (mgIsListening) Icons.Default.Mic else Icons.Default.MicNone,
                                                                        contentDescription = strings.voiceSearch,
                                                                        tint = if (mgIsListening) accent else textSecondary
                                                                    )
                                                                }
                                                                if (mgSearchQuery.isNotEmpty()) {
                                                                    IconButton(onClick = { mgSearchQuery = "" }) {
                                                                        Icon(Icons.Default.Clear, contentDescription = strings.clear, tint = textSecondary)
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        singleLine = true,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 0.dp, vertical = 2.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = accent,
                                                            unfocusedBorderColor = textSecondary.copy(alpha = 0.2f),
                                                            cursorColor = accent,
                                                            focusedTextColor = textPrimary,
                                                            unfocusedTextColor = textPrimary,
                                                            focusedContainerColor = cardBg.copy(alpha = 0.5f),
                                                            unfocusedContainerColor = cardBg.copy(alpha = 0.5f)
                                                        ),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                }

                                                item {
                                                    LazyRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        contentPadding = PaddingValues(vertical = 2.dp)
                                                    ) {
                                                        item {
                                                            Surface(
                                                                shape = RoundedCornerShape(16.dp),
                                                                color = if (selectedEquipment == null) accent else p.cr,
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(16.dp))
                                                                    .then(
                                                                        if (selectedEquipment != null) Modifier.border(1.dp, p.bd, RoundedCornerShape(16.dp))
                                                                        else Modifier
                                                                    )
                                                                    .clickable { selectedEquipment = null }
                                                            ) {
                                                                Text(
                                                                    strings.all,
                                                                    color = if (selectedEquipment == null) Color.White else p.tp,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = if (selectedEquipment == null) FontWeight.Bold else FontWeight.Medium,
                                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                                )
                                                            }
                                                        }
                                                        items(equipmentTypes) { equip ->
                                                            val isActive = selectedEquipment == equip
                                                            Surface(
                                                                shape = RoundedCornerShape(16.dp),
                                                                color = if (isActive) accent else p.cr,
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(16.dp))
                                                                    .then(
                                                                        if (!isActive) Modifier.border(1.dp, p.bd, RoundedCornerShape(16.dp))
                                                                        else Modifier
                                                                    )
                                                                    .clickable { selectedEquipment = if (isActive) null else equip }
                                                            ) {
                                                                Text(
                                                                    equip,
                                                                    color = if (isActive) Color.White else p.tp,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                items(filteredExercises) { exercise ->
                                                    val gifUrl = ExerciseGifs.getGif(exercise.name)
                                                    AppGlassCard(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(90.dp)
                                                            .clickable {
                                                                selectedExerciseFromMG = exercise
                                                            },
                                                        p = p,
                                                        cornerRadius = 14.dp,
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxSize(),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(90.dp)
                                                                    .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5))
                                                                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                if (gifUrl != null) {
                                                                    AsyncImage(
                                                                        model = gifUrl,
                                                                        contentDescription = exercise.name,
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        contentScale = ContentScale.Crop
                                                                    )
                                                                } else {
                                                                    Image(
                                                                        painter = painterResource(id = muscleGroupIcon(group)),
                                                                        contentDescription = null,
                                                                        modifier = Modifier.size(40.dp),
                                                                        alpha = 0.3f
                                                                    )
                                                                }
                                                            }
                                                            Spacer(Modifier.width(12.dp))
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    exercise.name.uppercase(),
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Black,
                                                                    letterSpacing = 1.sp,
                                                                    color = p.tp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Spacer(Modifier.height(2.dp))
                                                                Text(
                                                                    exercise.equipment,
                                                                    fontSize = 11.sp,
                                                                    color = p.ts,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                            Icon(
                                                                Icons.Default.ChevronRight,
                                                                contentDescription = null,
                                                                tint = p.ts,
                                                                modifier = Modifier
                                                                    .size(18.dp)
                                                                    .padding(end = 12.dp)
                                                            )
                                                            Spacer(Modifier.width(8.dp))
                                                        }
                                                    }
                                                }
                                            } else {
                                                item {
                                                    AppGlassCard(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        p = p,
                                                        cornerRadius = 16.dp
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth(),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = R.drawable.muscle_group),
                                                                contentDescription = null,
                                                                modifier = Modifier.size(40.dp),
                                                                colorFilter = ColorFilter.tint(p.ts.copy(alpha = 0.4f))
                                                            )
                                                            Spacer(Modifier.height(8.dp))
                                                            Text(
                                                                strings.chooseMuscleGroup.uppercase(),
                                                                fontSize = 12.sp,
                                                                color = p.ts,
                                                                letterSpacing = 1.sp,
                                                                textAlign = TextAlign.Center,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                             }
                                         }
                                     }
                                 }
                             }
                         } else if (currentDashboardTab == 2) {
                            StatsScreen(
                                isDark = isDark,
                                isLbs = isLbs,
                                strings = strings,
                                weeklyTopExercise = weeklyTopExercise,
                                weeklyTotalKg = weeklyTotalKg,
                                paddingValues = innerPadding,
                                userId = userId,
                                onExerciseHistoryClick = { exerciseName ->
                                    currentPage = DrawerPage.CALENDAR
                                },
                                currentStreak = currentStreak,
                                bestStreak = bestStreak,
                                badgeCount = badgeCount,
                                recentBadges = recentBadges,
                                allExerciseNames = allExerciseNames
                            )
                        } else if (currentDashboardTab == 3) {
                            WaterTrackingScreen(
                                isDark = isDark,
                                preferencesManager = preferencesManager,
                                strings = strings,
                                paddingValues = innerPadding
                            )
                        } else if (currentDashboardTab == 4) {
                            ProfileScreen(
                                isDark = isDark,
                                preferencesManager = preferencesManager,
                                userProfileManager = userProfileManager,
                                strings = strings,
                                onLanguageClick = { showLanguageDialog = true },
                                onUnitsClick = { showUnitsDialog = true },
                                onProfileChanged = { profileChangedTrigger++ },
                                onLogout = {
                                    preferencesManager.setLoggedIn(false)
                                    preferencesManager.setOnboardingComplete(false)
                                    isLoggedIn = false
                                    reloadToken++
                                },
                                onBiometricClick = {
                                    showBiometricInput = true
                                },
                                onBiometricChartsClick = {
                                    showBiometricCharts = true
                                },
                                onNameChanged = { newName -> profileName = newName },
                                onPhotoChanged = { newUri ->
                                    profilePhoto = newUri
                                    profilePhotoVersion++
                                },
                                onDeleteAccount = { password ->
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job()).launch {
                                        try {
                                            val uid = userProfileManager.getOwnUserId()
                                            FirestoreHelper().deleteUserAccount(uid)
                                        } catch (_: Exception) {}
                                        try {
                                            AppDatabase.getDatabase(context).clearAllTables()
                                        } catch (_: Exception) {}
                                        try {
                                            AuthManager(context).deleteAccount(password)
                                        } catch (_: Exception) {}
                                        preferencesManager.clearSession()
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            isLoggedIn = false
                                            reloadToken++
                                        }
                                    }
                                },
                                onChangePassword = { currentPass, newPass ->
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job()).launch {
                                        val result = AuthManager(context).changePassword(currentPass, newPass)
                                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            if (result.isSuccess) {
                                                android.widget.Toast.makeText(context, "Password changed successfully", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                android.widget.Toast.makeText(context, result.exceptionOrNull()?.message ?: "Failed to change password", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                lastBiometric = lastBiometric,
                                weeksSinceMeasurement = weeksSinceMeasurement,
                                hasBiometricData = allBiometrics.isNotEmpty(),
                                totalWorkouts = totalAllWorkouts,
                                currentStreak = currentStreak,
                                bestStreak = bestStreak,
                                totalVolume = totalAllVolume,
                                earnedBadges = recentBadges,
                                allBiometrics = allBiometrics,
                                paddingValues = innerPadding,
                                subscriptionTier = subscription.tier
                            )
                        }
                    }
                }
                // Floating Navbar — Glassmorphism Frosted Glass
                val navbarContext = LocalContext.current

                // ── Entrance animations: navbar fade-in + rise, WORKOUT spin-in (once per session) ──
                val navbarAnimationsEnabled = remember {
                    try {
                        android.provider.Settings.Global.getFloat(
                            navbarContext.contentResolver,
                            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE
                        ) != 0f
                    } catch (_: Exception) { true }
                }
                val playNavbarEntrance = !navbarEntranceAnimationPlayed && navbarAnimationsEnabled
                val navbarEntranceAlpha = remember { Animatable(if (playNavbarEntrance) 0f else 1f) }
                val navbarEntranceOffsetY = remember { Animatable(if (playNavbarEntrance) 48f else 0f) }
                val fabEntranceAlpha = remember { Animatable(if (playNavbarEntrance) 0f else 1f) }
                val fabEntranceRotation = remember { Animatable(if (playNavbarEntrance) -360f else 0f) }
                val fabEntranceScale = remember { Animatable(if (playNavbarEntrance) 0.4f else 1f) }
                LaunchedEffect(Unit) {
                    if (playNavbarEntrance) {
                        navbarEntranceAnimationPlayed = true
                        delay(150)
                        launch { navbarEntranceAlpha.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }
                        launch { navbarEntranceOffsetY.animateTo(0f, tween(800, easing = FastOutSlowInEasing)) }
                        // WORKOUT button enters after the navbar, spinning into place
                        delay(500)
                        launch { fabEntranceAlpha.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
                        launch { fabEntranceRotation.animateTo(0f, tween(900, easing = FastOutSlowInEasing)) }
                        fabEntranceScale.animateTo(
                            1f,
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                        )
                    }
                }
                val navbarShape = NavbarCradleShape(
                    cornerRadius = 28.dp,
                    cradleRadius = 33.dp,
                    cradleCenterY = 13.dp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            alpha = navbarEntranceAlpha.value
                            translationY = navbarEntranceOffsetY.value
                        }
                        .clip(navbarShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(
                                        Color(0xFF2A2A2A).copy(alpha = 0.95f),
                                        Color(0xFF1E1E1E).copy(alpha = 0.98f),
                                        Color(0xFF151515).copy(alpha = 1.0f)
                                    )
                                } else {
                                    // Light mode: aceeași transparență ca dark (0.95 / 0.98 / 1.0)
                                    listOf(
                                        Color.White.copy(alpha = 0.95f),
                                        Color(0xFFF7F7F7).copy(alpha = 0.98f),
                                        Color.White.copy(alpha = 1.0f)
                                    )
                                }
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = if (isDark) {
                                Color(0xFF444444).copy(alpha = 0.8f)
                            } else {
                                Color(0xFFDDDDDD).copy(alpha = 1.0f)
                            },
                            shape = navbarShape
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ── Haptic feedback helper ──
                        val performHaptic = {
                            try {
                                val vibrator = navbarContext.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                if (vibrator?.hasVibrator() == true) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(15, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibrator.vibrate(15)
                                    }
                                }
                            } catch (_: Exception) {}
                        }

                        val navItems = listOf(
                            Triple(painterResource(R.drawable.ic_nav_home), strings.acasa, 0),
                            Triple(painterResource(R.drawable.ic_nav_stats), strings.stats, 2)
                        )

                        navItems.forEach { (icon, label, tabIndex) ->
                            val selected = currentDashboardTab == tabIndex
                            
                            // ── Icon bounce animation ──
                            val bounceScale by animateFloatAsState(
                                targetValue = if (selected) 1.15f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "bounce"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        performHaptic()
                                        if (currentDashboardTab == tabIndex) {
                                            when {
            quickExerciseGrupa != null -> {
                if (selectedProgressFromQuick != null) selectedProgressFromQuick = null
                else { quickExerciseGrupa = null; quickExerciseName = null; quickExerciseList = emptyList(); quickExerciseIndex = 0 }
            }
                                                showTodayWorkout -> { showTodayWorkout = false }
                                                isWorkoutFlowActive -> workoutNavController.popBackStack()
                                                showTemplates -> { showTemplates = false; currentPage = null }
                                                selectedTemplate != null -> { selectedTemplate = null }
                                                showCalendar -> { showCalendar = false; currentPage = null }
                                                showFoodJournal -> { showFoodJournal = false; currentPage = null }
                                                showAiTrainer -> { showAiTrainer = false; currentPage = null }
                                                showBarcodeScanner -> { showBarcodeScanner = false; currentPage = null }
                                                showAddFood -> { showAddFood = false; pendingFoodProduct = null; showFoodJournal = true }
                                                showBiometricInput -> { showBiometricInput = false; currentPage = null }
                                                showBiometricCharts -> { showBiometricCharts = false; currentPage = null }
                                                showFriends -> { showFriends = false; currentPage = null }
                                                showLeaderboard -> { showLeaderboard = false; currentPage = null }
                                                showPlateCalculator -> { showPlateCalculator = false; currentPage = null }
                                                showOneRMCalculator -> { showOneRMCalculator = false; currentPage = null }
                                                showWorkoutAnalytics -> { showWorkoutAnalytics = false; currentPage = null }
                                                showSavedExercises -> { showSavedExercises = false; currentPage = null }
                                                showWeightGoal -> { showWeightGoal = false; currentPage = null }
                                                showBodyFatCalculator -> { showBodyFatCalculator = false; currentPage = null }
                                                currentPage != null -> currentPage = null
                                            }
                                        } else {
                                            currentDashboardTab = tabIndex
                                            showTodayWorkout = false
                                            selectedTemplate = null
                                            quickExerciseGrupa = null; quickExerciseName = null
                                            workoutNavController.popToWorkoutHome()
                                            showCalendar = false; showTemplates = false; showFoodJournal = false
                                            showAiTrainer = false; showBarcodeScanner = false; showAddFood = false
                                            showBiometricInput = false; showBiometricCharts = false
                                            showFriends = false; showLeaderboard = false
                                            showPlateCalculator = false; showOneRMCalculator = false
                                            showWorkoutAnalytics = false; showSavedExercises = false
                                            showWeightGoal = false; showBodyFatCalculator = false
                                            currentPage = null
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        icon, 
                                        contentDescription = label, 
                                        tint = if (selected) accent else textSecondary, 
                                        modifier = Modifier
                                            .size(26.dp)
                                            .graphicsLayer {
                                                scaleX = bounceScale
                                                scaleY = bounceScale
                                            }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(56.dp))

                        val rightItems = listOf(
                            Triple(painterResource(R.drawable.ic_nav_water), strings.waterIntake, 3),
                            Triple(painterResource(R.drawable.ic_nav_profile), strings.profile, 4)
                        )

                        rightItems.forEach { (icon, label, tabIndex) ->
                            val selected = currentDashboardTab == tabIndex
                            
                            // ── Icon bounce animation ──
                            val rightBounceScale by animateFloatAsState(
                                targetValue = if (selected) 1.15f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "rightBounce"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        performHaptic()
                                        if (currentDashboardTab == tabIndex) {
                                            when {
                                                quickExerciseGrupa != null -> {
                                                    if (selectedProgressFromQuick != null) selectedProgressFromQuick = null
                                                    else { quickExerciseGrupa = null; quickExerciseName = null; quickExerciseList = emptyList(); quickExerciseIndex = 0 }
                                                }
                                                showTodayWorkout -> { showTodayWorkout = false }
                                                isWorkoutFlowActive -> workoutNavController.popBackStack()
                                                showTemplates -> { showTemplates = false; currentPage = null }
                                                selectedTemplate != null -> { selectedTemplate = null }
                                                showCalendar -> { showCalendar = false; currentPage = null }
                                                showFoodJournal -> { showFoodJournal = false; currentPage = null }
                                                showAiTrainer -> { showAiTrainer = false; currentPage = null }
                                                showBarcodeScanner -> { showBarcodeScanner = false; currentPage = null }
                                                showAddFood -> { showAddFood = false; pendingFoodProduct = null; showFoodJournal = true }
                                                showBiometricInput -> { showBiometricInput = false; currentPage = null }
                                                showBiometricCharts -> { showBiometricCharts = false; currentPage = null }
                                                showFriends -> { showFriends = false; currentPage = null }
                                                showLeaderboard -> { showLeaderboard = false; currentPage = null }
                                                showPlateCalculator -> { showPlateCalculator = false; currentPage = null }
                                                showOneRMCalculator -> { showOneRMCalculator = false; currentPage = null }
                                                showWorkoutAnalytics -> { showWorkoutAnalytics = false; currentPage = null }
                                                showSavedExercises -> { showSavedExercises = false; currentPage = null }
                                                showWeightGoal -> { showWeightGoal = false; currentPage = null }
                                                showBodyFatCalculator -> { showBodyFatCalculator = false; currentPage = null }
                                                currentPage != null -> currentPage = null
                                            }
                                        } else {
                                            currentDashboardTab = tabIndex
                                            showTodayWorkout = false
                                            selectedTemplate = null
                                            quickExerciseGrupa = null; quickExerciseName = null
                                            workoutNavController.popToWorkoutHome()
                                            showCalendar = false; showTemplates = false; showFoodJournal = false
                                            showAiTrainer = false; showBarcodeScanner = false; showAddFood = false
                                            showBiometricInput = false; showBiometricCharts = false
                                            showFriends = false; showLeaderboard = false
                                            showPlateCalculator = false; showOneRMCalculator = false
                                            showWorkoutAnalytics = false; showSavedExercises = false
                                            showWeightGoal = false; showBodyFatCalculator = false
                                            currentPage = null
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        icon, 
                                        contentDescription = label, 
                                        tint = if (selected) accent else textSecondary, 
                                        modifier = Modifier
                                            .size(26.dp)
                                            .graphicsLayer {
                                                scaleX = rightBounceScale
                                                scaleY = rightBounceScale
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                val centerSelected = currentDashboardTab == 1
                FloatingActionButton(
                    onClick = {
                        // Always navigate to Today's Workout from the workout tab
                        showTemplates = false; showCalendar = false; showFoodJournal = false
                        showAiTrainer = false; showBarcodeScanner = false; showAddFood = false
                        showBiometricInput = false; showBiometricCharts = false
                        showFriends = false; showLeaderboard = false
                        showPlateCalculator = false; showOneRMCalculator = false
                        showWorkoutAnalytics = false; showSavedExercises = false
                        showWeightGoal = false; showBodyFatCalculator = false
                        currentPage = null; selectedTemplate = null
                        quickExerciseGrupa = null; quickExerciseName = null
                        quickExerciseList = emptyList(); quickExerciseIndex = 0
                        showTodayWorkout = false
                        muscleGroupsSubTab = 1
                        if (currentDashboardTab != 1) {
                            currentDashboardTab = 1
                            workoutNavController.popToWorkoutHome()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .size(56.dp)
                        .graphicsLayer {
                            alpha = fabEntranceAlpha.value
                            rotationZ = fabEntranceRotation.value
                            scaleX = fabEntranceScale.value
                            scaleY = fabEntranceScale.value
                        },
                    containerColor = if (centerSelected) accent else accent.copy(alpha = 0.85f),
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.barbell),
                        contentDescription = strings.workouts,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            isDark = isDark,
            currentLanguage = currentLanguage,
            strings = strings,
            onSelect = { code ->
                LanguageManager.saveLanguage(context, code)
                currentLanguage = code
                reloadToken++
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    updateAvailable?.let { release ->
        AlertDialog(
            onDismissRequest = { updateAvailable = null },
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White,
            title = { Text(strings.updateTitle, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF111111)) },
            text = {
                Text(
                    strings.updateMessage.format(release.tagName, BuildConfig.VERSION_NAME),
                    color = if (isDark) Color(0xFFCCCCCC) else Color(0xFF444444)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    updateAvailable = null
                    try {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(release.htmlUrl)
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }) {
                    Text(strings.updateDownload, fontWeight = FontWeight.Bold, color = Volcanico)
                }
            },
            dismissButton = {
                TextButton(onClick = { updateAvailable = null }) {
                    Text(strings.updateLater, color = if (isDark) Color(0xFF999999) else Color(0xFF777777))
                }
            }
        )
    }
    if (showUnitsDialog) {
        UnitsSelectionDialog(
            isDark = isDark,
            isLbs = isLbs,
            strings = strings,
            onSelect = { lbs ->
                isLbs = lbs
                preferencesManager.setLbs(lbs)
                showUnitsDialog = false
            },
            onDismiss = { showUnitsDialog = false }
        )
    }
    if (showServerDialog) {
        ServerUrlDialog(
            isDark = isDark,
            strings = strings,
            currentUrl = preferencesManager.getServerUrl(),
            currentApiKey = preferencesManager.getAiApiKey(),
            onSave = { url, apiKey ->
                preferencesManager.setServerUrl(url)
                preferencesManager.setAiApiKey(apiKey)
                NetworkClient.getApi(url)
                showServerDialog = false
            },
            onDismiss = { showServerDialog = false }
        )
    }

    // ===== Monetization: full-screen pricing overlay =====
    if (showPricing) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showPricing = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            PricingScreen(
                isDark = isDark,
                currentTier = if (AdminManager.isAdmin(userEmail)) SubscriptionTier.PRO_LIFETIME else subscription.tier,
                pricingOptions = pricingOptions,
                strings = strings,
                onSelectPlan = { tier -> purchasePlan(tier) },
                onRestore = { restorePurchases() },
                onBack = { showPricing = false },
                devMode = BuildConfig.DEBUG
            )
        }
    }
        }
}
}

// ============================================
// Forma navbar-ului cu „cradle" (crestătură curbă) pentru butonul de workout
// ============================================
/**
 * Forma navbar-ului cu o crestătură semicirculară pe marginea de sus, în centru.
 * Curba înconjoară partea inferioară a butonului central de workout, ca butonul
 * să pară așezat în navbar, nu plutind deasupra lui.
 */
private class NavbarCradleShape(
    private val cornerRadius: Dp,
    private val cradleRadius: Dp,
    private val cradleCenterY: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cr = with(density) { cornerRadius.toPx() }
        val cradleR = with(density) { cradleRadius.toPx() }
        val cx = size.width / 2f
        val cy = with(density) { cradleCenterY.toPx() } // centrul cercului crestăturii, relativ la marginea de sus
        // Intersecția cercului crestăturii cu marginea de sus (y = 0)
        val dy = cy
        val halfW = if (cradleR > dy) sqrt(cradleR * cradleR - dy * dy) else 0f
        // Unghiul dintre verticală și punctul de intersecție (în radiani)
        val alpha = atan2(dy, halfW)
        // Unghiul de start: punctul stânga-sus al arcului (sistem y în jos)
        val startAngle = atan2(-dy, -halfW) * 180f / PI.toFloat()
        // Sweep-ul trebuie să meargă PE SUB cerc (prin punctul de jos, +90° în sistem y-jos),
        // nu pe deasupra — altfel crestătura iese în sus, invizibilă.
        val sweep = -(180f + 2f * alpha * 180f / PI.toFloat())

        val path = Path().apply {
            // colțul stânga-jos (rotunjit, la fel ca sus)
            moveTo(0f, size.height - cr)
            lineTo(0f, cr)
            // colțul stânga-sus
            arcTo(Rect(0f, 0f, cr * 2f, cr * 2f), 180f, 90f, false)
            // marginea de sus până la crestătură
            lineTo(cx - halfW, 0f)
            // arcul crestăturii — înconjoară partea de jos a butonului
            arcTo(
                Rect(cx - cradleR, cy - cradleR, cx + cradleR, cy + cradleR),
                startAngle,
                sweep,
                false
            )
            // marginea de sus după crestătură
            lineTo(size.width - cr, 0f)
            // colțul dreapta-sus
            arcTo(Rect(size.width - cr * 2f, 0f, size.width, cr * 2f), 270f, 90f, false)
            // latura dreaptă în jos
            lineTo(size.width, size.height - cr)
            // colțul dreapta-jos (rotunjit)
            arcTo(
                Rect(size.width - cr * 2f, size.height - cr * 2f, size.width, size.height),
                0f, 90f, false
            )
            // marginea de jos până la colțul stânga-jos
            lineTo(cr, size.height)
            // colțul stânga-jos (rotunjit)
            arcTo(
                Rect(0f, size.height - cr * 2f, cr * 2f, size.height),
                90f, 90f, false
            )
            close()
        }
        return Outline.Generic(path)
    }
}

// ============================================
// Ecranul 2: Lista de Exercitii
// ============================================
// Template color config
// ============================================
    private fun templateGradient(templateName: String): List<Color> {
    return when (templateName.lowercase()) {
        "push"      -> listOf(Volcanico.copy(alpha = 0.85f), VolcanicoLight.copy(alpha = 0.85f))
        "pull"      -> listOf(Color(0xFF1565C0).copy(alpha = 0.85f), Color(0xFF42A5F5).copy(alpha = 0.85f))
        "legs"      -> listOf(Color(0xFF2E7D32).copy(alpha = 0.85f), Color(0xFF66BB6A).copy(alpha = 0.85f))
        "upper"     -> listOf(Color(0xFF1976D2).copy(alpha = 0.85f), Color(0xFF42A5F5).copy(alpha = 0.85f))
        "full body" -> listOf(VolcanicoDark.copy(alpha = 0.85f), Color(0xFFFF9800).copy(alpha = 0.85f))
        else        -> listOf(Volcanico.copy(alpha = 0.85f), VolcanicoLight.copy(alpha = 0.85f))
    }
}

private fun templateIcon(templateName: String): Int {
    return when (templateName.lowercase()) {
        "push" -> R.drawable.template_push
        "pull" -> R.drawable.template_pull
        "legs" -> R.drawable.template_legs
        "upper" -> R.drawable.template_upper
        "full body" -> R.drawable.template_fullbody
        else -> R.drawable.template_push
    }
}

private fun muscleGroupIcon(group: String): Int {
    return when (group) {
        "Piept" -> R.drawable.ic_piept
        "Spate" -> R.drawable.ic_spate
        "Umeri" -> R.drawable.ic_umeri
        "Biceps" -> R.drawable.ic_biceps
        "Triceps" -> R.drawable.ic_triceps
        "Abdomen" -> R.drawable.ic_abdomen
        "Picioare" -> R.drawable.ic_picioare
        "Fese" -> R.drawable.ic_fese
        "Gambe" -> R.drawable.ic_gambe
        "Antebrate" -> R.drawable.ic_antebrat
        "Gat & Trapezi" -> R.drawable.ic_gat
        "Cardio" -> R.drawable.ic_cardio
        else -> R.drawable.ic_chest
    }
}

private fun templateMuscleGroups(template: WorkoutTemplate): List<String> {
    return template.exercitii.map { it.grupaMusculara }.distinct()
}

// ============================================
// Ecranul Template-uri de antrenament
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    var selectedTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }

    BackHandler {
        if (selectedTemplate != null) selectedTemplate = null
        else onBackClick()
    }

    if (selectedTemplate != null) {
        TemplateDetailScreen(
            template = selectedTemplate!!,
            onBackClick = { selectedTemplate = null },
            onBackToMain = onBackClick
        )
    } else {
        Scaffold(
            containerColor = bgColor(),
            topBar = {
                KineticAppBar(onBack = onBackClick)
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                item {
                    PageTitle(strings.templates)
                }
                items(DataProvider.templateuri) { template ->
                    val gradientColors = templateGradient(template.nume)
                    val estimatedDuration = template.exercitii.size * 3
                    val totalSets = template.exercitii.size * 4
                    val muscleGroups = templateMuscleGroups(template)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { selectedTemplate = template },
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = gradientColors,
                                            start = Offset(0f, 0f),
                                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                        )
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.45f),
                                                Color.Transparent,
                                                Color.Transparent
                                            ),
                                            startX = 0f,
                                            endX = 600f
                                        )
                                    )
                            )

                            Image(
                                painter = painterResource(id = templateIcon(template.nume)),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(200.dp)
                                    .align(Alignment.CenterEnd)
                                    .alpha(0.55f),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(22.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        template.nume.uppercase().take(20),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp,
                                        letterSpacing = 4.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "${template.exercitii.size} ${strings.exercises}  ·  ~${estimatedDuration}min  ·  ${totalSets} ${strings.sets}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LazyRow(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(muscleGroups) { mg ->
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = Color.White.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    LanguageManager.translateMuscleGroup(mg, strings),
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                                                                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    template: WorkoutTemplate,
    isLbs: Boolean = false,
    isDark: Boolean = false,
    onBackClick: () -> Unit,
    onBackToMain: () -> Unit,
    onWorkoutSaved: () -> Unit = {}
) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    val p = appPalette(isDark)
    var selectedExercise by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var selectedGrupa by remember { mutableStateOf("") }
    var selectedProgressFromTemplate by remember { mutableStateOf<String?>(null) }
    val exerciseSummaries by viewModel.exerciseSummaries.collectAsState()
    var templateRecoveryMap by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

    val exercises = remember {
        mutableStateListOf<TemplateExercise>().apply {
            addAll(template.exercitii)
        }
    }

    LaunchedEffect(exercises) {
        val allNames = exercises.map { it.exercise.nume }
        val uid = UserProfileManager(context).getOwnUserId()
        if (allNames.isNotEmpty()) {
            viewModel.loadExerciseSummaries(uid, allNames)
        }
        templateRecoveryMap = AntrenamentRepository(AppDatabase.getDatabase(context)).getToateRecuperarile(uid).toMap()
    }

    var workoutStarted by remember { mutableStateOf(false) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }

    BackHandler {
        when {
            selectedProgressFromTemplate != null -> selectedProgressFromTemplate = null
            selectedExercise != null -> {
                selectedExercise = null
                workoutStarted = false
                currentExerciseIndex = 0
            }
            workoutStarted -> { workoutStarted = false; currentExerciseIndex = 0 }
            else -> onBackClick()
        }
    }

    if (selectedProgressFromTemplate != null) {
        CalendarScreen(
            isLbs = isLbs,
            initialExercise = selectedProgressFromTemplate,
            isDark = isDark,
            onBackClick = { selectedProgressFromTemplate = null }
        )
    } else if (selectedExercise != null) {
        val hasNextExercise = workoutStarted && currentExerciseIndex < exercises.size - 1
        ExerciseInputScreen(
            exercise = selectedExercise!!,
            grupaMusculara = selectedGrupa,
            isDark = isDark,
            onBackClick = {
                selectedExercise = null
                workoutStarted = false
                currentExerciseIndex = 0
            },
            onNextExercise = {
                if (hasNextExercise) {
                    currentExerciseIndex++
                    selectedExercise = exercises[currentExerciseIndex].exercise
                    selectedGrupa = exercises[currentExerciseIndex].grupaMusculara
                } else {
                    selectedExercise = null
                    workoutStarted = false
                    currentExerciseIndex = 0
                }
            },
            onOpenProgress = { name -> selectedProgressFromTemplate = name },
            onWorkoutSaved = onWorkoutSaved,
            strings = strings,
            currentIndex = currentExerciseIndex,
            totalExercises = exercises.size,
            nextExerciseName = exercises.getOrNull(currentExerciseIndex + 1)?.exercise?.nume
        )
    } else {
        val gradientColors = templateGradient(template.nume)

        Scaffold(
            containerColor = bgColor(),
            topBar = {
                KineticAppBar(onBack = onBackClick)
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = gradientColors,
                                            start = Offset(0f, 0f),
                                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.45f),
                                                Color.Transparent,
                                                Color.Transparent
                                            ),
                                            startX = 0f,
                                            endX = 600f
                                        )
                                    )
                            )
                            Image(
                                painter = painterResource(id = templateIcon(template.nume)),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(160.dp)
                                    .align(Alignment.CenterEnd)
                                    .alpha(0.55f),
                                contentScale = ContentScale.Crop
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(22.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${exercises.size} ${strings.exercises}  ·  ~${exercises.size * 3}min",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Button(
                                    onClick = {
                                        workoutStarted = true
                                        currentExerciseIndex = 0
                                        selectedExercise = exercises[0].exercise
                                        selectedGrupa = exercises[0].grupaMusculara
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = gradientColors[0],
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        strings.startWorkout.uppercase(),
                                        color = gradientColors[0],
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                items(exercises.size) { index ->
                    val te = exercises[index]
                    val gifUrl = ExerciseGifs.getGif(te.exercise.nume)

                    AppGlassCard(
                        modifier = Modifier
                            .fillMaxWidth(),
                        p = p,
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5))
                                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (gifUrl != null) {
                                    AsyncImage(
                                        model = gifUrl,
                                        contentDescription = te.exercise.nume,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = p.ac.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(24.dp)
                                        .background(p.ac.copy(alpha = 0.85f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        workoutStarted = true
                                        currentExerciseIndex = index
                                        selectedExercise = te.exercise
                                        selectedGrupa = te.grupaMusculara
                                    }
                            ) {
                                Text(
                                    te.exercise.nume.uppercase(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = p.tp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    LanguageManager.translateMuscleGroup(te.grupaMusculara, strings),
                                    color = p.ts,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Column(
                                modifier = Modifier.clickable {
                                    workoutStarted = true
                                    currentExerciseIndex = index
                                    selectedExercise = te.exercise
                                    selectedGrupa = te.grupaMusculara
                                },
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    te.exercise.equipment,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.ac
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = p.ts,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 12.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}
// Componente Muscle Recovery
// ============================================

fun getRecoveryColor(level: Double): Color {
    return when {
        level < 0.3 -> RecoveryGreen
        level < 0.6 -> RecoveryYellow
        level < 0.8 -> RecoveryOrange
        else -> RecoveryRed
    }
}

internal fun getRecoveryLabel(level: Double, strings: LanguageManager.Strings): String {
    return when {
        level < 0.05 -> strings.recovered
        level < 0.3 -> strings.almostRecovered
        level < 0.6 -> strings.moderate
        level < 0.8 -> strings.tired
        else -> strings.exhausted
    }
}

internal fun formatTimpRamas(ms: Long, strings: LanguageManager.Strings): String {
    if (ms <= 0) return strings.recovered
    val ore = (ms / 3_600_000).toInt()
    val minute = ((ms % 3_600_000) / 60_000).toInt()
    return if (ore > 0) "~${ore}h ${minute}m" else "~${minute}m"
}

@Composable
fun RecoveryBarCard(grupaMusculara: String, modifier: Modifier = Modifier, isDark: Boolean? = null) {
    val context = LocalContext.current
    // Tema APLICAȚIEI (transmisă de apelant), nu tema sistemului — altfel cardul folosea
    // culorile dark chiar și când aplicația e forțată pe light (dar sistemul e pe dark).
    val resolvedDark = isDark ?: isSystemInDarkTheme()
    val strings = LanguageManager.getStrings(context)
    val userId = remember { UserProfileManager(context).getOwnUserId() }
    var groupLevel by remember { mutableStateOf(0.0) }
    LaunchedEffect(grupaMusculara) {
        val repo = AntrenamentRepository(AppDatabase.getDatabase(context))
        val allRecovery = repo.getToateRecuperarile(userId).toMap()
        groupLevel = allRecovery[grupaMusculara] ?: 0.0
    }
    val recoveryPct = ((1.0 - groupLevel) * 100).toInt().coerceIn(0, 100)
    val barColor = getRecoveryColor(groupLevel)
    val animatedLevel by animateFloatAsState(
        targetValue = groupLevel.toFloat(),
        animationSpec = tween(durationMillis = 1000)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (resolvedDark) DarkCardElevated else surfaceColor())
            .border(1.dp, if (resolvedDark) RedBorderSoft else dividerColor(), RoundedCornerShape(22.dp))
            .padding(16.dp, 14.dp, 20.dp, 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Favorite, null, tint = barColor, modifier = Modifier.size(15.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.muscleRecovery, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = secondaryTextColor())
                Text("$recoveryPct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = barColor, fontFamily = FontFamily.Monospace)
            }
            Spacer(Modifier.height(7.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)).background(Color(0x06FFFFFF))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fraction = (1f - animatedLevel).coerceIn(0f, 1f)).clip(RoundedCornerShape(5.dp)).background(barColor))
            }
        }
    }
}

@Composable
fun RecoveryBarCompact(
    level: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    val animatedLevel by animateFloatAsState(
        targetValue = level.toFloat(),
        animationSpec = tween(durationMillis = 1000)
    )
    val barColor = getRecoveryColor(level)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                strings.muscleRecovery,
                color = secondaryTextColor(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${((1.0 - level) * 100).toInt()}%",
                color = barColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                                .background(RecoveryTrack, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (1f - animatedLevel).coerceIn(0f, 1f))
                                        .background(barColor, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun RecoveryBarForGroup(grupaMusculara: String) {
    val context = LocalContext.current
    val userId = remember { UserProfileManager(context).getOwnUserId() }
    val viewModel: MainViewModel = viewModel()
    var level by remember { mutableStateOf(0.0) }

    LaunchedEffect(grupaMusculara) {
        viewModel.getRecuperareMusculara(userId, grupaMusculara) { level = it }
    }

    var refreshTick by remember { mutableStateOf(0L) }
    LaunchedEffect(grupaMusculara) {
        while (true) {
            delay(30_000)
            refreshTick = System.currentTimeMillis()
        }
    }
    LaunchedEffect(refreshTick, grupaMusculara) {
        viewModel.getRecuperareMusculara(userId, grupaMusculara) { level = it }
    }

    RecoveryBarCompact(
        level = level,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleRecoveryScreen(onBackClick: () -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    val userId = remember { UserProfileManager(context).getOwnUserId() }
    var recoveryData by remember { mutableStateOf<List<Pair<String, Double>>>(listOf()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.getToateRecuperarile(userId) { data ->
            recoveryData = data
            isLoading = false
        }
    }

    var refreshTick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            refreshTick = System.currentTimeMillis()
        }
    }
    LaunchedEffect(refreshTick) {
        viewModel.getToateRecuperarile(userId) { data ->
            recoveryData = data
        }
    }

    Scaffold(
        containerColor = bgColor(),
        topBar = {
            KineticAppBar(onBack = onBackClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor())
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recoveryData) { (grupa, level) ->
                        val remainingMs = MainViewModel.calculeazaTimpRamas(level, grupa)
                        val barColor = getRecoveryColor(level)
                        val animatedLevel by animateFloatAsState(
                            targetValue = level.toFloat(),
                            animationSpec = tween(durationMillis = 1000)
                        )

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            isDark = isSystemInDarkTheme(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        LanguageManager.translateMuscleGroup(grupa, strings),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = textColor(),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        getRecoveryLabel(level, strings),
                                        color = barColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(14.dp)
                                                                                .background(RecoveryTrack, RoundedCornerShape(7.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                    .fillMaxWidth(fraction = (1f - animatedLevel).coerceIn(0f, 1f))
                                                                                        .background(barColor, RoundedCornerShape(7.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${(level * 100).toInt()}% ${strings.fatigue}",
                                        color = secondaryTextColor(),
                                        fontSize = 12.sp
                                    )
                                    if (level > 0.05) {
                                        Text(
                                            formatTimpRamas(remainingMs, strings),
                                            color = secondaryTextColor(),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                val recHours = when (grupa) {
                                    "Biceps" -> 36L
                                    "Gambe" -> 36L
                                    "Antebrate" -> 36L
                                    "Triceps" -> 48L
                                    "Abdomen" -> 48L
                                    "Umeri" -> 48L
                                    "Piept" -> 48L
                                    "Gat & Trapezi" -> 48L
                                    "Spate" -> 72L
                                    "Picioare" -> 72L
                                    "Fese" -> 72L
                                    else -> 48L
                                }
                                Text(
                                    "${strings.recommendedRecovery}: ~${recHours}h",
                                    color = accentColor().copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// Ecranul Progres (redesignat cu Line Chart)
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    isLbs: Boolean = false,
    initialExercise: String? = null,
    isDark: Boolean,
    onBackClick: () -> Unit
) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val userId = remember { UserProfileManager(context).getOwnUserId() }
    val strings = LanguageManager.getStrings(context)
    var selectedExercise by remember { mutableStateOf(initialExercise ?: "") }
    var progresData by remember { mutableStateOf<List<ProgresLunar>>(listOf()) }
    var showExerciseSelector by remember { mutableStateOf(initialExercise == null) }
    var selectedGroupFilter by remember { mutableStateOf<String?>(null) }
    var exerciseSearchQuery by remember { mutableStateOf("") }
    var exerciseEquipmentFilter by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    fun startVoiceListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            android.widget.Toast.makeText(context, strings.voiceSearchError, android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        isListening = true
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, strings.voiceSearch)
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    exerciseSearchQuery = matches[0]
                }
                isListening = false
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                isListening = false
                android.widget.Toast.makeText(context, strings.voiceSearchError, android.widget.Toast.LENGTH_SHORT).show()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer.startListening(recognizerIntent)
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceListening()
    }
    DisposableEffect(Unit) {
        onDispose { speechRecognizer.cancel(); speechRecognizer.destroy() }
    }
    LaunchedEffect(selectedGroupFilter) {
        exerciseSearchQuery = ""
        exerciseEquipmentFilter = null
    }
    var stats by remember { mutableStateOf(ExerciseStats(0.0, 0, 0.0)) }
    val textSecondary = appPalette(isDark).ts

    BackHandler {
        when {
            initialExercise != null -> onBackClick()
            !showExerciseSelector -> {
                showExerciseSelector = true
                selectedGroupFilter = null
            }
            selectedGroupFilter != null -> selectedGroupFilter = null
            else -> onBackClick()
        }
    }

    LaunchedEffect(initialExercise) {
        if (initialExercise != null) {
            try {
                viewModel.getProgresLunar(userId, initialExercise) { progres -> progresData = progres }
                viewModel.getStatisticiExercitiu(userId, initialExercise) { stats = it }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        containerColor = bgColor(),
        topBar = {
            if (initialExercise != null) {
                KineticAppBar(onBack = onBackClick)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            if (showExerciseSelector) {
                if (selectedGroupFilter == null) {
                    Text(
                        strings.chooseMuscleGroup,
                        style = MaterialTheme.typography.titleLarge,
                        color = secondaryTextColor(),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(DataProvider.grupeMusculare) { group ->
                            val iconRes = when (group) {
                                "Piept" -> R.drawable.ic_piept
                                "Spate" -> R.drawable.ic_spate
                                "Umeri" -> R.drawable.ic_umeri
                                "Biceps" -> R.drawable.ic_biceps
                                "Triceps" -> R.drawable.ic_triceps
                                "Abdomen" -> R.drawable.ic_abdomen
                                "Picioare" -> R.drawable.ic_picioare
                                "Fese" -> R.drawable.ic_fese
                                "Gambe" -> R.drawable.ic_gambe
                                "Antebrate" -> R.drawable.ic_antebrat
                                "Gat & Trapezi" -> R.drawable.ic_gat
                                else -> R.drawable.ic_piept
                            }
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedGroupFilter = group },
                                shape = RoundedCornerShape(14.dp),
                                isDark = isDark,
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                                                                        .background(IconBackground, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(iconRes),
                                            contentDescription = group,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = LanguageManager.translateMuscleGroup(group, strings),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = textColor(),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = accentColor(),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    TextButton(onClick = { selectedGroupFilter = null }) {
                        IconButton(onClick = { selectedGroupFilter = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = strings.back, tint = textSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("< ${strings.back} ${strings.muscleGroups.lowercase()}", color = accentColor())
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        LanguageManager.translateMuscleGroup(selectedGroupFilter!!, strings).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    // Search + Equipment Filter
                    OutlinedTextField(
                        value = exerciseSearchQuery,
                        onValueChange = { exerciseSearchQuery = it },
                        placeholder = { Text(strings.search, color = textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                        trailingIcon = {
                            Row {
                                // Mic button for voice search
                                IconButton(
                                    onClick = {
                                        if (isListening) {
                                            speechRecognizer.stopListening()
                                            isListening = false
                                        } else if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            startVoiceListening()
                                        } else {
                                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                        contentDescription = strings.voiceSearch,
                                        tint = if (isListening) accentColor() else textSecondary
                                    )
                                }
                                // Clear button
                                if (exerciseSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { exerciseSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = strings.clear, tint = textSecondary)
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor(),
                            unfocusedBorderColor = dividerColor(),
                            cursorColor = accentColor(),
                            focusedTextColor = textColor(),
                            unfocusedTextColor = textColor(),
                            focusedContainerColor = cardColor().copy(alpha = 0.5f),
                            unfocusedContainerColor = cardColor().copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    val currentGroupFilter2 = selectedGroupFilter
                    val allEquipTypes = remember(currentGroupFilter2) {
                        val equips = if (currentGroupFilter2 != null) {
                            DataProvider.getDeduplicatedExercises(currentGroupFilter2)
                                .map { it.equipment }.filter { it.isNotEmpty() }.distinct().sorted()
                        } else emptyList()
                        equips
                    }
                    if (allEquipTypes.size > 1) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                            item {
                                FilterChip(
                                    selected = exerciseEquipmentFilter == null,
                                    onClick = { exerciseEquipmentFilter = null },
                                    label = { Text(strings.all, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor(),
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                            items(allEquipTypes) { equip ->
                                FilterChip(
                                    selected = exerciseEquipmentFilter == equip,
                                    onClick = { exerciseEquipmentFilter = if (exerciseEquipmentFilter == equip) null else equip },
                                    label = { Text(equip, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor(),
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }
                    val currentGroupFilter = selectedGroupFilter
                    val allGroupExercises = remember(currentGroupFilter) {
                        if (currentGroupFilter != null) DataProvider.getDeduplicatedExercises(currentGroupFilter) else emptyList()
                    }
                    val filteredGroupExercises = remember(allGroupExercises, exerciseSearchQuery, exerciseEquipmentFilter) {
                        allGroupExercises.filter { ex ->
                            val matchesSearch = exerciseSearchQuery.isBlank() ||
                                ex.name.contains(exerciseSearchQuery, ignoreCase = true) ||
                                ex.equipment.contains(exerciseSearchQuery, ignoreCase = true)
                            val matchesEquip = exerciseEquipmentFilter == null ||
                                ex.equipment == exerciseEquipmentFilter
                            matchesSearch && matchesEquip
                        }
                    }
                    Text(
                        text = "${"$"}{filteredGroupExercises.size} ${"$"}{strings.exercises}",
                        fontSize = 12.sp,
                        color = textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (filteredGroupExercises.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        strings.noExercisesFound,
                                        color = textSecondary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        strings.tryDifferentFilter,
                                        color = textSecondary.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        items(filteredGroupExercises) { exercitiu ->
                            GlassCard(
                                modifier = Modifier
                                    .clickable {
                                        selectedExercise = exercitiu.nume
                                        showExerciseSelector = false
                                        selectedGroupFilter = null
                                        viewModel.getProgresLunar(userId, exercitiu.nume) { progres ->
                                            progresData = progres
                                        }
                                        viewModel.getStatisticiExercitiu(userId, exercitiu.nume) { stats = it }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                isDark = isDark,
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                                                                        .background(DarkRed.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.DirectionsBike,
                                            contentDescription = null,
                                            tint = accentColor(),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exercitiu.nume,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = textColor(),
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = LanguageManager.translateMuscleGroup(exercitiu.group, strings),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = secondaryTextColor()
                                        )
                                    }
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = accentColor().copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    isDark = isDark,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column {
                        Text(
                            selectedExercise.uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = textColor(),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(strings.monthlyProgress, color = secondaryTextColor(), fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill(strings.maxWeight, weightLabel(stats.maxGreutate, isLbs), Modifier.weight(1f))
                    StatPill(strings.maxReps, "${stats.maxRepetari}", Modifier.weight(1f))
                    StatPill(strings.maxSet, weightLabel(stats.maxVolumSet, isLbs), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (progresData.isEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        isDark = isDark,
                        contentPadding = PaddingValues(32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = secondaryTextColor(),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(strings.noDataYet, color = secondaryTextColor(), fontSize = 16.sp)
                            Text(
                                strings.completeWorkoutsToSee,
                                color = secondaryTextColor().copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val chartData = progresData.map { p ->
                        val parts = p.luna.split("-")
                        val monthName = when (parts.getOrElse(1) { "" }) {
                            "01" -> strings.jan; "02" -> strings.feb; "03" -> strings.mar
                            "04" -> strings.apr; "05" -> strings.may; "06" -> strings.jun
                            "07" -> strings.jul; "08" -> strings.aug; "09" -> strings.sep
                            "10" -> strings.oct; "11" -> strings.nov; "12" -> strings.dec
                            else -> p.luna
                        }
                        monthName to p.greutateMaxima
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        isDark = isDark,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column {
                            Text(strings.weightProgression, color = textColor(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LineChart(
                                data = chartData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isDark = isDark,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Column {
                            Text(strings.monthlyDetails, color = textColor(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                isDark = isDark,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(strings.month, color = textColor(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${strings.max} ${if (isLbs) strings.lbs else strings.kg}", color = textColor(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            progresData.reversed().forEachIndexed { index, progres ->
                                val bg = if (index % 2 == 0) cardColor() else surfaceColor()
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = bg),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(progres.luna, color = textColor(), fontSize = 13.sp)
                                        Box(
                                            modifier = Modifier
                                                                                                .background(DarkRed.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "${weightLabel(progres.greutateMaxima, isLbs)}",
                                                 color = accentColor(),
                                                 fontWeight = FontWeight.Bold,
                                                 fontSize = 13.sp
                                             )
                }
            }
        }
    }
}
                }
            }
        }
    }
}
}

// ============================================
// Profile Screen
// ============================================

/** Paletă de culori pentru pagina Profile, adaptată la tema aplicației (dark / light). */
private data class ProfilePalette(
    val bg: Color,
    val sf: Color,
    val cr: Color,
    val bd: Color,
    val tp: Color,
    val ts: Color,
    val tt: Color,
    val ac: Color,
    val acg: Color,
    val acs: Color,
    val gn: Color,
    val gns: Color,
    val am: Color,
    val ams: Color,
    val bl: Color,
    val bls: Color,
    val pu: Color,
    val pus: Color,
    val rs: Color,
    val rss: Color
)

private fun profilePalette(isDark: Boolean): ProfilePalette {
    val red = if (isDark) Color(0xFFFF3C3C) else LightPrimaryRed
    return ProfilePalette(
        bg = if (isDark) DarkBackground else LightBackground,
        sf = if (isDark) DarkBackground else LightBackground,
        cr = if (isDark) Color.White.copy(alpha = 0.025f) else Color.Black.copy(alpha = 0.04f),
        bd = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.08f),
        tp = if (isDark) Color(0xFFF0F0F5) else LightTextPrimary,
        ts = if (isDark) Color(0xFFF0F0F5).copy(alpha = 0.48f) else LightTextSecondary,
        tt = if (isDark) Color(0xFFF0F0F5).copy(alpha = 0.18f) else LightTextSecondary.copy(alpha = 0.45f),
        ac = red,
        acg = red.copy(alpha = 0.2f),
        acs = red.copy(alpha = 0.07f),
        gn = Color(0xFF2DD4A0),
        gns = Color(0xFF2DD4A0).copy(alpha = 0.07f),
        am = Color(0xFFF5A623),
        ams = Color(0xFFF5A623).copy(alpha = 0.07f),
        bl = Color(0xFF4E8CFF),
        bls = Color(0xFF4E8CFF).copy(alpha = 0.07f),
        pu = Color(0xFFA855F7),
        pus = Color(0xFFA855F7).copy(alpha = 0.07f),
        rs = Color(0xFFFB7185),
        rss = Color(0xFFFB7185).copy(alpha = 0.07f)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    isDark: Boolean,
    preferencesManager: PreferencesManager,
    userProfileManager: UserProfileManager,
    strings: LanguageManager.Strings,
    onLanguageClick: () -> Unit,
    onUnitsClick: () -> Unit,
    onLogout: () -> Unit,
    onProfileChanged: () -> Unit = {},
    onBiometricClick: () -> Unit,
    onBiometricChartsClick: () -> Unit = {},
    onNameChanged: (String) -> Unit = {},
    lastBiometric: BiometricEntity? = null,
    weeksSinceMeasurement: Int = -1,
    hasBiometricData: Boolean = false,
    totalWorkouts: Int = 0,
    currentStreak: Int = 0,
    bestStreak: Int = 0,
    totalVolume: Double = 0.0,
    earnedBadges: List<BadgeEntity> = emptyList(),
    allBiometrics: List<BiometricEntity> = emptyList(),
    paddingValues: PaddingValues = PaddingValues(),
    subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    onBioChanged: (String) -> Unit = {},
    onPhotoChanged: (String) -> Unit = {},
    onChangePassword: (String, String) -> Unit = { _, _ -> },
    onDeleteAccount: (String?) -> Unit = {},
    onNotification: () -> Unit = {}
) {
    val p = profilePalette(isDark)
    val context = androidx.compose.ui.platform.LocalContext.current

    val profile = userProfileManager.getOwnProfile()
    var profileName by remember { mutableStateOf(profile?.name?.ifBlank { null } ?: strings.guest) }
    var profileBio by remember { mutableStateOf(profile?.bio ?: "") }
    var profilePhotoUri by remember { mutableStateOf(profile?.photoUri ?: "") }
    val initials = profileName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<BadgeEntity?>(null) }
    // Cache-busting local: la fiecare poză nouă forțăm reload-ul în acest ecran
    var photoVersion by remember { mutableIntStateOf(0) }

    // ── Poză de profil: alege din galerie → upload direct (fără crop) ──
    // Crop-ul clasic ("com.android.camera.action.CROP") e deprecated și instabil
    // pe Android 11+ — uploadăm poza originală, fără pasul intermediar de crop.
    fun uploadPickedPhoto(uri: android.net.Uri) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job()).launch {
            try {
                val uid = userProfileManager.getOwnUserId()
                val downloadUrl = FirestoreHelper().uploadProfilePhoto(context, uid, uri)
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    profilePhotoUri = downloadUrl
                    photoVersion++
                }
                userProfileManager.saveOwnProfile(profileName, downloadUrl, profileBio)
                // Doar URL-urile reale (Firebase Storage) se sincronizează în cloud;
                // URI-urile locale file:// rămân doar pe acest device.
                if (downloadUrl.startsWith("http")) {
                    try { FirestoreHelper().saveUserProfile(uid, profileName, downloadUrl, profileBio) } catch (_: Exception) {}
                    try {
                        val db = AppDatabase.getDatabase(context)
                        SocialRepository(db).syncUserProfile(uid, profileName, downloadUrl)
                    } catch (_: Exception) {}
                }
                withContext(kotlinx.coroutines.Dispatchers.Main) { onPhotoChanged(downloadUrl) }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Failed to upload photo: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { picked -> uploadPickedPhoto(picked) }
    }

    val allBadgesList = BadgeEngine.ALL_BADGES
    val earnedKeys = earnedBadges.map { it.key }.toSet()

    val isLbs = preferencesManager.isLbs()
    val weightKg = preferencesManager.getUserWeight()
    val heightCm = preferencesManager.getUserHeight()
    val displayWeight = if (isLbs) weightKg * 2.20462f else weightKg
    val weightUnit = if (isLbs) "lbs" else strings.kg
    val bmi = if (heightCm > 0f) weightKg / ((heightCm / 100f) * (heightCm / 100f)) else 0f
    val bmiLabel = when {
        bmi < 18.5f -> "Under"
        bmi < 25f -> "Normal"
        bmi < 30f -> "Over"
        else -> "Obese"
    }
    val bmiColor = when {
        bmi < 18.5f -> p.am
        bmi < 25f -> p.gn
        bmi < 30f -> p.am
        else -> p.ac
    }

    val xp = totalWorkouts * 100
    val level = 1 + xp / 1000
    val levelRem = xp % 1000
    val levelProgress = levelRem / 1000f

    val memberSince = remember {
        (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.metadata?.creationTimestamp)
            ?.let { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(java.util.Date(it)) }
            ?: ""
    }

    val langName = remember(LanguageManager.getLanguage()) {
        getLanguageOptions(strings).firstOrNull { it.code == LanguageManager.getLanguage() }?.name ?: ""
    }

    val tierLabel = when (subscriptionTier) {
        SubscriptionTier.FREE -> "FREE"
        SubscriptionTier.PREMIUM_MONTHLY -> "PRO"
        SubscriptionTier.PREMIUM_ANNUAL -> "PRO+"
        SubscriptionTier.PRO_LIFETIME -> "LIFETIME"
    }
    val tierColor = when (subscriptionTier) {
        SubscriptionTier.FREE -> p.ts
        SubscriptionTier.PREMIUM_MONTHLY -> p.ac
        SubscriptionTier.PREMIUM_ANNUAL -> p.gn
        SubscriptionTier.PRO_LIFETIME -> p.am
    }

    val lastWeight = lastBiometric?.weightKg
    val prevWeight = run {
        val withWeight = allBiometrics.filter { it.weightKg > 0 }
        if (withWeight.size >= 2) withWeight[withWeight.size - 2].weightKg else null
    }
    val delta = if (lastWeight != null && prevWeight != null) lastWeight - prevWeight else null
    val deltaDisplay = delta?.let {
        val d = if (isLbs) it * 2.20462 else it
        String.format(Locale.US, "%+.1f %s", d, weightUnit)
    }
    val deltaIcon = if (delta != null && delta > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    val deltaTint = when { delta == null -> p.ts; delta < 0 -> p.gn; else -> p.am }
    val timeLabel = when {
        weeksSinceMeasurement == 0 -> strings.thisWeek
        weeksSinceMeasurement > 0 -> "$weeksSinceMeasurement ${strings.weeksAgo}"
        else -> ""
    }

    val goalKeys = listOf("strength", "mass", "weight_loss", "maintenance")
    val goalLabels = listOf(strings.goalStrength, strings.goalMass, strings.goalWeightLoss, strings.goalMaintenance)
    val expKeys = listOf("beginner", "intermediate", "advanced")
    val expLabels = listOf(strings.beginnerLabel, strings.intermediateLabel, strings.advancedLabel)
    val equipKeys = listOf("full_gym", "home_dumbbells", "home_no_equipment")
    val equipLabels = listOf(strings.fullGym, strings.homeDumbbells, strings.homeNoEquip)
    var goalKey by remember(preferencesManager.getFitnessGoal()) { mutableStateOf(preferencesManager.getFitnessGoal()) }
    var expKey by remember(preferencesManager.getExperienceLevel()) { mutableStateOf(preferencesManager.getExperienceLevel()) }
    var equipKey by remember(preferencesManager.getEquipmentAvailable()) { mutableStateOf(preferencesManager.getEquipmentAvailable()) }

    val goalSel = goalKeys.indexOf(goalKey).let { if (it >= 0) it else 0 }
    val expSel = expKeys.indexOf(expKey).let { if (it >= 0) it else 1 }
    val equipSel = equipKeys.indexOf(equipKey).let { if (it >= 0) it else 0 }
    val sessions = preferencesManager.getSessionsPerWeek()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.bg),
        contentAlignment = Alignment.TopCenter
    ) {
        var visibleItems by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            // Fade-in rapid: toate secțiunile apar simultan (300ms), fără stagger
            visibleItems = 8
        }

        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .background(p.sf, RoundedCornerShape(44.dp))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = AppConstants.BOTTOM_NAV_PADDING + 28.dp
                )
        ) {
            AnimatedVisibility(
                visible = visibleItems > 0,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            ProfileHeader(
                p = p,
                title = strings.profile.ifBlank { "Profile" },
                onNotification = onNotification,
                onEdit = { showEditDialog = true }
            )
            }

            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visibleItems > 1,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            ProfileHero(
                strings = strings,
                p = p,
                profileName = profileName,
                initials = initials,
                profileBio = profileBio,
                profilePhotoUri = cacheBustedPhotoUrl(profilePhotoUri, photoVersion),
                onAvatarClick = { photoPickerLauncher.launch("image/*") },
                tierLabel = tierLabel,
                tierColor = tierColor,
                level = level,
                xpText = "$levelRem / 1K ${strings.xp}",
                levelProgress = levelProgress,
                totalWorkouts = totalWorkouts,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                totalVolume = totalVolume,
                workoutsLabel = strings.workoutsLabel.ifBlank { "Workouts" }.uppercase(),
                volumeLabel = strings.totalVolumeLabel.ifBlank { "Volume" }.uppercase(),
                weightUnit = weightUnit
            )
            }

            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visibleItems > 2,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column {
            SectionLabel(p, strings.badges.ifBlank { "BADGES" }.uppercase())
            BadgesRow(
                p = p,
                badges = allBadgesList,
                earnedKeys = earnedKeys,
                onBadgeClick = { selectedBadge = it }
            )
            }
            }

            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visibleItems > 3,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column {
            SectionLabel(p, strings.personalInfo.ifBlank { "BODY METRICS" }.uppercase())
            BodyMetricsCard(
                p = p,
                strings = strings,
                weightValue = String.format(Locale.US, "%.0f", displayWeight),
                weightUnit = weightUnit,
                heightValue = String.format(Locale.US, "%.0f", heightCm),
                heightUnit = "cm",
                bmiValue = String.format(Locale.US, "%.1f", bmi),
                bmiLabel = bmiLabel,
                bmiColor = bmiColor,
                waterText = "${preferencesManager.getWaterGoalMl()} ${strings.ml}",
                memberSince = memberSince
            )
            }
            }

            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visibleItems > 4,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column {
            SectionLabel(p, strings.trainingSectionLabel.ifBlank { "TRAINING" }.uppercase())
            TrainingCard(
                p = p,
                strings = strings,
                goalLabels = goalLabels,
                goalSel = goalSel,
                expLabels = expLabels,
                expSel = expSel,
                equipLabels = equipLabels,
                equipSel = equipSel,
                sessions = sessions,
                onGoalChange = { i ->
                    val newKey = goalKeys[i]
                    goalKey = newKey
                    preferencesManager.setFitnessGoal(newKey)
                    onProfileChanged()
                },
                onExpChange = { i ->
                    val newKey = expKeys[i]
                    expKey = newKey
                    preferencesManager.setExperienceLevel(newKey)
                    onProfileChanged()
                },
                onEquipChange = { i ->
                    val newKey = equipKeys[i]
                    equipKey = newKey
                    preferencesManager.setEquipmentAvailable(newKey)
                    onProfileChanged()
                }
            )
            }
            }

            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visibleItems > 5,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column {
            SectionLabel(p, strings.biometricTracking.ifBlank { "BIOMETRIC" }.uppercase())
            BiometricCard(
                p = p,
                strings = strings,
                deltaIcon = deltaIcon,
                deltaText = deltaDisplay ?: "",
                deltaTint = deltaTint,
                timeLabel = timeLabel,
                lastWeightText = if (lastWeight != null) {
                    val v = if (isLbs) lastWeight * 2.20462 else lastWeight
                    String.format(Locale.US, "%s: %.1f %s", strings.weight, v, weightUnit)
                } else "",
                bodyFatText = (lastBiometric?.bodyFatPercent ?: 0.0).takeIf { it > 0 }?.let {
                    String.format(Locale.US, "%s: %.1f%s", strings.bodyFat, it, strings.percent)
                } ?: "",
                onMeasure = onBiometricClick,
                onChart = onBiometricChartsClick,
                measureLabel = strings.addMeasurement.ifBlank { "Measure" },
                chartLabel = strings.progressChart.ifBlank { "Chart" }
            )
            }
            }

            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visibleItems > 6,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            Column {
            SectionLabel(p, strings.settingsAndMore.ifBlank { "SETTINGS" }.uppercase())
            var notifOn by remember { mutableStateOf(preferencesManager.isBiometricReminderEnabled()) }
            SettingsCard(
                p = p,
                strings = strings,
                languageValue = langName,
                unitsValue = weightUnit,
                notifOn = notifOn,
                onNotifToggle = { enabled ->
                    notifOn = enabled
                    preferencesManager.setBiometricReminderEnabled(enabled)
                    val receiver = BiometricReminderReceiver()
                    if (enabled) receiver.scheduleWeekly(context) else receiver.cancelAlarm(context)
                },
                onLanguageClick = onLanguageClick,
                onUnitsClick = onUnitsClick,
                onPasswordClick = { showPasswordChangeDialog = true }
            )
            }
            }

            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visibleItems > 7,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(tween(300))
            ) {
            DangerZone(p, strings, onDelete = { showDeleteAccountDialog = true }, onLogout = onLogout)
            }
        }
    }

    selectedBadge?.let { badge ->
        val translated = LanguageManager.getTranslatedBadge(badge.key)
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            containerColor = p.sf,
            titleContentColor = p.tp,
            textContentColor = p.ts,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(BadgeEngine.badgeIconRes(badge.key)),
                        contentDescription = badge.title,
                        modifier = Modifier.size(32.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(p.ac)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(translated.title, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(translated.description, fontSize = 14.sp, color = p.ts)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(strings.howToGet, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.ac)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(translated.hint, fontSize = 14.sp, color = p.tp)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("OK", color = p.ac, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showEditDialog) {
        var nameText by remember { mutableStateOf(profileName) }
        var weightTextEdit by remember { mutableStateOf(String.format(Locale.US, "%.1f", displayWeight).trimEnd('0', '.')) }
        var heightTextEdit by remember { mutableStateOf(String.format(Locale.US, "%.1f", heightCm).trimEnd('0', '.')) }
        var bioEdit by remember { mutableStateOf(profileBio) }

        @Composable
        fun fieldColors() = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = p.ac,
            unfocusedBorderColor = p.ts,
            cursorColor = p.ac,
            focusedTextColor = p.tp,
            unfocusedTextColor = p.tp,
            focusedLabelColor = p.ac,
            unfocusedLabelColor = p.ts
        )

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = p.sf,
            titleContentColor = p.tp,
            textContentColor = p.ts,
            title = { Text(strings.editProfile.ifBlank { "Edit Profile" }, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text(strings.nameField) },
                        singleLine = true,
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = weightTextEdit,
                            onValueChange = { weightTextEdit = it },
                            label = { Text("${strings.weight} ($weightUnit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = fieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightTextEdit,
                            onValueChange = { heightTextEdit = it },
                            label = { Text("${strings.height} (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = fieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = bioEdit,
                        onValueChange = { bioEdit = it },
                        label = { Text(strings.bio) },
                        maxLines = 3,
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newName = nameText.trim()
                    val newBio = bioEdit.trim()
                    val newW = weightTextEdit.toFloatOrNull()
                    val newH = heightTextEdit.toFloatOrNull()
                    if (newName.isNotBlank() && newName != profileName) {
                        profileName = newName
                        onNameChanged(newName)
                        userProfileManager.saveOwnProfile(newName, profile?.photoUri ?: "")
                        val uid = userProfileManager.getOwnUserId()
                        if (uid != "local_user") {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job()).launch {
                                try { FirestoreHelper().saveUserProfile(uid, newName, profile?.photoUri ?: "") } catch (_: Exception) {}
                                try { AuthManager(context).updateDisplayName(newName) } catch (_: Exception) {}
                                try {
                                    val db = AppDatabase.getDatabase(context)
                                    SocialRepository(db).syncUserProfile(uid, newName, profile?.photoUri ?: "")
                                } catch (_: Exception) {}
                            }
                        }
                    }
                    if (newBio != profileBio) {
                        profileBio = newBio
                        onBioChanged(newBio)
                        userProfileManager.saveBio(newBio)
                        val uid = userProfileManager.getOwnUserId()
                        if (uid != "local_user") {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job()).launch {
                                try { FirestoreHelper().saveBio(uid, newBio) } catch (_: Exception) {}
                            }
                        }
                    }
                    if (newW != null) preferencesManager.setUserWeight(if (isLbs) newW / 2.20462f else newW)
                    if (newH != null) preferencesManager.setUserHeight(newH)
                    onProfileChanged()
                    showEditDialog = false
                }) {
                    Text(strings.confirm, color = p.ac, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(strings.cancel, color = p.ts)
                }
            }
        )
    }

    if (showPasswordChangeDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPasswordChangeDialog = false },
            containerColor = p.sf,
            titleContentColor = p.tp,
            textContentColor = p.ts,
            title = { Text(strings.changePassword.ifBlank { "Change Password" }, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it; passwordError = "" },
                        label = { Text(strings.currentPassword) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.ac,
                            unfocusedBorderColor = p.ts,
                            cursorColor = p.ac,
                            focusedTextColor = p.tp,
                            unfocusedTextColor = p.tp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; passwordError = "" },
                        label = { Text(strings.newPassword) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.ac,
                            unfocusedBorderColor = p.ts,
                            cursorColor = p.ac,
                            focusedTextColor = p.tp,
                            unfocusedTextColor = p.tp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; passwordError = "" },
                        label = { Text(strings.confirmNewPassword) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = p.ac,
                            unfocusedBorderColor = p.ts,
                            cursorColor = p.ac,
                            focusedTextColor = p.tp,
                            unfocusedTextColor = p.tp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passwordError.isNotBlank()) {
                        Text(passwordError, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        currentPassword.isBlank() -> passwordError = strings.currentPasswordRequired
                        newPassword.length < 6 -> passwordError = strings.passwordTooShort
                        newPassword != confirmPassword -> passwordError = strings.passwordMismatch
                        else -> {
                            onChangePassword(currentPassword, newPassword)
                            showPasswordChangeDialog = false
                        }
                    }
                }) {
                    Text(strings.changeLabel, color = p.ac, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordChangeDialog = false }) {
                    Text(strings.cancel, color = p.ts)
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        var deletePassword by remember { mutableStateOf("") }
        var deleteError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = p.sf,
            titleContentColor = Color.Red,
            textContentColor = p.ts,
            title = { Text(strings.deleteAccount, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        strings.deleteAccountWarning,
                        fontSize = 14.sp,
                        color = p.ts
                    )
                    if (!userProfileManager.getOwnProfile()?.userId.isNullOrBlank() &&
                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.providerData?.any { it.providerId == com.google.firebase.auth.EmailAuthProvider.PROVIDER_ID } == true) {
                        OutlinedTextField(
                            value = deletePassword,
                            onValueChange = { deletePassword = it; deleteError = "" },
                            label = { Text(strings.enterPasswordToConfirm) },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Red,
                                unfocusedBorderColor = p.ts,
                                cursorColor = Color.Red,
                                focusedTextColor = p.tp,
                                unfocusedTextColor = p.tp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (deleteError.isNotBlank()) {
                        Text(deleteError, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val isEmailUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.providerData?.any { it.providerId == com.google.firebase.auth.EmailAuthProvider.PROVIDER_ID } == true
                    if (isEmailUser && deletePassword.isBlank()) {
                        deleteError = strings.passwordRequiredToDelete
                    } else {
                        onDeleteAccount(if (isEmailUser) deletePassword else null)
                        showDeleteAccountDialog = false
                    }
                }) {
                    Text(strings.delete, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(strings.cancel, color = p.ts)
                }
            }
        )
    }
}

// ═══════════════════════════════════════
//  HEADER
// ═══════════════════════════════════════

@Composable
private fun ProfileHeader(p: ProfilePalette, title: String, onNotification: () -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontFamily = FontFamily.SansSerif,
            fontSize = 30.sp,
            fontWeight = FontWeight.W900,
            letterSpacing = (-1.5).sp,
            color = p.tp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HeaderBtn(p, Icons.Default.Edit, onEdit)
        }
    }
}

@Composable
private fun HeaderBtn(p: ProfilePalette, icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(p.cr)
            .border(1.dp, p.bd, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = p.ts, modifier = Modifier.size(12.dp))
    }
}

// ═══════════════════════════════════════
//  SECTION LABEL
// ═══════════════════════════════════════

@Composable
private fun SectionLabel(p: ProfilePalette, text: String) {
    Text(
        text,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.5.sp,
        color = p.tt,
        modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
    )
}

// ═══════════════════════════════════════
//  GLASS CARD
// ═══════════════════════════════════════

@Composable
private fun ProfileGlassCard(
    p: ProfilePalette,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(p.cr)
            .border(1.dp, p.bd, RoundedCornerShape(18.dp))
            .padding(14.dp),
        content = content
    )
}

// ═══════════════════════════════════════
//  PROFILE HERO
// ═══════════════════════════════════════

@Composable
private fun ProfileHero(
    strings: LanguageManager.Strings,
    p: ProfilePalette,
    profileName: String,
    initials: String,
    profileBio: String,
    profilePhotoUri: String,
    onAvatarClick: () -> Unit,
    tierLabel: String,
    tierColor: Color,
    level: Int,
    xpText: String,
    levelProgress: Float,
    totalWorkouts: Int,
    currentStreak: Int,
    bestStreak: Int,
    totalVolume: Double,
    workoutsLabel: String,
    volumeLabel: String,
    weightUnit: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        p.ac.copy(alpha = 0.08f),
                        p.pu.copy(alpha = 0.04f),
                        p.bl.copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    start = Offset.Zero,
                    end = Offset(800f, 600f)
                )
            )
            .border(1.dp, p.bd, RoundedCornerShape(22.dp))
            .padding(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(p.ac, Color(0xFFFF6B4A)),
                                start = Offset.Zero,
                                end = Offset(76f, 76f)
                            )
                        )
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoUri.isNotBlank()) {
                        AsyncImage(
                            model = profilePhotoUri,
                            contentDescription = null,
                            modifier = Modifier.size(76.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(initials, fontFamily = FontFamily.SansSerif, fontSize = 24.sp, fontWeight = FontWeight.W900, color = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(p.gn)
                        .border(3.dp, p.sf, CircleShape)
                )
            }

            Column {
                Text(profileName, fontFamily = FontFamily.SansSerif, fontSize = 22.sp, fontWeight = FontWeight.W800, letterSpacing = (-0.8).sp, color = p.tp)
                if (profileBio.isNotBlank()) {
                    Text(profileBio, fontSize = 11.sp, color = p.ts, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniBadge(p, androidx.compose.ui.res.painterResource(R.drawable.trophy_star), tierLabel, tierColor, tierColor.copy(alpha = 0.08f))
                    MiniBadge(p, androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Shield), "${strings.lv} $level", p.bl, p.bls)
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${strings.lv} $level", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = p.ac, letterSpacing = 1.sp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(p.cr)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(levelProgress.coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(p.ac, Color(0xFFFF6B4A))))
                )
            }
            Text(xpText, fontSize = 9.sp, color = p.ts)
        }

        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            val volFormatted = if (totalVolume >= 1000) String.format(Locale.US, "%.1fK", totalVolume / 1000) else String.format(Locale.US, "%.0f", totalVolume)
            StatItem(p, "$totalWorkouts", workoutsLabel, p.ac, "TOTAL", p.tt, Color.Transparent, Modifier.weight(1f))
            StatItem(p, "$currentStreak", "STREAK", p.am, "BEST $bestStreak", p.tt, Color.Transparent, Modifier.weight(1f))
            StatItem(p, volFormatted, volumeLabel, p.gn, weightUnit.uppercase(), p.tt, Color.Transparent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniBadge(p: ProfilePalette, icon: androidx.compose.ui.graphics.painter.Painter, text: String, tint: Color, bg: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .border(1.dp, tint.copy(alpha = 0.08f), RoundedCornerShape(5.dp))
            .padding(3.dp, 3.dp, 8.dp, 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, null, tint = if (icon is androidx.compose.ui.graphics.vector.VectorPainter) tint else androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(8.dp))
        Text(text, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = tint, letterSpacing = 0.6.sp)
    }
}

@Composable
private fun StatItem(
    p: ProfilePalette,
    value: String,
    label: String,
    valueColor: Color,
    sub: String,
    subColor: Color,
    subBg: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(p.cr)
            .border(1.dp, p.bd, RoundedCornerShape(12.dp))
            .padding(12.dp, 10.dp, 8.dp, 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontFamily = FontFamily.SansSerif, fontSize = 20.sp, fontWeight = FontWeight.W800, color = valueColor)
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Normal, color = p.tt, letterSpacing = 0.8.sp, maxLines = 1)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(subBg)
                .padding(1.dp, 1.dp, 5.dp, 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(sub, fontSize = 7.sp, fontWeight = FontWeight.SemiBold, color = subColor)
        }
    }
}

// ═══════════════════════════════════════
//  BADGES ROW
// ═══════════════════════════════════════

@Composable
private fun BadgesRow(
    p: ProfilePalette,
    badges: List<BadgeEntity>,
    earnedKeys: Set<String>,
    onBadgeClick: (BadgeEntity) -> Unit
) {
    val palette = listOf(p.ac, p.gn, p.am, p.pu, p.bl, p.rs)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        itemsIndexed(badges) { index, badge ->
            val earned = badge.key in earnedKeys
            val color = if (earned) palette[index % palette.size] else p.ts
            val bg = if (earned) color.copy(alpha = 0.12f) else p.cr
            val translated = LanguageManager.getTranslatedBadge(badge.key)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .border(1.dp, if (earned) color.copy(alpha = 0.1f) else p.bd, RoundedCornerShape(10.dp))
                    .clickable { onBadgeClick(badge) }
                    .padding(7.dp, 7.dp, 11.dp, 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (earned) color.copy(alpha = 0.15f) else p.cr),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(BadgeEngine.badgeIconRes(badge.key)),
                        contentDescription = translated.title,
                        modifier = Modifier
                            .size(12.dp)
                            .alpha(if (earned) 1f else 0.4f),
                        colorFilter = ColorFilter.tint(if (earned) color else p.ts.copy(alpha = 0.5f))
                    )
                }
                Text(
                    translated.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (earned) p.tp else p.ts.copy(alpha = 0.6f)
                )
                if (earned) {
                    Text("✓", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = color)
                }
            }
        }
    }
}

// ═══════════════════════════════════════
//  BODY METRICS CARD
// ═══════════════════════════════════════

@Composable
private fun BodyMetricsCard(
    p: ProfilePalette,
    strings: LanguageManager.Strings,
    weightValue: String,
    weightUnit: String,
    heightValue: String,
    heightUnit: String,
    bmiValue: String,
    bmiLabel: String,
    bmiColor: Color,
    waterText: String,
    memberSince: String
) {
    ProfileGlassCard(p) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricBlock(p, strings.weight.uppercase(), weightValue, weightUnit, p.ac, Modifier.weight(1f))
            MetricBlock(p, strings.height.uppercase(), heightValue, heightUnit, p.bl, Modifier.weight(1f))
            MetricBlock(p, "BMI", bmiValue, bmiLabel, bmiColor, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        InfoRow(p, Icons.Default.WaterDrop, p.gn, p.gns, strings.waterGoal, waterText)
        InfoRow(p, Icons.Default.CalendarMonth, p.am, p.ams, strings.memberSince.ifBlank { "Member since" }, memberSince.ifBlank { "—" })
    }
}

@Composable
private fun MetricBlock(
    p: ProfilePalette,
    label: String,
    value: String,
    unit: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(p.cr)
            .border(1.dp, p.bd, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 9.sp, color = p.tt, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(value, fontFamily = FontFamily.SansSerif, fontSize = 22.sp, fontWeight = FontWeight.W800, color = valueColor)
        Text(unit, fontSize = 9.sp, color = p.ts, maxLines = 1)
    }
}

@Composable
private fun InfoRow(
    p: ProfilePalette,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .drawBehind {
                drawLine(p.bd, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(11.dp))
            }
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = p.tp)
        }
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.ts)
    }
}

// ═══════════════════════════════════════
//  TRAINING CARD
// ═══════════════════════════════════════

@Composable
private fun TrainingCard(
    p: ProfilePalette,
    strings: LanguageManager.Strings,
    goalLabels: List<String>,
    goalSel: Int,
    expLabels: List<String>,
    expSel: Int,
    equipLabels: List<String>,
    equipSel: Int,
    sessions: Int,
    onGoalChange: (Int) -> Unit,
    onExpChange: (Int) -> Unit,
    onEquipChange: (Int) -> Unit
) {
    ProfileGlassCard(p) {
        PillSection(p, strings.profileGoalLabel.ifBlank { "GOAL" }.uppercase(), goalLabels, goalSel, onGoalChange, p.acs, p.ac)

        Spacer(Modifier.height(14.dp))

        PillSection(p, strings.profileExperienceLabel.ifBlank { "EXPERIENCE" }.uppercase(), expLabels, expSel, onExpChange, p.bls, p.bl)

        Spacer(Modifier.height(14.dp))

        PillSection(p, strings.profileEquipmentLabel.ifBlank { "EQUIPMENT" }.uppercase(), equipLabels, equipSel, onEquipChange, p.pus, p.pu)

        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(strings.frequencyLabel.ifBlank { "FREQUENCY" }.uppercase(), fontSize = 9.sp, color = p.tt, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
            Text("$sessions ${strings.xPerWeek.ifBlank { "x / week" }}", fontSize = 9.sp, color = p.ts)
        }
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(7) { i ->
                val f = if (i < sessions.coerceIn(0, 7)) 1f - (i * 0.12f) else 0f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(if (f > 0f) p.ac.copy(alpha = f) else p.cr)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PillSection(
    p: ProfilePalette,
    label: String,
    items: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    selColor: Color,
    selTint: Color
) {
    Text(label, fontSize = 9.sp, color = p.tt, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(7.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        items.forEachIndexed { i, item ->
            val isSel = i == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (isSel) selColor else p.cr)
                    .border(1.dp, if (isSel) Color.Transparent else p.bd, RoundedCornerShape(7.dp))
                    .clickable { onSelect(i) }
                    .padding(5.dp, 5.dp, 10.dp, 5.dp)
            ) {
                Text(
                    item,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSel) selTint else p.ts,
                    letterSpacing = 0.3.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// ═══════════════════════════════════════
//  BIOMETRIC CARD
// ═══════════════════════════════════════

@Composable
private fun BiometricCard(
    p: ProfilePalette,
    strings: LanguageManager.Strings,
    deltaIcon: ImageVector,
    deltaText: String,
    deltaTint: Color,
    timeLabel: String,
    lastWeightText: String,
    bodyFatText: String,
    measureLabel: String,
    chartLabel: String,
    onMeasure: () -> Unit,
    onChart: () -> Unit
) {
    ProfileGlassCard(p) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(p.cr)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.85f)
                    cubicTo(w * 0.25f, h * 0.78f, w * 0.35f, h * 0.68f, w * 0.5f, h * 0.5f)
                    cubicTo(w * 0.65f, h * 0.4f, w * 0.85f, h * 0.3f, w, h * 0.28f)
                }
                drawPath(
                    path,
                    Brush.horizontalGradient(listOf(p.ac, Color(0xFFFF6B4A))),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawCircle(p.ac, radius = 3.dp.toPx(), center = Offset(w, h * 0.28f))
                drawCircle(p.ac.copy(alpha = 0.12f), radius = 6.dp.toPx(), center = Offset(w, h * 0.28f))
            }

            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (deltaText.isNotBlank()) {
                    Icon(deltaIcon, null, tint = deltaTint, modifier = Modifier.size(7.dp))
                    Text(deltaText, fontSize = 8.sp, color = p.ts)
                }
            }
            Text(
                timeLabel.ifBlank { "—" },
                fontSize = 7.sp,
                color = p.tt,
                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
            )
        }

        if (lastWeightText.isNotBlank() || bodyFatText.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (lastWeightText.isNotBlank()) {
                    Text(lastWeightText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.tp)
                }
                if (bodyFatText.isNotBlank()) {
                    Text(bodyFatText, fontSize = 11.sp, color = p.ts)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onMeasure,
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(p.ac, Color(0xFFFF6B4A)))),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(9.dp))
                        Text(measureLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp)
                    }
                }
            }

            OutlinedButton(
                onClick = onChart,
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, p.bd),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = p.cr, contentColor = p.ts)
            ) {
                Icon(Icons.Default.AreaChart, null, modifier = Modifier.size(9.dp))
                Spacer(Modifier.width(5.dp))
                Text(chartLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp)
            }
        }
    }
}

// ═══════════════════════════════════════
//  SETTINGS CARD
// ═══════════════════════════════════════

@Composable
private fun SettingsCard(
    p: ProfilePalette,
    strings: LanguageManager.Strings,
    languageValue: String,
    unitsValue: String,
    notifOn: Boolean,
    onNotifToggle: (Boolean) -> Unit,
    onLanguageClick: () -> Unit,
    onUnitsClick: () -> Unit,
    onPasswordClick: () -> Unit
) {
    ProfileGlassCard(p, modifier = Modifier.padding(vertical = 2.dp)) {
        SettingRow(p, Icons.Default.Language, p.pu, p.pus, strings.language.ifBlank { "Language" }, languageValue, onClick = onLanguageClick)
        SettingRow(p, Icons.Default.SquareFoot, p.am, p.ams, strings.units.ifBlank { "Units" }, unitsValue, onClick = onUnitsClick)
        SettingRowToggle(p, Icons.Default.Notifications, p.gn, p.gns, strings.biometricReminderTitle.ifBlank { "Notifications" }, notifOn, onNotifToggle)
        SettingRow(p, Icons.Default.Lock, p.rs, p.rss, strings.changePassword.ifBlank { "Change Password" }, null, onClick = onPasswordClick)
    }
}

@Composable
private fun SettingRow(
    p: ProfilePalette,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp)
            .drawBehind {
                drawLine(p.bd, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(11.dp)) }
        Spacer(Modifier.width(11.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = p.tp, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, fontSize = 10.sp, color = p.ts, modifier = Modifier.padding(end = 3.dp), maxLines = 1)
        }
        Icon(Icons.Default.ChevronRight, null, tint = p.tt, modifier = Modifier.size(9.dp))
    }
}

@Composable
private fun SettingRowToggle(
    p: ProfilePalette,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    label: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp)
            .drawBehind {
                drawLine(p.bd, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(11.dp)) }
        Spacer(Modifier.width(11.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = p.tp, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(38.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isOn) p.acs else p.cr)
                .border(1.dp, if (isOn) p.ac.copy(alpha = 0.12f) else p.bd, RoundedCornerShape(10.dp))
                .clickable { onToggle(!isOn) },
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(14.dp)
                    .offset(x = if (isOn) 20.dp else 0.dp)
                    .clip(CircleShape)
                    .background(if (isOn) p.ac else p.ts)
            )
        }
    }
}

// ═══════════════════════════════════════
//  DANGER ZONE
// ═══════════════════════════════════════

@Composable
private fun DangerZone(
    p: ProfilePalette,
    strings: LanguageManager.Strings,
    onDelete: () -> Unit,
    onLogout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(p.ac.copy(alpha = 0.04f))
                .border(1.dp, p.ac.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .clickable(onClick = onDelete)
                .padding(11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Delete, null, tint = p.ac, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(5.dp))
            Text(strings.deleteAccount.ifBlank { "Delete Account" }, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.ac, letterSpacing = 0.3.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(p.cr)
                .border(1.dp, p.bd, RoundedCornerShape(12.dp))
                .clickable(onClick = onLogout)
                .padding(11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Logout, null, tint = p.ts, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(5.dp))
            Text(strings.logout.ifBlank { "Logout" }, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = p.ts, letterSpacing = 0.3.sp)
        }
    }
}
