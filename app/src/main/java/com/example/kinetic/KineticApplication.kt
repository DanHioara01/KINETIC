package com.example.kinetic

import android.app.Application
import android.content.Context
import androidx.work.*
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.disk.DiskCache
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class KineticApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(GifDecoder.Factory())
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()
    }

    val revenueCatManager: RevenueCatManager by lazy { RevenueCatManager(this) }
    val adUnlockManager: AdUnlockManager by lazy { AdUnlockManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        org.osmdroid.config.Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        org.osmdroid.config.Configuration.getInstance().userAgentValue = "KineticGPS/1.0 (Android; $packageName; fitness-app)"

        // Monetization: RevenueCat (billing) + AdMob (rewarded ads)
        revenueCatManager.initialize(REVENUECAT_API_KEY)
        adUnlockManager.initialize()

        backgroundSync(this)
        schedulePeriodicSync()
        scheduleStepsSync()
    }

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "kinetic_background_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun scheduleStepsSync() {
        // Keeps today's steps fresh for the home-screen widget while the app is closed.
        // 15 minutes is the smallest interval WorkManager allows for periodic work.
        val stepsRequest = PeriodicWorkRequestBuilder<StepsSyncWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "kinetic_steps_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            stepsRequest
        )
    }

    private fun backgroundSync(context: Context) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val prefs = context.getSharedPreferences("session_prefs", MODE_PRIVATE)
                val isLoggedIn = prefs.getBoolean("logged_in", false)
                if (!isLoggedIn) return@execute

                val profilePrefs = context.getSharedPreferences("user_profiles", MODE_PRIVATE)
                val userId = profilePrefs.getString("own_user_id", null) ?: return@execute
                val name = profilePrefs.getString("own_name", "") ?: ""
                val photo = profilePrefs.getString("own_photo", "") ?: ""
                if (name.isBlank() || userId == "local_user") return@execute

                val db = AppDatabase.getDatabase(context)
                SocialRepository(db).syncUserProfileBlocking(userId, name, photo)

                val userProfileManager = UserProfileManager(context)
                val preferencesManager = PreferencesManager(context, userProfileManager)
                val syncRepo = SyncRepository(db, NetworkClient.api, preferencesManager)
                kotlinx.coroutines.runBlocking { syncRepo.initialSync(userId) }
            } catch (_: Exception) {}
        }
    }

    companion object {
        private const val REVENUECAT_API_KEY = "test_sZihURArrrNglOatWoBhwCYDQCN"

        @Volatile private var instance: KineticApplication? = null
        fun get(): KineticApplication? = instance
    }
}
