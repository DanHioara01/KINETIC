package com.example.kinetic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kinetic.ui.theme.*

private const val CHANNEL_ID = "rest_timer"
private const val NOTIFICATION_ID = 9999

@Composable
fun RestTimerOverlay(
    isDark: Boolean,
    restSeconds: Int,
    onDismiss: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var remaining by remember { mutableIntStateOf(restSeconds) }
    var isPaused by remember { mutableStateOf(false) }

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(restSeconds) {
        createNotificationChannel(context)
    }

    DisposableEffect(restSeconds, isPaused) {
        val timer = object : CountDownTimer(remaining * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    remaining = (millisUntilFinished / 1000).toInt()
                }
            }

            override fun onFinish() {
                remaining = 0
                vibratePhone(context)
                sendTimerNotification(context)
                onDismiss()
            }
        }
        if (!isPaused && remaining > 0) {
            timer.start()
        }
        onDispose { timer.cancel() }
    }

    val accent = if (isDark) accentColor() else LightPrimaryRed
    val surfaceBg = if (isDark) bgColor().copy(alpha = 0.95f) else LightBackground.copy(alpha = 0.95f)
    val textPrimary = if (isDark) textColor() else LightTextPrimary
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                "REST",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary.copy(alpha = 0.6f),
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                accent,
                                accent.copy(alpha = 0.3f),
                                accent
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                                                .background(if (isDark) bgColor() else Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val minutes = remaining / 60
                    val seconds = remaining % 60
                    Text(
                        "%d:%02d".format(minutes, seconds),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining <= 5) Color.Red else textPrimary,
                        modifier = if (remaining <= 5) Modifier.alpha(pulseAlpha) else Modifier
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { onSkip() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = textPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = accent)
                    Spacer(Modifier.width(4.dp))
                    Text("Skip", color = accent, fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = {
                        isPaused = !isPaused
                        if (isPaused) {
                            val timer = object : CountDownTimer(remaining * 1000L, 1000) {
                                override fun onTick(millisUntilFinished: Long) {}
                                override fun onFinish() {}
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = textPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = accent
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isPaused) "Resume" else "Pause", color = accent, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(60, 90, 120).forEach { secs ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                                                        .background(if (restSeconds == secs) accent else textPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable {
                                remaining = secs
                                isPaused = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${secs}s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (restSeconds == secs) Color.White else textPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                                                .background(textPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun RestTimerButton(
    isDark: Boolean,
    restSeconds: Int,
    onTimerStart: (Int) -> Unit
) {
    val accent = if (isDark) accentColor() else LightPrimaryRed
    val textSecondary = if (isDark) secondaryTextColor() else LightTextSecondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
                        .background(accent.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .clickable { onTimerStart(restSeconds) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = "Rest Timer",
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "${restSeconds}s",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent
        )
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notification when rest timer finishes"
            enableVibration(true)
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }
}

private fun sendTimerNotification(context: Context) {
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Rest Timer")
        .setContentText("Time to start your next set!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    try {
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    } catch (_: SecurityException) {
    }
}

private fun vibratePhone(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(500)
        }
    } catch (_: Exception) {}
}
