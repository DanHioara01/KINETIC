package com.example.kinetic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {

            WaterReminderReceiver().scheduleAllEnabledAlarms(context)
            BiometricReminderReceiver().scheduleIfEnabled(context)
            WorkoutReminderReceiver().scheduleIfEnabled(context)
            WeeklySummaryReceiver().scheduleIfEnabled(context)
            StreakReminderReceiver().scheduleIfEnabled(context)
            GoalProgressReceiver().scheduleIfEnabled(context)
            AchievementReceiver().scheduleIfEnabled(context)
        }
    }
}
