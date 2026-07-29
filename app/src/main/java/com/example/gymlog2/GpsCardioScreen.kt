package com.example.gymlog2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlog2.ui.theme.RecoveryRed
import com.example.gymlog2.ui.theme.accentColor
import com.example.gymlog2.ui.theme.bgColor
import com.example.gymlog2.ui.theme.cardColor
import com.example.gymlog2.ui.theme.secondaryTextColor
import com.example.gymlog2.ui.theme.textColor
import com.example.gymlog2.ui.theme.LightBackground
import com.example.gymlog2.ui.theme.LightCard
import com.example.gymlog2.ui.theme.LightPrimaryRed
import com.example.gymlog2.ui.theme.LightTextPrimary
import com.example.gymlog2.ui.theme.LightTextSecondary
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val gpsState = GpsTrackingState
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var savedRoutes by remember { mutableStateOf<List<CardioRouteEntity>>(emptyList()) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSavedRoutes by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf<CardioRouteEntity?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var locationStatus by remember { mutableStateOf("Caut semnal GPS...") }
    var showGpsDisabledDialog by remember { mutableStateOf(false) }
    var showLocationDeniedDialog by remember { mutableStateOf(false) }
    var pendingStartAfterPermission by remember { mutableStateOf(false) }

    val locationManager = remember {
        context.getSystemService(LocationManager::class.java)
    }

    val sensorManager = remember {
        context.getSystemService(SensorManager::class.java)
    }
    val rotationMatrix = remember { FloatArray(9) }
    val orientationAngles = remember { FloatArray(3) }

    DisposableEffect(gpsState.isTracking) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!gpsState.isTracking) return
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuth = -Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                gpsState.bearing = azimuth
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (gpsState.isTracking) {
            val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            rotationSensor?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    fun checkGpsEnabled(): Boolean {
        val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        val networkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        return gpsEnabled || networkEnabled
    }

    val locationUpdateCallback = remember { mutableStateOf<com.google.android.gms.location.LocationCallback?>(null) }
    val handler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    val timerRunnable = remember {
        object : Runnable {
            override fun run() {
                if (GpsTrackingState.isTracking) {
                    GpsTrackingState.elapsedTime += 1000
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTrackingService() {
        try {
            GpsTrackingState.isTracking = true
            GpsTrackingState.isPaused = false
            GpsTrackingState.elapsedTime = 0L
            GpsTrackingState.totalDistance = 0.0
            GpsTrackingState.routePoints = emptyList()
            GpsTrackingState.currentSpeed = 0.0
            GpsTrackingState.lastRouteLocation = null

            val request = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000L
            ).apply {
                setMinUpdateDistanceMeters(0f)
                setGranularity(com.google.android.gms.location.Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()

            locationUpdateCallback.value = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    result.lastLocation?.let { loc ->
                        GpsTrackingState.lastLocation = loc
                        if (loc.hasSpeed()) {
                            GpsTrackingState.currentSpeed = loc.speed * 3.6
                        } else {
                            val prev = GpsTrackingState.lastRouteLocation
                            if (prev != null) {
                                val dt = (System.currentTimeMillis() - prev.time) / 1000.0
                                if (dt > 0) {
                                    val dist = prev.distanceTo(loc).toDouble()
                                    GpsTrackingState.currentSpeed = (dist / dt) * 3.6
                                }
                            }
                        }
                        val prev = GpsTrackingState.lastRouteLocation
                        if (prev != null) {
                            val dist = prev.distanceTo(loc)
                            if (dist > 2f) {
                                GpsTrackingState.totalDistance += dist / 1000.0
                                GpsTrackingState.routePoints = GpsTrackingState.routePoints + GpsPoint(loc.latitude, loc.longitude, System.currentTimeMillis())
                            }
                        } else {
                            GpsTrackingState.routePoints = listOf(GpsPoint(loc.latitude, loc.longitude, System.currentTimeMillis()))
                        }
                        GpsTrackingState.lastRouteLocation = loc
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(request, locationUpdateCallback.value!!, android.os.Looper.getMainLooper())

            handler.removeCallbacks(timerRunnable)
            handler.postDelayed(timerRunnable, 1000)
        } catch (e: Exception) {
            GpsTrackingState.isTracking = false
            android.widget.Toast.makeText(context, "Eroare GPS: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun stopTrackingService() {
        locationUpdateCallback.value?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationUpdateCallback.value = null
        handler.removeCallbacks(timerRunnable)
        GpsTrackingState.isTracking = false
    }

    fun pauseTrackingService() {
        locationUpdateCallback.value?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationUpdateCallback.value = null
        handler.removeCallbacks(timerRunnable)
        GpsTrackingState.isTracking = false
        GpsTrackingState.isPaused = true
    }

    @SuppressLint("MissingPermission")
    fun resumeTrackingService() {
        try {
            GpsTrackingState.isTracking = true
            GpsTrackingState.isPaused = false

            val request = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000L
            ).apply {
                setMinUpdateDistanceMeters(0f)
                setGranularity(com.google.android.gms.location.Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()

            locationUpdateCallback.value = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    result.lastLocation?.let { loc ->
                        GpsTrackingState.lastLocation = loc
                        if (loc.hasSpeed()) {
                            GpsTrackingState.currentSpeed = loc.speed * 3.6
                        } else {
                            val prev = GpsTrackingState.lastRouteLocation
                            if (prev != null) {
                                val dt = (System.currentTimeMillis() - prev.time) / 1000.0
                                if (dt > 0) {
                                    val dist = prev.distanceTo(loc).toDouble()
                                    GpsTrackingState.currentSpeed = (dist / dt) * 3.6
                                }
                            }
                        }
                        val prev = GpsTrackingState.lastRouteLocation
                        if (prev != null) {
                            val dist = prev.distanceTo(loc)
                            if (dist > 2f) {
                                GpsTrackingState.totalDistance += dist / 1000.0
                                GpsTrackingState.routePoints = GpsTrackingState.routePoints + GpsPoint(loc.latitude, loc.longitude, System.currentTimeMillis())
                            }
                        } else {
                            GpsTrackingState.routePoints = GpsTrackingState.routePoints + GpsPoint(loc.latitude, loc.longitude, System.currentTimeMillis())
                        }
                        GpsTrackingState.lastRouteLocation = loc
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(request, locationUpdateCallback.value!!, android.os.Looper.getMainLooper())

            handler.removeCallbacks(timerRunnable)
            handler.postDelayed(timerRunnable, 1000)
        } catch (e: Exception) {
            GpsTrackingState.isTracking = false
            android.widget.Toast.makeText(context, "Eroare GPS: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun cancelTrackingService() {
        locationUpdateCallback.value?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationUpdateCallback.value = null
        handler.removeCallbacks(timerRunnable)
        GpsTrackingState.reset()
    }

    fun refreshPermissionAndGps() {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        hasLocationPermission = fine == PackageManager.PERMISSION_GRANTED ||
            coarse == PackageManager.PERMISSION_GRANTED
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (pendingStartAfterPermission) {
            pendingStartAfterPermission = false
            if (hasLocationPermission && checkGpsEnabled()) {
                startTrackingService()
            } else if (hasLocationPermission) {
                showGpsDisabledDialog = true
            } else {
                locationStatus = strings.locationPermissionRequired
            }
        }
    }

    fun tryStartTracking() {
        refreshPermissionAndGps()

        if (!hasLocationPermission) {
            pendingStartAfterPermission = true
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else if (!checkGpsEnabled()) {
            showGpsDisabledDialog = true
        } else {
            startTrackingService()
        }
    }

    fun tryResumeTracking() {
        refreshPermissionAndGps()

        if (!hasLocationPermission) {
            pendingStartAfterPermission = true
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else if (!checkGpsEnabled()) {
            showGpsDisabledDialog = true
        } else {
            resumeTrackingService()
        }
    }

    LaunchedEffect(hasLocationPermission) {
        refreshPermissionAndGps()
        if (hasLocationPermission && gpsState.lastLocation == null) {
            try {
                @SuppressLint("MissingPermission")
                val loc = fusedLocationClient.lastLocation.await()
                if (loc != null && gpsState.lastLocation == null) {
                    gpsState.lastLocation = loc
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(showGpsDisabledDialog) {
        if (!showGpsDisabledDialog) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(1000)
            refreshPermissionAndGps()
            if (hasLocationPermission && checkGpsEnabled()) {
                showGpsDisabledDialog = false
                startTrackingService()
                break
            }
        }
    }

    LaunchedEffect(showLocationDeniedDialog) {
        if (!showLocationDeniedDialog) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(1000)
            refreshPermissionAndGps()
            if (hasLocationPermission) {
                showLocationDeniedDialog = false
                if (checkGpsEnabled()) {
                    startTrackingService()
                } else {
                    showGpsDisabledDialog = true
                }
                break
            }
        }
    }

    LaunchedEffect(userId) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            savedRoutes = db.cardioRouteDao().getAllForUser(userId)
        }
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        else String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun formatPace(speedKmh: Double): String {
        if (speedKmh <= 0) return "--:--"
        val paceMinPerKm = 60.0 / speedKmh
        val minutes = paceMinPerKm.toInt()
        val seconds = ((paceMinPerKm - minutes) * 60).toInt()
        return String.format(Locale.US, "%d:%02d /km", minutes, seconds)
    }

    fun estimateCalories(distanceKm: Double, type: String): Double {
        val calPerKm = when (type) {
            "running" -> 70.0
            "cycling" -> 30.0
            "walking" -> 40.0
            else -> 50.0
        }
        return distanceKm * calPerKm
    }

    BackHandler {
        if (selectedRoute != null) {
            selectedRoute = null
        } else if (gpsState.isTracking) {
            val activity = context as? MainActivity
            activity?.enterPipMode()
        } else if (showSavedRoutes) {
            showSavedRoutes = false
        } else {
            onBack()
        }
    }

    if (gpsState.isInPipMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                RouteCanvas(
                    points = gpsState.routePoints,
                    isTracking = gpsState.isTracking,
                    lastLocation = gpsState.lastLocation,
                    bearing = gpsState.bearing,
                    forceCenterOnLocation = gpsState.lastLocation != null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161616))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        String.format(Locale.US, "%.1f", gpsState.currentSpeed),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text("km/h", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        String.format(Locale.US, "%.2f", gpsState.totalDistance),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(strings.distance, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatDuration(gpsState.elapsedTime),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(strings.duration, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
        }
        return
    }

    if (showGpsDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDisabledDialog = false },
            containerColor = cardBg,
            titleContentColor = textPrimary,
            title = { Text(strings.gpsDisabledTitle, fontWeight = FontWeight.Bold) },
            text = {
                Text(strings.gpsDisabledMessage, color = textSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGpsDisabledDialog = false
                        try {
                            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.openSettings, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDisabledDialog = false }) {
                    Text(strings.cancel, color = accent)
                }
            }
        )
    }

    if (showLocationDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDeniedDialog = false },
            containerColor = cardBg,
            titleContentColor = textPrimary,
            title = { Text(strings.locationPermissionRequired, fontWeight = FontWeight.Bold) },
            text = {
                Text(strings.gpsDisabledMessage, color = textSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationDeniedDialog = false
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = android.net.Uri.fromParts("package", context.packageName, null)
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.openSettings, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDeniedDialog = false }) {
                    Text(strings.cancel, color = accent)
                }
            }
        )
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
                                withContext(Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(context)
                                    val route = CardioRouteEntity(
                                        userId = userId,
                                        name = routeName.trim(),
                                        routePoints = gpsState.routePoints.joinToString(";") { "${it.lat},${it.lng}" },
                                        distanceKm = gpsState.totalDistance,
                                        durationMs = gpsState.elapsedTime,
                                        avgSpeedKmh = gpsState.currentSpeed,
                                        avgPaceMinKm = if (gpsState.currentSpeed > 0) 60.0 / gpsState.currentSpeed else 0.0,
                                        caloriesBurned = estimateCalories(gpsState.totalDistance, gpsState.activityType),
                                        startTime = if (gpsState.routePoints.isNotEmpty()) gpsState.routePoints.first().timestamp else System.currentTimeMillis(),
                                        endTime = System.currentTimeMillis(),
                                        activityType = gpsState.activityType
                                    )
                                    val prefs = PreferencesManager(context)
                                    val syncRepo = SyncRepository(db, NetworkClient.api, prefs)
                                    syncRepo.saveCardioRoute(route)
                                    savedRoutes = db.cardioRouteDao().getAllForUser(userId)
                                }
                            }
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(strings.save, color = Color.White, fontWeight = FontWeight.SemiBold)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (selectedRoute != null) selectedRoute!!.name else "Cardio",
                            fontWeight = FontWeight.Bold
                        )
                        if (gpsState.isTracking) {
                            Spacer(Modifier.width(8.dp))
                            val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
                                initialValue = 1f,
                                targetValue = 0.3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseAlpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(RecoveryRed.copy(alpha = pulseAlpha))
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedRoute != null) {
                            selectedRoute = null
                        } else if (gpsState.isTracking || gpsState.isPaused) cancelTrackingService() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = textPrimary)
                    }
                },
                actions = {
                    if (selectedRoute == null && !gpsState.isTracking) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (selectedRoute != null) {
                        RouteDetailContent(
                            route = selectedRoute!!,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            strings = strings
                        )
                    } else if (showSavedRoutes) {
                        SavedRoutesSection(
                            routes = savedRoutes,
                            accent = accent,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            strings = strings,
                            onRouteClick = { route -> selectedRoute = route },
                            onDelete = { route ->
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        val db = AppDatabase.getDatabase(context)
                                        db.cardioRouteDao().delete(route)
                                        savedRoutes = db.cardioRouteDao().getAllForUser(userId)
                                    }
                                }
                            }
                        )
                    } else {
                        AnimatedVisibility(
                            visible = !gpsState.isTracking || gpsState.routePoints.isEmpty(),
                            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val activities = remember { listOf("running" to Icons.Default.DirectionsRun, "cycling" to Icons.Default.DirectionsBike, "walking" to Icons.Default.DirectionsWalk) }
                                activities.forEach { (type, icon) ->
                                    val isActive = gpsState.activityType == type
                                    FilterChip(
                                        selected = isActive,
                                        onClick = { if (!gpsState.isTracking) gpsState.activityType = type },
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
                        }

                        val mapCornerRadius = 12.dp
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(mapCornerRadius),
                            colors = CardDefaults.cardColors(containerColor = cardBg)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                RouteCanvas(
                                    points = gpsState.routePoints,
                                    isTracking = gpsState.isTracking,
                                    lastLocation = gpsState.lastLocation,
                                    bearing = gpsState.bearing,
                                    forceCenterOnLocation = gpsState.lastLocation != null,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (gpsState.lastLocation == null || !hasLocationPermission) {
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBg.copy(alpha = 0.92f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            locationStatus,
                                            color = textPrimary,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val normalizedBearing = ((gpsState.bearing % 360) + 360) % 360
                                    val direction = when {
                                        normalizedBearing < 45 || normalizedBearing >= 315 -> "N"
                                        normalizedBearing < 135 -> "E"
                                        normalizedBearing < 225 -> "S"
                                        else -> "W"
                                    }
                                    Text(
                                        direction,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompactStat(
                                modifier = Modifier.weight(1f),
                                value = String.format(Locale.US, "%.2f", gpsState.totalDistance),
                                unit = "km",
                                label = strings.distance,
                                icon = painterResource(id = R.drawable.distance)
                            )
                            CompactStat(
                                modifier = Modifier.weight(1f),
                                value = formatDuration(gpsState.elapsedTime),
                                unit = "",
                                label = strings.duration,
                                icon = Icons.Default.Timer
                            )
                            CompactStat(
                                modifier = Modifier.weight(1f),
                                value = String.format(Locale.US, "%.1f", gpsState.currentSpeed),
                                unit = "km/h",
                                label = strings.speed,
                                icon = Icons.Default.Speed
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (gpsState.isTracking) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF616161))
                                            .clickable { cancelTrackingService() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(strings.cancel, color = Color(0xFF9E9E9E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFC107))
                                            .clickable { pauseTrackingService() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(strings.pauseTracking, color = Color(0xFFFFC107), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            } else if (gpsState.isPaused) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF616161))
                                            .clickable { cancelTrackingService() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(strings.cancel, color = Color(0xFF9E9E9E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFC107))
                                            .clickable { tryResumeTracking() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(strings.resumeTracking, color = Color(0xFFFFC107), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(RecoveryRed)
                                            .clickable { showSaveDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(strings.save, color = RecoveryRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Button(
                                    onClick = { tryStartTracking() },
                                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.startTracking, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
    }
}


@Composable
private fun CompactStat(
    modifier: Modifier = Modifier,
    value: String,
    unit: String,
    label: String,
    icon: androidx.compose.ui.graphics.painter.Painter
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                if (unit.isNotEmpty()) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        unit,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }
            }
            Text(
                label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
@Composable
private fun RouteCanvas(
    points: List<GpsPoint>,
    isTracking: Boolean,
    lastLocation: Location?,
    bearing: Float = 0f,
    forceCenterOnLocation: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapViewRef = remember { mutableStateOf<org.osmdroid.views.MapView?>(null) }

    AndroidView(
        factory = { ctx ->
            org.osmdroid.config.Configuration.getInstance().load(
                ctx,
                ctx.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
            )
            org.osmdroid.config.Configuration.getInstance().userAgentValue = ctx.packageName

            org.osmdroid.views.MapView(ctx).apply {
                setMultiTouchControls(true)
                setBuiltInZoomControls(false)
                minZoomLevel = 3.0
                maxZoomLevel = 20.0
                setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)

                val defaultGeoPoint = org.osmdroid.util.GeoPoint(44.4268, 26.1025)
                controller.setZoom(15.0)
                controller.setCenter(defaultGeoPoint)

                mapViewRef.value = this
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            val currentPoint = lastLocation?.let { org.osmdroid.util.GeoPoint(it.latitude, it.longitude) }

            if (forceCenterOnLocation && currentPoint != null) {
                if (points.size >= 2) {
                    val geoPoints = points.map { org.osmdroid.util.GeoPoint(it.lat, it.lng) }

                    val routeShadow = org.osmdroid.views.overlay.Polyline().apply {
                        outlinePaint.color = android.graphics.Color.argb(95, 20, 20, 20)
                        outlinePaint.strokeWidth = 15f
                        outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                        outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        setPoints(geoPoints)
                    }
                    mapView.overlays.add(routeShadow)

                    val routeLine = org.osmdroid.views.overlay.Polyline().apply {
                        outlinePaint.color = android.graphics.Color.rgb(255, 45, 45)
                        outlinePaint.strokeWidth = 8f
                        outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                        outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                        setPoints(geoPoints)
                    }
                    mapView.overlays.add(routeLine)
                }

                if (isTracking) {
                    val marker = org.osmdroid.views.overlay.Marker(mapView)
                    marker.position = currentPoint
                    marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    val arrowBmp = createArrowBitmap(android.graphics.Color.rgb(0, 122, 255), 28)
                    marker.icon = android.graphics.drawable.BitmapDrawable(mapView.context.resources, arrowBmp)
                    marker.setRotation(bearing)
                    mapView.overlays.add(marker)
                }

                mapView.controller.setZoom(17.0)
                mapView.controller.setCenter(currentPoint)
            } else if (points.size >= 2) {
                val geoPoints = points.map { org.osmdroid.util.GeoPoint(it.lat, it.lng) }

                val routeShadow = org.osmdroid.views.overlay.Polyline().apply {
                    outlinePaint.color = android.graphics.Color.argb(95, 20, 20, 20)
                    outlinePaint.strokeWidth = 15f
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                    setPoints(geoPoints)
                }
                mapView.overlays.add(routeShadow)

                val routeLine = org.osmdroid.views.overlay.Polyline().apply {
                    outlinePaint.color = android.graphics.Color.rgb(255, 45, 45)
                    outlinePaint.strokeWidth = 8f
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                    setPoints(geoPoints)
                }
                mapView.overlays.add(routeLine)

                val ctx = mapView.context
                if (!isTracking) {
                    val startMarker = org.osmdroid.views.overlay.Marker(mapView)
                    startMarker.position = geoPoints.first()
                    startMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    startMarker.icon = android.graphics.drawable.BitmapDrawable(ctx.resources, createCircleBitmap(android.graphics.Color.rgb(76, 175, 80), 16))
                    mapView.overlays.add(startMarker)
                }

                if (!isTracking) {
                    val endMarker = org.osmdroid.views.overlay.Marker(mapView)
                    endMarker.position = geoPoints.last()
                    endMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    endMarker.icon = android.graphics.drawable.BitmapDrawable(ctx.resources, createCircleBitmap(android.graphics.Color.rgb(255, 45, 45), 16))
                    mapView.overlays.add(endMarker)
                }

                if (isTracking && currentPoint != null) {
                    mapView.controller.setZoom(17.0)
                    mapView.controller.animateTo(currentPoint)
                } else {
                    val bbox = org.osmdroid.util.BoundingBox.fromGeoPoints(geoPoints)
                    val center = bbox.center
                    mapView.controller.setCenter(center)
                    mapView.post {
                        mapView.zoomToBoundingBox(bbox.increaseByScale(2.0f), true)
                    }
                }
            } else if (points.size == 1) {
                val geoPoint = org.osmdroid.util.GeoPoint(points[0].lat, points[0].lng)
                mapView.controller.setZoom(16.0)
                if (isTracking) {
                    mapView.controller.animateTo(currentPoint ?: geoPoint)
                } else {
                    mapView.controller.setCenter(geoPoint)
                    val ctx = mapView.context
                    val marker = org.osmdroid.views.overlay.Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    marker.icon = android.graphics.drawable.BitmapDrawable(ctx.resources, createCircleBitmap(android.graphics.Color.rgb(255, 45, 45), 16))
                    mapView.overlays.add(marker)
                }
            } else if (currentPoint != null) {
                mapView.controller.setZoom(16.0)
                if (isTracking) {
                    mapView.controller.animateTo(currentPoint)
                } else {
                    mapView.controller.setCenter(currentPoint)
                }
            }

            currentPoint?.let { point ->
                val marker = org.osmdroid.views.overlay.Marker(mapView)
                marker.position = point
                marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                if (isTracking) {
                    val arrowBmp = createArrowBitmap(android.graphics.Color.rgb(0, 122, 255), 28)
                    marker.icon = android.graphics.drawable.BitmapDrawable(mapView.context.resources, arrowBmp)
                    marker.setRotation(bearing)
                } else if (points.isNotEmpty()) {
                    marker.icon = android.graphics.drawable.BitmapDrawable(
                        mapView.context.resources,
                        createCircleBitmap(android.graphics.Color.rgb(255, 45, 45), 16)
                    )
                }
                if (isTracking || points.isNotEmpty()) {
                    mapView.overlays.add(marker)
                }
            }

            val scaleBar = org.osmdroid.views.overlay.ScaleBarOverlay(mapView)
            scaleBar.setScaleBarOffset(16, 16)
            mapView.overlays.add(scaleBar)
            mapView.invalidate()
        },
        modifier = modifier
    )
}

private fun createCircleBitmap(color: Int, sizeDp: Int): Bitmap {
    val sizePx = sizeDp * 3
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = android.graphics.Paint().apply {
        this.color = color
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 2f, paint)
    return bitmap
}

private fun createArrowBitmap(color: Int, sizeDp: Int): Bitmap {
    val sizePx = sizeDp * 3
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = android.graphics.Paint().apply {
        this.color = color
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    val path = android.graphics.Path()
    val cx = sizePx / 2f
    val cy = sizePx / 2f
    path.moveTo(cx, cy - sizePx * 0.45f)
    path.lineTo(cx + sizePx * 0.35f, cy + sizePx * 0.35f)
    path.lineTo(cx, cy + sizePx * 0.15f)
    path.lineTo(cx - sizePx * 0.35f, cy + sizePx * 0.35f)
    path.close()
    canvas.drawPath(path, paint)
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f
    canvas.drawPath(path, paint)
    return bitmap
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
    textSecondary: Color,
    iconBgColor: Color = accent,
    isPrimary: Boolean = false,
    valueColor: Color = textPrimary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPrimary) Color(0xFF0D0D0D) else cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isPrimary) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isPrimary) 44.dp else 36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconBgColor,
                    modifier = Modifier.size(if (isPrimary) 22.dp else 18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        value,
                        fontSize = if (isPrimary) 30.sp else 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPrimary) Color.White else valueColor
                    )
                    if (unit.isNotEmpty()) {
                        Spacer(Modifier.width(3.dp))
                        Text(
                            unit,
                            fontSize = if (isPrimary) 13.sp else 11.sp,
                            color = if (isPrimary) Color.White.copy(alpha = 0.6f) else textSecondary,
                            modifier = Modifier.padding(bottom = if (isPrimary) 4.dp else 2.dp)
                        )
                    }
                }
                Text(
                    label,
                    fontSize = if (isPrimary) 12.sp else 11.sp,
                    color = if (isPrimary) Color.White.copy(alpha = 0.5f) else textSecondary
                )
            }
        }
    }
}

@Composable
private fun RouteDetailContent(
    route: CardioRouteEntity,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    strings: LanguageManager.Strings
) {
    val routePoints = remember(route) {
        route.routePoints.split(";").filter { it.contains(",") }.mapNotNull { pair ->
            val parts = pair.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) GpsPoint(lat, lng, 0L) else null
            } else null
        }
    }

    val stepsEstimate = (route.distanceKm * 1312).toInt().coerceIn(0, 99999)

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            strings.savedRoutes.uppercase(),
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = accent,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                RouteCanvas(
                    points = routePoints,
                    isTracking = false,
                    lastLocation = null,
                    modifier = Modifier.fillMaxSize()
                )
                if (routePoints.isEmpty()) {
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
                            "No route data",
                            color = textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Speed,
                value = String.format(Locale.US, "%.2f", route.distanceKm),
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
                value = formatDurationShort(route.durationMs),
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Speed,
                value = String.format(Locale.US, "%.1f", route.avgSpeedKmh),
                unit = "km/h",
                label = strings.speed,
                cardBg = cardBg,
                accent = accent,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalFireDepartment,
                value = String.format(Locale.US, "%.0f", route.caloriesBurned),
                unit = "kcal",
                label = strings.caloriesBurned,
                cardBg = cardBg,
                accent = accent,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DirectionsRun,
                value = "$stepsEstimate",
                unit = "",
                label = "Steps",
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

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    route.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(route.startTime)),
                        fontSize = 12.sp,
                        color = textSecondary
                    )
                    Text(
                        route.activityType.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        color = accent
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedRoutesSection(
    routes: List<CardioRouteEntity>,
    accent: Color,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    strings: LanguageManager.Strings,
    onRouteClick: (CardioRouteEntity) -> Unit,
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
                        .padding(bottom = 8.dp)
                        .clickable { onRouteClick(route) },
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
                                Text("${String.format(Locale.US, "%.2f", route.distanceKm)} km", fontSize = 11.sp, color = textSecondary)
                                Text(formatDurationShort(route.durationMs), fontSize = 11.sp, color = textSecondary)
                                Text("${String.format(Locale.US, "%.0f", route.caloriesBurned)} kcal", fontSize = 11.sp, color = textSecondary)
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
                                Text(strings.delete, color = Color.White)
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
    return if (hours > 0) String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    else String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
