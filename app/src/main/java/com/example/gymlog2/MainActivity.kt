package com.example.gymlog2

import android.app.PictureInPictureParams
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import kotlinx.coroutines.tasks.await
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gymlog2.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableLongStateOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesManager = PreferencesManager(this)
        LanguageManager.loadSavedLanguage(this)

        setContent {
            val themeMode = remember { mutableStateOf(preferencesManager.getThemeMode()) }
            var showWelcome by remember { mutableStateOf(preferencesManager.isLoggedIn()) }
            val context = androidx.compose.ui.platform.LocalContext.current
            val strings = LanguageManager.getStrings(context)
            val userName = remember {
                val profile = UserProfileManager(this).getOwnProfile()
                profile?.name?.takeIf { it.isNotBlank() && it != "Guest" && it != "Facebook User" } ?: ""
            }

            GymLOGTheme(themeMode = themeMode.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                ) {
                    if (showWelcome) {
                        WelcomeScreen(
                            userName = userName.ifEmpty { strings.athlete },
                            strings = strings,
                            onFinished = { showWelcome = false }
                        )
                    } else {
                        var mainAlphaTarget by remember { mutableFloatStateOf(0f) }
                        val mainAlpha by animateFloatAsState(
                            targetValue = mainAlphaTarget,
                            animationSpec = tween(500),
                            label = "mainAlpha"
                        )
                        LaunchedEffect(Unit) {
                            mainAlphaTarget = 1f
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(mainAlpha),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            MuscleGroupList(
                                onThemeChanged = { themeMode.value = it }
                            )
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (GpsTrackingState.isTracking && !GpsTrackingState.isInPipMode) {
            enterPipMode()
        }
    }
}

    private fun todayKey(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(java.util.Date())
    }

    // ============================================
    // Helper functions for weight conversion
// ============================================
private fun convertWeight(kg: Double, isLbs: Boolean): Double = if (isLbs) kg * 2.20462 else kg
internal fun weightLabel(kg: Double, isLbs: Boolean): String {
    val value = if (isLbs) kg * 2.20462 else kg
    val unit = if (isLbs) "lbs" else "kg"
    return if (value == value.toLong().toDouble()) "${value.toLong()} $unit" else "${String.format("%.1f", value)} $unit"
}

// ============================================
// Ecranul 1: Lista de Grupe Musculare
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleGroupList(onThemeChanged: (ThemeMode) -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val userProfileManager = remember { UserProfileManager(context) }

    LaunchedEffect(Unit) {
        val savedUrl = preferencesManager.getServerUrl()
        if (savedUrl.isNotBlank()) {
            NetworkClient.getApi(savedUrl)
        }
    }

    var isLoggedIn by remember { mutableStateOf(preferencesManager.isLoggedIn()) }
    var showOnboarding by remember {
        mutableStateOf(isLoggedIn && !preferencesManager.isOnboardingComplete() &&
            (userProfileManager.getOwnProfile()?.name ?: "").let { it.isEmpty() || it == "Guest" || it == "Facebook User" })
    }
    var showProfileSetup by remember { mutableStateOf(false) }
    var showSignUp by remember { mutableStateOf(false) }
    var selectedGroup: String? by remember { mutableStateOf(null) }
    var selectedDirectExercise: ExerciseDefinition? by remember { mutableStateOf(null) }
    var selectedDirectGroup: String? by remember { mutableStateOf(null) }
    var selectedDirectProgressExercise: String? by remember { mutableStateOf(null) }
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
    var isLbs by remember { mutableStateOf(preferencesManager.isLbs()) }
    var currentLanguage by remember { mutableStateOf(LanguageManager.getLanguage()) }
    var currentThemeMode by remember { mutableStateOf(preferencesManager.getThemeMode()) }
    var reloadToken by remember { mutableIntStateOf(0) }
    var badgeCheckTrigger by remember { mutableIntStateOf(0) }
    var newBadgeNotifications by remember { mutableStateOf<List<String>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val strings = LanguageManager.getStrings(context)
    val isDark = when (currentThemeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val profile = userProfileManager.getOwnProfile()
    var profileName by remember { mutableStateOf(profile?.name ?: strings.guest) }
    val profilePhoto = profile?.photoUri ?: ""
    val userId = profile?.userId ?: userProfileManager.getOwnUserId()

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
                            SocialRepository(db).syncUserProfile(userId, profileName, profilePhoto)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val prefs = PreferencesManager(context)
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
    var todayCardioDistance by remember { mutableDoubleStateOf(0.0) }
    var todayCardioDuration by remember { mutableLongStateOf(0L) }
    var todayCardioCalories by remember { mutableDoubleStateOf(0.0) }
    var todayStepsEstimate by remember { mutableIntStateOf(0) }
    var pedometerSteps by remember { mutableIntStateOf(0) }
    var manualSteps by remember { mutableIntStateOf(0) }

    val onDashboard by remember {
        derivedStateOf {
            selectedGroup == null && !showCalendar && !showTemplates && !showBiometricInput &&
                !showBiometricCharts && !showFoodJournal && !showBarcodeScanner && !showAddFood &&
                !showAiTrainer && !showPlateCalculator && !showOneRMCalculator && !showWorkoutAnalytics &&
                currentPage != DrawerPage.GPS_CARDIO && currentPage != DrawerPage.REST_DAYS
        }
    }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val prefs = context.getSharedPreferences("pedometer_prefs", android.content.Context.MODE_PRIVATE)
        var initialSteps = prefs.getFloat("initial_steps_${todayKey()}", -1f).toInt()

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val totalSteps = event.values[0].toInt()
                if (initialSteps < 0) {
                    initialSteps = totalSteps
                    prefs.edit().putFloat("initial_steps_${todayKey()}", initialSteps.toFloat()).apply()
                }
                pedometerSteps = (totalSteps - initialSteps).coerceAtLeast(0)
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
        derivedStateOf { (todayStepsEstimate + pedometerSteps + manualSteps).coerceIn(0, 99999) }
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
                BadgeEngine.ALL_BADGES.find { it.key == key }?.title ?: key
            }
            snackbarHostState.showSnackbar("🏆 $badgeNames")
            newBadgeNotifications = emptyList()
        }
    }

    LaunchedEffect(reloadToken, userId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val prefs = PreferencesManager(context)
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

    LaunchedEffect(reloadToken, onDashboard) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
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

            weeklyTotalKg = db.antrenamentDao().getTotalVolume(AppConstants.DEFAULT_USER_ID, weekStart, weekEnd) ?: 0.0
            val mostFrequent = db.exercitiuDao().getMostFrequentExercise(AppConstants.DEFAULT_USER_ID, weekStart, weekEnd)
            weeklyTopExercise = mostFrequent?.numeExercitiu

            val weekWorkouts = db.antrenamentDao().getWorkoutsInPeriod(AppConstants.DEFAULT_USER_ID, weekStart, weekEnd)
            weekWorkoutCount = weekWorkouts.size
            weekVolume = weekWorkouts.sumOf { it.totalWeight }

            val lastWeekStart = weekStart - 7L * 24 * 60 * 60 * 1000
            val lastWeekEnd = weekStart
            val lastWeekWorkouts = db.antrenamentDao().getWorkoutsInPeriod(AppConstants.DEFAULT_USER_ID, lastWeekStart, lastWeekEnd)
            lastWeekWorkoutCount = lastWeekWorkouts.size
            lastWeekVolume = lastWeekWorkouts.sumOf { it.totalWeight }

            val todayWorkouts = db.antrenamentDao().getWorkoutsInPeriod(AppConstants.DEFAULT_USER_ID, dayStart, dayEnd)
            todayVolume = todayWorkouts.sumOf { it.totalWeight }
            if (todayWorkouts.isNotEmpty()) {
                val allTodayExercises = db.exercitiuDao().getForAntrenaments(todayWorkouts.map { it.id })
                todayExercises = allTodayExercises.map { it.numeExercitiu }.distinct()
            } else {
                todayExercises = emptyList()
            }

            val prs = db.personalRecordDao().getAllForUser(AppConstants.DEFAULT_USER_ID)
            lastPR = prs.firstOrNull()
            allRecentPRs = prs.take(5)

            val cal3 = java.util.Calendar.getInstance()
            cal3.timeInMillis = System.currentTimeMillis()
            cal3.add(java.util.Calendar.DAY_OF_YEAR, -365)
            val yearAgo = cal3.timeInMillis
            allExerciseNames = db.exercitiuDao().getDistinctExerciseNames(AppConstants.DEFAULT_USER_ID, yearAgo, System.currentTimeMillis())

            recoveryMap = AntrenamentRepository(db).getToateRecuperarile().toMap()

            val streakEntity = db.streakDao().getForUser(userId)
            currentStreak = streakEntity?.currentStreak ?: 0
            bestStreak = streakEntity?.bestStreak ?: 0

            val userBadges = db.userBadgeDao().getForUser(userId)
            badgeCount = userBadges.size
            val allBadges = db.badgeDao().getAll()
            val badgeMap = allBadges.associateBy { it.key }
            recentBadges = userBadges.mapNotNull { badgeMap[it.badgeKey] }

            val allWorkouts = db.antrenamentDao().getAllForUser(userId)
            totalAllWorkouts = allWorkouts.size
            totalAllVolume = allWorkouts.sumOf { it.totalWeight }

            val cardioSummary = db.cardioRouteDao().getTodaySummary(userId, dayStart, dayEnd)
            todayCardioDistance = cardioSummary?.totalDistance ?: 0.0
            todayCardioDuration = cardioSummary?.totalDuration ?: 0L
            todayCardioCalories = cardioSummary?.totalCalories ?: 0.0
            todayStepsEstimate = (todayCardioDistance * 1312).toInt().coerceIn(0, 99999)
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
                    val workouts = db.antrenamentDao().getAllForUser(AppConstants.DEFAULT_USER_ID)
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
                        repo.salveazaAntrenamentSimple(session.grupaMusculara, ex.numeExercitiu, ex.seturi, "")
                    }
                }
                recoveryMap = repo.getToateRecuperarile().toMap()
            }
            reloadToken++
        }
    }

    val authManager = remember { AuthManager(context) }
    val loginHandler = remember { LoginHandler(context, preferencesManager, userProfileManager, authManager, scope) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }

    val googleSignInClient = remember {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(preferencesManager.getGoogleOAuthClientId())
            .requestEmail()
            .build()
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
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
            onEmailLogin = { email, _ ->
                kotlinx.coroutines.MainScope().launch {
                    val result = loginHandler.loginWithEmail(email)
                    isLoggedIn = true
                }
            },
            onGoogleLogin = {
                googleSignInError = null
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            },
            onFacebookLogin = {
                kotlinx.coroutines.MainScope().launch {
                    loginHandler.loginWithFacebook()
                    isLoggedIn = true
                }
            },
            onGuestLogin = {
                kotlinx.coroutines.MainScope().launch {
                    loginHandler.loginAsGuest()
                    isLoggedIn = true
                }
            },
            onSignUpClick = { showSignUp = true }
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
                onSignUp = { name, email, password, _, _ ->
                    kotlinx.coroutines.MainScope().launch {
                        val result = loginHandler.loginWithEmail(email)
                        isLoggedIn = true
                    }
                },
                onGoogleSignUp = {},
                onFacebookSignUp = {},
                onLoginClick = { showSignUp = false }
            )
        }

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
                    showOnboarding = false
                    showProfileSetup = true
                }
            )
        } else if (showProfileSetup) {
            ProfileSetupScreen(
                strings = strings,
                onSave = { name, photoUri ->
                    val uid = userProfileManager.getOwnUserId()
                    userProfileManager.createOrUpdateProfile(
                        name = name,
                        photoUri = photoUri,
                        userId = uid
                    )
                    preferencesManager.setOnboardingComplete(true)
                    showProfileSetup = false
                                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job()).launch {
                        try { SocialRepository(AppDatabase.getDatabase(context)).syncUserProfile(uid, name, photoUri) } catch (_: Exception) {}
                    }
                }
            )
        }
        return
    }

    BackHandler {
        when {
            selectedDirectProgressExercise != null -> { selectedDirectProgressExercise = null }
            selectedDirectExercise != null -> { selectedDirectExercise = null; selectedDirectGroup = null }
            selectedGroup != null -> selectedGroup = null
            showCalendar -> { showCalendar = false; currentPage = null }
            showTemplates -> { showTemplates = false; currentPage = null }
            showPlateCalculator -> { showPlateCalculator = false; currentPage = null }
            showOneRMCalculator -> { showOneRMCalculator = false; currentPage = null }
            showWorkoutAnalytics -> { showWorkoutAnalytics = false; currentPage = null }
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
                    userId = userId,
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
                            null -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.CALENDAR -> { showCalendar = true; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.FOOD_JOURNAL -> { showCalendar = false; showTemplates = false; showFoodJournal = true; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.AI_TRAINER -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = true; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.FRIENDS -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = true; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.GPS_CARDIO -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.REST_DAYS -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.PLATE_CALCULATOR -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = true; showOneRMCalculator = false; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.ONE_RM_CALCULATOR -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = true; showWorkoutAnalytics = false; selectedGroup = null }
                            DrawerPage.WORKOUT_ANALYTICS -> { showCalendar = false; showTemplates = false; showFoodJournal = false; showBarcodeScanner = false; showAddFood = false; showAiTrainer = false; showFriends = false; showLeaderboard = false; showPlateCalculator = false; showOneRMCalculator = false; showWorkoutAnalytics = true; selectedGroup = null }
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
                    onOpenServerSettings = { showServerDialog = true }
                )
            }
        ) {
        // PiP activ pe GPS Cardio: randăm doar harta, fără Scaffold/bottom bar
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
        val surfaceBg = if (isDark) bgColor() else LightBackground
        val textPrimary = if (isDark) textColor() else LightTextPrimary
        val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
        val cardBg = if (isDark) cardColor() else LightCard
        val accent = if (isDark) accentColor() else LightPrimaryRed
        val iconBg = if (isDark) IconBackground else LightIconBackground

        Scaffold(
            containerColor = surfaceBg,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(containerColor = surfaceBg) {
                    NavigationBarItem(
                        selected = currentDashboardTab == 0,
                        onClick = {
                            if (currentDashboardTab == 0) {
                                when {
                                    selectedGroup != null -> selectedGroup = null
                                    showTemplates -> { showTemplates = false; currentPage = null }
                                    showCalendar -> { showCalendar = false; currentPage = null }
                                    showFoodJournal -> { showFoodJournal = false; currentPage = null }
                                    showAiTrainer -> { showAiTrainer = false; currentPage = null }
                                    showBarcodeScanner -> { showBarcodeScanner = false; currentPage = null }
                                    showAddFood -> { showAddFood = false; currentPage = null }
                                    showBiometricInput -> { showBiometricInput = false; currentPage = null }
                                    showBiometricCharts -> { showBiometricCharts = false; currentPage = null }
                                    showFriends -> { showFriends = false; currentPage = null }
                                    showLeaderboard -> { showLeaderboard = false; currentPage = null }
                                    currentPage != null -> currentPage = null
                                }
                            } else {
                                currentDashboardTab = 0
                                selectedGroup = null
                                showCalendar = false
                                showTemplates = false
                                showFoodJournal = false
                                showAiTrainer = false
                                showBarcodeScanner = false
                                showAddFood = false
                                showBiometricInput = false
                                showBiometricCharts = false
                                showFriends = false
                                showLeaderboard = false
                                currentPage = null
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = strings.acasa,
                                tint = if (currentDashboardTab == 0) accent else textSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary,
                            indicatorColor = cardBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentDashboardTab == 1,
                        onClick = {
                            if (currentDashboardTab == 1) {
                                when {
                                    showTemplates -> { showTemplates = false; currentPage = null }
                                    currentPage != null -> currentPage = null
                                }
                            } else {
                                currentDashboardTab = 1
                                selectedGroup = null
                                showCalendar = false
                                showTemplates = false
                                showFoodJournal = false
                                showAiTrainer = false
                                showBarcodeScanner = false
                                showAddFood = false
                                showBiometricInput = false
                                showBiometricCharts = false
                                showFriends = false
                                showLeaderboard = false
                                currentPage = null
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = strings.workouts,
                                tint = if (currentDashboardTab == 1) accent else textSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary,
                            indicatorColor = cardBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentDashboardTab == 2,
                        onClick = {
                            if (currentDashboardTab == 2) {
                                when {
                                    currentPage != null -> currentPage = null
                                }
                            } else {
                                currentDashboardTab = 2
                                selectedGroup = null
                                showCalendar = false
                                showTemplates = false
                                showFoodJournal = false
                                showAiTrainer = false
                                showBarcodeScanner = false
                                showAddFood = false
                                showBiometricInput = false
                                showBiometricCharts = false
                                showFriends = false
                                showLeaderboard = false
                                currentPage = null
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = strings.stats,
                                tint = if (currentDashboardTab == 2) accent else textSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary,
                            indicatorColor = cardBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentDashboardTab == 3,
                        onClick = {
                            if (currentDashboardTab == 3) {
                                when {
                                    currentPage != null -> currentPage = null
                                }
                            } else {
                                currentDashboardTab = 3
                                selectedGroup = null
                                showCalendar = false
                                showTemplates = false
                                showFoodJournal = false
                                showAiTrainer = false
                                showBarcodeScanner = false
                                showAddFood = false
                                showBiometricInput = false
                                showBiometricCharts = false
                                showFriends = false
                                showLeaderboard = false
                                currentPage = null
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.LocalDrink,
                                contentDescription = strings.waterIntake,
                                tint = if (currentDashboardTab == 3) accent else textSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary,
                            indicatorColor = cardBg
                        )
                    )
                    NavigationBarItem(
                        selected = currentDashboardTab == 4,
                        onClick = {
                            if (currentDashboardTab == 4) {
                                when {
                                    showBiometricCharts -> { showBiometricCharts = false; currentPage = null }
                                    showBiometricInput -> { showBiometricInput = false; currentPage = null }
                                    currentPage != null -> currentPage = null
                                }
                            } else {
                                currentDashboardTab = 4
                                selectedGroup = null
                                showCalendar = false
                                showTemplates = false
                                showFoodJournal = false
                                showAiTrainer = false
                                showBarcodeScanner = false
                                showAddFood = false
                                showBiometricInput = false
                                showBiometricCharts = false
                                showFriends = false
                                showLeaderboard = false
                                currentPage = null
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = strings.profile,
                                tint = if (currentDashboardTab == 4) accent else textSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accent,
                            selectedTextColor = accent,
                            unselectedIconColor = textSecondary,
                            unselectedTextColor = textSecondary,
                            indicatorColor = cardBg
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (showTemplates) {
                    TemplateScreen(onBackClick = { showTemplates = false; currentPage = null })
                } else if (showCalendar) {
                    CalendarWorkoutScreen(
                        onBackClick = { showCalendar = false; currentPage = null },
                        onWorkoutDeleted = { reloadToken++ }
                    )
                } else if (showBiometricInput) {
                    BiometricInputScreen(
                        isDark = isDark,
                        strings = strings,
                        latestEntry = lastBiometric,
                        onSave = { weight, bf, waist, hips, thighs, chest, arms ->
                            scope.launch {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val prefs = PreferencesManager(context)
                                    val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                                    val bm = BiometricManager(db, syncRepo)
                                    bm.saveEntry(userId, weight, bf, waist, hips, thighs, chest, arms)
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
                } else if (showBiometricCharts) {
                    BiometricChartScreen(
                        isDark = isDark,
                        strings = strings,
                        entries = allBiometrics,
                        onDelete = { entry ->
                            scope.launch {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val prefs = PreferencesManager(context)
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
                        onBack = { showFoodJournal = false; currentPage = null }
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
                        onSave = { name, brand, mealType, servingSize, calories, protein, carbs, fat, fiber ->
                            scope.launch {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val prefs = PreferencesManager(context)
                                    val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                                    val fm = FoodManager(db, syncRepo)
                                    fm.addFood(
                                        userId = userId,
                                        barcode = pendingFoodProduct?.barcode ?: "",
                                        name = name,
                                        brand = brand,
                                        mealType = mealType,
                                        servingSize = servingSize,
                                        servingUnit = pendingFoodProduct?.servingUnit ?: "g",
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
                        },
                        onBack = { showAddFood = false; pendingFoodProduct = null; showFoodJournal = true }
                    )
                } else if (showAiTrainer) {
                    val db = remember { AppDatabase.getDatabase(context) }
                    val manager = remember { AiTrainerManager(db) }
                    AiTrainerScreen(
                        aiManager = manager,
                        isDark = isDark,
                        strings = strings,
                        userId = userId,
                        preferencesManager = preferencesManager,
                        onBack = { showAiTrainer = false; currentPage = null }
                    )
                } else if (showLeaderboard) {
                    LeaderboardScreen(
                        isDark = isDark,
                        isLbs = isLbs,
                        strings = strings,
                        onBackClick = { showLeaderboard = false; showFriends = true }
                    )
                } else if (showFriends) {
                    FriendsScreen(
                        isDark = isDark,
                        isLbs = isLbs,
                        strings = strings,
                        onBackClick = { showFriends = false; currentPage = null },
                        onOpenLeaderboard = { showFriends = false; showLeaderboard = true }
                    )
                } else if (showPlateCalculator) {
                    PlateCalculatorScreen(
                        isDark = isDark,
                        strings = strings,
                        onBack = { showPlateCalculator = false; currentPage = null }
                    )
                } else if (showOneRMCalculator) {
                    OneRMCalculatorScreen(
                        isDark = isDark,
                        strings = strings,
                        onBack = { showOneRMCalculator = false; currentPage = null }
                    )
                } else if (showWorkoutAnalytics) {
                    val analyticsDb = AppDatabase.getDatabase(context)
                    WorkoutAnalyticsScreen(
                        isDark = isDark,
                        strings = strings,
                        db = analyticsDb,
                        userId = userId,
                        onBack = { showWorkoutAnalytics = false; currentPage = null }
                    )
                } else if (currentPage == DrawerPage.GPS_CARDIO) {
                    GpsCardioScreen(
                        isDark = isDark,
                        strings = strings,
                        userId = userId,
                        onBack = { currentPage = null }
                    )
                } else if (currentPage == DrawerPage.REST_DAYS) {
                    RestDayScreen(
                        isDark = isDark,
                        strings = strings,
                        userId = userId,
                        recoveryMap = recoveryMap,
                        onBack = { currentPage = null }
                    )
                } else if (selectedDirectProgressExercise != null) {
                    CalendarScreen(
                        isLbs = isLbs,
                        initialExercise = selectedDirectProgressExercise,
                        onBackClick = { selectedDirectProgressExercise = null }
                    )
                } else if (selectedDirectExercise != null) {
                    ExerciseInputScreen(
                        exercise = selectedDirectExercise!!,
                        grupaMusculara = selectedDirectGroup ?: "",
                        isLbs = isLbs,
                        onBackClick = { selectedDirectExercise = null; selectedDirectGroup = null },
                        onOpenProgress = { name -> selectedDirectExercise = null; selectedDirectGroup = null; selectedDirectProgressExercise = name },
                        onWorkoutSaved = { reloadToken++; badgeCheckTrigger++; scope.launch { recoveryMap = AntrenamentRepository(db).getToateRecuperarile().toMap() } },
                        strings = strings
                    )
                } else if (selectedGroup != null) {
                    ExerciseListScreen(
                        grupaMusculara = selectedGroup!!,
                        isLbs = isLbs,
                        isDark = isDark,
                        onBackClick = { selectedGroup = null },
                        onWorkoutSaved = { reloadToken++; badgeCheckTrigger++; scope.launch { recoveryMap = AntrenamentRepository(db).getToateRecuperarile().toMap() } }
                    )
                } else if (currentPage == DrawerPage.CALENDAR) {
                    CalendarWorkoutScreen(
                        onBackClick = { currentPage = null },
                        onWorkoutDeleted = { reloadToken++ }
                    )
                } else {
                    Scaffold(
                        containerColor = surfaceBg,
                        topBar = {
                            TopAppBar(
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                title = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "KINETIC",
                                            fontFamily = Oswald,
                                            fontSize = 26.sp,
                                            letterSpacing = 6.sp
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(
                                            Icons.Default.Menu,
                                            contentDescription = strings.menu,
                                            tint = textPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = surfaceBg,
                                    titleContentColor = textPrimary
                                ),
                                actions = {
                                    IconButton(onClick = {
                                        val newMode = if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                                        preferencesManager.setThemeMode(newMode)
                                        currentThemeMode = newMode
                                        onThemeChanged(newMode)
                                    }) {
                                        Icon(
                                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = if (isDark) strings.light else strings.dark,
                                            tint = accent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        if (currentDashboardTab == 0) {
                            var selectedMood by remember { mutableIntStateOf(1) }
                            val onboardingProfile = remember { preferencesManager.getOnboardingProfile() }
                            val generatedWorkout = remember(onboardingProfile, selectedMood) {
                                if (onboardingProfile.goal.isNotEmpty() && onboardingProfile.selectedGroups.isNotEmpty()) {
                                    FitnessAssistant.generateWorkout(onboardingProfile, selectedMood)
                                } else emptyList()
                            }
                            val generatedTips = remember(onboardingProfile, selectedMood) {
                                FitnessAssistant.generateTips(onboardingProfile, selectedMood)
                            }

                            val dayOfYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR) }
                            var activeTipIndex by remember(generatedTips) {
                                mutableIntStateOf(if (generatedTips.isNotEmpty()) dayOfYear % generatedTips.size else 0)
                            }

                            val greeting = remember {
                                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                                when {
                                    hour < 12 -> strings.goodMorning
                                    hour < 18 -> strings.goodAfternoon
                                    else -> strings.goodEvening
                                }
                            }

                            LazyColumn(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = paddingValues.calculateBottomPadding()),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(innerPadding)
                            ) {
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            greeting.uppercase(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSecondary,
                                            letterSpacing = 2.sp,
                                            fontSize = 12.sp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            profileName.uppercase(),
                                            fontFamily = Oswald,
                                            fontSize = 32.sp,
                                            color = textPrimary,
                                            letterSpacing = 4.sp
                                        )

                                    }
                                }

                                item {
                                    DailyActivityCard(
                                        isDark = isDark,
                                        cardBg = cardBg,
                                        textPrimary = textPrimary,
                                        textSecondary = textSecondary,
                                        accent = accent,
                                        iconBg = iconBg,
                                        todayDistanceKm = todayCardioDistance,
                                        todayDurationMs = todayCardioDuration,
                                        todayCalories = todayCardioCalories,
                                        stepsEstimate = totalSteps,
                                        onAddSteps = { steps -> manualSteps += steps }
                                    )
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
                                    ) {
                                        Column(modifier = Modifier.padding(18.dp)) {
                                            Text(
                                                strings.howDoYouFeel.uppercase(),
                                                fontSize = 12.sp,
                                                letterSpacing = 2.sp,
                                                color = textSecondary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                val moods = remember(strings) { listOf(
                                                    Triple(0, Icons.Default.Battery1Bar, strings.tiredLabel),
                                                    Triple(1, Icons.Default.Battery3Bar, strings.normalLabel),
                                                    Triple(2, Icons.Default.BatteryFull, strings.energeticLabel)
                                                ) }
                                                moods.forEach { (index, icon, label) ->
                                                    val isSelected = selectedMood == index
                                                    Card(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .clickable { selectedMood = index }
                                                            .then(
                                                                if (isSelected) Modifier.border(
                                                                    1.5.dp,
                                                                    accent,
                                                                    RoundedCornerShape(14.dp)
                                                                ) else Modifier,
                                                            ),
                                                        shape = RoundedCornerShape(14.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = if (isSelected) accent.copy(alpha = 0.1f) else Color.Transparent
                                                        )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 14.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Icon(
                                                                icon,
                                                                contentDescription = label,
                                                                tint = if (isSelected) accent else textSecondary,
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                            Spacer(Modifier.height(6.dp))
                                                            Text(
                                                                label,
                                                                fontSize = 12.sp,
                                                                color = if (isSelected) accent else textSecondary,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
                                    ) {
                                        Column(modifier = Modifier.padding(18.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    strings.weeklySummary.uppercase(),
                                                    fontSize = 12.sp,
                                                    letterSpacing = 2.sp,
                                                    color = textSecondary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (currentStreak > 0) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text("🔥", fontSize = 13.sp)
                                                        Text(
                                                            "$currentStreak ${strings.daysConsecutive}",
                                                            fontSize = 12.sp,
                                                            color = textSecondary,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(14.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                // Workouts this week
                                                Card(
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = CardDefaults.cardColors(containerColor = iconBg)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(14.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(
                                                            Icons.Default.FitnessCenter,
                                                            contentDescription = null,
                                                            tint = textSecondary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(Modifier.height(6.dp))
                                                        Text(
                                                            "$weekWorkoutCount",
                                                            fontSize = 26.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = textPrimary
                                                        )
                                                        Text(
                                                            strings.workoutsLabel,
                                                            fontSize = 11.sp,
                                                            color = textSecondary,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        if (lastWeekWorkoutCount > 0) {
                                                            val diff = weekWorkoutCount - lastWeekWorkoutCount
                                                            val diffText = if (diff >= 0) "+$diff" else "$diff"
                                                            Text(
                                                                "$diffText vs ${strings.lastWeekLabel}",
                                                                fontSize = 10.sp,
                                                                color = if (diff >= 0) accent else textSecondary,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                                // Volume this week
                                                Card(
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = CardDefaults.cardColors(containerColor = iconBg)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(14.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(
                                                            Icons.Default.TrendingUp,
                                                            contentDescription = null,
                                                            tint = textSecondary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(Modifier.height(6.dp))
                                                        Text(
                                                            weightLabel(weekVolume, isLbs),
                                                            fontSize = 22.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = textPrimary,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Text(
                                                            strings.volumeLabel,
                                                            fontSize = 11.sp,
                                                            color = textSecondary,
                                                            textAlign = TextAlign.Center
                                                        )
                                                        if (lastWeekVolume > 0) {
                                                            val diffPct = ((weekVolume - lastWeekVolume) / lastWeekVolume * 100).toInt()
                                                            val diffText = if (diffPct >= 0) "+${diffPct}%" else "${diffPct}%"
                                                            Text(
                                                                "$diffText vs ${strings.lastWeekLabel}",
                                                                fontSize = 10.sp,
                                                                color = if (diffPct >= 0) accent else textSecondary,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                                // Best streak
                                                Card(
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = CardDefaults.cardColors(containerColor = iconBg)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(14.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text("🏆", fontSize = 18.sp)
                                                        Spacer(Modifier.height(6.dp))
                                                        Text(
                                                            "$bestStreak",
                                                            fontSize = 26.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = textPrimary
                                                        )
                                                        Text(
                                                            strings.bestStreakLabel,
                                                            fontSize = 11.sp,
                                                            color = textSecondary,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                            if (weeklyTopExercise != null) {
                                                Spacer(Modifier.height(12.dp))
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(iconBg)
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Text("⭐", fontSize = 16.sp)
                                                    Column {
                                                        Text(
                                                            strings.topExerciseLabel,
                                                            fontSize = 11.sp,
                                                            color = textSecondary
                                                        )
                                                        Text(
                                                            weeklyTopExercise ?: "",
                                                            fontSize = 14.sp,
                                                            color = textPrimary,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    if (generatedTips.isNotEmpty()) {
                                        val currentTip = generatedTips.getOrNull(activeTipIndex)
                                        if (currentTip != null) {
                                            val (tipLabel, tipIcon) = when (selectedMood) {
                                                0 -> when (activeTipIndex) {
                                                    0 -> Pair(strings.recovery, Icons.Default.FitnessCenter)
                                                    1 -> Pair(strings.nutritionLabel, Icons.Default.Restaurant)
                                                    else -> Pair(strings.technicalTip, Icons.Default.Lightbulb)
                                                }
                                                1 -> when (activeTipIndex) {
                                                    0 -> Pair(strings.technicalTip, Icons.Default.Lightbulb)
                                                    1 -> Pair(strings.nutritionLabel, Icons.Default.Restaurant)
                                                    else -> Pair(strings.goalLabel, Icons.Default.EmojiEvents)
                                                }
                                                else -> when (activeTipIndex) {
                                                    0 -> Pair(strings.motivationLabel, Icons.Default.EmojiEvents)
                                                    1 -> Pair(strings.goalLabel, Icons.Default.EmojiEvents)
                                                    else -> Pair(strings.technicalTip, Icons.Default.Lightbulb)
                                                }
                                            }

                                            LaunchedEffect(activeTipIndex) {
                                                kotlinx.coroutines.delay(10000)
                                                activeTipIndex = (activeTipIndex + 1) % generatedTips.size
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        activeTipIndex = (activeTipIndex + 1) % generatedTips.size
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(
                                                    tipIcon,
                                                    contentDescription = null,
                                                    tint = textSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        tipLabel,
                                                        fontSize = 12.sp,
                                                        color = textSecondary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    AnimatedContent(
                                                        targetState = TipsTranslator.translateTip(currentTip.text, LanguageManager.getLanguage()),
                                                        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
                                                        label = "tipTransition"
                                                    ) { tipText ->
                                                        Text(
                                                            tipText,
                                                            fontSize = 13.sp,
                                                            color = textSecondary.copy(alpha = 0.7f),
                                                            lineHeight = 18.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    if (generatedWorkout.isNotEmpty()) {
                                        val goalLabel = when (onboardingProfile.goal) {
                                            "strength" -> strings.goalStrength.uppercase()
                                            "mass" -> strings.goalMass.uppercase()
                                            "weight_loss" -> strings.goalWeightLoss.uppercase()
                                            "maintenance" -> strings.goalMaintenance.uppercase()
                                            else -> ""
                                        }
                                        val groupedByGroup = generatedWorkout.groupBy { it.group }
                                        val totalExercises = generatedWorkout.size
                                        val estimatedMinutes = totalExercises * 3
                                        val totalSets = generatedWorkout.sumOf { it.sets }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(containerColor = cardBg)
                                        ) {
                                            Column(modifier = Modifier.padding(18.dp)) {
                                                Text(
                                                    strings.todaysWorkout.uppercase(),
                                                    fontSize = 11.sp,
                                                    letterSpacing = 2.sp,
                                                    color = accent,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                val templateName = goalLabel.ifEmpty { "PUSH" }
                                                Text(
                                                    "$templateName \u2014 ziua ${currentStreak.coerceAtLeast(1)}",
                                                    fontSize = 22.sp,
                                                    color = textPrimary,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    "$totalExercises ${strings.exercises} \u00B7 ~$estimatedMinutes min \u00B7 $totalSets ${strings.sets}",
                                                    fontSize = 13.sp,
                                                    color = textSecondary
                                                )
                                                Spacer(Modifier.height(14.dp))

                                                groupedByGroup.forEach { (group, exercises) ->
                                                    Text(
                                                        group.uppercase(),
                                                        fontSize = 11.sp,
                                                        letterSpacing = 2.sp,
                                                        color = textSecondary,
                                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                                    )
                                                    exercises.take(3).forEach { ex ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 3.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                ex.name,
                                                                fontSize = 14.sp,
                                                                color = textPrimary,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Text(
                                                                "${ex.sets}x${ex.reps}",
                                                                fontSize = 13.sp,
                                                                color = accent,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(Modifier.height(16.dp))
                                                Button(
                                                    onClick = {
                                                        if (generatedWorkout.isNotEmpty()) {
                                                            currentDashboardTab = 1
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
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
                                                        strings.startWorkout.uppercase(),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 2.sp,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (currentDashboardTab == 1) {

                            if (selectedTemplate != null) {
                                TemplateDetailScreen(
                                    template = selectedTemplate!!,
                                    onBackClick = { selectedTemplate = null },
                                    onBackToMain = { currentDashboardTab = 0 }
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val tabLabels = remember(strings) { listOf(strings.templates, strings.muscleGroups) }
                                        tabLabels.forEachIndexed { index, label ->
                                            val isActive = muscleGroupsSubTab == index
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .then(
                                                        if (isActive) Modifier.border(1.5.dp, accent, RoundedCornerShape(12.dp))
                                                        else Modifier.border(1.dp, textSecondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                    )
                                                    .background(if (isActive) accent.copy(alpha = 0.08f) else Color.Transparent)
                                                    .clickable { muscleGroupsSubTab = index }
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

                                    if (muscleGroupsSubTab == 0) {
                                        LazyColumn(
                                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = paddingValues.calculateBottomPadding()),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            items(DataProvider.templateuri) { template ->
                                                val gradientColors = templateGradient(template.nume)
                                                val estimatedDuration = template.exercitii.size * 3
                                                val totalSets = template.exercitii.size * 4
                                                val muscleGroups = templateMuscleGroups(template)

                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
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
                                                                .width(180.dp)
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
                                                                    template.nume.uppercase(),
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Black,
                                                                    fontSize = 26.sp,
                                                                    letterSpacing = 4.sp
                                                                )
                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                Text(
                                                                    "${template.exercitii.size} ${strings.exercises}  \u00B7  ~${estimatedDuration}min  \u00B7  ${totalSets} sets",
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
                                                                        .clip(CircleShape)
                                                                        .background(Color.White.copy(alpha = 0.2f)),
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
                                    } else {
                                        var selectedMuscleGroup by remember { mutableStateOf<String?>(null) }
                                        var selectedEquipment by remember { mutableStateOf<String?>(null) }

                                        LazyColumn(
                                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = paddingValues.calculateBottomPadding() + 24.dp),
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
                                                            color = if (isActive) accent else cardBg,
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(20.dp))
                                                                .then(
                                                                    if (!isActive) Modifier.border(1.dp, textSecondary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
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
                                                                    color = if (isActive) Color.White else textPrimary,
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
                                                                    .clip(CircleShape)
                                                                    .background(color)
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
                                                val allExercises = DataProvider.exercitiiPeGrupa[group] ?: listOf()
                                                val equipmentTypes = allExercises.map { it.equipment }.distinct()
                                                val filteredExercises = if (selectedEquipment != null) {
                                                    allExercises.filter { it.equipment == selectedEquipment }
                                                } else allExercises

                                                item {
                                                    val groupLevel = recoveryMap[group] ?: 0.0
                                                    val recoveryPct = ((1.0 - groupLevel) * 100).toInt().coerceIn(0, 100)
                                                    val barColor = getRecoveryColor(groupLevel)
                                                    var animBar by remember { mutableFloatStateOf(0f) }
                                                    LaunchedEffect(group) { animBar = 1f }
                                                    val barProgress by animateFloatAsState(
                                                        targetValue = animBar * recoveryPct / 100f,
                                                        animationSpec = tween(durationMillis = 800)
                                                    )

                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(containerColor = cardBg)
                                                    ) {
                                                        Column(modifier = Modifier.padding(16.dp)) {
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
                                                                    color = textPrimary
                                                                )
                                                                Text(
                                                                    "${allExercises.size} ${strings.exercises}",
                                                                    fontSize = 12.sp,
                                                                    color = textSecondary,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                            Spacer(Modifier.height(8.dp))
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
                                                                    "$recoveryPct%",
                                                                    color = barColor,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            Spacer(Modifier.height(4.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(8.dp)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(RecoveryTrack)
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxHeight()
                                                                        .fillMaxWidth(fraction = barProgress.coerceIn(0f, 1f))
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(barColor)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                item {
                                                    LazyRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        contentPadding = PaddingValues(vertical = 2.dp)
                                                    ) {
                                                        item {
                                                            Surface(
                                                                shape = RoundedCornerShape(16.dp),
                                                                color = if (selectedEquipment == null) accent else cardBg,
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(16.dp))
                                                                    .then(
                                                                        if (selectedEquipment != null) Modifier.border(1.dp, textSecondary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                                                        else Modifier
                                                                    )
                                                                    .clickable { selectedEquipment = null }
                                                            ) {
                                                                Text(
                                                                    "All",
                                                                    color = if (selectedEquipment == null) Color.White else textPrimary,
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
                                                                color = if (isActive) accent else cardBg,
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(16.dp))
                                                                    .then(
                                                                        if (!isActive) Modifier.border(1.dp, textSecondary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                                                        else Modifier
                                                                    )
                                                                    .clickable { selectedEquipment = if (isActive) null else equip }
                                                            ) {
                                                                Text(
                                                                    equip,
                                                                    color = if (isActive) Color.White else textPrimary,
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
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(90.dp)
                                                            .clickable {
                                                                selectedDirectExercise = exercise
                                                                selectedDirectGroup = group
                                                            },
                                                        shape = RoundedCornerShape(14.dp),
                                                        colors = CardDefaults.cardColors(containerColor = cardBg)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxSize(),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(90.dp)
                                                                    .background(Color.Black)
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
                                                                    color = textPrimary
                                                                )
                                                                Spacer(Modifier.height(2.dp))
                                                                Text(
                                                                    exercise.equipment,
                                                                    fontSize = 11.sp,
                                                                    color = textSecondary,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                            Icon(
                                                                Icons.Default.ChevronRight,
                                                                contentDescription = null,
                                                                tint = textSecondary,
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
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(containerColor = cardBg)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(24.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Icon(
                                                                Icons.Default.FitnessCenter,
                                                                contentDescription = null,
                                                                tint = textSecondary.copy(alpha = 0.4f),
                                                                modifier = Modifier.size(40.dp)
                                                            )
                                                            Spacer(Modifier.height(8.dp))
                                                            Text(
                                                                strings.chooseMuscleGroup.uppercase(),
                                                                fontSize = 12.sp,
                                                                color = textSecondary,
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
                                lastPR = lastPR,
                                paddingValues = innerPadding,
                                onExerciseHistoryClick = { exerciseName ->
                                    currentPage = DrawerPage.CALENDAR
                                },
                                currentStreak = currentStreak,
                                bestStreak = bestStreak,
                                badgeCount = badgeCount,
                                recentBadges = recentBadges,
                                allRecentPRs = allRecentPRs,
                                allExerciseNames = allExerciseNames
                            )
                        } else if (currentDashboardTab == 3) {
                            WaterTrackingScreen(
                                preferencesManager = preferencesManager,
                                strings = strings,
                                accent = accent,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary,
                                cardBg = cardBg,
                                surfaceBg = surfaceBg,
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
                                lastBiometric = lastBiometric,
                                weeksSinceMeasurement = weeksSinceMeasurement,
                                hasBiometricData = allBiometrics.isNotEmpty(),
                                totalWorkouts = totalAllWorkouts,
                                currentStreak = currentStreak,
                                bestStreak = bestStreak,
                                totalVolume = totalAllVolume,
                                earnedBadges = recentBadges,
                                allBiometrics = allBiometrics,
                                paddingValues = innerPadding
                            )
                        }
                    }
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
}
}

// ============================================
// Ecranul 2: Lista de Exercitii
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(grupaMusculara: String, isLbs: Boolean = false, isDark: Boolean = true, onBackClick: () -> Unit, onWorkoutSaved: () -> Unit = {}) {
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    val viewModel: MainViewModel = viewModel()
    var exercitii by remember { mutableStateOf<List<ExerciseListItem>>(emptyList()) }
    var selectedExercise: ExerciseDefinition? by remember { mutableStateOf(null) }
    var selectedProgressExercise: String? by remember { mutableStateOf(null) }
    var reloadToken by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEquipment by remember { mutableStateOf<String?>(null) }

    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed

    LaunchedEffect(grupaMusculara, reloadToken) {
        viewModel.getExercitiiPentruGrupa(grupaMusculara) { exercitii = it }
    }

    val equipmentTypes = remember { listOf("Dumbbells", "Barbell", "Machine", "Cable", "Bodyweight", "EZ Bar", "Smith Machine", "Kettlebell", "Stability Ball", "Sled Machine", "Band") }
    val filteredExercises by remember(exercitii, searchQuery, selectedEquipment) {
        derivedStateOf {
            exercitii.filter { item ->
                (searchQuery.isBlank() || item.exercise.nume.contains(searchQuery, ignoreCase = true)) &&
                (selectedEquipment == null || item.equipment == selectedEquipment)
            }
        }
    }

    BackHandler {
        when {
            selectedProgressExercise != null -> selectedProgressExercise = null
            selectedExercise != null -> selectedExercise = null
            else -> onBackClick()
        }
    }

    if (selectedProgressExercise != null) {
        CalendarScreen(
            isLbs = isLbs,
            initialExercise = selectedProgressExercise,
            onBackClick = { selectedProgressExercise = null }
        )
    } else if (selectedExercise != null) {
        ExerciseInputScreen(
            exercise = selectedExercise!!,
            grupaMusculara = grupaMusculara,
            isLbs = isLbs,
            onBackClick = { selectedExercise = null },
            onOpenProgress = { name -> selectedProgressExercise = name; selectedExercise = null },
            onWorkoutSaved = onWorkoutSaved,
            strings = strings
        )
    } else {
    val listState = rememberLazyListState()
    val searchBarPx = with(LocalDensity.current) { 170.dp.roundToPx() }
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val firstItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            if (firstItem != null && firstItem.index == 0) {
                -firstItem.offset.toFloat() / firstItem.size.toFloat()
            } else if (firstItem != null) {
                1f
            } else {
                0f
            }
        }.collect { progress ->
            scrollOffset = progress.coerceIn(0f, 1f)
        }
    }

    val offsetAnim by animateFloatAsState(
        targetValue = scrollOffset,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "offset"
    )

    Scaffold(
        containerColor = bgColor(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(LanguageManager.translateMuscleGroup(grupaMusculara, strings)) },
                navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = accent
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = surfaceBg,
                        titleContentColor = textPrimary
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 170.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredExercises) { item ->
                        val exercitiu = item.exercise
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedExercise = exercitiu },
                            colors = CardDefaults.cardColors(containerColor = cardColor()),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val gifUrl = ExerciseGifs.getGif(exercitiu.nume)
                                    if (gifUrl != null) {
                                        AsyncImage(
                                            model = gifUrl,
                                            contentDescription = exercitiu.nume,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(240.dp)
                                                .padding(4.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.PlayCircle,
                                                contentDescription = null,
                                                tint = accentColor().copy(alpha = 0.6f),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = strings.demoExercise,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = secondaryTextColor(),
                                                letterSpacing = 2.sp
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = exercitiu.nume,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = textColor(),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.setFavorite(
                                                grupa = grupaMusculara,
                                                numeExercitiu = exercitiu.nume,
                                                isFavorite = !item.isFavorite
                                            ) {
                                                reloadToken++
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = strings.favorite,
                                            tint = if (item.isFavorite) RecoveryYellow else secondaryTextColor()
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkRed.copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            strings.add,
                                            color = accentColor(),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-170 * offsetAnim).dp)
                        .alpha(1f - offsetAnim)
                        .background(surfaceBg)
                ) {
                    RecoveryBarForGroup(grupaMusculara = grupaMusculara)

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(strings.search, color = textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = dividerColor(),
                            cursorColor = accent,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedContainerColor = cardBg.copy(alpha = 0.5f),
                            unfocusedContainerColor = cardBg.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedEquipment == null,
                                onClick = { selectedEquipment = null },
                                label = { Text(strings.allGroups) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent,
                                    selectedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                                    containerColor = cardBg,
                                    labelColor = textSecondary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        items(equipmentTypes) { eq ->
                            FilterChip(
                                selected = selectedEquipment == eq,
                                onClick = { selectedEquipment = if (selectedEquipment == eq) null else eq },
                                label = { Text(eq) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = accent,
                                    selectedLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                                    containerColor = cardBg,
                                    labelColor = textSecondary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// Ecranul 3: Input Seturi
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseInputScreen(
    exercise: ExerciseDefinition,
    grupaMusculara: String,
    isLbs: Boolean = false,
    onBackClick: () -> Unit,
    onNextExercise: (() -> Unit)? = null,
    onOpenProgress: (String) -> Unit = {},
    onWorkoutSaved: () -> Unit = {},
    strings: LanguageManager.Strings
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()
    var currentSets by remember { mutableStateOf(listOf<SetEntry>(SetEntry(0.0, 0))) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var isPR by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<ExercitiuEntity>>(emptyList()) }
    var stats by remember { mutableStateOf(ExerciseStats(0.0, 0, 0.0)) }
    var volumeSummary by remember { mutableStateOf(VolumeSummary(0.0, 0.0, 0.0)) }
    var restSeconds by remember { mutableStateOf(90) }
    var remainingSeconds by remember { mutableStateOf(0) }
    var customTimerText by remember { mutableStateOf("") }
    var editingSet by remember { mutableStateOf<ExercitiuEntity?>(null) }

    BackHandler {
        when {
            editingSet != null -> editingSet = null
            else -> onBackClick()
        }
    }

    fun refreshExerciseData() {
        viewModel.getIstoricExercitiu(exercise.nume) { history = it }
        viewModel.getStatisticiExercitiu(exercise.nume) { stats = it }
        viewModel.getVolumeSummary { volumeSummary = it }
    }

    LaunchedEffect(exercise.nume) {
        viewModel.incarcaUltimulAntrenament(exercise.nume) { ultimeleSeturi ->
            if (ultimeleSeturi.isNotEmpty()) {
                currentSets = ultimeleSeturi
            }
        }
        refreshExerciseData()
    }

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(1_000)
            remainingSeconds--
        }
    }

    editingSet?.let { set ->
        EditSetDialog(
            set = set,
            isLbs = isLbs,
            onDismiss = { editingSet = null },
            onSave = { updated ->
                viewModel.updateSet(updated) {
                    editingSet = null
                    refreshExerciseData()
                }
            }
        )
    }

    if (showSaveConfirmation) {
        var animScale by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(Unit) { animScale = 1f }
        val scale by animateFloatAsState(targetValue = animScale, animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f), label = "check")
        val iconColor = if (isPR) GoldPR else RecoveryGreen
        val icon = if (isPR) Icons.Default.EmojiEvents else Icons.Default.CheckCircle

        val validSets = currentSets.filter { it.greutateKg > 0 || it.repetari > 0 }
        val totalVolume = validSets.sumOf { it.greutateKg * it.repetari }
        val totalSets = validSets.size
        val maxWeight = validSets.maxOfOrNull { it.greutateKg } ?: 0.0
        val totalReps = validSets.sumOf { it.repetari }

        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            containerColor = surfaceColor(),
            titleContentColor = textColor(),
            textContentColor = secondaryTextColor(),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(56.dp).scale(scale)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isPR) "🎉 NEW PR!" else LanguageManager.getStrings(context).workoutCompleted,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = exercise.nume,
                        fontSize = 14.sp,
                        color = secondaryTextColor(),
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = LanguageManager.getStrings(context).volume, value = String.format("%.0f", totalVolume), unit = "kg", accent = accentColor())
                        StatItem(label = LanguageManager.getStrings(context).sets, value = "$totalSets", unit = "", accent = accentColor())
                        StatItem(label = LanguageManager.getStrings(context).reps, value = "$totalReps", unit = "", accent = accentColor())
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StatItem(label = LanguageManager.getStrings(context).maxWeight, value = String.format("%.1f", maxWeight), unit = "kg", accent = accentColor())
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveConfirmation = false
                        onBackClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", color = accentColor(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        )
    }

    Scaffold(
        containerColor = bgColor(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(exercise.nume)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = accentColor())
                    }
                },
                actions = {
                    IconButton(onClick = { onOpenProgress(exercise.nume) }) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = strings.progress,
                            tint = accentColor()
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.salveazaAntrenament(
                                grupaMusculara = grupaMusculara,
                                numeExercitiu = exercise.nume,
                                seturi = currentSets.filter { it.greutateKg > 0 || it.repetari > 0 },
                                note = noteText
                            ) { newPR ->
                                refreshExerciseData()
                                onWorkoutSaved()
                                isPR = newPR
                                showSaveConfirmation = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = strings.saveWorkout,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor(),
                    titleContentColor = textColor()
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
        ) {
            item { RecoveryBarForGroup(grupaMusculara = grupaMusculara) }
            item {
                val gifUrl = ExerciseGifs.getGif(exercise.nume)
                if (gifUrl != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor()),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                exercise.nume,
                                color = textColor(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            AsyncImage(
                                model = gifUrl,
                                contentDescription = exercise.nume,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
            item {
                RestTimerCard(
                    restSeconds = restSeconds,
                    remainingSeconds = remainingSeconds,
                    customTimerText = customTimerText,
                    onRestSecondsChange = { restSeconds = it },
                    onCustomTimerTextChange = { customTimerText = it.filter { char -> char.isDigit() } },
                    onStart = {
                        remainingSeconds = customTimerText.toIntOrNull() ?: restSeconds
                    },
                    onStop = { remainingSeconds = 0 }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(strings.setLabel, color = secondaryTextColor(), fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                    Text(strings.kg, color = secondaryTextColor(), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.reps, color = secondaryTextColor(), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(48.dp))
                }
                HorizontalDivider(color = dividerColor(), modifier = Modifier.padding(vertical = 4.dp))
            }
            itemsIndexed(currentSets) { index, set ->
                SetInputRow(
                    index = index,
                    setEntry = set,
                    isLbs = isLbs,
                    onUpdate = { updatedSet ->
                        currentSets = currentSets.toMutableList().also { it[index] = updatedSet }
                    },
                    onDelete = {
                        currentSets = currentSets.toMutableList().also { it.removeAt(index) }
                    }
                )
            }
            item {
                OutlinedButton(
                    onClick = { currentSets = currentSets + SetEntry(0.0, 0) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(DarkRed, Red))
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = accentColor(),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.addSet, color = accentColor(), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
            item {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(strings.exerciseNotes) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor(),
                        unfocusedBorderColor = dividerColor(),
                        focusedLabelColor = accentColor(),
                        unfocusedLabelColor = secondaryTextColor(),
                        cursorColor = accentColor(),
                        focusedTextColor = textColor(),
                        unfocusedTextColor = textColor()
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item {
                ExerciseHistoryCard(
                    history = history,
                    isLbs = isLbs,
                    onEdit = { editingSet = it },
                    onDelete = { set ->
                        viewModel.deleteSet(set) {
                            refreshExerciseData()
                        }
                    }
                )
            }
            item { ExerciseStatsCard(stats = stats, volumeSummary = volumeSummary, isLbs = isLbs) }
            if (onNextExercise != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onNextExercise() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor()),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                            Text(
                            strings.nextExercise.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// Componenta: Statistici + PR-uri
// ============================================
@Composable
fun ExerciseStatsCard(stats: ExerciseStats, volumeSummary: VolumeSummary, isLbs: Boolean = false) {
    val strings = LanguageManager.getStrings(LocalContext.current)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor()),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(strings.prAndVolume, color = textColor(), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill(strings.maxWeight, weightLabel(stats.maxGreutate, isLbs), Modifier.weight(1f))
                StatPill(strings.maxReps, "${stats.maxRepetari}", Modifier.weight(1f))
                StatPill(strings.maxSet, weightLabel(stats.maxVolumSet, isLbs), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill(strings.today, weightLabel(volumeSummary.azi, isLbs), Modifier.weight(1f))
                StatPill(strings.thisWeek, weightLabel(volumeSummary.saptamana, isLbs), Modifier.weight(1f))
                StatPill(strings.thisMonth, weightLabel(volumeSummary.luna, isLbs), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(surfaceColor())
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = secondaryTextColor(), fontSize = 11.sp)
        Text(value, color = textColor(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// ============================================
// Componenta: Timer Pauza
// ============================================
@Composable
fun RestTimerCard(
    restSeconds: Int,
    remainingSeconds: Int,
    customTimerText: String,
    onRestSecondsChange: (Int) -> Unit,
    onCustomTimerTextChange: (String) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val strings = LanguageManager.getStrings(LocalContext.current)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor()),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.restTimer, color = textColor(), fontWeight = FontWeight.Bold)
                Text(formatSeconds(remainingSeconds), color = accentColor(), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(60, 90, 120).forEach { seconds ->
                    FilterChip(
                        selected = restSeconds == seconds,
                        onClick = { onRestSecondsChange(seconds) },
                        label = { Text("${seconds}s") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkRed,
                            selectedLabelColor = textColor(),
                            labelColor = secondaryTextColor()
                        )
                    )
                }
                OutlinedTextField(
                    value = customTimerText,
                    onValueChange = onCustomTimerTextChange,
                    label = { Text("custom") },
                    modifier = Modifier.width(96.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor(),
                        unfocusedBorderColor = dividerColor(),
                        focusedLabelColor = accentColor(),
                        unfocusedLabelColor = secondaryTextColor(),
                        cursorColor = accentColor(),
                        focusedTextColor = textColor(),
                        unfocusedTextColor = textColor()
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkRed),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.start)
                }
                OutlinedButton(onClick = onStop, modifier = Modifier.weight(1f)) {
                    Text(strings.stop, color = accentColor())
                }
            }
        }
    }
}

// ============================================
// Componenta: Istoric Exercitiu
// ============================================
@Composable
fun ExerciseHistoryCard(
    history: List<ExercitiuEntity>,
    isLbs: Boolean = false,
    onEdit: (ExercitiuEntity) -> Unit,
    onDelete: (ExercitiuEntity) -> Unit
) {
    val strings = LanguageManager.getStrings(LocalContext.current)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor()),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(strings.exerciseHistory, color = textColor(), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (history.isEmpty()) {
                Text(strings.noSavedSetsYet, color = secondaryTextColor(), fontSize = 13.sp)
            } else {
                history.take(8).forEach { set ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${weightLabel(set.greutateKg, isLbs)} x ${set.repetari} reps",
                                color = textColor(),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Set ${set.setIndex + 1}  (${set.numeExercitiu})",
                                color = secondaryTextColor(),
                                fontSize = 12.sp
                            )
                            if (set.notes.isNotBlank()) {
                                Text(set.notes, color = secondaryTextColor(), fontSize = 12.sp)
                            }
                        }
                        IconButton(onClick = { onEdit(set) }) {
                            Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = accentColor())
                        }
                        IconButton(onClick = { onDelete(set) }) {
                            Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = DarkRed)
                        }
                    }
                    HorizontalDivider(color = dividerColor().copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun EditSetDialog(
    set: ExercitiuEntity,
    isLbs: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (ExercitiuEntity) -> Unit
) {
    val strings = LanguageManager.getStrings(LocalContext.current)
    val displayWeight = convertWeight(set.greutateKg, isLbs)
    var kgText by remember(set.id) { mutableStateOf(String.format("%.1f", displayWeight)) }
    var repsText by remember(set.id) { mutableStateOf(set.repetari.toString()) }
    var noteText by remember(set.id) { mutableStateOf(set.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor(),
        titleContentColor = textColor(),
        textContentColor = secondaryTextColor(),
        title = { Text(strings.editSet) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = kgText,
                    onValueChange = { kgText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(if (isLbs) "lbs" else "kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it.filter { char -> char.isDigit() } },
                    label = { Text("reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("notite") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val displayVal = kgText.toDoubleOrNull() ?: (if (isLbs) set.greutateKg * 2.20462 else set.greutateKg)
                val kgVal = if (isLbs) displayVal / 2.20462 else displayVal
                onSave(
                    set.copy(
                        greutateKg = kgVal,
                        repetari = repsText.toIntOrNull() ?: set.repetari,
                        notes = noteText
                    )
                )
            }) {
                Text(strings.saveNotes, color = accentColor())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = secondaryTextColor())
            }
        }
    )
}

fun formatSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "%d:%02d".format(minutes, remaining)
}

fun formatDate(date: java.util.Date): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(date)
}

@Composable
fun SetInputRow(
    index: Int,
    setEntry: SetEntry,
    isLbs: Boolean = false,
    onUpdate: (SetEntry) -> Unit,
    onDelete: () -> Unit
) {
    val strings = LanguageManager.getStrings(LocalContext.current)
    val displayWeight = convertWeight(setEntry.greutateKg, isLbs)
    var kgText by remember { mutableStateOf(if (displayWeight > 0) String.format("%.1f", displayWeight) else "") }
    var repsText by remember { mutableStateOf(if (setEntry.repetari > 0) setEntry.repetari.toString() else "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(DarkRed),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = textColor(),
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        OutlinedTextField(
            value = kgText,
            onValueChange = { newValue ->
                kgText = newValue.filter { it.isDigit() || it == '.' }
                val displayVal = kgText.toDoubleOrNull() ?: 0.0
                val kgVal = if (isLbs) displayVal / 2.20462 else displayVal
                onUpdate(SetEntry(kgVal, repsText.toIntOrNull() ?: 0))
            },
            label = { Text(if (isLbs) "lbs" else "kg", color = secondaryTextColor()) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor(),
                unfocusedBorderColor = dividerColor(),
                focusedLabelColor = accentColor(),
                unfocusedLabelColor = secondaryTextColor(),
                cursorColor = accentColor(),
                focusedTextColor = textColor(),
                unfocusedTextColor = textColor()
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = repsText,
            onValueChange = { newValue ->
                repsText = newValue.filter { it.isDigit() }
                val reps = repsText.toIntOrNull() ?: 0
                onUpdate(SetEntry(kgText.toDoubleOrNull() ?: 0.0, reps))
            },
            label = { Text("Reps", color = secondaryTextColor()) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor(),
                unfocusedBorderColor = dividerColor(),
                focusedLabelColor = accentColor(),
                unfocusedLabelColor = secondaryTextColor(),
                cursorColor = accentColor(),
                focusedTextColor = textColor(),
                unfocusedTextColor = textColor()
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = strings.delete,
                tint = DarkRed.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DailyActivityCard(
    isDark: Boolean,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    accent: Color,
    iconBg: Color,
    todayDistanceKm: Double,
    todayDurationMs: Long,
    todayCalories: Double,
    stepsEstimate: Int,
    stepGoal: Int = 7000,
    activeTimeGoalMin: Int = 90,
    calorieGoal: Int = 500,
    onAddSteps: ((Int) -> Unit)? = null
) {
    val activeMinutes = (todayDurationMs / 60000).toInt()
    val totalBurned = (todayCalories + todayDistanceKm * 50).toInt().coerceAtLeast(todayCalories.toInt())
    var showAddStepsDialog by remember { mutableStateOf(false) }
    var stepsInput by remember { mutableStateOf("") }

    if (showAddStepsDialog && onAddSteps != null) {
        AlertDialog(
            onDismissRequest = { showAddStepsDialog = false },
            containerColor = cardBg,
            titleContentColor = textPrimary,
            title = { Text("Add Steps", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter the number of steps", color = textSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stepsInput,
                        onValueChange = { stepsInput = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("e.g. 500", color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        val steps = stepsInput.toIntOrNull() ?: 0
                        if (steps > 0) {
                            onAddSteps(steps)
                            showAddStepsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStepsDialog = false }) {
                    Text("Cancel", color = accent)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier.size(180.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val outerRadius = size.minDimension / 2 - 4.dp.toPx()
            val strokeWidth = 16.dp.toPx()

            val stepsProgress = (stepsEstimate.toFloat() / stepGoal).coerceIn(0f, 1f)
            val greenColor = Color(0xFF34C759)
            drawArc(
                color = greenColor.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(centerX - outerRadius, centerY - outerRadius),
                size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
            )
            drawArc(
                color = greenColor,
                startAngle = 135f,
                sweepAngle = 270f * stepsProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(centerX - outerRadius, centerY - outerRadius),
                size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
            )

            val midRadius = outerRadius - strokeWidth - 5.dp.toPx()
            val blueColor = Color(0xFF007AFF)
            val timeProgress = (activeMinutes.toFloat() / activeTimeGoalMin).coerceIn(0f, 1f)
            drawArc(
                color = blueColor.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(centerX - midRadius, centerY - midRadius),
                size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2)
            )
            drawArc(
                color = blueColor,
                startAngle = 135f,
                sweepAngle = 270f * timeProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(centerX - midRadius, centerY - midRadius),
                size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2)
            )

            val innerRadius = midRadius - strokeWidth - 5.dp.toPx()
            val pinkColor = Color(0xFFFF2D55)
            val calProgress = (todayCalories.toFloat() / calorieGoal).coerceIn(0f, 1f)
            drawArc(
                color = pinkColor.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(centerX - innerRadius, centerY - innerRadius),
                size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2)
            )
            drawArc(
                color = pinkColor,
                startAngle = 135f,
                sweepAngle = 270f * calProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(centerX - innerRadius, centerY - innerRadius),
                size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Steps", fontSize = 11.sp, color = textSecondary)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "$stepsEstimate",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )
                Text(
                    "/$stepGoal",
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007AFF))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Active time", fontSize = 11.sp, color = textSecondary)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "$activeMinutes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )
                Text(
                    "/$activeTimeGoalMin min",
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2D55))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Calories", fontSize = 11.sp, color = textSecondary)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${todayCalories.toInt()}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                )
                Text(
                    "/$calorieGoal Cal",
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }
        }
    }
}

// ============================================
// Page Title with border
// ============================================
@Composable
fun PageTitle(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, accentColor(), RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.uppercase(),
                color = accentColor(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )
        }
    }
}

// ============================================
// Template color config
// ============================================
private fun templateGradient(templateName: String): List<Color> {
    return when (templateName.lowercase()) {
        "push" -> listOf(Volcanico.copy(alpha = 0.85f), VolcanicoLight.copy(alpha = 0.85f))
        "pull" -> listOf(Color(0xFF1565C0).copy(alpha = 0.85f), Color(0xFF42A5F5).copy(alpha = 0.85f))
        "legs" -> listOf(Color(0xFF2E7D32).copy(alpha = 0.85f), Color(0xFF66BB6A).copy(alpha = 0.85f))
        "upper" -> listOf(AccentPurple.copy(alpha = 0.85f), NoturnoMedium.copy(alpha = 0.85f))
        "full body" -> listOf(VolcanicoDark.copy(alpha = 0.85f), Color(0xFFFF9800).copy(alpha = 0.85f))
        else -> listOf(Volcanico.copy(alpha = 0.85f), VolcanicoLight.copy(alpha = 0.85f))
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
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = accentColor())
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bgColor(),
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
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
                                        template.nume.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp,
                                        letterSpacing = 4.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "${template.exercitii.size} ${strings.exercises}  ·  ~${estimatedDuration}min  ·  ${totalSets} sets",
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
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
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
    onBackClick: () -> Unit,
    onBackToMain: () -> Unit
) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    var selectedExercise by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var selectedGrupa by remember { mutableStateOf("") }

    val exercises = remember {
        mutableStateListOf<TemplateExercise>().apply {
            addAll(template.exercitii)
        }
    }

    var workoutStarted by remember { mutableStateOf(false) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }

    BackHandler {
        when {
            selectedExercise != null -> {
                selectedExercise = null
                if (workoutStarted && currentExerciseIndex < exercises.size - 1) {
                    currentExerciseIndex++
                    selectedExercise = exercises[currentExerciseIndex].exercise
                    selectedGrupa = exercises[currentExerciseIndex].grupaMusculara
                }
            }
            workoutStarted -> { workoutStarted = false; currentExerciseIndex = 0 }
            else -> onBackClick()
        }
    }

    if (selectedExercise != null) {
        val hasNextExercise = workoutStarted && currentExerciseIndex < exercises.size - 1
        ExerciseInputScreen(
            exercise = selectedExercise!!,
            grupaMusculara = selectedGrupa,
            onBackClick = {
                selectedExercise = null
                if (workoutStarted && currentExerciseIndex < exercises.size - 1) {
                    currentExerciseIndex++
                    selectedExercise = exercises[currentExerciseIndex].exercise
                    selectedGrupa = exercises[currentExerciseIndex].grupaMusculara
                } else if (workoutStarted) {
                    workoutStarted = false
                    currentExerciseIndex = 0
                }
            },
            onNextExercise = if (hasNextExercise) {
                {
                    selectedExercise = null
                    currentExerciseIndex++
                    selectedExercise = exercises[currentExerciseIndex].exercise
                    selectedGrupa = exercises[currentExerciseIndex].grupaMusculara
                }
            } else null,
            strings = strings
        )
    } else {
        val gradientColors = templateGradient(template.nume)

        Scaffold(
            containerColor = bgColor(),
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = accentColor())
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bgColor(),
                        titleContentColor = textColor()
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(paddingValues)
            ) {
                item {
                    PageTitle(template.nume)
                }
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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedExercise = te.exercise
                                selectedGrupa = te.grupaMusculara
                            },
                        colors = CardDefaults.cardColors(containerColor = cardColor()),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = gifUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    te.exercise.nume,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor(),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    LanguageManager.translateMuscleGroup(te.grupaMusculara, strings),
                                    color = secondaryTextColor(),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    te.exercise.equipment,
                                    color = accentColor(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(
                                modifier = Modifier.padding(end = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val item = exercises.removeAt(index)
                                            exercises.add(index - 1, item)
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription = null,
                                        tint = if (index > 0) accentColor() else Color.Transparent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (index < exercises.size - 1) {
                                            val item = exercises.removeAt(index)
                                            exercises.add(index + 1, item)
                                        }
                                    },
                                    enabled = index < exercises.size - 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = if (index < exercises.size - 1) accentColor() else Color.Transparent,
                                        modifier = Modifier.size(18.dp)
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

// ============================================
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
                "${(level * 100).toInt()}%",
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
                .clip(RoundedCornerShape(4.dp))
                .background(RecoveryTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedLevel.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun RecoveryBarForGroup(grupaMusculara: String) {
    val viewModel: MainViewModel = viewModel()
    var level by remember { mutableStateOf(0.0) }

    LaunchedEffect(grupaMusculara) {
        viewModel.getRecuperareMusculara(grupaMusculara) { level = it }
    }

    var refreshTick by remember { mutableStateOf(0L) }
    LaunchedEffect(grupaMusculara) {
        while (true) {
            delay(30_000)
            refreshTick = System.currentTimeMillis()
        }
    }
    LaunchedEffect(refreshTick, grupaMusculara) {
        viewModel.getRecuperareMusculara(grupaMusculara) { level = it }
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
fun MuscleRecoveryScreen(onBackClick: () -> Unit, recoveryMap: Map<String, Double> = emptyMap()) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    var recoveryData by remember { mutableStateOf<List<Pair<String, Double>>>(listOf()) }

    // Use parent recoveryMap if available, otherwise fall back to DB
    LaunchedEffect(recoveryMap) {
        if (recoveryMap.isNotEmpty()) {
            recoveryData = recoveryMap.toList()
        } else {
            viewModel.getToateRecuperarile { data ->
                recoveryData = data
            }
        }
    }

    Scaffold(
        containerColor = bgColor(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text(strings.recovery) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = accentColor()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor(),
                    titleContentColor = textColor()
                )
            )
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

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor()),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(RecoveryTrack)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = animatedLevel.coerceIn(0f, 1f))
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(barColor)
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
// Componenta: Line Chart (Canvas)
// ============================================
@Composable
fun LineChart(
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    lineColor: Color = accentColor(),
    dotColor: Color = accentColor()
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.second }
    val minValue = data.minOf { it.second }.coerceAtMost(maxValue * 0.8)
    val range = (maxValue - minValue).coerceAtLeast(1.0)
    val topPadding = 32.dp
    val bottomPadding = 40.dp
    val startPadding = 48.dp
    val endPadding = 16.dp

    val gridLines = 4
    val resolvedDivider = dividerColor()
    val resolvedText = textColor()
    val resolvedCard = cardColor()

    val textPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 24f
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val chartLeft = startPadding.toPx()
        val chartRight = w - endPadding.toPx()
        val chartTop = topPadding.toPx()
        val chartBottom = h - bottomPadding.toPx()
        val chartW = chartRight - chartLeft
        val chartH = chartBottom - chartTop

        for (i in 0..gridLines) {
            val y = chartTop + chartH * i / gridLines
            drawLine(
                color = resolvedDivider,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )
            val value = maxValue - (range * i / gridLines)
            drawContext.canvas.nativeCanvas.apply {
                textPaint.color = 0xFFB0B0B0.toInt()
                textPaint.textSize = 24f
                textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                drawText("%.0f".format(value), chartLeft - 8f, y + 6f, textPaint)
            }
        }

        if (data.size == 1) {
            val x = chartLeft + chartW / 2
            val y = chartBottom - ((data[0].second - minValue) / range * chartH).toFloat()
            drawCircle(color = dotColor, radius = 8f, center = Offset(x, y))
            drawContext.canvas.nativeCanvas.apply {
                textPaint.color = 0xFFFFFFFF.toInt()
                textPaint.textSize = 22f
                textPaint.textAlign = android.graphics.Paint.Align.CENTER
                drawText("%.1f".format(data[0].second), x, y - 16f, textPaint)
                textPaint.color = 0xFFB0B0B0.toInt()
                drawText(data[0].first, x, chartBottom + 30f, textPaint)
            }
            return@Canvas
        }

        val points = data.mapIndexed { index, (label, value) ->
            val x = chartLeft + chartW * index / (data.size - 1)
            val y = chartBottom - ((value - minValue) / range * chartH).toFloat()
            Offset(x, y) to label
        }

        val path = Path()
        path.moveTo(points.first().first.x, chartBottom)
        points.forEach { (pt, _) -> path.lineTo(pt.x, pt.y) }
        path.lineTo(points.last().first.x, chartBottom)
        path.close()

        drawPath(
            path,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), lineColor.copy(alpha = 0.0f)),
                startY = chartTop,
                endY = chartBottom
            )
        )

        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i].first,
                end = points[i + 1].first,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        points.forEachIndexed { index, (pt, label) ->
            drawCircle(color = resolvedCard, radius = 7f, center = pt)
            drawCircle(color = dotColor, radius = 5f, center = pt)

            drawContext.canvas.nativeCanvas.apply {
                textPaint.color = 0xFFFFFFFF.toInt()
                textPaint.textSize = 20f
                textPaint.textAlign = android.graphics.Paint.Align.CENTER
                drawText("%.1f".format(data[index].second), pt.x, pt.y - 14f, textPaint)
                textPaint.color = 0xFFB0B0B0.toInt()
                drawText(label, pt.x, chartBottom + 28f, textPaint)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, unit: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        if (unit.isNotBlank()) {
            Text(text = unit, fontSize = 11.sp, color = secondaryTextColor())
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = secondaryTextColor()
        )
    }
}

// ============================================
// Ecranul Progres (redesignat cu Line Chart)
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(isLbs: Boolean = false, initialExercise: String? = null, onBackClick: () -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val strings = LanguageManager.getStrings(context)
    var selectedExercise by remember { mutableStateOf(initialExercise ?: "") }
    var progresData by remember { mutableStateOf<List<ProgresLunar>>(listOf()) }
    var showExerciseSelector by remember { mutableStateOf(initialExercise == null) }
    var selectedGroupFilter by remember { mutableStateOf<String?>(null) }
    var stats by remember { mutableStateOf(ExerciseStats(0.0, 0, 0.0)) }

    BackHandler {
        when {
            showExerciseSelector -> showExerciseSelector = false
            else -> onBackClick()
        }
    }

    LaunchedEffect(initialExercise) {
        if (initialExercise != null) {
            viewModel.getProgresLunar(initialExercise) { progres -> progresData = progres }
            viewModel.getStatisticiExercitiu(initialExercise) { stats = it }
        }
    }

    Scaffold(
        containerColor = bgColor(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = accentColor())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor(),
                    titleContentColor = textColor()
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            PageTitle(strings.progress)
            if (showExerciseSelector) {
                if (selectedGroupFilter == null) {
                    Text(
                        strings.chooseMuscleGroup,
                        style = MaterialTheme.typography.titleLarge,
                        color = secondaryTextColor(),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedGroupFilter = group },
                                colors = CardDefaults.cardColors(containerColor = cardColor()),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(IconBackground),
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
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = accentColor())
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
                    val exercitiiDinGrupa = DataProvider.exercitiiPeGrupa[selectedGroupFilter] ?: listOf()
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(exercitiiDinGrupa) { exercitiu ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedExercise = exercitiu.nume
                                        showExerciseSelector = false
                                        selectedGroupFilter = null
                                        viewModel.getProgresLunar(exercitiu.nume) { progres ->
                                            progresData = progres
                                        }
                                        viewModel.getStatisticiExercitiu(exercitiu.nume) { stats = it }
                                    },
                                colors = CardDefaults.cardColors(containerColor = cardColor()),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(DarkRed.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.FitnessCenter,
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
                                            fontWeight = FontWeight.Medium
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
                TextButton(onClick = {
                    showExerciseSelector = true
                    selectedGroupFilter = null
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = accentColor())
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.changeExercise, color = accentColor())
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor()),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            selectedExercise.uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = textColor(),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor()),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor()),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor()),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(strings.monthlyDetails, color = textColor(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(strings.month, color = textColor(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Max ${if (isLbs) "lbs" else "kg"}", color = textColor(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DarkRed.copy(alpha = 0.2f))
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDark: Boolean,
    preferencesManager: PreferencesManager,
    userProfileManager: UserProfileManager,
    strings: LanguageManager.Strings,
    onLanguageClick: () -> Unit,
    onUnitsClick: () -> Unit,
    onLogout: () -> Unit,
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
    paddingValues: PaddingValues = PaddingValues()
) {
    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed
    val iconBg = if (isDark) IconBackground else LightIconBackground
    val cyanAccent = Color(0xFF00E5FF)
    val greenAccent = Color(0xFF1DB954)
    val purpleAccent = Color(0xFF9F7AEA)
    val purpleTitleColor = Color(0xFFB794F4)
    val goldAccent = Color(0xFFF6E05E)

    val profile = userProfileManager.getOwnProfile()
    var profileName by remember { mutableStateOf(profile?.name ?: strings.guest) }
    val initials =
        profileName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()

    val context = androidx.compose.ui.platform.LocalContext.current

    var editingName by remember { mutableStateOf(false) }
    var editingWeight by remember { mutableStateOf(false) }
    var editingHeight by remember { mutableStateOf(false) }
    var nameText by remember { mutableStateOf(profileName) }
    var weightText by remember {
        mutableStateOf(
            preferencesManager.getUserWeight().let {
                if (it == it.toLong().toFloat()) it.toLong().toString() else String.format(
                    "%.1f",
                    it
                )
            })
    }
    var heightText by remember {
        mutableStateOf(
            preferencesManager.getUserHeight().let {
                if (it == it.toLong().toFloat()) it.toLong().toString() else String.format(
                    "%.1f",
                    it
                )
            })
    }

    val allBadgesList = BadgeEngine.ALL_BADGES
    val earnedKeys = earnedBadges.map { it.key }.toSet()
    var selectedBadge by remember { mutableStateOf<BadgeEntity?>(null) }

    selectedBadge?.let { badge ->
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            containerColor = cardBg,
            titleContentColor = textPrimary,
            textContentColor = textSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(badge.icon, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(badge.title, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        badge.description,
                        fontSize = 14.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cum să obții:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        badge.hint,
                        fontSize = 14.sp,
                        color = textPrimary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("OK", color = accent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = paddingValues.calculateTopPadding() + 4.dp,
            bottom = paddingValues.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageTitle(strings.profile)
        }

        item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingName = true }
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    profileName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    strings.tapToEdit,
                    fontSize = 11.sp,
                    color = textSecondary.copy(alpha = 0.5f)
                )
            }
        }

        item {
            // Premium Stat Cards

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Total Volume (Cyan)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isDark) listOf(
                                    cyanAccent.copy(alpha = 0.08f),
                                    Color(0xFF0A1A2A),
                                    Color(0xFF050F15)
                                ) else listOf(
                                    cyanAccent.copy(alpha = 0.06f),
                                    Color(0xFFF0F9FF),
                                    Color(0xFFFFFFFF)
                                )
                            )
                        )
                        .border(1.dp, cyanAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(cyanAccent.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            val barColor = cyanAccent
                            drawRect(
                                color = barColor,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.42f),
                                size = Size(size.width * 0.7f, size.height * 0.16f)
                            )
                            drawRect(
                                color = barColor,
                                topLeft = Offset(0f, size.height * 0.22f),
                                size = Size(size.width * 0.2f, size.height * 0.56f)
                            )
                            drawRect(
                                color = barColor,
                                topLeft = Offset(size.width * 0.8f, size.height * 0.22f),
                                size = Size(size.width * 0.2f, size.height * 0.56f)
                            )
                            drawRect(
                                color = barColor.copy(alpha = 0.5f),
                                topLeft = Offset(size.width * 0.03f, size.height * 0.3f),
                                size = Size(size.width * 0.1f, size.height * 0.4f)
                            )
                            drawRect(
                                color = barColor.copy(alpha = 0.5f),
                                topLeft = Offset(size.width * 0.87f, size.height * 0.3f),
                                size = Size(size.width * 0.1f, size.height * 0.4f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val volFormatted = if (totalVolume >= 1000) String.format("%.1fK", totalVolume / 1000) else String.format("%.0f", totalVolume)
                    Text(
                        text = volFormatted,
                        color = textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "VOLUME",
                        color = cyanAccent.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                // Card 2: Workouts (Green)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isDark) listOf(
                                    greenAccent.copy(alpha = 0.08f),
                                    Color(0xFF0A1A0A),
                                    Color(0xFF050F05)
                                ) else listOf(
                                    greenAccent.copy(alpha = 0.06f),
                                    Color(0xFFF0FFF4),
                                    Color(0xFFFFFFFF)
                                )
                            )
                        )
                        .border(1.dp, greenAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(greenAccent.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(22.dp)) {
                            val calColor = greenAccent
                            drawRect(
                                color = calColor,
                                topLeft = Offset(size.width * 0.1f, size.height * 0.3f),
                                size = Size(size.width * 0.8f, size.height * 0.6f)
                            )
                            drawRect(
                                color = calColor,
                                topLeft = Offset(size.width * 0.1f, size.height * 0.3f),
                                size = Size(size.width * 0.8f, size.height * 0.15f)
                            )
                            drawRect(
                                color = calColor,
                                topLeft = Offset(size.width * 0.28f, size.height * 0.12f),
                                size = Size(size.width * 0.08f, size.height * 0.25f)
                            )
                            drawRect(
                                color = calColor,
                                topLeft = Offset(size.width * 0.64f, size.height * 0.12f),
                                size = Size(size.width * 0.08f, size.height * 0.25f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$totalWorkouts",
                        color = textPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "WORKOUTS",
                        color = greenAccent.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PB Card — Full Width with Purple-Gold gradient border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark) listOf(
                                purpleAccent.copy(alpha = 0.10f),
                                Color(0xFF12091A),
                                Color(0xFF0A0510)
                            ) else listOf(
                                purpleAccent.copy(alpha = 0.06f),
                                Color(0xFFF5F0FF),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                purpleAccent.copy(alpha = 0.6f),
                                goldAccent.copy(alpha = 0.6f),
                                purpleAccent.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(purpleAccent.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            val tColor = goldAccent
                            drawRect(
                                color = tColor,
                                topLeft = Offset(size.width * 0.25f, size.height * 0.08f),
                                size = Size(size.width * 0.5f, size.height * 0.45f)
                            )
                            drawRect(
                                color = tColor,
                                topLeft = Offset(size.width * 0.4f, size.height * 0.53f),
                                size = Size(size.width * 0.2f, size.height * 0.2f)
                            )
                            drawRect(
                                color = tColor,
                                topLeft = Offset(size.width * 0.22f, size.height * 0.73f),
                                size = Size(size.width * 0.56f, size.height * 0.12f)
                            )
                            drawRect(
                                color = tColor.copy(alpha = 0.5f),
                                topLeft = Offset(size.width * 0.1f, size.height * 0.12f),
                                size = Size(size.width * 0.15f, size.height * 0.3f)
                            )
                            drawRect(
                                color = tColor.copy(alpha = 0.5f),
                                topLeft = Offset(size.width * 0.75f, size.height * 0.12f),
                                size = Size(size.width * 0.15f, size.height * 0.3f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NEW PB'S",
                            color = purpleTitleColor.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$bestStreak best streak",
                            color = textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "\uD83C\uDFC6",
                        fontSize = 26.sp
                    )
                }
            }
        }

        if (allBadgesList.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        strings.badges,
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allBadgesList) { badge ->
                            val earned = badge.key in earnedKeys
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (earned) accent.copy(alpha = 0.12f) else Color.Transparent)
                                    .clickable { selectedBadge = badge }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    badge.icon,
                                    fontSize = 28.sp,
                                    modifier = Modifier.alpha(if (earned) 1f else 0.25f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    badge.title,
                                    fontSize = 10.sp,
                                    color = if (earned) textPrimary else textSecondary.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        strings.personalInfo,
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingName = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.nameField,
                            color = textSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (editingName) {
                            OutlinedTextField(
                                value = nameText,
                                onValueChange = { nameText = it },
                                modifier = Modifier.width(180.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = textSecondary,
                                    cursorColor = accent,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                val newName = nameText.trim()
                                if (newName.isNotBlank() && newName != profileName) {
                                    profileName = newName
                                    onNameChanged(newName)
                                    userProfileManager.saveOwnProfile(
                                        newName,
                                        profile?.photoUri ?: ""
                                    )
                                    val uid = userProfileManager.getOwnUserId()
                                    if (uid != "local_user") {
                                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.Job()).launch {
                                            try {
                                                FirestoreHelper().saveUserProfile(uid, newName, profile?.photoUri ?: "")
                                            } catch (_: Exception) {}
                                            try {
                                                AuthManager(context).updateDisplayName(newName)
                                            } catch (_: Exception) {}
                                            try {
                                                val db = AppDatabase.getDatabase(context)
                                                SocialRepository(db).syncUserProfile(uid, newName, profile?.photoUri ?: "")
                                            } catch (_: Exception) {}
                                        }
                                    }
                                }
                                editingName = false
                            }) {
                                Text(strings.confirm, color = accent)
                            }
                        } else {
                            Text(
                                profileName,
                                color = textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = textSecondary.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingWeight = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.weight,
                            color = textSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (editingWeight) {
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                modifier = Modifier.width(120.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = textSecondary,
                                    cursorColor = accent,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                val w =
                                    weightText.toFloatOrNull() ?: preferencesManager.getUserWeight()
                                preferencesManager.setUserWeight(w)
                                editingWeight = false
                            }) {
                                Text(strings.confirm, color = accent)
                            }
                        } else {
                            Text(
                                "${
                                    preferencesManager.getUserWeight().let {
                                        if (it == it.toLong().toFloat()) it.toLong()
                                            .toString() else String.format("%.1f", it)
                                    }
                                } ${strings.kg}",
                                color = textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = textSecondary.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingHeight = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.height,
                            color = textSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (editingHeight) {
                            OutlinedTextField(
                                value = heightText,
                                onValueChange = { heightText = it },
                                modifier = Modifier.width(120.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accent,
                                    unfocusedBorderColor = textSecondary,
                                    cursorColor = accent,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = {
                                val h =
                                    heightText.toFloatOrNull() ?: preferencesManager.getUserHeight()
                                preferencesManager.setUserHeight(h)
                                editingHeight = false
                            }) {
                                Text(strings.confirm, color = accent)
                            }
                        } else {
                            Text(
                                "${
                                    preferencesManager.getUserHeight().let {
                                        if (it == it.toLong().toFloat()) it.toLong()
                                            .toString() else String.format("%.1f", it)
                                    }
                                } cm",
                                color = textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = textSecondary.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.waterGoal,
                            color = textSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "${
                                preferencesManager.getUserWeight().toInt()
                            } × 33${strings.ml} = ${preferencesManager.getWaterGoalMl()}${strings.ml}",
                            color = textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBiometricClick() },
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.biometricTracking,
                            style = MaterialTheme.typography.titleMedium,
                            color = textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (lastBiometric != null) {
                        Text(
                            strings.lastMeasurement,
                            color = textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val units = if (preferencesManager.isLbs()) "lbs" else "kg"
                        if (lastBiometric.weightKg > 0) {
                            val displayWeight = if (preferencesManager.isLbs()) lastBiometric.weightKg * 2.20462 else lastBiometric.weightKg
                            Text(
                                "${strings.weight}: ${String.format("%.1f", displayWeight)} $units",
                                color = textPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (lastBiometric.bodyFatPercent > 0) {
                            Text(
                                "${strings.bodyFat}: ${String.format("%.1f", lastBiometric.bodyFatPercent)}${strings.percent}",
                                color = textPrimary,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val timeLabel = when {
                            weeksSinceMeasurement == 0 -> strings.thisWeek
                            weeksSinceMeasurement > 0 -> "$weeksSinceMeasurement ${strings.weeksAgo}"
                            else -> ""
                        }
                        if (timeLabel.isNotEmpty()) {
                            Text(
                                timeLabel,
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { onBiometricChartsClick() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                strings.viewCharts,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    strings.noMeasurements,
                                    color = textSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    strings.addMeasurement,
                                    color = accent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accent.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.ShowChart,
                                    contentDescription = null,
                                    tint = accent.copy(alpha = 0.6f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    strings.progressChart,
                                    fontSize = 8.sp,
                                    color = accent.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    strings.biometricReminder,
                                    color = textSecondary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    strings.weeklyReminder,
                                    fontSize = 11.sp,
                                    color = textSecondary.copy(alpha = 0.6f)
                                )
                            }
                        }
                        var biometricReminderEnabled by remember { mutableStateOf(preferencesManager.isBiometricReminderEnabled()) }
                        Switch(
                            checked = biometricReminderEnabled,
                            onCheckedChange = { enabled ->
                                biometricReminderEnabled = enabled
                                preferencesManager.setBiometricReminderEnabled(enabled)
                                val receiver = BiometricReminderReceiver()
                                if (enabled) {
                                    receiver.scheduleWeekly(context)
                                } else {
                                    receiver.cancelAlarm(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accent,
                                uncheckedThumbColor = textSecondary,
                                uncheckedTrackColor = textSecondary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        strings.settingsAndMore,
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onLanguageClick)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                strings.language,
                                color = textSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = textSecondary
                        )
                    }

                    HorizontalDivider(color = textSecondary.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onUnitsClick)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Straighten,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                strings.units,
                                color = textSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = textSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1215)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        strings.logout,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


