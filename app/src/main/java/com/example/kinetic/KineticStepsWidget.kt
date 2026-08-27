package com.example.kinetic

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ImageProvider
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.LocalContext
import androidx.glance.LocalState
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.io.ByteArrayOutputStream
import java.util.Calendar
import java.util.Locale

private const val TAG = "KineticStepsWidget"
private val RINGS_BITMAP_KEY = stringPreferencesKey("rings_bitmap_base64")

/**
 * Home-screen widget that monitors today's steps, active time and calories.
 *
 * Steps come from two sources and are combined exactly like the home page:
 *  - hardware pedometer steps persisted by the app while it's open ([PreferencesManager.getTodaySteps])
 *  - steps estimated from today's saved GPS cardio routes (distance × 1312)
 * While a GPS session is running the larger estimate wins (adding would double-count);
 * otherwise saved routes + pedometer are summed.
 *
 * The three concentric rings (red = calories, green = active time, blue = steps) are
 * drawn into a [Bitmap] because Glance's [androidx.glance.appwidget.CircularProgressIndicator]
 * is indeterminate-only; the bitmap is rendered via [Image].
 *
 * Rendered with Jetpack Glance (Compose) — update with [KineticStepsGlanceWidget.updateAll].
 */
class KineticStepsGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            cacheTodayCardioSummary(context)
        } catch (t: Throwable) {
            Log.w(TAG, "cacheTodayCardioSummary failed", t)
        }

        try {
            val data = loadStepsData(context)
            val calProgress = (data.calories / data.calorieGoal.coerceAtLeast(1)).toFloat().coerceIn(0f, 1f)
            val timeProgress = (data.activeMinutes.toFloat() / data.activeTimeGoalMin.coerceAtLeast(1)).coerceIn(0f, 1f)
            val stepsProgress = (data.steps.toFloat() / data.stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

            val bitmap = drawRingsBitmap(280, calProgress, timeProgress, stepsProgress)
            val base64 = bitmapToBase64(bitmap)
            bitmap.recycle()

            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[RINGS_BITMAP_KEY] = base64
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to create rings bitmap in provideGlance", t)
        }

        provideContent {
            StepsWidgetContent()
        }
    }

    /**
     * Caches today's saved cardio routes (from the DB) into prefs for the widget
     * composition. Skipped when a live baseline exists — the baseline is fresher and
     * the composition prefers it.
     */
    private suspend fun cacheTodayCardioSummary(context: Context) {
        val userProfileManager = UserProfileManager(context)
        val prefs = PreferencesManager(context, userProfileManager)
        if (prefs.hasTodayCardioBaseline()) return
        val userId = userProfileManager.getOwnUserId()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis
        val summary = AppDatabase.getDatabase(context)
            .cardioRouteDao()
            .getTodaySummary(userId, dayStart, System.currentTimeMillis())
        if (summary != null) {
            prefs.setTodayCachedCardioSummary(summary)
        }
    }
}

private data class StepsWidgetData(
    val steps: Int,
    val stepGoal: Int,
    val calories: Double,
    val activeMinutes: Long,
    val calorieGoal: Int,
    val activeTimeGoalMin: Int,
    val caloriesLabel: String,
    val activeTimeLabel: String,
    val stepsLabel: String
)

private fun loadStepsData(context: Context): StepsWidgetData {
    val userProfileManager = UserProfileManager(context)
    val prefs = PreferencesManager(context, userProfileManager)

    val stepGoal = prefs.getStepGoal().coerceAtLeast(1)
    val strings = LanguageManager.getStrings(context)

    // Baseline = saved GPS routes. The home page persists this "before the current
    // session" baseline in prefs; use it when available (it also avoids double-counting
    // a route saved mid-session). Otherwise fall back to the summary snapshot cached
    // by provideGlance.
    var routeDistanceKm: Double
    var routeDurationMs: Long
    var routeCalories: Double
    if (prefs.hasTodayCardioBaseline()) {
        routeDistanceKm = prefs.getTodayCardioBaselineDist().coerceAtLeast(0.0)
        routeDurationMs = prefs.getTodayCardioBaselineDur().coerceAtLeast(0L)
        routeCalories = prefs.getTodayCardioBaselineCal().coerceAtLeast(0.0)
    } else {
        // Snapshot cached by provideGlance — a blocking Room query is not allowed in
        // the composition (it runs on the main thread).
        val cached = try {
            prefs.getTodayCachedCardioSummary()
        } catch (_: Exception) {
            null
        }
        routeDistanceKm = cached?.totalDistance ?: 0.0
        routeDurationMs = cached?.totalDuration ?: 0L
        routeCalories = cached?.totalCalories ?: 0.0
    }
    val routeSteps = (routeDistanceKm * 1312).toInt().coerceIn(0, 99999)
    val pedometerSteps = prefs.getTodaySteps().coerceIn(0, 99999)

    // Live GPS session (if any) — mirrors the home-page formula exactly so the
    // widget stays in sync while a session is running, not only after it's saved.
    val sessionActive = GpsTrackingState.isTracking || GpsTrackingState.isPaused
    val liveDist = if (sessionActive) GpsTrackingState.totalDistance else 0.0
    val liveDur = if (sessionActive) GpsTrackingState.elapsedTime else 0L
    val liveSteps = if (sessionActive) GpsTrackingState.estimatedSteps else 0
    val liveCal = liveDist * cardioCalPerKm(GpsTrackingState.activityType)

    // Use movement-based active time (pedometer-based, not GPS duration)
    val pedoPrefs = context.getSharedPreferences("pedometer_prefs", android.content.Context.MODE_PRIVATE)
    val todayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    val movementActiveMs = pedoPrefs.getLong("active_time_$todayKey", 0L)
    val totalDurationMs = movementActiveMs + liveDur
    // Same as MainActivity.totalSteps: while GPS is on, take the max (the session's
    // steps are estimated from distance, so adding the pedometer would double-count);
    // otherwise saved routes + pedometer.
    val steps = (
        if (sessionActive) maxOf(routeSteps + liveSteps, pedometerSteps)
        else routeSteps + pedometerSteps
    ).coerceIn(0, 99999)
    val totalCalories = routeCalories + liveCal + (steps * 0.04)

    return StepsWidgetData(
        steps = steps,
        stepGoal = stepGoal,
        calories = totalCalories,
        activeMinutes = totalDurationMs / 60000L,
        calorieGoal = 500,
        activeTimeGoalMin = 90,
        caloriesLabel = strings.caloriesLabel,
        activeTimeLabel = strings.activeTimeLabel,
        stepsLabel = strings.stepsLabel
    )
}

@Composable
private fun StepsWidgetContent() {
    val context = LocalContext.current
    val data = try {
        loadStepsData(context)
    } catch (_: Throwable) {
        StepsWidgetData(
            steps = 0,
            stepGoal = 7000,
            calories = 0.0,
            activeMinutes = 0L,
            calorieGoal = 500,
            activeTimeGoalMin = 90,
            caloriesLabel = "Calories",
            activeTimeLabel = "Active time",
            stepsLabel = "Steps"
        )
    }

    // Read the pre-built bitmap from Glance state (created in provideGlance on background thread).
    // Reading LocalState forces recomposition on every Glance update.
    val state = LocalState.current as? Preferences
    val ringsBitmap: Bitmap = try {
        val base64 = state?.get(RINGS_BITMAP_KEY)
        if (!base64.isNullOrBlank()) {
            base64ToBitmap(base64)
        } else {
            // Fallback: create bitmap on main thread if state is empty (first render)
            val calProgress = (data.calories / data.calorieGoal.coerceAtLeast(1)).toFloat().coerceIn(0f, 1f)
            val timeProgress = (data.activeMinutes.toFloat() / data.activeTimeGoalMin.coerceAtLeast(1)).coerceIn(0f, 1f)
            val stepsProgress = (data.steps.toFloat() / data.stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
            drawRingsBitmap(280, calProgress, timeProgress, stepsProgress)
        }
    } catch (t: Throwable) {
        Log.e(TAG, "Failed to decode rings bitmap from state", t)
        Bitmap.createBitmap(280, 280, Bitmap.Config.ARGB_8888)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xCC0A0A0A))
            .cornerRadius(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // ── Title: centered KINETIC ───────────────────────────
            Text(
                text = "KINETIC",
                modifier = GlanceModifier.fillMaxWidth(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(GlanceModifier.height(6.dp))

            // ── Rings + metrics row ───────────────────────────────
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Left: 3 concentric progress rings ──────────────
                Box(
                    modifier = GlanceModifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(ringsBitmap),
                        contentDescription = null,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }

                Spacer(GlanceModifier.width(10.dp))

                // ── Right: metric rows ────────────────────────────
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WidgetMetricRow(
                        dotColor = Color(0xFFFF3B30),
                        label = data.stepsLabel.uppercase(),
                        valueText = formatSteps(data.steps),
                        goalText = "/ ${formatStepGoalShort(data.stepGoal)}"
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    WidgetMetricRow(
                        dotColor = Color(0xFF34C759),
                        label = data.activeTimeLabel.uppercase(),
                        valueText = data.activeMinutes.toString(),
                        goalText = "/ ${data.activeTimeGoalMin}",
                        unit = "min"
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    WidgetMetricRow(
                        dotColor = Color(0xFF0A84FF),
                        label = data.caloriesLabel.uppercase(),
                        valueText = data.calories.toInt().toString(),
                        goalText = "/ ${data.calorieGoal}"
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetMetricRow(
    dotColor: Color,
    label: String,
    valueText: String,
    goalText: String,
    unit: String? = null
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored dot
        Spacer(
            modifier = GlanceModifier
                .size(8.dp)
                .background(ColorProvider(dotColor))
                .cornerRadius(4.dp)
        )
        Spacer(GlanceModifier.width(6.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(Color(0x99FFFFFF)),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueText,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                if (unit != null) {
                    Spacer(GlanceModifier.width(3.dp))
                    Text(
                        text = unit,
                        style = TextStyle(
                            color = ColorProvider(Color(0x99FFFFFF)),
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }
                Text(
                    text = goalText,
                    style = TextStyle(
                        color = ColorProvider(Color(0x66FFFFFF)),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Draws three concentric progress rings into a transparent bitmap,
 * matching the home-page DailyActivityCard exactly:
 *  - start angle 135° (bottom-left), sweep 270° (3/4 circle)
 *  - round caps, stroke 12dp-equivalent, 6dp gap between rings
 *  - track alpha 0.15, same colours (red=calories, green=time, blue=steps)
 */
private fun drawRingsBitmap(
    sizePx: Int,
    calProgress: Float,
    timeProgress: Float,
    stepsProgress: Float
): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centre = sizePx / 2f

    // Scale dp values to px: the home page uses a 128.dp container.
    // We scale proportionally so the rings look identical at any widget size.
    val scale = sizePx / 128f   // px per dp-unit (128dp = home-page container)
    val stroke = 12f * scale    // 12.dp stroke
    val gap = 6f * scale         // 6.dp gap between rings
    val padding = 3f * scale     // 3.dp padding from edge

    val outerRadius = centre - stroke / 2 - padding
    val middleRadius = outerRadius - stroke - gap
    val innerRadius = outerRadius - 2 * (stroke + gap)

    // Home-page start angle: 135° (bottom-left), sweep: 270° (3/4 circle)
    val startAngle = 135f
    val maxSweep = 270f

    data class Ring(val radius: Float, val progress: Float, val color: Int)
    val rings = listOf(
        Ring(outerRadius, stepsProgress, 0xFFFF3B30.toInt()),  // steps — red (outer)
        Ring(middleRadius, timeProgress, 0xFF34C759.toInt()), // active time — green (middle)
        Ring(innerRadius, calProgress, 0xFF0A84FF.toInt())     // calories — blue (inner)
    )

    for ((radius, progress, colorInt) in rings) {
        if (radius <= 0f) continue
        val rect = RectF(centre - radius, centre - radius, centre + radius, centre + radius)

        // Track (background arc)
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            color = 0x26FFFFFF  // alpha ~0.15, matches home page
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawArc(rect, startAngle, maxSweep, false, trackPaint)

        // Progress arc
        if (progress > 0f) {
            val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = stroke
                color = colorInt
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawArc(rect, startAngle, maxSweep * progress, false, progressPaint)
        }
    }
    return bitmap
}

private fun bitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val bytes = stream.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

private fun base64ToBitmap(base64: String): Bitmap {
    val bytes = Base64.decode(base64, Base64.NO_WRAP)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun formatSteps(steps: Int): String = when {
    steps >= 10000 -> String.format(Locale.US, "%.1fk", steps / 1000.0)
    else -> String.format(Locale.US, "%,d", steps)
}

private fun formatStepGoalShort(goal: Int): String = when {
    goal >= 10000 -> "${goal / 1000}K"
    else -> goal.toString()
}

// Receiver keeps the original class name so already-placed widgets stay bound after updates.
class KineticStepsWidget : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KineticStepsGlanceWidget()
}
