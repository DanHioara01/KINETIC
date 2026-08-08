package com.example.kinetic

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Periodic background worker that keeps today's step count fresh for the
 * [KineticStepsWidget] even when the app is closed.
 *
 * The step counter sensor (TYPE_STEP_COUNTER) reports the total steps since the
 * last reboot, so we subtract the persisted per-day baseline — the same logic the
 * foreground app uses in MainActivity. If the device is asleep and no sensor event
 * arrives within the timeout, we keep the last persisted value and just refresh the
 * widget (which also picks up newly saved GPS cardio routes).
 */
class StepsSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val stepCounter = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        try {
            if (sensorManager != null && stepCounter != null) {
                val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val pedometerPrefs = context.getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)
                val baseline = pedometerPrefs.getFloat("initial_steps_$todayKey", -1f).toInt()

                val counterEvent = CompletableDeferred<Int>()
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        counterEvent.complete(event.values[0].toInt())
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                sensorManager.registerListener(listener, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)

                val counterValue = withTimeoutOrNull(10_000) { counterEvent.await() }

                // Always unregister, whether we got an event or timed out
                sensorManager.unregisterListener(listener)

                if (counterValue != null) {
                    val initial = if (baseline < 0 || counterValue < baseline) {
                        // First read of the day, or the counter reset on device reboot —
                        // re-establish the baseline so today's count restarts correctly
                        pedometerPrefs.edit().putFloat("initial_steps_$todayKey", counterValue.toFloat()).apply()
                        counterValue
                    } else {
                        baseline
                    }
                    val stepsToday = (counterValue - initial).coerceAtLeast(0)
                    PreferencesManager(context, UserProfileManager(context)).setTodaySteps(stepsToday)
                }
            }
        } catch (_: Exception) {
            // Sensor read failed — still refresh the widget with the last persisted values
        }

        // Refresh the widget so it shows the freshest persisted values
        try {
            KineticStepsGlanceWidget().updateAll(context)
        } catch (_: Exception) {}

        return Result.success()
    }
}
