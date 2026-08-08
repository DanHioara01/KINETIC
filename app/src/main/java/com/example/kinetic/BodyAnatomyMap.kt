package com.example.kinetic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.kinetic.ui.theme.*

fun getRecoveryPillColor(level: Double): Color {
    return when {
        level < 0.3 -> Color(0xFF4CAF50)
        level < 0.6 -> Color(0xFFFF9800)
        else -> Volcanico
    }
}

fun getRecoveryBodyAlpha(level: Double): Float {
    return when {
        level < 0.05 -> 0.0f
        level < 0.3 -> 0.55f
        level < 0.6 -> 0.65f
        else -> 0.75f
    }
}

fun getRecoveryBodyColor(level: Double): Color {
    return when {
        level < 0.3 -> Color(0xFF4CAF50)
        level < 0.6 -> Color(0xFFFF9800)
        else -> Volcanico
    }
}

private const val SVG_VIEWPORT_W = 68.587668f
private const val SVG_VIEWPORT_H = 92.604164f

data class OverlayBox(val left: Float, val top: Float, val right: Float, val bottom: Float)

data class MuscleGroupRegion(
    val group: String,
    val overlayRes: Int,
    val hitBoxes: List<OverlayBox>,
    val pillFractionX: Float,
    val pillFractionY: Float
)

private val muscleGroupRegions = listOf(
    MuscleGroupRegion(
        group = "Gat & Trapezi",
        overlayRes = R.drawable.overlay_gat_trapezi,
        hitBoxes = listOf(
            OverlayBox(0.15f, 0.00f, 0.30f, 0.16f),
            OverlayBox(0.68f, 0.00f, 0.90f, 0.17f)
        ),
        pillFractionX = 0.50f, pillFractionY = 0.08f
    ),
    MuscleGroupRegion(
        group = "Umeri",
        overlayRes = R.drawable.overlay_umeri,
        hitBoxes = listOf(
            OverlayBox(0.05f, 0.14f, 0.41f, 0.26f),
            OverlayBox(0.64f, 0.13f, 0.90f, 0.26f)
        ),
        pillFractionX = 0.50f, pillFractionY = 0.18f
    ),
    MuscleGroupRegion(
        group = "Piept",
        overlayRes = R.drawable.overlay_piept,
        hitBoxes = listOf(
            OverlayBox(0.10f, 0.18f, 0.38f, 0.30f)
        ),
        pillFractionX = 0.24f, pillFractionY = 0.24f
    ),
    MuscleGroupRegion(
        group = "Biceps",
        overlayRes = R.drawable.overlay_biceps,
        hitBoxes = listOf(
            OverlayBox(0.03f, 0.28f, 0.41f, 0.40f)
        ),
        pillFractionX = 0.22f, pillFractionY = 0.34f
    ),
    MuscleGroupRegion(
        group = "Triceps",
        overlayRes = R.drawable.overlay_triceps,
        hitBoxes = listOf(
            OverlayBox(0.58f, 0.28f, 0.96f, 0.48f)
        ),
        pillFractionX = 0.78f, pillFractionY = 0.38f
    ),
    MuscleGroupRegion(
        group = "Antebrate",
        overlayRes = R.drawable.overlay_antebrate,
        hitBoxes = listOf(
            OverlayBox(0.02f, 0.40f, 0.42f, 0.52f)
        ),
        pillFractionX = 0.22f, pillFractionY = 0.46f
    ),
    MuscleGroupRegion(
        group = "Abdomen",
        overlayRes = R.drawable.overlay_abdomen,
        hitBoxes = listOf(
            OverlayBox(0.16f, 0.26f, 0.34f, 0.55f),
            OverlayBox(0.64f, 0.38f, 0.90f, 0.48f)
        ),
        pillFractionX = 0.25f, pillFractionY = 0.40f
    ),
    MuscleGroupRegion(
        group = "Picioare",
        overlayRes = R.drawable.overlay_picioare,
        hitBoxes = listOf(
            OverlayBox(0.11f, 0.42f, 0.37f, 0.64f),
            OverlayBox(0.64f, 0.42f, 0.91f, 0.64f)
        ),
        pillFractionX = 0.25f, pillFractionY = 0.54f
    ),
    MuscleGroupRegion(
        group = "Fese",
        overlayRes = R.drawable.overlay_fese,
        hitBoxes = listOf(
            OverlayBox(0.60f, 0.38f, 0.94f, 0.48f)
        ),
        pillFractionX = 0.78f, pillFractionY = 0.43f
    ),
    MuscleGroupRegion(
        group = "Spate",
        overlayRes = R.drawable.overlay_spate,
        hitBoxes = listOf(
            OverlayBox(0.60f, 0.14f, 0.96f, 0.40f)
        ),
        pillFractionX = 0.78f, pillFractionY = 0.26f
    ),
    MuscleGroupRegion(
        group = "Gambe",
        overlayRes = R.drawable.overlay_gambe,
        hitBoxes = listOf(
            OverlayBox(0.14f, 0.64f, 0.34f, 1.00f),
            OverlayBox(0.64f, 0.64f, 0.92f, 1.00f)
        ),
        pillFractionX = 0.25f, pillFractionY = 0.84f
    )
)

@Composable
fun BodyAnatomyMapSimple(
    recoveryMap: Map<String, Double>,
    onGroupClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(SVG_VIEWPORT_W / SVG_VIEWPORT_H),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.body_anatomy),
                contentDescription = "Body anatomy",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            muscleGroupRegions.forEach { region ->
                val level = recoveryMap[region.group] ?: 0.0
                val alpha = getRecoveryBodyAlpha(level)
                val color = getRecoveryBodyColor(level)

                Image(
                    painter = painterResource(region.overlayRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = if (alpha > 0f) ColorFilter.tint(color.copy(alpha = alpha)) else ColorFilter.tint(Color.Transparent)
                )
            }
        }
    }
}
