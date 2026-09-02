package com.example.kinetic

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    // ── FCM Token (per-user) ──────────────────────────────────────────

    suspend fun saveFcmToken(userId: String) {
        val token = FirebaseMessaging.getInstance().token.await()
        if (token.isNotBlank()) {
            db.collection("users").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
                .await()
        }
    }

    // ── User Profile (root level of users/{uid}) ──────────────────────

    suspend fun saveUserProfile(userId: String, name: String, photoUri: String, bio: String = "") {
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "userId" to userId
        )
        if (photoUri.isNotBlank()) data["photoUri"] = photoUri
        if (bio.isNotBlank()) data["bio"] = bio
        db.collection("users").document(userId).set(data, SetOptions.merge()).await()
    }

    suspend fun uploadProfilePhoto(context: Context, userId: String, imageUri: Uri): String {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() } ?: return ""
        val photoDir = File(context.filesDir, "profile_photos")
        if (!photoDir.exists()) photoDir.mkdirs()
        val localFile = File(photoDir, "${userId}.jpg")
        localFile.writeBytes(bytes)
        // Upload direct la Supabase Storage (bucket public "profile_photos") via REST.
        // La eșec păstrăm copia locală, ca poza să se vadă oricum pe acest device.
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
                val url = "${AppConstants.SUPABASE_URL}/storage/v1/object/${AppConstants.SUPABASE_PHOTO_BUCKET}/$userId.jpg"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${AppConstants.SUPABASE_ANON_KEY}")
                    .addHeader("apikey", AppConstants.SUPABASE_ANON_KEY)
                    .addHeader("x-upsert", "true")
                    .post(bytes.toRequestBody("image/jpeg".toMediaType()))
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        // Query-ul forțează re-fetch la ceilalți utilizatori (Coil ține cache pe URL).
                        "${AppConstants.SUPABASE_URL}/storage/v1/object/public/${AppConstants.SUPABASE_PHOTO_BUCKET}/$userId.jpg?v=${System.currentTimeMillis()}"
                    } else {
                        android.util.Log.e("PhotoUpload", "upload failed for $userId: HTTP ${response.code} ${response.body?.string().orEmpty()}")
                        Uri.fromFile(localFile).toString()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PhotoUpload", "upload failed for $userId: ${e.message}", e)
                Uri.fromFile(localFile).toString()
            }
        }
    }

    suspend fun deleteProfilePhoto(userId: String) {
        try {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()
                val url = "${AppConstants.SUPABASE_URL}/storage/v1/object/${AppConstants.SUPABASE_PHOTO_BUCKET}/$userId.jpg"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${AppConstants.SUPABASE_ANON_KEY}")
                    .addHeader("apikey", AppConstants.SUPABASE_ANON_KEY)
                    .delete()
                    .build()
                client.newCall(request).execute().use { }
            }
        } catch (_: Exception) {}
    }

    suspend fun saveBio(userId: String, bio: String) {
        db.collection("users").document(userId)
            .set(mapOf("bio" to bio), SetOptions.merge()).await()
    }

    suspend fun deleteUserAccount(userId: String) {
        try {
            deleteProfilePhoto(userId)
        } catch (_: Exception) {}
        try {
            db.collection("users").document(userId).delete().await()
        } catch (_: Exception) {}
        try {
            val collections = listOf("profile", "onboarding", "bodyMetrics", "waterIntake", "settings", "workouts", "badges", "social")
            for (col in collections) {
                try {
                    db.collection("users").document(userId).collection(col).get().await().documents.forEach { it.reference.delete().await() }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    /**
     * Creates or updates the user document at users/{uid}.
     * Uses SetOptions.merge() so we don't overwrite existing data.
     */
    suspend fun createUserDocument(
        userId: String,
        name: String,
        email: String = "",
        weight: Double? = null,
        height: Double? = null,
        loginMethod: String = ""
    ) {
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "streak" to 0
        )
        if (weight != null && weight > 0) {
            data["weight"] = weight
            data["waterGoal"] = (weight * 33).toInt()
        }
        if (height != null && height > 0) data["height"] = height
        if (loginMethod.isNotBlank()) data["loginMethod"] = loginMethod
        db.collection("users").document(userId).set(data, SetOptions.merge()).await()
    }

    suspend fun updateUserDocument(userId: String, data: Map<String, Any>) {
        db.collection("users").document(userId).set(data, SetOptions.merge()).await()
    }

    suspend fun userDocumentExists(userId: String): Boolean {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.exists()
        } catch (_: Exception) { false }
    }

    // ── Profile Subcollection: users/{uid}/profile ────────────────────

    suspend fun saveProfileSubcollection(userId: String, name: String, email: String, photoUrl: String = "", loginMethod: String = "") {
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "loginMethod" to loginMethod,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        if (photoUrl.isNotBlank()) data["photoUrl"] = photoUrl
        db.collection("users").document(userId).collection("profile").document("profile")
            .set(data, SetOptions.merge()).await()
    }

    // ── Onboarding Subcollection: users/{uid}/onboarding ──────────────

    suspend fun saveOnboarding(userId: String, goal: String, experience: String, equipment: String, sessionsPerWeek: Int, limitations: String, muscleGroups: List<String>) {
        val data = mapOf(
            "goal" to goal,
            "experienceLevel" to experience,
            "equipment" to equipment,
            "sessionsPerWeek" to sessionsPerWeek,
            "limitations" to limitations,
            "selectedMuscleGroups" to muscleGroups,
            "isComplete" to true
        )
        db.collection("users").document(userId).collection("onboarding").document("onboarding")
            .set(data, SetOptions.merge()).await()
    }

    // ── Body Metrics Subcollection: users/{uid}/bodyMetrics ───────────

    suspend fun saveBodyMetrics(userId: String, weightKg: Double, heightCm: Double) {
        val data = mapOf(
            "weightKg" to weightKg,
            "heightCm" to heightCm,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("users").document(userId).collection("bodyMetrics").document("current")
            .set(data, SetOptions.merge()).await()
    }

    // ── Water Intake Subcollection: users/{uid}/waterIntake/{date} ────

    suspend fun saveWaterIntake(userId: String, date: String, ml: Int, goalMl: Int) {
        val data = mapOf("ml" to ml, "goalMl" to goalMl)
        db.collection("users").document(userId).collection("waterIntake").document(date)
            .set(data, SetOptions.merge()).await()
    }

    // ── Settings Subcollection: users/{uid}/settings ──────────────────

    suspend fun saveSettings(userId: String, settings: Map<String, Any>) {
        db.collection("users").document(userId).collection("settings").document("settings")
            .set(settings, SetOptions.merge()).await()
    }

    // ── Search (public, for friend discovery) ─────────────────────────

    suspend fun searchUsers(query: String): List<Pair<String, String>> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        val results = mutableListOf<Pair<String, String>>()
        try {
            val snapshot = db.collection("users")
                .orderBy("name")
                .startAt(q).endAt(q + "\uf8ff")
                .limit(20).get().await()
            for (doc in snapshot.documents) {
                results.add(doc.id to (doc.getString("name") ?: ""))
            }
        } catch (_: Exception) { }
        return results
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            db.collection("users").document(userId).get().await().data
        } catch (_: Exception) { null }
    }

    suspend fun getOnboarding(userId: String): Map<String, Any>? {
        return try {
            db.collection("users").document(userId).collection("onboarding").document("onboarding").get().await().data
        } catch (_: Exception) { null }
    }

    suspend fun getBodyMetrics(userId: String): Map<String, Any>? {
        return try {
            db.collection("users").document(userId).collection("bodyMetrics").document("current").get().await().data
        } catch (_: Exception) { null }
    }

    suspend fun getSettings(userId: String): Map<String, Any>? {
        return try {
            db.collection("users").document(userId).collection("settings").document("settings").get().await().data
        } catch (_: Exception) { null }
    }

    suspend fun syncUserStats(userId: String, totalVolume: Double, workoutCount: Int) {
        try {
            db.collection("users").document(userId)
                .set(mapOf("totalVolume" to totalVolume, "workoutCount" to workoutCount, "lastSeen" to System.currentTimeMillis()), SetOptions.merge())
                .await()
        } catch (_: Exception) { }
    }
}
