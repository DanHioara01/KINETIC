package com.example.kinetic

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subscription: UserSubscriptionEntity)

    @Query("SELECT * FROM user_subscriptions WHERE userId = :userId LIMIT 1")
    fun observeForUser(userId: String): Flow<UserSubscriptionEntity?>

    @Query("SELECT * FROM user_subscriptions WHERE userId = :userId LIMIT 1")
    suspend fun getForUser(userId: String): UserSubscriptionEntity?

    @Query("DELETE FROM user_subscriptions WHERE userId = :userId")
    suspend fun deleteForUser(userId: String)
}

@Dao
interface AdUnlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unlock: AdUnlockEntity): Long

    @Query("SELECT * FROM ad_unlocks WHERE userId = :userId AND featureId = :featureId AND unlockedUntil > :now ORDER BY unlockedUntil DESC LIMIT 1")
    suspend fun getActiveUnlock(userId: String, featureId: String, now: Long = System.currentTimeMillis()): AdUnlockEntity?

    @Query("SELECT * FROM ad_unlocks WHERE userId = :userId AND unlockedUntil > :now")
    suspend fun getActiveUnlocks(userId: String, now: Long = System.currentTimeMillis()): List<AdUnlockEntity>

    @Query("SELECT * FROM ad_unlocks WHERE userId = :userId AND unlockedUntil > :now")
    fun observeActiveUnlocks(userId: String, now: Long = System.currentTimeMillis()): Flow<List<AdUnlockEntity>>

    @Query("DELETE FROM ad_unlocks WHERE unlockedUntil < :now")
    suspend fun cleanupExpired(now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM ad_unlocks WHERE userId = :userId AND id IN (SELECT id FROM ad_unlocks WHERE userId = :userId AND unlockedUntil - :durationMs >= :todayStart)")
    suspend fun getTodayUnlockCount(userId: String, todayStart: Long, durationMs: Long): Int
}
