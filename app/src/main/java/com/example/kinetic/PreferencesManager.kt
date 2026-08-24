package com.example.kinetic

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class WaterAlarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

/**
 * Stores per-account app data (theme, onboarding, water intake, weight, deload settings, etc.).
 *
 * IMPORTANT: All data here is scoped to the currently logged-in account. Internally this class
 * keeps a small device-wide prefs file just to remember login state itself (isLoggedIn,
 * loginMethod, guestKey) — those have to live outside any per-user bucket, since we need them
 * BEFORE we know which user is "current". Everything else (theme, language, units, water,
 * weight, onboarding, deload...) lives in a SharedPreferences file namespaced by the current
 * userId, resolved fresh on every call via [UserProfileManager.getOwnUserId]. This means account
 * switches are picked up immediately, even if this class is a long-lived singleton.
 */
class PreferencesManager(
    private val context: Context,
    private val userProfileManager: UserProfileManager
) {
    // Device-wide: tracks *who* is logged in / how. Must stay outside the per-user bucket,
    // since we need it to even determine the current userId.
    private val sessionPrefs: SharedPreferences =
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    // Per-account bucket: recalculated on every access so account switches apply immediately.
    private fun userPrefs(): SharedPreferences {
        val userId = userProfileManager.getOwnUserId()
        return context.getSharedPreferences("user_data_$userId", Context.MODE_PRIVATE)
    }

    fun getCurrentUserId(): String = userProfileManager.getOwnUserId()

    // ---------- Session / login state (device-wide, NOT per-account) ----------

    fun isLoggedIn(): Boolean = sessionPrefs.getBoolean("logged_in", false)
    fun setLoggedIn(value: Boolean) { sessionPrefs.edit().putBoolean("logged_in", value).apply() }
    fun getLoginMethod(): String = sessionPrefs.getString("login_method", "") ?: ""
    fun setLoginMethod(method: String) { sessionPrefs.edit().putString("login_method", method).apply() }
    fun getGuestKey(): String = sessionPrefs.getString("guest_key", "") ?: ""
    fun setGuestKey(key: String) { sessionPrefs.edit().putString("guest_key", key).apply() }

    /**
     * Call this on logout, BEFORE switching to a different account (e.g. before loginAsGuest()
     * after a Google logout). Clears the session flags so no stale login state leaks between
     * accounts. Per-account data itself is untouched — it's already isolated per userId, so the
     * old account's data will simply be there again if that account logs back in.
     */
    fun clearSession() {
        sessionPrefs.edit()
            .putBoolean("logged_in", false)
            .putString("login_method", "")
            .apply()
    }

    // ---------- Theme / language / units (now per-account) ----------

    fun getThemeMode(): ThemeMode {
        return try {
            ThemeMode.valueOf(userPrefs().getString("theme_mode", "SYSTEM") ?: "SYSTEM")
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        userPrefs().edit().putString("theme_mode", mode.name).apply()
    }

    fun getLanguage(): String {
        return userPrefs().getString("language", "ro") ?: "ro"
    }

    fun setLanguage(code: String) {
        userPrefs().edit().putString("language", code).apply()
    }

    fun isLbs(): Boolean = userPrefs().getBoolean("use_lbs", false)
    fun setLbs(value: Boolean) { userPrefs().edit().putBoolean("use_lbs", value).apply() }

    // ---------- Onboarding (per-account) ----------

    fun isOnboardingComplete(): Boolean = userPrefs().getBoolean("onboarding_complete", false)
    fun setOnboardingComplete(value: Boolean) { userPrefs().edit().putBoolean("onboarding_complete", value).apply() }
    fun getFitnessGoal(): String = userPrefs().getString("fitness_goal", "") ?: ""
    fun setFitnessGoal(goal: String) { userPrefs().edit().putString("fitness_goal", goal).apply() }

    fun getExperienceLevel(): String = userPrefs().getString("experience_level", "") ?: ""
    fun setExperienceLevel(level: String) { userPrefs().edit().putString("experience_level", level).apply() }

    fun getEquipmentAvailable(): String = userPrefs().getString("equipment_available", "") ?: ""
    fun setEquipmentAvailable(equipment: String) { userPrefs().edit().putString("equipment_available", equipment).apply() }

    fun getSessionsPerWeek(): Int = userPrefs().getInt("sessions_per_week", 3)
    fun setSessionsPerWeek(count: Int) { userPrefs().edit().putInt("sessions_per_week", count).apply() }

    fun getSelectedDays(): List<String> {
        val raw = userPrefs().getString("selected_days", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }
    fun setSelectedDays(days: List<String>) {
        userPrefs().edit().putString("selected_days", days.joinToString(",")).apply()
    }

    fun getPhysicalLimitations(): String = userPrefs().getString("physical_limitations", "") ?: ""
    fun setPhysicalLimitations(limitations: String) { userPrefs().edit().putString("physical_limitations", limitations).apply() }

    fun getSelectedMuscleGroups(): String = userPrefs().getString("selected_muscle_groups", "") ?: ""
    fun setSelectedMuscleGroups(groups: List<String>) {
        userPrefs().edit().putString("selected_muscle_groups", groups.joinToString(",")).apply()
    }

    fun getOnboardingProfile(): UserOnboardingProfile {
        val groupsStr = getSelectedMuscleGroups()
        val groups = if (groupsStr.isBlank()) emptyList() else groupsStr.split(",")
        return UserOnboardingProfile(
            goal = getFitnessGoal(),
            experience = getExperienceLevel(),
            equipment = getEquipmentAvailable(),
            sessionsPerWeek = getSessionsPerWeek(),
            selectedDays = getSelectedDays(),
            limitations = getPhysicalLimitations(),
            selectedGroups = groups,
            age = getUserAge(),
            gender = getUserGender(),
            activityLevel = getActivityLevel(),
            weight = getUserWeight(),
            height = getUserHeight()
        )
    }

    fun resetOnboarding() {
        userPrefs().edit()
            .putBoolean("onboarding_complete", false)
            .putString("fitness_goal", "")
            .putString("experience_level", "")
            .putString("equipment_available", "")
            .putInt("sessions_per_week", 3)
            .putString("selected_days", "")
            .putString("physical_limitations", "")
            .putString("selected_muscle_groups", "")
            .remove("workout_start_date_epoch")
            .apply()
    }

    fun getWorkoutStartDate(): LocalDate {
        val epochDay = userPrefs().getLong("workout_start_date_epoch", -1L)
        return if (epochDay >= 0) LocalDate.ofEpochDay(epochDay) else LocalDate.now()
    }

    fun setWorkoutStartDate(date: LocalDate) {
        userPrefs().edit().putLong("workout_start_date_epoch", date.toEpochDay()).apply()
    }

    // ---------- Body metrics (per-account) ----------

    fun getUserWeight(): Float = userPrefs().getFloat("user_weight", 70f)
    fun setUserWeight(kg: Float) { userPrefs().edit().putFloat("user_weight", kg).apply() }

    fun getUserHeight(): Float = userPrefs().getFloat("user_height", 170f)
    fun setUserHeight(cm: Float) { userPrefs().edit().putFloat("user_height", cm).apply() }

    fun getUserAge(): Int = userPrefs().getInt("user_age", 25)
    fun setUserAge(age: Int) { userPrefs().edit().putInt("user_age", age).apply() }

    fun getUserGender(): String = userPrefs().getString("user_gender", "") ?: ""
    fun setUserGender(gender: String) { userPrefs().edit().putString("user_gender", gender).apply() }

    fun getActivityLevel(): String = userPrefs().getString("activity_level", "sedentary") ?: "sedentary"
    fun setActivityLevel(level: String) { userPrefs().edit().putString("activity_level", level).apply() }

    // ---------- Steps (per-account) ----------

    fun getStepGoal(): Int = userPrefs().getInt("step_goal", 7000)
    fun setStepGoal(goal: Int) { userPrefs().edit().putInt("step_goal", goal).apply() }

    // ---------- Sleep (manual, pentru Readiness) ----------

    fun getSleepHours(): Double = userPrefs().getFloat("sleep_hours_last", 7.5f).toDouble()
    fun setSleepHours(hours: Double) { userPrefs().edit().putFloat("sleep_hours_last", hours.toFloat().coerceIn(0f, 12f)).apply() }
    fun getSleepQuality(): Int = userPrefs().getInt("sleep_quality_last", 3)
    fun setSleepQuality(quality: Int) { userPrefs().edit().putInt("sleep_quality_last", quality.coerceIn(1, 5)).apply() }

    // ---------- Readiness score history (7 days) ----------
    fun saveReadinessScore(score: Int) {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        userPrefs().edit().putInt("readiness_$dayKey", score.coerceIn(0, 100)).apply()
    }

    fun getReadinessHistory7Days(): List<Pair<String, Int>> {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val dayFmt = java.text.SimpleDateFormat("EEE", java.util.Locale(java.util.Locale.US.language))
        val result = mutableListOf<Pair<String, Int>>()
        val p = userPrefs()
        for (i in 6 downTo 0) {
            val c = java.util.Calendar.getInstance()
            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val key = fmt.format(c.time)
            val score = p.getInt("readiness_$key", -1)
            val dayName = dayFmt.format(c.time).take(2)
            result.add(dayName to score)
        }
        return result
    }

    fun getTodaySteps(): Int {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return userPrefs().getInt("steps_$dayKey", 0)
    }

    fun setTodaySteps(steps: Int) {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        userPrefs().edit().putInt("steps_$dayKey", steps.coerceAtLeast(0)).apply()
    }

    // ---------- Today's cardio baseline (saved GPS routes) ----------
    // The home page keeps a "before the current session" baseline in memory and adds
    // the live session on top. Persisting it lets the home-screen widget show exactly
    // the same numbers (and avoids double-counting a route saved mid-session).

    fun getTodayCardioBaselineDist(): Double {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return userPrefs().getFloat("cardio_base_dist_$dayKey", -1f).toDouble()
    }

    fun getTodayCardioBaselineDur(): Long {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return userPrefs().getLong("cardio_base_dur_$dayKey", -1L)
    }

    fun getTodayCardioBaselineCal(): Double {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return userPrefs().getFloat("cardio_base_cal_$dayKey", -1f).toDouble()
    }

    fun hasTodayCardioBaseline(): Boolean {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return userPrefs().contains("cardio_base_dist_$dayKey")
    }

    fun setTodayCardioBaseline(distKm: Double, durationMs: Long, calories: Double) {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        userPrefs().edit()
            .putFloat("cardio_base_dist_$dayKey", distKm.toFloat())
            .putLong("cardio_base_dur_$dayKey", durationMs)
            .putFloat("cardio_base_cal_$dayKey", calories.toFloat())
            .apply()
    }

    // ---------- Today's cardio summary cache (for the home-screen widget) ----------
    // The Glance widget composition runs on the main thread and cannot perform a
    // blocking Room query, so GlanceAppWidget.provideGlance (which can suspend) caches
    // today's cardio route summary here. The widget composition reads this snapshot;
    // whenever the app saves a route it also writes a baseline above, which the widget
    // prefers because it is fresher.

    private fun todayKey(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    fun setTodayCachedCardioSummary(summary: CardioSummary) {
        val dayKey = todayKey()
        userPrefs().edit()
            .putFloat("cardio_cache_dist_$dayKey", (summary.totalDistance ?: 0.0).toFloat())
            .putLong("cardio_cache_dur_$dayKey", summary.totalDuration ?: 0L)
            .putFloat("cardio_cache_cal_$dayKey", (summary.totalCalories ?: 0.0).toFloat())
            .apply()
    }

    fun getTodayCachedCardioSummary(): CardioSummary? {
        val dayKey = todayKey()
        val p = userPrefs()
        if (!p.contains("cardio_cache_dist_$dayKey")) return null
        return CardioSummary(
            totalDistance = p.getFloat("cardio_cache_dist_$dayKey", 0f).toDouble(),
            totalDuration = p.getLong("cardio_cache_dur_$dayKey", 0L),
            totalCalories = p.getFloat("cardio_cache_cal_$dayKey", 0f).toDouble()
        )
    }

    // ---------- Water intake (per-account) ----------

    fun getWaterGoalMl(): Int {
        val custom = userPrefs().getInt("water_goal_custom", 0)
        if (custom > 0) return custom
        val weight = getUserWeight()
        val raw = (weight * 33).toInt()
        return (raw / 50) * 50
    }

    fun setWaterGoalMl(goal: Int) {
        userPrefs().edit().putInt("water_goal_custom", goal).apply()
    }

    fun getWaterStreakDays(): Int {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        var streak = 0
        val cal = java.util.Calendar.getInstance()
        while (true) {
            val key = fmt.format(cal.time)
            val ml = userPrefs().getInt("water_$key", 0)
            val goal = getWaterGoalMl()
            if (ml >= goal && goal > 0) {
                streak++
                cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun getTodayWaterMl(): Int {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        return userPrefs().getInt("water_$dayKey", 0)
    }

    fun addWaterMl(ml: Int) {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val current = getTodayWaterMl()
        userPrefs().edit().putInt("water_$dayKey", current + ml).apply()
    }

    fun resetTodayWaterMl() {
        val dayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        userPrefs().edit().putInt("water_$dayKey", 0).apply()
    }

    fun getWaterHistory7Days(): List<Pair<String, Int>> {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val dayFmt = java.text.SimpleDateFormat("EEE", java.util.Locale(LanguageManager.getLanguage().ifEmpty { "en" }))
        val result = mutableListOf<Pair<String, Int>>()
        val prefs = userPrefs()
        for (i in 6 downTo 0) {
            val c = java.util.Calendar.getInstance()
            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val key = fmt.format(c.time)
            val ml = prefs.getInt("water_$key", 0)
            val dayName = dayFmt.format(c.time).take(3)
            result.add(dayName to ml)
        }
        return result
    }

    fun isWaterReminderEnabled(): Boolean = userPrefs().getBoolean("water_reminder_enabled", false)
    fun setWaterReminderEnabled(enabled: Boolean) { userPrefs().edit().putBoolean("water_reminder_enabled", enabled).apply() }

    fun getWaterReminders(): List<WaterAlarm> {
        val json = userPrefs().getString("water_reminders_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                WaterAlarm(
                    id = obj.getInt("id"),
                    hour = obj.getInt("hour"),
                    minute = obj.getInt("minute"),
                    enabled = obj.getBoolean("enabled")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveWaterReminders(alarms: List<WaterAlarm>) {
        val arr = JSONArray()
        alarms.forEach { alarm ->
            arr.put(JSONObject().apply {
                put("id", alarm.id)
                put("hour", alarm.hour)
                put("minute", alarm.minute)
                put("enabled", alarm.enabled)
            })
        }
        userPrefs().edit().putString("water_reminders_json", arr.toString()).apply()
    }

    fun addWaterReminder(hour: Int, minute: Int): WaterAlarm {
        val alarms = getWaterReminders().toMutableList()
        val newId = (alarms.maxOfOrNull { it.id } ?: 0) + 1
        val alarm = WaterAlarm(id = newId, hour = hour, minute = minute, enabled = true)
        alarms.add(alarm)
        saveWaterReminders(alarms)
        return alarm
    }

    fun updateWaterReminder(id: Int, hour: Int, minute: Int) {
        val alarms = getWaterReminders().map {
            if (it.id == id) it.copy(hour = hour, minute = minute) else it
        }
        saveWaterReminders(alarms)
    }

    fun toggleWaterReminder(id: Int, enabled: Boolean) {
        val alarms = getWaterReminders().map {
            if (it.id == id) it.copy(enabled = enabled) else it
        }
        saveWaterReminders(alarms)
    }

    fun deleteWaterReminder(id: Int) {
        val alarms = getWaterReminders().filter { it.id != id }
        saveWaterReminders(alarms)
    }

    // ---------- Reminders / training settings (per-account) ----------

    fun isBiometricReminderEnabled(): Boolean = userPrefs().getBoolean("biometric_reminder_enabled", false)
    fun setBiometricReminderEnabled(enabled: Boolean) { userPrefs().edit().putBoolean("biometric_reminder_enabled", enabled).apply() }

    fun isAutoDeloadEnabled(): Boolean = userPrefs().getBoolean("auto_deload_enabled", false)
    fun setAutoDeloadEnabled(enabled: Boolean) { userPrefs().edit().putBoolean("auto_deload_enabled", enabled).apply() }

    fun getDeloadIntervalWeeks(): Int = userPrefs().getInt("deload_interval_weeks", 4)
    fun setDeloadIntervalWeeks(weeks: Int) { userPrefs().edit().putInt("deload_interval_weeks", weeks).apply() }

    fun getDeloadReductionFactor(): Float = userPrefs().getFloat("deload_reduction_factor", 0.65f)
    fun setDeloadReductionFactor(factor: Float) { userPrefs().edit().putFloat("deload_reduction_factor", factor).apply() }

    fun getLastDeloadTimestamp(): Long = userPrefs().getLong("last_deload_timestamp", 0L)
    fun setLastDeloadTimestamp(timestamp: Long) { userPrefs().edit().putLong("last_deload_timestamp", timestamp).apply() }

    fun getProgressionType(): String = userPrefs().getString("progression_type", "linear") ?: "linear"
    fun setProgressionType(type: String) { userPrefs().edit().putString("progression_type", type).apply() }

    fun getDefaultRepRangeMin(): Int = userPrefs().getInt("default_rep_range_min", 8)
    fun setDefaultRepRangeMin(min: Int) { userPrefs().edit().putInt("default_rep_range_min", min).apply() }

    fun getDefaultRepRangeMax(): Int = userPrefs().getInt("default_rep_range_max", 12)
    fun setDefaultRepRangeMax(max: Int) { userPrefs().edit().putInt("default_rep_range_max", max).apply() }

    fun getDeloadThreshold(): Float = userPrefs().getFloat("deload_threshold", 0.55f)
    fun setDeloadThreshold(threshold: Float) { userPrefs().edit().putFloat("deload_threshold", threshold).apply() }

    fun isDeloadActive(): Boolean = userPrefs().getBoolean("deload_active", false)
    fun setDeloadActive(active: Boolean) { userPrefs().edit().putBoolean("deload_active", active).apply() }

    fun getDeloadStartDate(): Long = userPrefs().getLong("deload_start_date", 0L)
    fun setDeloadStartDate(timestamp: Long) { userPrefs().edit().putLong("deload_start_date", timestamp).apply() }

    fun getDeloadEndDate(): Long = userPrefs().getLong("deload_end_date", 0L)
    fun setDeloadEndDate(timestamp: Long) { userPrefs().edit().putLong("deload_end_date", timestamp).apply() }

    fun getDeloadReason(): String = userPrefs().getString("deload_reason", "") ?: ""
    fun setDeloadReason(reason: String) { userPrefs().edit().putString("deload_reason", reason).apply() }

    fun hasSeenDeloadInfo(): Boolean = userPrefs().getBoolean("seen_deload_info", false)
    fun setSeenDeloadInfo(seen: Boolean) { userPrefs().edit().putBoolean("seen_deload_info", seen).apply() }

    fun getLastSyncTimestamp(table: String): Long = userPrefs().getLong("sync_ts_$table", 0L)
    fun setLastSyncTimestamp(table: String, ts: Long) { userPrefs().edit().putLong("sync_ts_$table", ts).apply() }

    // ---------- App-wide config (NOT per-account: server URL, API keys, OAuth client id) ----------
    // These are configuration for the app installation itself, not user data, so they
    // intentionally stay in the device-wide session bucket rather than per-account storage.

    fun getServerUrl(): String = sessionPrefs.getString("server_url", "") ?: ""
    fun setServerUrl(url: String) { sessionPrefs.edit().putString("server_url", url).apply() }

    fun getAiApiKey(): String = sessionPrefs.getString("ai_api_key", "") ?: ""
    fun setAiApiKey(key: String) { sessionPrefs.edit().putString("ai_api_key", key).apply() }

    fun getGoogleOAuthClientId(): String = sessionPrefs.getString("google_oauth_client_id", "") ?: ""
    fun setGoogleOAuthClientId(id: String) { sessionPrefs.edit().putString("google_oauth_client_id", id).apply() }

    // ---------- Welcome sound (device-wide) ----------
    fun isWelcomeSoundEnabled(): Boolean = sessionPrefs.getBoolean("welcome_sound_enabled", true)
    fun setWelcomeSoundEnabled(enabled: Boolean) { sessionPrefs.edit().putBoolean("welcome_sound_enabled", enabled).apply() }

    // ---------- Migration ----------

    /**
     * One-time migration: copies data from the legacy "theme_prefs" file into the current
     * user's per-account bucket. Safe to call multiple times — once migrated, the old file's
     * "migrated" flag is set and subsequent calls are no-ops.
     */
    fun migrateLegacyDataIfNeeded() {
        val legacy = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val migrated = legacy.getBoolean("migrated_to_per_user", false)
        if (migrated) return

        val user = userPrefs()
        val editor = user.edit()

        val keysToMigrate = listOf(
            "theme_mode", "language", "use_lbs",
            "onboarding_complete", "fitness_goal", "experience_level",
            "equipment_available", "sessions_per_week", "selected_days", "physical_limitations",
            "selected_muscle_groups", "user_weight", "user_height",
            "user_age", "user_gender", "activity_level",
            "water_reminder_enabled", "water_reminders_json",
            "biometric_reminder_enabled",
            "auto_deload_enabled", "deload_interval_weeks", "deload_reduction_factor",
            "last_deload_timestamp", "progression_type",
            "default_rep_range_min", "default_rep_range_max", "deload_threshold"
        )

        for (key in keysToMigrate) {
            val all = legacy.all
            if (all.containsKey(key) && !user.contains(key)) {
                when (val value = all[key]) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is String -> editor.putString(key, value)
                }
            }
        }

        editor.apply()
        legacy.edit().putBoolean("migrated_to_per_user", true).apply()
    }
}