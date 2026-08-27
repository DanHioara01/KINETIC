package com.example.kinetic

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kinetic.MessagesHelper
import java.util.Calendar

class WeeklySummaryReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "weekly_summary"
        const val NOTIFICATION_ID = 8801
        const val REQUEST_CODE = 8801
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)
        showNotification(context)
        scheduleWeekly(context)
    }

    private fun showNotification(context: Context) {
        val strings = LanguageManager.getStrings(context)

        val upm = UserProfileManager(context)
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val workoutsThisWeek = if (userId.isNotEmpty()) {
            try {
                val db = AppDatabase.getDatabase(context)
                val allWorkouts = kotlinx.coroutines.runBlocking { db.antrenamentDao().getAllForUser(userId) }
                val weekStartMillis = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY)
                    .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                allWorkouts.count { it.data >= weekStartMillis }
            } catch (_: Exception) { 0 }
        } else 0
        val contentText = strings.weeklySummaryText.replace("__COUNT__", workoutsThisWeek.toString())

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_stats", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(strings.weeklySummaryTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            // Also add to in-app messages
            val db = AppDatabase.getDatabase(context)
            MessagesHelper.addGeneric(db.messageDao(), "Weekly summary", "$workoutsThisWeek workouts this week. Keep going!", "INFO")
        } catch (_: SecurityException) { }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                LanguageManager.getStrings(context).weeklySummaryChannelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Weekly training summary notification"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun scheduleWeekly(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WeeklySummaryReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 7)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WeeklySummaryReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleIfEnabled(context: Context) {
        scheduleWeekly(context)
    }
}
