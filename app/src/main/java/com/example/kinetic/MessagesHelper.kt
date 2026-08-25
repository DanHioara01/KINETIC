package com.example.kinetic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Helper to insert system messages into the inbox.
 * Call these from anywhere in the app.
 */
object MessagesHelper {

    fun addWorkoutReminder(dao: MessageDao) = insert(dao, MessageEntity(
        title = "Time to workout!",
        body = "You haven't trained today. Get moving and stay consistent!",
        type = "REMINDER"
    ))

    fun addWaterReminder(dao: MessageDao) = insert(dao, MessageEntity(
        title = "Hydration reminder",
        body = "Don't forget to drink water. Stay hydrated for better performance!",
        type = "REMINDER"
    ))

    fun addSyncSuccess(dao: MessageDao) = insert(dao, MessageEntity(
        title = "Sync complete",
        body = "Your workout data has been synced to the cloud successfully.",
        type = "SUCCESS"
    ))

    fun addSyncFailed(dao: MessageDao) = insert(dao, MessageEntity(
        title = "Sync failed",
        body = "Could not sync your data. Check your internet connection and try again.",
        type = "WARNING"
    ))

    fun addUpdateAvailable(dao: MessageDao, version: String) = insert(dao, MessageEntity(
        title = "Update available",
        body = "Version $version is ready to install. Open settings to download.",
        type = "INFO"
    ))

    fun addStreakMessage(dao: MessageDao, count: Int) = insert(dao, MessageEntity(
        title = "$count day streak!",
        body = "Amazing! You've trained $count days in a row. Keep it up!",
        type = "SUCCESS"
    ))

    fun addPersonalRecord(dao: MessageDao, exercise: String, weight: Double) = insert(dao, MessageEntity(
        title = "New personal record!",
        body = "You lifted ${weight.toInt()}kg on $exercise. That's a new PR!",
        type = "SUCCESS"
    ))

    fun addRestDay(dao: MessageDao) = insert(dao, MessageEntity(
        title = "Rest day",
        body = "Today is your rest day. Recovery is just as important as training.",
        type = "INFO"
    ))

    fun addGeneric(dao: MessageDao, title: String, body: String, type: String = "INFO") = insert(dao, MessageEntity(
        title = title, body = body, type = type
    ))

    private fun insert(dao: MessageDao, msg: MessageEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            if (dao.count() >= 50) dao.deleteOlderThan(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
            dao.insert(msg)
        }
    }
}
