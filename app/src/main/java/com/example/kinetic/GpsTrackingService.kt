package com.example.kinetic

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class GpsTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "gps_tracking"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.kinetic.STOP_TRACKING"
        const val ACTION_START = "com.example.kinetic.START_TRACKING"
        const val ACTION_PAUSE = "com.example.kinetic.PAUSE_TRACKING"
        const val ACTION_RESUME = "com.example.kinetic.RESUME_TRACKING"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var lastRouteLocation: Location? = null

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (GpsTrackingState.isTracking) {
                GpsTrackingState.elapsedTime += 1000
                updateNotification()
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopTracking()
                return START_NOT_STICKY
            }
            ACTION_PAUSE -> {
                pauseTracking()
                return START_STICKY
            }
            ACTION_RESUME -> {
                resumeTracking()
                return START_STICKY
            }
            ACTION_START -> {
                val activityType = intent.getStringExtra("activity_type") ?: "running"
                startTracking(activityType)
                return START_STICKY
            }
            null -> {
                // Process was killed and restarted by the system mid-session — bring the session back.
                return if (wasSessionActive()) {
                    resumeTracking()
                    START_STICKY
                } else {
                    START_NOT_STICKY
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking(activityType: String) {
        GpsTrackingState.isTracking = true
        GpsTrackingState.isPaused = false
        GpsTrackingState.activityType = activityType
        GpsTrackingState.elapsedTime = 0L
        GpsTrackingState.totalDistance = 0.0
        GpsTrackingState.routePoints = emptyList()
        GpsTrackingState.currentSpeed = 0.0
        GpsTrackingState.estimatedSteps = 0
        GpsTrackingState.hasNotifiedGoal = false
        GpsTrackingState.isServiceRunning = true
        lastRouteLocation = null

        // Load the user's step goal so the notification shows the right target
        try {
            val upm = UserProfileManager(this)
            GpsTrackingState.stepGoal = PreferencesManager(this, upm).getStepGoal().coerceAtLeast(1)
        } catch (_: Exception) {}

        try {
            goForeground()
        } catch (e: Exception) {
            GpsTrackingState.isTracking = false
            GpsTrackingState.isServiceRunning = false
            return
        }
        persistSession(active = true)

        registerLocationUpdates()
        handler.removeCallbacks(timerRunnable)
        handler.postDelayed(timerRunnable, 1000)
    }

    private fun goForeground() {
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(formatDuration(GpsTrackingState.elapsedTime), "0.0"),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification(formatDuration(GpsTrackingState.elapsedTime), "0.0"))
        }
    }

    private fun persistSession(active: Boolean) {
        getSharedPreferences("gps_session", MODE_PRIVATE).edit()
            .putBoolean("active", active)
            .putString("activity_type", GpsTrackingState.activityType)
            .apply()
    }

    private fun wasSessionActive(): Boolean =
        getSharedPreferences("gps_session", MODE_PRIVATE).getBoolean("active", false)

    @SuppressLint("MissingPermission")
    private fun registerLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).apply {
            setMinUpdateDistanceMeters(0f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location -> processLocation(location) }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    private fun resumeTracking() {
        GpsTrackingState.isTracking = true
        GpsTrackingState.isPaused = false
        GpsTrackingState.isServiceRunning = true
        try {
            // Required: resume may arrive via startForegroundService when the service was restarted
            // by the system, and a location FGS must always be in the foreground state.
            goForeground()
            registerLocationUpdates()
            handler.removeCallbacks(timerRunnable)
            handler.postDelayed(timerRunnable, 1000)
            persistSession(active = true)
            updateNotification()
        } catch (e: Exception) {
            GpsTrackingState.isTracking = false
            stopTracking()
        }
    }

    private fun pauseTracking() {
        GpsTrackingState.isTracking = false
        GpsTrackingState.isPaused = true
        handler.removeCallbacks(timerRunnable)
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
        lastRouteLocation = null
        // Keep the foreground service alive — show a Paused state in the notification
        try {
            val nm = getSystemService(NotificationManager::class.java)
            nm.notify(NOTIFICATION_ID, buildNotification(formatDuration(GpsTrackingState.elapsedTime), "0.0"))
        } catch (_: Exception) {}
        persistSession(active = true)
    }

    @SuppressLint("MissingPermission")
    private fun processLocation(location: Location) {
        GpsTrackingState.lastLocation = location

        if (location.hasSpeed()) {
            GpsTrackingState.currentSpeed = location.speed * 3.6
        }

        if (location.hasBearing()) {
            GpsTrackingState.bearing = location.bearing
        } else if (GpsTrackingState.routePoints.isNotEmpty()) {
            val prev = GpsTrackingState.routePoints.last()
            val results = FloatArray(1)
            Location.distanceBetween(prev.lat, prev.lng, location.latitude, location.longitude, results)
            if (results[0] >= 1f) {
                val bearing = Math.toDegrees(
                    Math.atan2(
                        (location.longitude - prev.lng) * Math.cos(Math.toRadians(location.latitude)),
                        location.latitude - prev.lat
                    )
                ).toFloat()
                GpsTrackingState.bearing = if (bearing < 0) bearing + 360f else bearing
            }
        }

        if (GpsTrackingState.isTracking) {
            val point = GpsPoint(location.latitude, location.longitude, System.currentTimeMillis())
            val previousPoint = GpsTrackingState.routePoints.lastOrNull()
            val shouldAddPoint = previousPoint == null || run {
                val results = FloatArray(1)
                Location.distanceBetween(
                    previousPoint.lat, previousPoint.lng,
                    location.latitude, location.longitude,
                    results
                )
                results[0] >= 1f
            }

            if (shouldAddPoint) {
                GpsTrackingState.routePoints = GpsTrackingState.routePoints + point
            }

            lastRouteLocation?.let { prev ->
                val results = FloatArray(1)
                Location.distanceBetween(prev.latitude, prev.longitude, location.latitude, location.longitude, results)
                if (results[0] >= 1f) {
                    GpsTrackingState.totalDistance += results[0] / 1000.0
                }
            }
            lastRouteLocation = location

            // Calculate estimated steps (average stride ~0.762m)
            GpsTrackingState.estimatedSteps = (GpsTrackingState.totalDistance * 1000.0 / 0.762).toInt()

            // Check step goal notification
            if (GpsTrackingState.estimatedSteps >= GpsTrackingState.stepGoal && !GpsTrackingState.hasNotifiedGoal) {
                GpsTrackingState.hasNotifiedGoal = true
                showStepGoalNotification()
            }

            updateNotification()
        }
    }

    private fun stopTracking() {
        GpsTrackingState.isTracking = false
        GpsTrackingState.isPaused = false
        GpsTrackingState.isServiceRunning = false
        handler.removeCallbacks(timerRunnable)

        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
        lastRouteLocation = null

        persistSession(active = false)
        GpsTrackingState.reset()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        GpsTrackingState.isTracking = false
        GpsTrackingState.isServiceRunning = false
        handler.removeCallbacks(timerRunnable)
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            LanguageManager.getStrings(this).gpsChannelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "GPS tracking notification"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        // Step Goal channel is created by StepGoalNotifier.ensureChannel()
        StepGoalNotifier.ensureChannel(this)
    }

    private fun buildNotification(time: String, speed: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_gps_cardio", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, GpsTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_gps_cardio", true)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 2, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val strings = LanguageManager.getStrings(this)
        val activityLabel = when (GpsTrackingState.activityType) {
            "running" -> strings.running
            "cycling" -> strings.cycling
            "walking" -> strings.walking
            else -> strings.cardio
        }

        val distance = String.format(Locale.US, "%.2f", GpsTrackingState.totalDistance)
        val steps = GpsTrackingState.estimatedSteps.toString()
        val stateLabel = if (GpsTrackingState.isPaused) " (${strings.paused})" else ""
        val title = "$activityLabel$stateLabel | $time"
        val text = if (GpsTrackingState.isPaused) "${strings.paused} — $distance km | $steps ${strings.steps}"
        else "$speed km/h | $distance km | $steps ${strings.steps}"

        val pace = if (GpsTrackingState.currentSpeed > 0) {
            val paceMinPerKm = 60.0 / GpsTrackingState.currentSpeed
            val pMin = paceMinPerKm.toInt()
            val pSec = ((paceMinPerKm - pMin) * 60).toInt()
            String.format(Locale.US, "%d:%02d min/km", pMin, pSec)
        } else "--:--"

        val expandedText = buildString {
            appendLine("${strings.speed}: $speed km/h    ${strings.distance}: $distance km")
            appendLine("${strings.pace}: $pace    ${strings.steps}: $steps")
            appendLine("${strings.goal}: ${GpsTrackingState.stepGoal} ${strings.steps}")
        }.trimEnd()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, strings.stop, stopPendingIntent)
            .addAction(android.R.drawable.ic_menu_mapmode, strings.openApp, openPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification() {
        val time = formatDuration(GpsTrackingState.elapsedTime)
        val speed = String.format(Locale.US, "%.1f", GpsTrackingState.currentSpeed)
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(time, speed))
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        else String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun showStepGoalNotification() {
        // Shared notifier: shows the congratulation once per day, no matter the counting source.
        StepGoalNotifier.notifyIfGoalReached(this, GpsTrackingState.estimatedSteps, GpsTrackingState.stepGoal)
    }
}
