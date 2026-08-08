package com.example.kinetic

class FeatureFlagRepository(private val db: AppDatabase) {
    suspend fun setFlag(key: String, enabled: Boolean) {
        db.featureFlagDao().upsert(FeatureFlagEntity(key, enabled))
    }

    suspend fun isEnabled(key: String): Boolean {
        return db.featureFlagDao().getFlag(key)?.enabled ?: false
    }

    suspend fun getAllFlags(): List<FeatureFlagEntity> {
        return db.featureFlagDao().getAll()
    }
}
