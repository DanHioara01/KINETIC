package com.example.kinetic

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = applicationContext.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("logged_in", false)
            if (!isLoggedIn) return Result.success()

            val profilePrefs = applicationContext.getSharedPreferences("user_profiles", Context.MODE_PRIVATE)
            val userId = profilePrefs.getString("own_user_id", null) ?: return Result.success()
            if (userId == "local_user") return Result.success()

            val db = AppDatabase.getDatabase(applicationContext)
            val preferencesManager = PreferencesManager(applicationContext, UserProfileManager(applicationContext))
            val syncRepo = SyncRepository(db, NetworkClient.api, preferencesManager)
            syncRepo.syncAllFromServer(userId)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
