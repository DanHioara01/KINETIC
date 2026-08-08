package com.example.kinetic

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import kotlin.random.Random

class UserProfileManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_profiles", Context.MODE_PRIVATE)

    data class UserProfile(
        val userId: String,
        val name: String,
        val photoUri: String = "",
        val shortId: String = "",
        val bio: String = ""
    )

    private fun generateShortId(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return buildString(6) {
            repeat(6) { append(chars[Random.nextInt(chars.length)]) }
        }
    }

    fun getOwnShortId(): String {
        val id = getOwnUserId()
        val existing = prefs.getString("short_id_$id", null)
        if (existing != null) return existing
        val short = generateShortId()
        prefs.edit().putString("short_id_$id", short).apply()
        return short
    }

    fun getShortId(userId: String): String {
        val existing = prefs.getString("short_id_$userId", null)
        if (existing != null) return existing
        val short = generateShortId()
        prefs.edit().putString("short_id_$userId", short).apply()
        return short
    }

    /**
     * Returns the current user's profile. Prefers Firebase UID as the source
     * of truth; falls back to SharedPreferences for offline / pre-login.
     */
    fun getOwnProfile(): UserProfile? {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        val id = firebaseUid ?: prefs.getString("own_user_id", null) ?: return null
        val name = prefs.getString("own_name", "") ?: ""
        val photo = prefs.getString("own_photo", "") ?: ""
        val bio = prefs.getString("own_bio", "") ?: ""
        val shortId = getShortId(id)
        return UserProfile(id, name, photo, shortId, bio)
    }

    fun isProfileComplete(): Boolean {
        val name = prefs.getString("own_name", "") ?: ""
        return name.isNotBlank()
    }

    /**
     * Returns the active userId. Uses Firebase UID when available,
     * otherwise falls back to the stored own_user_id.
     */
    fun getOwnUserId(): String {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        return firebaseUid ?: prefs.getString("own_user_id", null) ?: "local_user"
    }

    fun getOwnBio(): String {
        return prefs.getString("own_bio", "") ?: ""
    }

    fun saveBio(bio: String) {
        val userId = getOwnUserId()
        prefs.edit().putString("own_bio", bio).apply()
        val json = prefs.getString("known_profiles", "{}") ?: "{}"
        val obj = JSONObject(json)
        if (obj.has(userId)) {
            obj.getJSONObject(userId).put("bio", bio)
            prefs.edit().putString("known_profiles", obj.toString()).apply()
        }
    }

    fun createOrUpdateProfile(name: String, photoUri: String = "", userId: String = getOwnUserId(), bio: String = "") {
        val json = prefs.getString("known_profiles", "{}") ?: "{}"
        val obj = JSONObject(json)
        val entry = JSONObject().put("name", name).put("photo", photoUri).put("bio", bio)
        obj.put(userId, entry)
        prefs.edit()
            .putString("known_profiles", obj.toString())
            .putString("own_user_id", userId)
            .putString("own_name", name)
            .putString("own_photo", photoUri)
            .putString("own_bio", bio)
            .apply()
    }

    fun saveProfile(profile: UserProfile) {
        val json = prefs.getString("known_profiles", "{}") ?: "{}"
        val obj = JSONObject(json)
        val entry = JSONObject().put("name", profile.name).put("photo", profile.photoUri).put("bio", profile.bio)
        obj.put(profile.userId, entry)
        prefs.edit().putString("known_profiles", obj.toString()).apply()
    }

    fun getProfile(userId: String): UserProfile? {
        val json = prefs.getString("known_profiles", "{}") ?: "{}"
        val obj = JSONObject(json)
        if (!obj.has(userId)) return null
        val entry = obj.getJSONObject(userId)
        val shortId = getShortId(userId)
        return UserProfile(userId, entry.optString("name", ""), entry.optString("photo", ""), shortId, entry.optString("bio", ""))
    }

    fun updateProfile(userId: String, name: String, photoUri: String = "", bio: String = "") {
        val json = prefs.getString("known_profiles", "{}") ?: "{}"
        val obj = JSONObject(json)
        if (obj.has(userId)) {
            val entry = obj.getJSONObject(userId)
            entry.put("name", name)
            entry.put("photo", photoUri)
            entry.put("bio", bio)
            obj.put(userId, entry)
            prefs.edit().putString("known_profiles", obj.toString()).apply()
        }
    }

    fun getAllKnownProfiles(): List<UserProfile> {
        val json = prefs.getString("known_profiles", "{}") ?: "{}"
        val obj = JSONObject(json)
        val result = mutableListOf<UserProfile>()
        for (key in obj.keys()) {
            val entry = obj.getJSONObject(key)
            val shortId = getShortId(key)
            result.add(UserProfile(key, entry.optString("name", ""), entry.optString("photo", ""), shortId, entry.optString("bio", "")))
        }
        return result
    }

    fun searchProfiles(query: String): List<UserProfile> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val shortQ = q.removePrefix("#")
        return getAllKnownProfiles().filter {
            it.name.lowercase().contains(q) ||
            it.userId.lowercase().contains(q) ||
            it.shortId.lowercase().contains(shortQ)
        }
    }

    fun saveOwnProfile(name: String, photoUri: String = "", bio: String = "") {
        val userId = getOwnUserId()
        prefs.edit()
            .putString("own_user_id", userId)
            .putString("own_name", name)
            .putString("own_photo", photoUri)
            .putString("own_bio", bio)
            .apply()
        saveProfile(UserProfile(userId, name, photoUri, getShortId(userId), bio))
    }
}

/**
 * Adaugă un sufix de cache-busting pentru Coil fără să strice URL-ul pozei:
 * - URL-uri Firebase Storage (conțin deja `?alt=media&token=...`) → se adaugă `&v=`
 * - URL-uri file:// → fragment `#` (calea fișierului rămâne validă)
 * - restul (http(s), content://) → `?v=`
 *
 * Sufixul schimbă cheia de cache a lui Coil, forțând reîncărcarea noii poze.
 */
internal fun cacheBustedPhotoUrl(url: String, version: Int): String {
    if (url.isBlank() || version <= 0) return url
    return when {
        url.startsWith("file://") || url.startsWith("content://") -> "$url#$version"
        url.contains('?') -> "$url&v=$version"
        else -> "$url?v=$version"
    }
}
