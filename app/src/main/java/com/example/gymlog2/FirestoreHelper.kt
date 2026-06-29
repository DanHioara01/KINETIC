package com.example.gymlog2

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveFcmToken(userId: String) {
        val token = FirebaseMessaging.getInstance().token.await()
        if (token.isNotBlank()) {
            db.collection("users").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .await()
        }
    }

    suspend fun saveUserProfile(userId: String, name: String, photoUri: String) {
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "userId" to userId
        )
        if (photoUri.isNotBlank()) data["photoUri"] = photoUri
        db.collection("users").document(userId).set(data, SetOptions.merge()).await()
    }

    suspend fun createUserDocument(
        userId: String,
        name: String,
        email: String,
        weight: Double? = null,
        height: Double? = null
    ) {
        val waterGoal = if (weight != null && weight > 0) (weight * 33).toInt() else 2000
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "weight" to (weight ?: 0),
            "height" to (height ?: 0),
            "waterGoal" to waterGoal,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "streak" to 0,
            "measurements" to emptyList<Any>()
        )
        db.collection("users").document(userId).set(data, SetOptions.merge()).await()
    }

    suspend fun updateUserDocument(userId: String, data: Map<String, Any>) {
        db.collection("users").document(userId).set(data, SetOptions.merge()).await()
    }

    suspend fun userDocumentExists(userId: String): Boolean {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.exists()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun searchUsers(query: String): List<Pair<String, String>> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        val results = mutableListOf<Pair<String, String>>()
        try {
            val snapshot = db.collection("users")
                .orderBy("name")
                .startAt(q)
                .endAt(q + "\uf8ff")
                .limit(20)
                .get()
                .await()
            for (doc in snapshot.documents) {
                val userId = doc.id
                val name = (doc.getString("name") ?: "")
                results.add(userId to name)
            }
        } catch (_: Exception) { }
        return results
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            db.collection("users").document(userId).get().await().data
        } catch (_: Exception) { null }
    }

    suspend fun syncUserStats(userId: String, totalVolume: Double, workoutCount: Int) {
        try {
            db.collection("users").document(userId)
                .set(
                    mapOf(
                        "totalVolume" to totalVolume,
                        "workoutCount" to workoutCount,
                        "lastSeen" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
        } catch (_: Exception) { }
    }
}
