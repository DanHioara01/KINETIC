package com.example.kinetic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun TodayWorkoutScreen(
    workoutName: String,
    exercises: List<Exercise>,
    onCompleteExercise: (String) -> Unit,
    onAddExercise: () -> Unit,
    onClose: () -> Unit,
    onOpenSpotify: () -> Unit,
    onSaveExercise: (String, String) -> Unit,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    accent: Color,
    accentDim: Color,
    strings: LanguageManager.Strings
) {
    val groupedByGroup = exercises.groupBy { it.group }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0D0D0D) else Color(0xFFF5F5F0))
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = workoutName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = strings.close,
                    tint = textSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SpotifyWorkoutCard(onOpenSpotify = onOpenSpotify, strings = strings)
            }

            for ((group, exercises) in groupedByGroup) {
                if (exercises.isEmpty()) continue

                item {
                    Text(
                        text = group.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(exercises) { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBg, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Text(
                                text = "${exercise.sets}×${exercise.reps} • ${exercise.weight}",
                                fontSize = 11.sp,
                                color = textSecondary.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                        IconButton(onClick = { onSaveExercise(exercise.name, exercise.group) }) {
                            Icon(
                                Icons.Default.BookmarkBorder,
                                contentDescription = strings.saveExercise,
                                tint = accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotifyWorkoutCard(onOpenSpotify: () -> Unit, strings: LanguageManager.Strings) {
    val SpotifyGreen = Color(0xFF1DB954)
    val context = LocalContext.current
    var isTriggered by remember { mutableStateOf(false) }
    var triggerTime by remember { mutableStateOf(0L) }

    // Equalizer animation - always running
    val infiniteTransition = rememberInfiniteTransition()
    val eqTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing))
    )

    // EQ alpha - hidden by default, appears on tap
    val eqAlpha by animateFloatAsState(
        targetValue = if (isTriggered) 0.7f else 0f,
        animationSpec = tween(200),
        label = "eqAlpha"
    )

    // Card press state
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    // Tap -> wait 0.5s -> open Spotify -> wait 1.5s -> reset
    LaunchedEffect(triggerTime) {
        if (triggerTime > 0) {
            isTriggered = true
            delay(500)
            openSpotifyApp(context)
            delay(1500)
            isTriggered = false
        }
    }

    // Card design - dark background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1A1A))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                // Haptic feedback
                try {
                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(30)
                    }
                } catch (_: Exception) {}
                triggerTime = System.currentTimeMillis()
            },
        contentAlignment = Alignment.Center
    ) {
        // Equalizer bars - appear on tap
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .alpha(eqAlpha)
                .clip(RoundedCornerShape(16.dp))
        ) {
            drawEqualizer(eqTime)
        }

        // Row content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spotify logo box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpotifyGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_spotify),
                    contentDescription = "Spotify",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text
            Text(
                text = if (isTriggered) strings.startingWorkoutLabel else strings.tapToPlayLabel,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Play arrow
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = SpotifyGreen,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun DrawScope.drawEqualizer(time: Float) {
    val SpotifyGreen = Color(0xFF1DB954)
    val numBars = 20
    val barSpacing = size.width / numBars
    val barWidth = barSpacing * 0.6f

    for (i in 0 until numBars) {
        val phase = i * 0.8f
        val heightFraction = 0.3f + 0.6f * (sin(time + phase) * 0.5f + 0.5f)
        val barHeight = size.height * heightFraction

        drawRect(
            color = SpotifyGreen,
            topLeft = Offset(
                x = i * barSpacing + (barSpacing - barWidth) / 2,
                y = size.height - barHeight
            ),
            size = Size(width = barWidth, height = barHeight)
        )
    }
}
