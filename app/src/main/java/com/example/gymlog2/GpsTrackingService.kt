package com.example.gymlog2

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
        const val ACTION_STOP = "com.example.gymlog2.STOP_TRACKING"
        const val ACTION_START = "com.example.gymlog2.START_TRACKING"
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
            ACTION_START -> {
                val activityType = intent.getStringExtra("activity_type") ?: "running"
                startTracking(activityType)
                return START_STICKY
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking(activityType: String) {
        GpsTrackingState.isTracking = true
        GpsTrackingState.activityType = activityType
        GpsTrackingState.elapsedTime = 0L
        GpsTrackingState.totalDistance = 0.0
        GpsTrackingState.routePoints = emptyList()
        GpsTrackingState.currentSpeed = 0.0
        lastRouteLocation = null

        try {
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification("00:00", "0.0"),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification("00:00", "0.0"))
            }
        } catch (e: Exception) {
            GpsTrackingState.isTracking = false
            return
        }

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

        handler.removeCallbacks(timerRunnable)
        handler.postDelayed(timerRunnable, 1000)
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

            updateNotification()
        }
    }

    private fun stopTracking() {
        GpsTrackingState.isTracking = false
        GpsTrackingState.isServiceRunning = false
        handler.removeCallbacks(timerRunnable)

        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
        lastRouteLocation = null

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
        val channel = NotificationChannel(
            CHANNEL_ID,
            "GPS Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "GPS tracking notification"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
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

        val activityLabel = when (GpsTrackingState.activityType) {
            "running" -> "Running"
            "cycling" -> "Cycling"
            "walking" -> "Walking"
            else -> "Cardio"
        }

        val distance = String.format(Locale.US, "%.2f", GpsTrackingState.totalDistance)
        val title = "$activityLabel | $time"
        val text = "$speed km/h | $distance km"

        val pace = if (GpsTrackingState.currentSpeed > 0) {
            val paceMinPerKm = 60.0 / GpsTrackingState.currentSpeed
            val pMin = paceMinPerKm.toInt()
            val pSec = ((paceMinPerKm - pMin) * 60).toInt()
            String.format(Locale.US, "%d:%02d min/km", pMin, pSec)
        } else "--:--"

        val expandedText = buildString {
            appendLine("Speed: $speed km/h    Distance: $distance km")
            appendLine("Pace: $pace    Points: ${GpsTrackingState.routePoints.size}")
        }.trimEnd()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
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
}
