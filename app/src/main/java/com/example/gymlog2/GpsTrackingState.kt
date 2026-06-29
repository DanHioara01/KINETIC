package com.example.gymlog2

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GpsTrackingState {
    var isTracking by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var elapsedTime by mutableLongStateOf(0L)
    var currentSpeed by mutableDoubleStateOf(0.0)
    var totalDistance by mutableDoubleStateOf(0.0)
    var routePoints by mutableStateOf<List<GpsPoint>>(emptyList())
    var lastLocation by mutableStateOf<Location?>(null)
    var activityType by mutableStateOf("running")
    var isServiceRunning by mutableStateOf(false)
    var bearing by mutableStateOf(0f)
    var isInPipMode by mutableStateOf(false)
    var lastRouteLocation: Location? = null

    fun reset() {
        isTracking = false
        isPaused = false
        elapsedTime = 0L
        currentSpeed = 0.0
        totalDistance = 0.0
        routePoints = emptyList()
        lastLocation = null
        lastRouteLocation = null
        bearing = 0f
    }
}
