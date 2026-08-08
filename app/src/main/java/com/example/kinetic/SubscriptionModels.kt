package com.example.kinetic

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ============================================================
// Room Entities (local cache, offline-first)
// ============================================================

@Entity(tableName = "user_subscriptions")
data class UserSubscriptionEntity(
    @PrimaryKey val userId: String,
    val subscriptionType: String = "FREE",       // FREE / PREMIUM_MONTHLY / PREMIUM_ANNUAL / PRO_LIFETIME
    val subscriptionStatus: String = "ACTIVE",   // ACTIVE / EXPIRED / CANCELLED / PENDING
    val expiryDate: Long? = null,
    val isLifetime: Boolean = false,
    val revenueCatId: String = "",
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ad_unlocks",
    indices = [Index("userId"), Index(value = ["userId", "featureId"])]
)
data class AdUnlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val featureId: String,
    val unlockedUntil: Long
)

// ============================================================
// Domain models & enums
// ============================================================

enum class SubscriptionTier(val id: String) {
    FREE("FREE"),
    PREMIUM_MONTHLY("PREMIUM_MONTHLY"),
    PREMIUM_ANNUAL("PREMIUM_ANNUAL"),
    PRO_LIFETIME("PRO_LIFETIME");

    companion object {
        fun fromId(id: String?): SubscriptionTier =
            entries.firstOrNull { it.id == id } ?: FREE
    }
}

/**
 * Every gated feature in the app. `isFree = true` means it is always accessible.
 * `titleKey` maps to a string key in LanguageManager.Strings for localized labels.
 */
enum class PremiumFeature(
    val id: String,
    val titleKey: String,
    val isFree: Boolean = false
) {
    // --- Free tier ---
    WORKOUT_LOGGING("workout_logging", "workouts", isFree = true),
    DASHBOARD("dashboard", "home", isFree = true),
    CALENDAR_VIEW("calendar_view", "calendarView", isFree = true),
    WATER_TRACKING("water_tracking", "water", isFree = true),

    // --- Premium / Pro ---
    AI_TRAINER("ai_trainer", "aiTrainer"),
    WORKOUT_ANALYTICS("workout_analytics", "workoutAnalytics"),
    CUSTOM_EXERCISES("custom_exercises", "customExercises"),
    FOOD_JOURNAL("food_journal", "foodJournal"),
    GPS_CARDIO("gps_cardio", "gpsCardioMap"),
    FRIENDS_SOCIAL("friends_social", "friends"),
    REST_DAYS("rest_days", "restDaysTitle"),
    TEMPLATES("templates", "customTemplates"),
    BIOMETRICS("biometrics", "biometrics"),
    PLATE_CALCULATOR("plate_calculator", "plateCalculator"),
    ONE_RM_CALCULATOR("one_rm_calculator", "oneRmCalculator"),
    ADVANCED_CHARTS("advanced_charts", "advancedCharts"),
    CSV_EXPORT("csv_export", "exportCsv");

    companion object {
        fun fromId(id: String?): PremiumFeature? = entries.firstOrNull { it.id == id }
    }
}

data class UserSubscription(
    val userId: String,
    val tier: SubscriptionTier,
    val status: String,
    val expiryDate: Long?,
    val isLifetime: Boolean,
    val activeAdUnlocks: List<AdUnlockEntity>,
    val lastSyncedAt: Long
) {
    val isActive: Boolean
        get() = isLifetime || (status == "ACTIVE" && (expiryDate?.let { expiry ->
            if (expiry > System.currentTimeMillis()) true
            else if (!isLifetime && expiry >= System.currentTimeMillis() - 24L * 60 * 60 * 1000L) true
            else false
        } ?: false))

    val isPremium: Boolean
        get() = tier != SubscriptionTier.FREE && isActive

    fun hasAccess(feature: PremiumFeature, email: String? = null): Boolean {
        if (AdminManager.isAdmin(email)) return true
        if (feature.isFree) return true
        if (isLifetime) return true
        if (isActive) return true
        return activeAdUnlocks.any {
            it.featureId == feature.id && it.unlockedUntil > System.currentTimeMillis()
        }
    }

    fun adUnlockUntil(feature: PremiumFeature): Long? =
        activeAdUnlocks.firstOrNull {
            it.featureId == feature.id && it.unlockedUntil > System.currentTimeMillis()
        }?.unlockedUntil

    companion object {
        fun free(userId: String) = UserSubscription(
            userId = userId,
            tier = SubscriptionTier.FREE,
            status = "ACTIVE",
            expiryDate = null,
            isLifetime = false,
            activeAdUnlocks = emptyList(),
            lastSyncedAt = 0L
        )
    }
}

/**
 * RevenueCat product identifiers, mapped to Google Play Console products.
 * Configure these in the RevenueCat dashboard (Offerings / Entitlements).
 */
/** Display model for a purchasable plan on the pricing screen. */
data class PricingOption(
    val tier: SubscriptionTier,
    val priceText: String,
    val rcPackage: com.revenuecat.purchases.Package?
)

object BillingProducts {
    const val ENTITLEMENT_KINETIC_PRO = "Kinetic Pro"

    const val OFFERING_DEFAULT = "default"

    const val PRODUCT_PREMIUM_MONTHLY = "monthly"
    const val PRODUCT_PREMIUM_ANNUAL = "yearly"
    const val PRODUCT_PRO_LIFETIME = "lifetime"

    // Duration a rewarded-ad unlock lasts (30 minutes)
    const val AD_UNLOCK_DURATION_MS = 30 * 60 * 1000L

    // Max rewarded-ad unlocks per day
    const val MAX_DAILY_AD_UNLOCKS = 5
}
