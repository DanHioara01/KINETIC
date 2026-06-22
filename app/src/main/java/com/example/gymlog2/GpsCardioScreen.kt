package com.example.gymlog2

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.*
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class GpsPoint(val lat: Double, val lng: Double, val timestamp: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsCardioScreen(
    isDark: Boolean,
    strings: LanguageManager.Strings,
    userId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val surfaceBg = if (isDark) bgColor() else LightBackground
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary
    val cardBg = if (isDark) cardColor() else LightCard
    val accent = if (isDark) accentColor() else LightPrimaryRed

    var isTracking by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<GpsPoint>>(emptyList()) }
    var currentSpeed by remember { mutableDoubleStateOf(0.0) }
    var totalDistance by remember { mutableDoubleStateOf(0.0) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var savedRoutes by remember { mutableStateOf<List<CardioRouteEntity>>(emptyList()) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSavedRoutes by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var activityType by remember { mutableStateOf("running") }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val point = GpsPoint(location.latitude, location.longitude, System.currentTimeMillis())
                    val newPoints = routePoints + point
                    routePoints = newPoints
                    currentSpeed = location.speed * 3.6

                    lastLocation?.let { prev ->
                        val results = FloatArray(1)
                        Location.distanceBetween(prev.latitude, prev.longitude, location.latitude, location.longitude, results)
                        totalDistance += results[0] / 1000.0
                    }
                    lastLocation = location
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            isTracking = true
        }
    }

    LaunchedEffect(Unit) {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        hasLocationPermission = fine == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            coarse == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(isTracking) {
        if (isTracking && hasLocationPermission) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).apply {
                setMinUpdateDistanceMeters(5f)
                setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    LaunchedEffect(isTracking) {
        if (isTracking) {
            elapsedTime = 0
            totalDistance = 0.0
            routePoints = emptyList()
            lastLocation = null
            currentSpeed = 0.0
            while (isTracking) {
                delay(1000)
                elapsedTime += 1000
            }
        }
    }

    LaunchedEffect(userId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            savedRoutes = db.cardioRouteDao().getAllForUser(userId)
        }
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
        else String.format("%02d:%02d", minutes, seconds)
    }

    fun formatPace(speedKmh: Double): String {
        if (speedKmh <= 0) return "--:--"
        val paceMinPerKm = 60.0 / speedKmh
        val minutes = paceMinPerKm.toInt()
        val seconds = ((paceMinPerKm - minutes) * 60).toInt()
        return String.format("%d:%02d /km", minutes, seconds)
    }

    fun estimateCalories(distanceKm: Double, activityType: String): Double {
        val calPerKm = when (activityType) {
            "running" -> 70.0
            "cycling" -> 30.0
            "walking" -> 40.0
            else -> 50.0
        }
        return distanceKm * calPerKm
    }

    fun stopTracking() {
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    BackHandler {
        if (isTracking) {
            stopTracking()
        } else if (showSavedRoutes) {
            showSavedRoutes = false
        } else {
            onBack()
        }
    }

    if (showSaveDialog) {
        var routeName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = cardBg,
            titleContentColor = textPrimary,
            title = { Text(strings.saveRoute, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(strings.routeName, color = textSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = routeName,
                        onValueChange = { routeName = it },
                        placeholder = { Text(strings.routeName, color = textSecondary.copy(alpha = 0.5f)) },
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (routeName.isNotBlank()) {
                            scope.launch {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val route = CardioRouteEntity(
                                        userId = userId,
                                        name = routeName.trim(),
                                        routePoints = routePoints.joinToString(";") { "${it.lat},${it.lng}" },
                                        distanceKm = totalDistance,
                                        durationMs = elapsedTime,
                                        avgSpeedKmh = currentSpeed,
                                        avgPaceMinKm = if (currentSpeed > 0) 60.0 / currentSpeed else 0.0,
                                        caloriesBurned = estimateCalories(totalDistance, activityType),
                                        startTime = if (routePoints.isNotEmpty()) routePoints.first().timestamp else System.currentTimeMillis(),
                                        endTime = System.currentTimeMillis(),
                                        activityType = activityType
                                    )
                                    db.cardioRouteDao().insert(route)
                                    savedRoutes = db.cardioRouteDao().getAllForUser(userId)
                                }
                            }
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.save, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(strings.cancel, color = accent)
                }
            }
        )
    }

    Scaffold(
        containerColor = surfaceBg,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(strings.gpsCardioMap, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isTracking) stopTracking() else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = strings.back, tint = textPrimary)
                    }
                },
                actions = {
                    if (!isTracking && routePoints.isNotEmpty()) {
                        IconButton(onClick = { showSavedRoutes = !showSavedRoutes }) {
                            Icon(Icons.Default.Route, contentDescription = strings.savedRoutes, tint = accent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceBg,
                    titleContentColor = textPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (showSavedRoutes) {
                SavedRoutesSection(
                    routes = savedRoutes,
                    isDark = isDark,
                    accent = accent,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    strings = strings,
                    onDelete = { route ->
                        scope.launch {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                val db = AppDatabase.getDatabase(context)
                                db.cardioRouteDao().delete(route)
                                savedRoutes = db.cardioRouteDao().getAllForUser(userId)
                            }
                        }
                    }
                )
            } else {
                // Activity type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activities = listOf("running" to Icons.Default.DirectionsRun, "cycling" to Icons.Default.DirectionsBike, "walking" to Icons.Default.DirectionsWalk)
                    activities.forEach { (type, icon) ->
                        val isActive = activityType == type
                        FilterChip(
                            selected = isActive,
                            onClick = { if (!isTracking) activityType = type },
                            label = { Text(type.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accent.copy(alpha = 0.15f),
                                selectedLabelColor = accent,
                                selectedLeadingIconColor = accent
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Map area with route drawing
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        RouteCanvas(
                            points = routePoints,
                            isTracking = isTracking,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (routePoints.isEmpty() && !isTracking) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = null,
                                    tint = textSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    strings.currentLocation,
                                    color = textSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (isTracking) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .background(accent, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        strings.trackingActive,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        value = String.format("%.1f", totalDistance),
                        unit = "km",
                        label = strings.distance,
                        cardBg = cardBg,
                        accent = accent,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Timer,
                        value = formatDuration(elapsedTime),
                        unit = "",
                        label = strings.duration,
                        cardBg = cardBg,
                        accent = accent,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        value = String.format("%.1f", currentSpeed),
                        unit = "km/h",
                        label = strings.speed,
                        cardBg = cardBg,
                        accent = accent,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        value = formatPace(currentSpeed),
                        unit = "",
                        label = strings.pace,
                        cardBg = cardBg,
                        accent = accent,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocalFireDepartment,
                        value = String.format("%.0f", estimateCalories(totalDistance, activityType)),
                        unit = "kcal",
                        label = strings.calories,
                        cardBg = cardBg,
                        accent = accent,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FitnessCenter,
                        value = routePoints.size.toString(),
                        unit = "pts",
                        label = "Route Points",
                        cardBg = cardBg,
                        accent = accent,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Control buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isTracking && routePoints.isEmpty()) {
                        Button(
                            onClick = {
                                if (hasLocationPermission) {
                                    isTracking = true
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.startTracking, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else if (isTracking) {
                        Button(
                            onClick = { stopTracking() },
                            colors = ButtonDefaults.buttonColors(containerColor = RecoveryRed),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.stopTracking, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                routePoints = emptyList()
                                totalDistance = 0.0
                                elapsedTime = 0
                                currentSpeed = 0.0
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            border = BorderStroke(1.5.dp, textSecondary.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = textSecondary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.cancel, color = textSecondary, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showSaveDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.saveRoute, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RouteCanvas(
    points: List<GpsPoint>,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    val redRoute = Color(0xFFFF2D2D)
    val gridColor = Color(0xFF1A3A4A).copy(alpha = 0.3f)

    Canvas(modifier = modifier) {
        // Draw grid pattern (map feel)
        val gridSize = 40.dp.toPx()
        for (x in 0f..size.width step gridSize) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.5.dp.toPx())
        }
        for (y in 0f..size.height step gridSize) {
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5.dp.toPx())
        }

        if (points.size >= 2) {
            // Calculate bounds
            val minLat = points.minOf { it.lat }
            val maxLat = points.maxOf { it.lat }
            val minLng = points.minOf { it.lng }
            val maxLng = points.maxOf { it.lng }

            val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
            val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

            val padding = 30.dp.toPx()
            val drawWidth = size.width - padding * 2
            val drawHeight = size.height - padding * 2

            fun latToY(lat: Double) = padding + drawHeight - ((lat - minLat) / latRange * drawHeight).toFloat()
            fun lngToX(lng: Double) = padding + ((lng - minLng) / lngRange * drawWidth).toFloat()

            // Draw route shadow
            val shadowPath = Path()
            shadowPath.moveTo(lngToX(points[0].lng), latToY(points[0].lat))
            for (i in 1 until points.size) {
                shadowPath.lineTo(lngToX(points[i].lng), latToY(points[i].lat))
            }
            drawPath(shadowPath, redRoute.copy(alpha = 0.2f), style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Draw route
            val routePath = Path()
            routePath.moveTo(lngToX(points[0].lng), latToY(points[0].lat))
            for (i in 1 until points.size) {
                routePath.lineTo(lngToX(points[i].lng), latToY(points[i].lat))
            }
            drawPath(routePath, redRoute, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Draw start point
            drawCircle(
                Color(0xFF4CAF50),
                radius = 10.dp.toPx(),
                center = Offset(lngToX(points.first().lng), latToY(points.first().lat))
            )
            drawCircle(
                Color.White,
                radius = 5.dp.toPx(),
                center = Offset(lngToX(points.first().lng), latToY(points.first().lat))
            )

            // Draw current position
            if (isTracking && points.isNotEmpty()) {
                val last = points.last()
                drawCircle(
                    redRoute,
                    radius = 12.dp.toPx(),
                    center = Offset(lngToX(last.lng), latToY(last.lat))
                )
                drawCircle(
                    Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(lngToX(last.lng), latToY(last.lat))
                )
            }
        } else if (isTracking) {
            // Pulsing indicator while waiting for first point
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulse by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAnim"
            )
            drawCircle(
                redRoute.copy(alpha = pulse),
                radius = 16.dp.toPx(),
                center = Offset(size.width / 2, size.height / 2)
            )
            drawCircle(
                Color.White,
                radius = 8.dp.toPx(),
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    unit: String,
    label: String,
    cardBg: Color,
    accent: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = textPrimary
                    )
                    if (unit.isNotEmpty()) {
                        Spacer(Modifier.width(3.dp))
                        Text(
                            unit,
                            fontSize = 11.sp,
                            color = textSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
                Text(label, fontSize = 11.sp, color = textSecondary)
            }
        }
    }
}

@Composable
private fun SavedRoutesSection(
    routes: List<CardioRouteEntity>,
    isDark: Boolean,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    strings: LanguageManager.Strings,
    onDelete: (CardioRouteEntity) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            strings.savedRoutes.uppercase(),
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = accent,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        if (routes.isEmpty()) {
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
                    Icon(Icons.Default.Route, contentDescription = null, tint = textSecondary.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(strings.noSavedRoutes, color = textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            routes.forEach { route ->
                var showDeleteConfirm by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (route.activityType) {
                                    "cycling" -> Icons.Default.DirectionsBike
                                    "walking" -> Icons.Default.DirectionsWalk
                                    else -> Icons.Default.DirectionsRun
                                },
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(route.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Spacer(Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("${String.format("%.2f", route.distanceKm)} km", fontSize = 11.sp, color = textSecondary)
                                Text(formatDurationShort(route.durationMs), fontSize = 11.sp, color = textSecondary)
                                Text("${String.format("%.0f", route.caloriesBurned)} kcal", fontSize = 11.sp, color = textSecondary)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(route.startTime)),
                                fontSize = 10.sp,
                                color = textSecondary.copy(alpha = 0.6f)
                            )
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = RecoveryRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        containerColor = cardBg,
                        titleContentColor = textPrimary,
                        title = { Text(strings.deleteRoute, fontWeight = FontWeight.Bold) },
                        confirmButton = {
                            Button(
                                onClick = { onDelete(route); showDeleteConfirm = false },
                                colors = ButtonDefaults.buttonColors(containerColor = RecoveryRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(strings.delete, color = androidx.compose.ui.graphics.Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text(strings.cancel, color = accent)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun formatDurationShort(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}
