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
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.kinetic.ui.theme.JetBrainsMono
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


// ============================================
// Ecranele de workout extrase din MainActivity
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(grupaMusculara: String, isLbs: Boolean = false, isDark: Boolean = true, onBackClick: () -> Unit, onWorkoutSaved: () -> Unit = {}) {
    val context = LocalContext.current
    val userId = remember { UserProfileManager(context).getOwnUserId() }
    val strings = LanguageManager.getStrings(context)
    val viewModel: MainViewModel = viewModel()
    var exercitii by remember { mutableStateOf<List<ExerciseListItem>>(emptyList()) }
    var selectedExercise: ExerciseDefinition? by remember { mutableStateOf(null) }
    var selectedProgressExercise: String? by remember { mutableStateOf(null) }
    var reloadToken by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEquipment by remember { mutableStateOf<String?>(null) }
    val exerciseSummaries by viewModel.exerciseSummaries.collectAsState()

    val p = appPalette(isDark)
    val surfaceBg = p.bg
    val textPrimary = p.tp
    val textSecondary = p.ts
    val cardBg = p.card
    val accent = p.ac

    LaunchedEffect(grupaMusculara, reloadToken) {
        viewModel.getExercitiiPentruGrupa(userId, grupaMusculara) { exercitii = it }
    }

    LaunchedEffect(exercitii) {
        val allNames = exercitii.map { it.exercise.nume }
        if (allNames.isNotEmpty()) {
            viewModel.loadExerciseSummaries(userId, allNames)
        }
    }

    val equipmentTypes = remember {
        listOf("Dumbbells", "Barbell", "Machine", "Cable", "Bodyweight", "EZ Bar", "Smith Machine", "Kettlebell", "Stability Ball", "Sled Machine", "Band")
    }
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
            isDark = isDark,
            onBackClick = { selectedProgressExercise = null }
        )
    } else if (selectedExercise != null) {
        ExerciseInputScreen(
            exercise = selectedExercise!!,
            grupaMusculara = grupaMusculara,
            isLbs = isLbs,
            isDark = isDark,
            onBackClick = { selectedExercise = null },
            onOpenProgress = { name -> selectedProgressExercise = name },
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
            KineticAppBar(onBack = onBackClick)
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 170.dp, bottom = AppConstants.BOTTOM_NAV_PADDING),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredExercises) { item ->
                        val exercitiu = item.exercise
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedExercise = exercitiu },
                            shape = RoundedCornerShape(16.dp),
                            isDark = isDark,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .background(if (isDark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)),
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exercitiu.nume,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = textColor(),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val summary = exerciseSummaries[exercitiu.nume]
                                        if (summary != null && summary.bestWeight > 0) {
                                            Text(
                                                text = "PR: ${summary.bestWeight.toInt()}kg × ${summary.bestReps}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFD94848)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.setFavorite(
                                                userId = userId,
                                                grupa = grupaMusculara,
                                                numeExercitiu = exercitiu.nume,
                                                isFavorite = !item.isFavorite
                                            ) {
                                                reloadToken++
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = strings.favorite,
                                            tint = if (item.isFavorite) RecoveryYellow else secondaryTextColor()
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                                                                        .background(DarkRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(strings.search, color = textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = strings.clear, tint = textSecondary)
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
                                label = { Text(LanguageManager.translateEquipment(eq, strings)) },
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

// ── Confetti/particle burst: particule efemere care radiază din butonul de save ──
private data class ConfettiParticle(
    val color: Color,
    val angle: Float,      // radiani; π..2π → evantai în sus (sin negativ = sus în Compose)
    val distance: Float,   // în dp
    val size: Float,       // în dp
    val rotationSpeed: Float,
    val delay: Float       // 0..0.35 stagger pentru efect natural
)

@Composable
private fun ConfettiBurst(trigger: Int, isDark: Boolean, isPR: Boolean = false) {
    if (trigger <= 0) return
    val progress = remember { Animatable(0f) }
    val particles = remember(trigger, isPR) {
        val rnd = Random(trigger * 7919L + 31L)
        val base = if (isPR) {
            if (isDark) {
                listOf(GoldPR, Color(0xFFF5A623), Color(0xFFFFD54F), Color(0xFFFFF176), Color.White)
            } else {
                listOf(Color(0xFFF5A623), Color(0xFFFFC107), Color(0xFFFFD54F), Color(0xFFFFF176), Color.White)
            }
        } else {
            if (isDark) {
                listOf(RecoveryGreen, Color(0xFF00E676), Color(0xFFA5D6A7), Color(0xFFB2FF59), Color.White)
            } else {
                listOf(Color(0xFF00C853), Color(0xFF4CAF50), Color(0xFFA5D6A7), Color(0xFFB2FF59), Color.White)
            }
        }
        List(30) {
            ConfettiParticle(
                color = base[rnd.nextInt(base.size)],
                angle = PI.toFloat() + rnd.nextFloat() * PI.toFloat(), // π..2π → jumătate de cerc în sus
                distance = 36f + rnd.nextFloat() * 66f,
                size = 3f + rnd.nextFloat() * 3.2f,
                rotationSpeed = (if (rnd.nextBoolean()) 1 else -1) * (200f + rnd.nextFloat() * 400f),
                delay = rnd.nextFloat() * 0.35f
            )
        }
    }
    val playedFor = remember { mutableIntStateOf(0) }
    LaunchedEffect(trigger) {
        if (playedFor.intValue != trigger) {
            playedFor.intValue = trigger
            progress.snapTo(0f)
            progress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        }
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .offset(y = (-52).dp)
    ) {
        val cx = size.width - 60.dp.toPx() // în dreptul butonului de save
        val cy = 118.dp.toPx()             // jos în rând, de unde pleacă burst-ul
        val gravity = 26.dp.toPx()
        particles.forEach { p ->
            val t = ((progress.value - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (t <= 0f) return@forEach
            val radius = p.distance.dp.toPx() * t
            val x = cx + cos(p.angle) * radius
            val y = cy + sin(p.angle) * radius + gravity * t * t
            val alpha = if (t > 0.72f) (1f - (t - 0.72f) / 0.28f) else 1f
            val half = p.size.dp.toPx() / 2f
            rotate(degrees = p.rotationSpeed * t, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    topLeft = Offset(x - half, y - half),
                    size = Size(half * 2f, half * 2f)
                )
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
    isDark: Boolean = false,
    onBackClick: () -> Unit,
    onNextExercise: (() -> Unit)? = null,
    onFinishExercise: () -> Unit = {},
    onOpenProgress: (String) -> Unit = {},
    onWorkoutSaved: () -> Unit = {},
    workoutStartTimeMs: Long = System.currentTimeMillis(),
    showFinishButton: Boolean = true,
    strings: LanguageManager.Strings,
    currentIndex: Int = 0,
    totalExercises: Int = 0,
    nextExerciseName: String? = null,
    nextExerciseSets: String = "",
    phase: String = "WARMUP"
) {
    val isStretch = exercise.nume.contains("Stretch", ignoreCase = true)
    if (isStretch) {
        StretchExerciseScreen(
            exercise = exercise,
            isLbs = isLbs,
            isDark = isDark,
            onBackClick = onBackClick,
            onNextExercise = onNextExercise,
            onFinishExercise = onFinishExercise,
            onOpenProgress = onOpenProgress,
            onWorkoutSaved = onWorkoutSaved,
            strings = strings,
            currentIndex = currentIndex,
            totalExercises = totalExercises,
            nextExerciseName = nextExerciseName,
            nextExerciseSets = nextExerciseSets,
            phase = phase
        )
        return
    }
    val context = LocalContext.current
    val userId = remember { UserProfileManager(context).getOwnUserId() }
    val viewModel: MainViewModel = viewModel()
    val soundManager = remember { SoundManager(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }
    var currentSets by remember { mutableStateOf(listOf<SetEntry>(SetEntry(0.0, 0))) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var isPR by remember { mutableStateOf(false) }
    // Pulsul de succes pe butonul de save al seriei
    var lastSavedSetIndex by remember { mutableIntStateOf(-1) }
    var savePulseTick by remember { mutableIntStateOf(0) }
    var lastPRSetIndex by remember { mutableIntStateOf(-1) }
    var prBurstTick by remember { mutableIntStateOf(0) }
    val pulseScope = rememberCoroutineScope()
    var noteText by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<ExercitiuEntity>>(emptyList()) }
    var stats by remember { mutableStateOf(ExerciseStats(0.0, 0, 0.0)) }
    var oneRmTrend by remember { mutableStateOf<List<Pair<Long, Double>>>(emptyList()) }
    val textSecondary = if (isSystemInDarkTheme()) secondaryTextColor() else LightTextSecondary
    var volumeSummary by remember { mutableStateOf(VolumeSummary(0.0, 0.0, 0.0)) }
    var restSeconds by remember { mutableStateOf(90) }
    var remainingSeconds by remember { mutableStateOf(0) }
    var timerWasRunning by remember { mutableStateOf(false) }
    var showTimerExpired by remember { mutableStateOf(false) }
    var customTimerText by remember { mutableStateOf("") }
    var editingSet by remember { mutableStateOf<ExercitiuEntity?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    // Ultima salvare de set — delta dintre salvări se acumulează corect în Weekly Summary.
    // (Fiecare set creează un rând separat în antrenamente, deci salvăm timpul scurs de la ultimul
    //  set, nu durata totală a sesiunii — altfel weekly sum ar fi durata × numărul de seturi.)
    var lastSetSavedMs by remember { mutableLongStateOf(workoutStartTimeMs) }

    LaunchedEffect(exercise.nume, userId) {
        val meta = AppDatabase.getDatabase(context).exerciseMetadataDao().getByName(userId, exercise.nume)
        isFavorite = meta?.isFavorite == true
    }

    BackHandler {
        when {
            editingSet != null -> editingSet = null
            else -> onBackClick()
        }
    }

    fun refreshExerciseData() {
        viewModel.getIstoricExercitiu(userId, exercise.nume) { history = it }
        viewModel.getStatisticiExercitiu(userId, exercise.nume) { stats = it }
        viewModel.getVolumeSummary(userId) { volumeSummary = it }
        viewModel.getOneRmTrend(userId, exercise.nume) { oneRmTrend = it }
    }

    LaunchedEffect(exercise.nume) {
        refreshExerciseData()
    }

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds > 0) {
            timerWasRunning = true
            delay(1_000)
            remainingSeconds--
        } else if (remainingSeconds == 0 && timerWasRunning) {
            timerWasRunning = false
            vibratePhone(context)
            showTimerExpired = true
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
        val icon: androidx.compose.ui.graphics.painter.Painter = if (isPR) androidx.compose.ui.res.painterResource(R.drawable.trophy_star) else androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.CheckCircle)

        val validSets = currentSets.filter { it.greutateKg > 0 || it.repetari > 0 }
        val totalVolume = validSets.sumOf { it.greutateKg * it.repetari }
        val totalSets = validSets.size
        val maxWeight = validSets.maxOfOrNull { it.greutateKg } ?: 0.0
        val totalReps = validSets.sumOf { it.repetari }

        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            containerColor = Color.Transparent,
            titleContentColor = textColor(),
            textContentColor = secondaryTextColor(),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (icon is androidx.compose.ui.graphics.vector.VectorPainter) iconColor else androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(56.dp).scale(scale)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isPR) "\uD83C\uDFC6 NEW PR!" else LanguageManager.getStrings(context).workoutCompleted,
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
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = LanguageManager.getStrings(context).volume, value = String.format(java.util.Locale.ROOT, "%.0f", totalVolume), unit = "kg", accent = accentColor())
                        StatItem(label = LanguageManager.getStrings(context).sets, value = "$totalSets", unit = "", accent = accentColor())
                        StatItem(label = LanguageManager.getStrings(context).reps, value = "$totalReps", unit = "", accent = accentColor())
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        StatItem(label = LanguageManager.getStrings(context).maxWeight, value = String.format(java.util.Locale.ROOT, "%.1f", maxWeight), unit = "kg", accent = accentColor())
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Doar închide dialogul — utilizatorul rămâne în exercițiul selectat
                        // și poate adăuga alte seturi sau ieși prin săgeata de back.
                        showSaveConfirmation = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.ok, color = accentColor(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0xFF0D0D0D).copy(alpha = 0.95f),
                                Color(0xFF0A0A0A).copy(alpha = 0.98f),
                                Color(0xFF050505).copy(alpha = 1.0f)
                            )
                        } else {
                            listOf(
                                Color(0xFFFFFFFF).copy(alpha = 0.95f),
                                Color(0xFFF8F8F8).copy(alpha = 0.98f),
                                Color(0xFFF0F0F0).copy(alpha = 1.0f)
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp)
                )
        )
    }

    val dark = isDark
    val Bg = if (dark) DarkBackground else LightBackground
    val S1 = if (dark) Color(0xFF0D0D0D) else Color(0xFFFFFFFF)
    val S2 = if (dark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    val RedC = Ember
    val RedL = EmberLight
    val RedD = Ember.copy(alpha = 0.22f)
    val AmberC = Color(0xFFFF9F43)
    val Mut = secondaryTextColor()
    val Dim = if (dark) Color(0x73E8E8EC) else Color(0x2A1A2E32)
    val Txt = if (dark) textColor() else LightTextPrimary
    val Bdr = if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val BdrInput = if (dark) NeutralBorderDark else LightDividerGray
    val Bdr2 = if (dark) DarkDivider else LightDividerGray

    if (showTimerExpired) {
        AlertDialog(
            onDismissRequest = { showTimerExpired = false },
            containerColor = Color.Transparent,
            titleContentColor = textColor(),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = accentColor(),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = LanguageManager.getStrings(context).timerFinished,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = LanguageManager.getStrings(context).timeToStartNextSet,
                        fontSize = 14.sp,
                        color = secondaryTextColor(),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTimerExpired = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.ok, color = accentColor(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(S1)
                .border(1.dp, Bdr, RoundedCornerShape(24.dp))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(S1)
                        .border(1.dp, Bdr, RoundedCornerShape(14.dp))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_back_arrow), null, tint = Dim, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${currentIndex + 1} / $totalExercises",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Dim,
                        letterSpacing = 2.sp
                    )
                    Text(
                        exercise.nume.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Txt,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(S1)
                            .border(1.dp, Bdr, RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.setFavorite(userId, grupaMusculara, exercise.nume, !isFavorite) {
                                    isFavorite = !isFavorite
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            null, tint = if (isFavorite) Color(0xFFFFEB3B) else Dim, modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = AppConstants.BOTTOM_NAV_PADDING)
            ) {
                if (showFinishButton) {
                    item {
                        if (onNextExercise != null) {
                            GradientNextExerciseButton(
                                text = strings.nextExercise,
                                onClick = { onNextExercise() }
                            )
                        } else {
                            GradientNextExerciseButton(
                                text = strings.finish,
                                onClick = { onFinishExercise() }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
                item {
                    RecoveryBarCard(grupaMusculara = grupaMusculara, isDark = isDark)
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Bdr, RoundedCornerShape(20.dp))
                    ) {
                        val gifUrl = ExerciseGifs.getGif(exercise.nume)
                        if (gifUrl != null) {
                            AsyncImage(
                                model = gifUrl,
                                contentDescription = exercise.nume,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = Dim,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = strings.exercise.uppercase().take(20),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Dim
                                )
                            }
                        }

                    Text(
                        text = LanguageManager.translateMuscleGroup(grupaMusculara, strings).uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = RedL.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(RedD)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                            Text(
                                text = strings.exercise.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = RedL
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(S1)
                            .border(1.dp, Bdr, RoundedCornerShape(22.dp))
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.paused, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Dim, letterSpacing = 1.8.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    listOf(60, 90, 120, 180).forEach { sec ->
                                        val presetMin = sec / 60
                                        val presetSec = sec % 60
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (sec == restSeconds) RedD else S2)
                                                .clickable { restSeconds = sec; remainingSeconds = 0 }
                                                .padding(6.dp, 5.dp, 13.dp, 5.dp)
                                        ) {
                                            Text(
                                                "$presetMin:${presetSec.toString().padStart(2, '0')}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (sec == restSeconds) RedL else Dim,
                                                fontFamily = JetBrainsMono
                                            )
                                        }
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        val min = remainingSeconds / 60
                        val sec = remainingSeconds % 60
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                min.toString(), fontSize = 54.sp, fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMono, letterSpacing = (-3).sp,
                                color = if (remainingSeconds > 0) RedL else Txt
                            )
                            Text(":", fontSize = 54.sp, fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMono, letterSpacing = (-3).sp,
                                color = if (remainingSeconds > 0) RedL else Dim)
                            Text(
                                sec.toString().padStart(2, '0'), fontSize = 54.sp, fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMono, letterSpacing = (-3).sp,
                                color = if (remainingSeconds > 0) RedL else Txt
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        val timerProgress = if (restSeconds > 0) remainingSeconds.toFloat() / restSeconds else 0f
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)).background(Color(0x06FFFFFF))) {
                            Box(Modifier.fillMaxWidth(timerProgress).fillMaxHeight().clip(RoundedCornerShape(6.dp)).background(Brush.horizontalGradient(listOf(RedC, RedL))))
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(S2)
                                    .border(1.dp, Bdr, RoundedCornerShape(14.dp))
                                    .clickable { remainingSeconds = 0 },
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Replay, null, tint = Dim, modifier = Modifier.size(16.dp)) }
                            Box(
                                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(18.dp))
                                    .background(if (remainingSeconds > 0) S2 else RedC)
                                    .clickable {
                                        if (remainingSeconds > 0) remainingSeconds = 0
                                        else remainingSeconds = restSeconds
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (remainingSeconds > 0) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    null, tint = if (remainingSeconds > 0) RedL else Color.White, modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    val counterScale = remember { Animatable(1f) }
                    LaunchedEffect(savePulseTick) {
                        if (savePulseTick > 0) {
                            counterScale.snapTo(1.35f)
                            counterScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.List, null, tint = RedL, modifier = Modifier.size(12.dp))
                            Text(strings.sets.uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                        val done = currentSets.count { it.greutateKg > 0 || it.repetari > 0 }
                        Text(
                            "$done/${currentSets.size}",
                            fontSize = 11.sp,
                            color = Dim,
                            fontWeight = FontWeight.Medium,
                            fontFamily = JetBrainsMono,
                            modifier = Modifier.graphicsLayer {
                                scaleX = counterScale.value
                                scaleY = counterScale.value
                            }
                        )
                    }
                    // Auto-progresie: sugestie pentru setul următor, bazată pe ultimul set salvat
                    val lastFilledSet = currentSets.lastOrNull { it.greutateKg > 0 || it.repetari > 0 }
                    if (lastFilledSet != null && lastFilledSet.repetari > 0 && lastFilledSet.greutateKg > 0) {
                        val step = 2.5
                        val nextWeight = ((lastFilledSet.greutateKg + step) / step).roundToInt() * step
                        val increase = nextWeight > lastFilledSet.greutateKg
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor().copy(alpha = if (isDark) 0.14f else 0.10f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = accentColor(),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "${strings.nextSetSuggestion}: ${weightLabel(nextWeight, isLbs)} × ${lastFilledSet.repetari}" +
                                        if (increase) "  (+${weightLabel(step, isLbs)})" else "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor()
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                itemsIndexed(currentSets) { index, set ->
                    // Micro-animație de succes: puls de scală pe butonul de save
                    val saveScale = remember { Animatable(1f) }
                    // Animație de ștergere: shrink + fade înainte de eliminare
                    val deleteScale = remember { Animatable(1f) }
                    val deleteAlpha = remember { Animatable(1f) }
                    LaunchedEffect(savePulseTick) {
                        if (lastSavedSetIndex == index) {
                            saveScale.snapTo(0.82f)
                            saveScale.animateTo(1.15f, tween(150, easing = FastOutSlowInEasing))
                            saveScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
                        }
                    }
                    Box {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = deleteScale.value
                                scaleY = deleteScale.value
                                alpha = deleteAlpha.value
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(S1)
                            .border(1.dp, Bdr, RoundedCornerShape(16.dp))
                            .padding(start = 14.dp, top = 14.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            color = Dim, fontFamily = JetBrainsMono,
                            modifier = Modifier.width(24.dp), textAlign = TextAlign.Center
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("KG", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Dim, letterSpacing = 1.sp)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = if (set.greutateKg > 0) set.greutateKg.toInt().toString() else "",
                                onValueChange = {
                                    val updated = set.copy(greutateKg = it.toDoubleOrNull() ?: 0.0)
                                    currentSets = currentSets.toMutableList().also { it[index] = updated }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                    fontFamily = JetBrainsMono, color = Txt, textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BdrInput, focusedBorderColor = RedL.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Color(0x15FFFFFF), focusedContainerColor = Color(0x15FFFFFF),
                                    cursorColor = RedL, focusedTextColor = Txt, unfocusedTextColor = Txt
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.reps.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Dim, letterSpacing = 1.sp)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = if (set.repetari > 0) set.repetari.toString() else "",
                                onValueChange = {
                                    val updated = set.copy(repetari = it.toIntOrNull() ?: 0)
                                    currentSets = currentSets.toMutableList().also { it[index] = updated }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                    fontFamily = JetBrainsMono, color = Txt, textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BdrInput, focusedBorderColor = RedL.copy(alpha = 0.3f),
                                    unfocusedContainerColor = Color(0x15FFFFFF), focusedContainerColor = Color(0x15FFFFFF),
                                    cursorColor = RedL, focusedTextColor = Txt, unfocusedTextColor = Txt
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (set.greutateKg > 0 || set.repetari > 0) RecoveryGreen else RecoveryGreen.copy(alpha = 0.12f))
                                .border(1.dp, if (set.greutateKg > 0 || set.repetari > 0) RecoveryGreen else RecoveryGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .graphicsLayer {
                                    scaleX = saveScale.value
                                    scaleY = saveScale.value
                                }
                                .clickable {
                                    // Durată reală: timpul scurs de la ultima salvare (primul set măsoară de la deschidere).
                                    // Suma pe toate rândurile = durata totală a sesiunii → Weekly Summary corect.
                                    val nowMs = System.currentTimeMillis()
                                    val elapsedDelta = (nowMs - lastSetSavedMs).coerceAtLeast(0L)
                                    lastSetSavedMs = nowMs
                                    viewModel.salveazaAntrenament(
                                        userId = userId,
                                        grupaMusculara = grupaMusculara,
                                        numeExercitiu = exercise.nume,
                                        seturi = currentSets.filter { it.greutateKg > 0 || it.repetari > 0 },
                                        note = noteText,
                                        durationMs = elapsedDelta
                                    ) { newPR ->
                                        refreshExerciseData()
                                        onWorkoutSaved()
                                        isPR = newPR
                                        lastSavedSetIndex = index
                                        savePulseTick++
                                        if (newPR) {
                                            lastPRSetIndex = index
                                            prBurstTick++
                                            soundManager.playPrSound()
                                        }
                                        pulseScope.launch {
                                            delay(if (newPR) 950 else 420)
                                            showSaveConfirmation = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = if (set.greutateKg > 0 || set.repetari > 0) Color.White else RecoveryGreen, modifier = Modifier.size(14.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (dark) RedL.copy(alpha = 0.22f) else DarkRed.copy(alpha = 0.18f))
                                .border(1.dp, if (dark) RedL.copy(alpha = 0.6f) else DarkRed.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                .clickable {
                                    if (currentSets.size > 1 && deleteAlpha.value > 0.5f) {
                                        pulseScope.launch {
                                            coroutineScope {
                                                launch { deleteScale.animateTo(0.55f, tween(240, easing = FastOutSlowInEasing)) }
                                                launch { deleteAlpha.animateTo(0f, tween(240, easing = FastOutSlowInEasing)) }
                                            }
                                            currentSets = currentSets.toMutableList().also { it.removeAt(index) }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, null, tint = if (dark) RedL else DarkRed, modifier = Modifier.size(14.dp))
                        }
                    }


                    Spacer(Modifier.height(2.dp))
                    }
                    // Confetti/particle burst la fiecare serie salvată (verde normal, auriu PR)
                    if (lastSavedSetIndex == index && savePulseTick > 0) {
                        ConfettiBurst(trigger = savePulseTick, isDark = isDark, isPR = lastPRSetIndex == index)
                    }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    val infiniteTransition = rememberInfiniteTransition(label = "addSetGlow")
                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.1f, targetValue = 0.35f,
                        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                        label = "addSetGlowAlpha"
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(RedL.copy(alpha = glowAlpha))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(2.dp, RedL.copy(alpha = 0.25f + glowAlpha * 0.3f), RoundedCornerShape(14.dp))
                                .clickable { currentSets = currentSets + SetEntry(0.0, 0) }
                                .padding(13.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, null, tint = RedL, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.addSet, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RedL)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text(strings.exerciseNotes) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedC, unfocusedBorderColor = Bdr,
                            focusedLabelColor = RedC, unfocusedLabelColor = Mut,
                            cursorColor = RedC, focusedTextColor = Txt, unfocusedTextColor = Txt
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    ExerciseHistoryCard(
                        history = history, isLbs = isLbs, isDark = isDark,
                        onEdit = { editingSet = it },
                        onDelete = { set -> viewModel.deleteSet(set) { refreshExerciseData() } }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                item {
                    ExerciseStatsCard(stats = stats, volumeSummary = volumeSummary, isLbs = isLbs, isDark = isDark, oneRmTrend = oneRmTrend)
                }
            }
        }
    }
}

private fun vibratePhone(context: android.content.Context) {
    try {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(500)
        }
    } catch (_: Exception) {}
}

// ============================================
// Componenta: Statistici + PR-uri
// ============================================
@Composable
fun ExerciseStatsCard(stats: ExerciseStats, volumeSummary: VolumeSummary, isLbs: Boolean = false, isDark: Boolean = false, oneRmTrend: List<Pair<Long, Double>> = emptyList()) {
    val strings = LanguageManager.getStrings(LocalContext.current)
    val cardBg = if (isDark) Color(0xFF0D0D0D) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
            .padding(22.dp)
    ) {
        Text(strings.prAndVolume, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = secondaryTextColor(), letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill(strings.maxWeight, weightLabel(stats.maxGreutate, isLbs), Modifier.weight(1f), isDark)
            StatPill(strings.maxReps, "${stats.maxRepetari}", Modifier.weight(1f), isDark)
            StatPill(strings.maxSet, weightLabel(stats.maxVolumSet, isLbs), Modifier.weight(1f), isDark)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill(strings.today, weightLabel(volumeSummary.azi, isLbs), Modifier.weight(1f), isDark)
            StatPill(strings.thisWeek, weightLabel(volumeSummary.saptamana, isLbs), Modifier.weight(1f), isDark)
            StatPill(strings.thisMonth, weightLabel(volumeSummary.luna, isLbs), Modifier.weight(1f), isDark)
        }

        val trendPoints = oneRmTrend.filter { it.second > 0 }
        if (trendPoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            val best1rm = trendPoints.maxOf { it.second }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.estimatedOneRm, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = secondaryTextColor(), letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    weightLabel(best1rm, isLbs),
                    color = textColor(), fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = JetBrainsMono
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val max1rm = trendPoints.maxOf { it.second }
            Row(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                trendPoints.takeLast(12).forEach { (_, v) ->
                    val frac = if (max1rm > 0) (v / max1rm).toFloat().coerceIn(0.08f, 1f) else 0.08f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(frac)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(accentColor().copy(alpha = 0.55f))
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String, modifier: Modifier = Modifier, isDark: Boolean = false) {
    val pillBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    val pillBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(pillBg)
            .border(1.dp, pillBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = secondaryTextColor(), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(value, color = textColor(), fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = JetBrainsMono)
    }
}

// ============================================
// Componenta: Timer Pauza
// ============================================
@Composable
fun ExerciseHistoryCard(
    history: List<ExercitiuEntity>,
    isLbs: Boolean = false,
    isDark: Boolean = false,
    onEdit: (ExercitiuEntity) -> Unit,
    onDelete: (ExercitiuEntity) -> Unit
) {
    val strings = LanguageManager.getStrings(LocalContext.current)
    // Iconițele DarkRed (#6D0122) sunt invizibile pe fundalul închis → roșu deschis în dark mode
    val cardBg = if (isDark) Color(0xFF0D0D0D) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val deleteTint = if (isDark) EmberLight else DarkRed
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
            .padding(22.dp)
    ) {
        Text(strings.exerciseHistory, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = secondaryTextColor(), letterSpacing = 0.5.sp)
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
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (set.notes.isNotBlank()) {
                            Text(set.notes, color = secondaryTextColor(), fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = { onEdit(set) }) {
                        Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = accentColor())
                    }
                    IconButton(onClick = { onDelete(set) }) {
                        Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = deleteTint)
                    }
                }
                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.05f) else dividerColor().copy(alpha = 0.5f))
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
    var kgText by remember(set.id) { mutableStateOf(String.format(java.util.Locale.ROOT, "%.1f", displayWeight)) }
    var repsText by remember(set.id) { mutableStateOf(set.repetari.toString()) }
    var noteText by remember(set.id) { mutableStateOf(set.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isSystemInDarkTheme()) Color(0xFF0D0D0D) else Color(0xFFFFFFFF),
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
                    label = { Text(strings.reps) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(strings.notes) },
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

@Composable
fun DailyActivityCard(
    strings: LanguageManager.Strings,
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
    onSetStepGoal: ((Int) -> Unit)? = null
) {
    val activeMinutes = todayDurationMs / 60000
    var showSetGoalDialog by remember { mutableStateOf(false) }
    var goalInput by remember { mutableStateOf("") }
    var currentStepGoal by remember { mutableIntStateOf(stepGoal) }

    if (showSetGoalDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSetGoalDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E1E1E),
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        strings.setStepGoal,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5F3EE)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        strings.enterDailyStepGoal,
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("e.g. 7000", color = textSecondary.copy(alpha = 0.5f), fontFamily = JetBrainsMono) },
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
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showSetGoalDialog = false }) {
                            Text(strings.cancel, color = accent)
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val goal = goalInput.toIntOrNull() ?: 0
                                if (goal > 0) {
                                    currentStepGoal = goal
                                    onSetStepGoal?.invoke(goal)
                                    showSetGoalDialog = false
                                }
                            },
                            modifier = Modifier.background(RedButtonGradient, RoundedCornerShape(10.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(strings.save, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    // Culori: roșu = pași, verde = timp activ, albastru = calorii
    val calColor = Color(0xFF0A84FF)
    val timeColor = Color(0xFF34C759)
    val stepsColor = Color(0xFFFF3B30)

    val calProgress by animateFloatAsState(
        targetValue = (todayCalories.toFloat() / calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(1200),
        label = "calProgress"
    )
    val timeProgress by animateFloatAsState(
        targetValue = (activeMinutes.toFloat() / activeTimeGoalMin.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(1200),
        label = "timeProgress"
    )
    val stepsProgress by animateFloatAsState(
        targetValue = (stepsEstimate.toFloat() / currentStepGoal.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(1200),
        label = "stepsProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, if (isDark) Color(0x0FFFFFFF) else Color(0x14000000))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Inele concentrice (stânga) — fără text în mijloc ──
            Box(
                modifier = Modifier.size(128.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val strokeWidth = 12.dp.toPx()
                    val outerRadius = size.minDimension / 2 - strokeWidth / 2 - 3.dp.toPx()

                    fun ring(color: Color, radius: Float, progress: Float) {
                        val topLeft = Offset(centerX - radius, centerY - radius)
                        val ringSize = Size(radius * 2, radius * 2)
                        drawArc(
                            color = color.copy(alpha = 0.15f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = ringSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = color,
                            startAngle = 135f,
                            sweepAngle = 270f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = ringSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Exterior: pași (roșu, mare) · mijloc: timp activ (verde) · interior: calorii (albastru, mic)
                    ring(stepsColor, outerRadius, stepsProgress)
                    ring(timeColor, outerRadius - strokeWidth - 6.dp.toPx(), timeProgress)
                    ring(calColor, outerRadius - 2 * (strokeWidth + 6.dp.toPx()), calProgress)
                }
            }

            Spacer(Modifier.width(20.dp))

            // ── Rânduri metrici (dreapta), ca în widget ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DailyMetricRow(
                    color = stepsColor,
                    label = strings.stepsLabel.uppercase(),
                    valueText = String.format(Locale.US, "%,d", stepsEstimate),
                    goalText = "/ ${formatStepGoalShort(currentStepGoal)}",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    goalLabel = strings.plusGoal,
                    onSetGoal = onSetStepGoal?.let { { showSetGoalDialog = true } }
                )
                DailyMetricRow(
                    color = timeColor,
                    label = strings.activeTimeLabel.uppercase(),
                    valueText = "$activeMinutes",
                    unit = "min",
                    goalText = "/ $activeTimeGoalMin",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
                DailyMetricRow(
                    color = calColor,
                    label = strings.caloriesLabel.uppercase(),
                    valueText = todayCalories.toInt().toString(),
                    goalText = "/ $calorieGoal",
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        }
    }
}

@Composable
private fun DailyMetricRow(
    color: Color,
    label: String,
    valueText: String,
    goalText: String,
    textPrimary: Color,
    textSecondary: Color,
    unit: String? = null,
    goalLabel: String = "+ Goal",
    onSetGoal: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = valueText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = textPrimary,
                    fontFamily = JetBrainsMono,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (unit != null) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        color = textSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = goalText,
                    fontSize = 12.sp,
                    color = textSecondary,
                    fontFamily = JetBrainsMono,
                    modifier = Modifier.padding(start = 3.dp, bottom = 1.dp)
                )
                if (onSetGoal != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(color.copy(alpha = 0.12f))
                            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .clickable { onSetGoal() }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(goalLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                }
            }
        }
    }
}

private fun formatStepGoalShort(goal: Int): String = when {
    goal >= 1000 && goal % 1000 == 0 -> "${goal / 1000}K"
    goal >= 1000 -> String.format(Locale.US, "%.1fK", goal / 1000.0)
    else -> goal.toString()
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
                .border(2.dp, Color.Red, RoundedCornerShape(24.dp))
                .padding(horizontal = 32.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title.uppercase(),
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        }
    }
}

// ============================================
// StatItem (folosit de ExerciseInputScreen)
// ============================================

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

