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
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.io.ByteArrayOutputStream

private const val WATER_TAG = "KineticWidget"
private val WATER_RING_KEY = stringPreferencesKey("water_ring_bitmap_base64")

/**
 * Home-screen widget showing today's water intake with a circular progress ring.
 * Rendered with Jetpack Glance (Compose) — update with [KineticGlanceWidget.updateAll].
 *
 * Tapping the widget opens the app on the Water tab (the water bottle screen),
 * where the user adds water. The widget is refreshed by the app (updateAll) every
 * time water changes, so it always shows the same value as the bottle.
 */
class KineticGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val data = loadWaterData(context)
            val fraction = if (data.waterGoalMl > 0) {
                (data.waterMl.toFloat() / data.waterGoalMl).coerceIn(0f, 1f)
            } else 0f

            val bitmap = drawWaterRingBitmap(240, fraction)
            val base64 = waterBitmapToBase64(bitmap)
            bitmap.recycle()

            updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[WATER_RING_KEY] = base64
                }
            }
        } catch (t: Throwable) {
            Log.e(WATER_TAG, "Failed to create water ring bitmap in provideGlance", t)
        }

        provideContent {
            WaterWidgetContent()
        }
    }
}

private data class WaterWidgetData(
    val waterMl: Int,
    val waterGoalMl: Int
)

private fun loadWaterData(context: Context): WaterWidgetData {
    // Same source as the water bottle screen (PreferencesManager.getTodayWaterMl),
    // so the widget always shows the exact value the app displays.
    val userProfileManager = UserProfileManager(context)
    val prefs = PreferencesManager(context, userProfileManager)
    return WaterWidgetData(
        waterMl = prefs.getTodayWaterMl(),
        waterGoalMl = prefs.getWaterGoalMl()
    )
}

/**
 * Tap action for the widget: opens the app on the Water tab (the water bottle
 * screen). Glance 1.1.1's actionStartActivity only accepts a ComponentName (no
 * extras), so a small callback is used to deliver the [MainActivity] intent with
 * the "open_water_tab" extra.
 */
class OpenWaterTabAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("open_water_tab", true)
            }
            context.startActivity(intent)
        } catch (t: Throwable) {
            Log.e("KineticWidget", "open water tab failed", t)
        }
    }
}

@Composable
private fun WaterWidgetContent() {
    val context = LocalContext.current
    val data = try {
        loadWaterData(context)
    } catch (_: Throwable) {
        WaterWidgetData(waterMl = 0, waterGoalMl = 2000)
    }

    // Read the pre-built bitmap from Glance state (created in provideGlance on background thread).
    // Reading LocalState forces recomposition on every Glance update.
    val state = LocalState.current as? Preferences
    val ringBitmap: Bitmap = try {
        val base64 = state?.get(WATER_RING_KEY)
        if (!base64.isNullOrBlank()) {
            waterBase64ToBitmap(base64)
        } else {
            val fraction = if (data.waterGoalMl > 0) {
                (data.waterMl.toFloat() / data.waterGoalMl).coerceIn(0f, 1f)
            } else 0f
            drawWaterRingBitmap(240, fraction)
        }
    } catch (t: Throwable) {
        Log.e(WATER_TAG, "Failed to decode water ring bitmap from state", t)
        Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)
    }

    // Whole widget is tappable: opens the app on the Water tab (water bottle screen).
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xCC0A0A0A))
            .cornerRadius(16.dp)
            .clickable(actionRunCallback<OpenWaterTabAction>())
            .padding(10.dp)
    ) {
        // Title: centered KINETIC
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

        // Center: ring + water drop + value + goal
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight(),
            contentAlignment = Alignment.Center
        ) {
            // Circular progress ring
            Image(
                provider = ImageProvider(ringBitmap),
                contentDescription = null,
                modifier = GlanceModifier.size(110.dp)
            )

            // Water drop + value + goal overlay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Water drop icon
                Text(
                    text = "\uD83D\uDCA7",
                    style = TextStyle(fontSize = 16.sp)
                )
                Spacer(GlanceModifier.height(2.dp))
                // Value
                Text(
                    text = data.waterMl.toString(),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                // Goal
                Text(
                    text = "/ ${data.waterGoalMl} ml",
                    style = TextStyle(
                        color = ColorProvider(Color(0x99FFFFFF)),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Draws a single circular progress ring (blue on gray) into a bitmap.
 */
private fun drawWaterRingBitmap(sizePx: Int, progress: Float): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val stroke = sizePx * 0.08f
    val radius = sizePx / 2f - stroke / 2f - 4f
    val centre = sizePx / 2f
    val rect = RectF(centre - radius, centre - radius, centre + radius, centre + radius)

    // Track (gray circle)
    val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = 0x33FFFFFF
        strokeCap = Paint.Cap.ROUND
    }
    canvas.drawArc(rect, 0f, 360f, false, trackPaint)

    // Progress arc (blue, starts from top)
    if (progress > 0f) {
        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            color = 0xFF34AADC.toInt()
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawArc(rect, -90f, progress * 360f, false, progressPaint)
    }

    return bitmap
}

private fun waterBitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    val bytes = stream.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

private fun waterBase64ToBitmap(base64: String): Bitmap {
    val bytes = Base64.decode(base64, Base64.NO_WRAP)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

// Receiver keeps the original class name so already-placed widgets stay bound after updates.
class KineticWidget : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KineticGlanceWidget()
}
