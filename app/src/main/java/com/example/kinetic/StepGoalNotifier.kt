package com.example.kinetic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fires a one-per-day congratulation notification when the user reaches their step goal.
 *
 * Used both from the daily step tracker (MainActivity) and from GPS tracking sessions
 * (GpsTrackingService) so the goal is announced no matter how the steps were counted.
 */
object StepGoalNotifier {

    const val CHANNEL_ID = "step_goal"
    const val NOTIFICATION_ID = 1002
    private const val PREFS_NAME = "step_goal_prefs"

    fun ensureChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            LanguageManager.getStrings(context).stepGoalChannel,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when you reach your step goal"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }
        nm.createNotificationChannel(channel)
    }

    /** Shows the congratulation notification if [currentSteps] reached [goal] and we haven't notified today yet. */
    fun notifyIfGoalReached(context: Context, currentSteps: Int, goal: Int) {
        if (goal <= 0 || currentSteps < goal) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (prefs.getBoolean(today, false)) return

        ensureChannel(context)
        prefs.edit().putBoolean(today, true).apply()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val strings = LanguageManager.getStrings(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(strings.stepGoalTitle)
            .setContentText(String.format(Locale.US, strings.stepGoalText, goal))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    String.format(Locale.US, strings.stepGoalBig, goal) + "\n" + strings.stepGoalKeepGoing
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            context.getSystemService(NotificationManager::class.java)
                .notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {}
    }
}
