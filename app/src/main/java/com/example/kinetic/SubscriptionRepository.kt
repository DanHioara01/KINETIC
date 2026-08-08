package com.example.kinetic

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Single source of truth for subscription state.
 * Combines Room (offline-first) with Firestore (real-time cross-device updates).
 *
 * Flow of truth:
 *  RevenueCat -> Node.js webhook -> Firestore -> (snapshot listener) -> Room -> UI
 */
class SubscriptionRepository(
    private val db: AppDatabase
) {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val subscriptionDao = db.userSubscriptionDao()
    private val adUnlockDao = db.adUnlockDao()

    private fun subDocRef(userId: String) =
        firestore.collection("users").document(userId)
            .collection("subscription").document("current")

    private fun adUnlocksRef(userId: String) =
        firestore.collection("users").document(userId).collection("adUnlocks")

    /**
     * Emits the current subscription combining Room subscription row + active ad unlocks.
     * Offline-first: reads Room immediately; Firestore listener keeps Room fresh via [startFirestoreListener].
     */
    fun observeSubscription(userId: String): Flow<UserSubscription> {
        return combine(
            subscriptionDao.observeForUser(userId),
            adUnlockDao.observeActiveUnlocks(userId)
        ) { entity, unlocks ->
            if (entity == null) {
                UserSubscription.free(userId).copy(activeAdUnlocks = unlocks)
            } else {
                UserSubscription(
                    userId = userId,
                    tier = SubscriptionTier.fromId(entity.subscriptionType),
                    status = entity.subscriptionStatus,
                    expiryDate = entity.expiryDate,
                    isLifetime = entity.isLifetime,
                    activeAdUnlocks = unlocks,
                    lastSyncedAt = entity.lastSyncedAt
                )
            }
        }
    }

    /**
     * Real-time Firestore listener that mirrors remote subscription changes into Room.
     * Call this after login; remove the returned registration on logout.
     */
    fun startFirestoreListener(
        userId: String,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {
        return subDocRef(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            if (snapshot == null || !snapshot.exists()) return@addSnapshotListener
            val data = snapshot.data ?: return@addSnapshotListener
            val entity = data.toEntity(userId)
            // Persist to Room off the main thread via a fire-and-forget coroutine
            CoroutineScope(Dispatchers.IO).launch {
                subscriptionDao.upsert(entity)
            }
        }
    }

    /** One-shot pull from Firestore into Room (used after purchase / restore / login). */
    suspend fun syncFromFirestore(userId: String) {
        try {
            val snapshot = subDocRef(userId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data
                if (data != null) subscriptionDao.upsert(data.toEntity(userId))
            } else {
                // No remote record yet: ensure a local FREE row exists
                if (subscriptionDao.getForUser(userId) == null) {
                    subscriptionDao.upsert(UserSubscriptionEntity(userId = userId))
                }
            }
            // Pull ad unlocks too
            val unlocks = adUnlocksRef(userId).get().await()
            for (doc in unlocks.documents) {
                val until = (doc.getTimestamp("unlockedUntil"))?.toDate()?.time ?: continue
                if (until > System.currentTimeMillis()) {
                    if (adUnlockDao.getActiveUnlock(userId, doc.id) == null) {
                        adUnlockDao.insert(AdUnlockEntity(userId = userId, featureId = doc.id, unlockedUntil = until))
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "syncFromFirestore failed: ${e.message}")
        }
    }

    /**
     * Persist a subscription update to Room + Firestore.
     * NOTE: for production, purchases should be validated server-side (RevenueCat webhook -> backend
     * -> Firestore). This method is used for local reconciliation after a successful client purchase.
     */
    suspend fun updateSubscription(entity: UserSubscriptionEntity) {
        subscriptionDao.upsert(entity)
        try {
            subDocRef(entity.userId).set(entity.toFirestoreMap(), com.google.firebase.firestore.SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "updateSubscription remote write failed: ${e.message}")
        }
    }

    suspend fun getLocal(userId: String): UserSubscriptionEntity? = subscriptionDao.getForUser(userId)

    /**
     * Reset the local (Room) subscription cache for a user to FREE.
     * Used to clear a subscription that was wrongly cached on the device (cross-login
     * contamination). Does NOT write to Firestore, so it never touches the server-side
     * source of truth.
     */
    suspend fun resetToFree(userId: String) {
        subscriptionDao.upsert(UserSubscriptionEntity(userId = userId))
    }

    // ---------------- Ad unlocks ----------------

    suspend fun saveAdUnlock(userId: String, featureId: String, durationMs: Long) {
        val unlockedUntil = System.currentTimeMillis() + durationMs
        adUnlockDao.insert(AdUnlockEntity(userId = userId, featureId = featureId, unlockedUntil = unlockedUntil))
        try {
            adUnlocksRef(userId).document(featureId)
                .set(mapOf("unlockedUntil" to Timestamp(unlockedUntil / 1000, 0))).await()
        } catch (e: Exception) {
            Log.w(TAG, "saveAdUnlock remote write failed: ${e.message}")
        }
    }

    suspend fun getTodayAdUnlockCount(userId: String): Int {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return adUnlockDao.getTodayUnlockCount(userId, cal.timeInMillis, BillingProducts.AD_UNLOCK_DURATION_MS)
    }

    suspend fun cleanupExpiredUnlocks() = adUnlockDao.cleanupExpired()

    suspend fun getCurrentTier(userId: String): SubscriptionTier {
        val entity = subscriptionDao.getForUser(userId)
        return if (entity != null) SubscriptionTier.fromId(entity.subscriptionType) else SubscriptionTier.FREE
    }

    // ---------------- Mapping helpers ----------------

    private fun Map<String, Any?>.toEntity(userId: String) = UserSubscriptionEntity(
        userId = userId,
        subscriptionType = this["subscriptionType"] as? String ?: "FREE",
        subscriptionStatus = this["subscriptionStatus"] as? String ?: "ACTIVE",
        expiryDate = (this["expiryDate"] as? Timestamp)?.toDate()?.time,
        isLifetime = this["isLifetime"] as? Boolean ?: false,
        revenueCatId = this["revenueCatId"] as? String ?: "",
        lastSyncedAt = System.currentTimeMillis()
    )

    private fun UserSubscriptionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "subscriptionType" to subscriptionType,
        "subscriptionStatus" to subscriptionStatus,
        "expiryDate" to expiryDate?.let { Timestamp(it / 1000, 0) },
        "isLifetime" to isLifetime,
        "revenueCatId" to revenueCatId,
        "updatedAt" to Timestamp.now()
    )

    companion object {
        private const val TAG = "SubscriptionRepo"
    }
}
