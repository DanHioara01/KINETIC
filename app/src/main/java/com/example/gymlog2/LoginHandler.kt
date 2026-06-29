package com.example.gymlog2

import android.content.Context
import com.example.gymlog2.AppConstants.FACEBOOK_USER_NAME
import com.example.gymlog2.AppConstants.GOOGLE_USER_NAME
import com.example.gymlog2.AppConstants.GUEST_USER_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoginHandler(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val userProfileManager: UserProfileManager,
    private val authManager: AuthManager,
    private val coroutineScope: CoroutineScope
) {
    data class LoginResult(
        val userId: String,
        val name: String,
        val photoUri: String
    )

    private suspend fun generateUserId(): String {
        val db = AppDatabase.getDatabase(context)
        while (true) {
            val newId = (100000..999999).random().toString()
            if (db.userProfileDao().getByUserId(newId) == null) return newId
        }
    }

    private suspend fun resolveUserId(loginKey: String, fallbackName: String, photoUri: String = ""): LoginResult {
        val prefs = context.getSharedPreferences("user_profiles", Context.MODE_PRIVATE)
        val db = AppDatabase.getDatabase(context)

        val savedId = prefs.getString("uid_map_$loginKey", null)
        if (savedId != null) {
            return LoginResult(savedId, fallbackName, photoUri)
        }

        val existing = db.userProfileDao().getByLoginKey(loginKey)
        if (existing != null) {
            prefs.edit().putString("uid_map_$loginKey", existing.userId).apply()
            return LoginResult(existing.userId, existing.name, existing.photoUri)
        }

        val newId = generateUserId()
        db.userProfileDao().upsert(
            UserProfileEntity(
                userId = newId,
                loginKey = loginKey,
                name = fallbackName,
                photoUri = photoUri
            )
        )
        prefs.edit().putString("uid_map_$loginKey", newId).apply()
        return LoginResult(newId, fallbackName, photoUri)
    }

    private fun LoginResult.completeLogin(method: String) {
        preferencesManager.setLoggedIn(true)
        preferencesManager.setLoginMethod(method)
        userProfileManager.createOrUpdateProfile(
            name = this.name,
            photoUri = this.photoUri,
            userId = this.userId
        )
        syncToBackend(this.userId, this.name, this.photoUri)
    }

    private fun syncToBackend(userId: String, name: String, photoUri: String) {
        coroutineScope.launch {
            try {
                SocialRepository(AppDatabase.getDatabase(context))
                    .syncUserProfile(userId, name, photoUri)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun loginWithEmail(email: String): LoginResult {
        val loginKey = "email:$email"
        val result = resolveUserId(loginKey, email.substringBefore("@"))
        result.completeLogin("email")
        return result
    }

    suspend fun loginWithGoogle(idToken: String): Result<LoginResult> {
        val authResult = authManager.signInWithGoogle(idToken)
        return authResult.map { firebaseUser ->
            val loginKey = firebaseUser.uid
            val name = firebaseUser.displayName ?: GOOGLE_USER_NAME
            val photo = firebaseUser.photoUrl?.toString() ?: ""
            val result = resolveUserId(loginKey, name, photo)
            result.completeLogin("google")
            result
        }
    }

    suspend fun loginWithFacebook() {
        val loginKey = "facebook"
        val result = resolveUserId(loginKey, FACEBOOK_USER_NAME)
        result.completeLogin("facebook")
    }

    suspend fun loginAsGuest() {
        val guestKey = preferencesManager.getGuestKey().ifEmpty {
            val key = "guest_${(100000..999999).random()}"
            preferencesManager.setGuestKey(key)
            key
        }

        val db = AppDatabase.getDatabase(context)
        val existing = db.userProfileDao().getByLoginKey(guestKey)
        if (existing != null) {
            preferencesManager.setLoggedIn(true)
            preferencesManager.setLoginMethod("guest")
            userProfileManager.createOrUpdateProfile(
                name = existing.name,
                photoUri = existing.photoUri,
                userId = existing.userId
            )
            syncToBackend(existing.userId, existing.name, existing.photoUri)
        } else {
            val result = resolveUserId(guestKey, GUEST_USER_NAME)
            result.completeLogin("guest")
        }
    }
}
