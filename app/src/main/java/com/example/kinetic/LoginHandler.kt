package com.example.kinetic

import android.content.Context
import android.util.Log
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
        val photoUri: String,
        val isNewUser: Boolean = false
    )

    /**
     * Core login completion: sets session state, persists profile locally,
     * syncs to Firestore and backend. Firebase UID is the single source of truth.
     */
    private fun LoginResult.completeLogin(method: String) {
        preferencesManager.setLoggedIn(true)
        preferencesManager.setLoginMethod(method)
        userProfileManager.createOrUpdateProfile(
            name = this.name,
            photoUri = this.photoUri,
            userId = this.userId
        )
        // Notify the reactive userId provider so all collectors re-query
        CurrentUserProvider.getInstance().refresh()
        syncToFirestoreAndBackend(this.userId, this.name, this.photoUri, method)
    }

    private fun syncToFirestoreAndBackend(userId: String, name: String, photoUri: String, method: String) {
        coroutineScope.launch {
            try {
                val firestoreHelper = FirestoreHelper()
                firestoreHelper.createUserDocument(
                    userId = userId,
                    name = name,
                    email = authManager.getEmail(),
                    loginMethod = method
                )
            } catch (e: Exception) {
                Log.w(TAG, "Firestore profile sync failed for userId=$userId", e)
            }
            try {
                SocialRepository(AppDatabase.getDatabase(context))
                    .syncUserProfile(userId, name, photoUri)
            } catch (e: Exception) {
                Log.w(TAG, "Backend profile sync failed for userId=$userId", e)
            }
        }
    }

    // ── Email / Password ──────────────────────────────────────────────

    suspend fun loginWithEmail(email: String, password: String): Result<LoginResult> {
        val emailErr = authManager.validateEmail(email)
        if (emailErr != null) return Result.failure(AuthManager.AuthException(emailErr, emailErr))
        val passErr = authManager.validatePassword(password)
        if (passErr != null) return Result.failure(AuthManager.AuthException(passErr, passErr))

        val authResult = authManager.signInWithEmail(email, password)
        return authResult.map { firebaseUser ->
            val name = firebaseUser.displayName ?: email.substringBefore("@")
            val photo = firebaseUser.photoUrl?.toString() ?: ""
            val result = LoginResult(
                userId = firebaseUser.uid,
                name = name,
                photoUri = photo
            )
            result.completeLogin("email")
            result
        }
    }

    // ── Google ────────────────────────────────────────────────────────

    suspend fun loginWithGoogle(idToken: String): Result<LoginResult> {
        val authResult = authManager.signInWithGoogle(idToken)
        return authResult.map { firebaseUser ->
            val name = firebaseUser.displayName ?: "Google User"
            val photo = firebaseUser.photoUrl?.toString() ?: ""
            val result = LoginResult(
                userId = firebaseUser.uid,
                name = name,
                photoUri = photo
            )
            result.completeLogin("google")
            result
        }
    }

    // ── Facebook ──────────────────────────────────────────────────────

    suspend fun loginWithFacebook(token: String): Result<LoginResult> {
        val authResult = authManager.signInWithFacebook(token)
        return authResult.map { firebaseUser ->
            val name = firebaseUser.displayName ?: "Facebook User"
            val photo = firebaseUser.photoUrl?.toString() ?: ""
            val result = LoginResult(
                userId = firebaseUser.uid,
                name = name,
                photoUri = photo
            )
            result.completeLogin("facebook")
            result
        }
    }

    // ── Guest / Anonymous ─────────────────────────────────────────────

    suspend fun loginAsGuest(): Result<LoginResult> {
        val authResult = authManager.signInAnonymously()
        return authResult.map { firebaseUser ->
            val result = LoginResult(
                userId = firebaseUser.uid,
                name = "Guest",
                photoUri = ""
            )
            result.completeLogin("guest")
            result
        }
    }

    // ── Account Linking (preserve UID for guest → real account) ───────

    /**
     * Links the current anonymous account with email credentials.
     * The Firebase UID stays the same — all existing data is preserved.
     */
    suspend fun linkGuestWithEmail(email: String, password: String): Result<LoginResult> {
        val emailErr = authManager.validateEmail(email)
        if (emailErr != null) return Result.failure(AuthManager.AuthException(emailErr, emailErr))
        val passErr = authManager.validatePassword(password)
        if (passErr != null) return Result.failure(AuthManager.AuthException(passErr, passErr))

        val linkResult = authManager.linkWithEmailCredential(email, password)
        return linkResult.map { firebaseUser ->
            val name = firebaseUser.displayName ?: email.substringBefore("@")
            val result = LoginResult(
                userId = firebaseUser.uid,
                name = name,
                photoUri = ""
            )
            result.completeLogin("email")
            result
        }
    }

    suspend fun linkGuestWithGoogle(idToken: String): Result<LoginResult> {
        val linkResult = authManager.linkWithGoogle(idToken)
        return linkResult.map { firebaseUser ->
            val name = firebaseUser.displayName ?: "Google User"
            val photo = firebaseUser.photoUrl?.toString() ?: ""
            val result = LoginResult(
                userId = firebaseUser.uid,
                name = name,
                photoUri = photo
            )
            result.completeLogin("google")
            result
        }
    }

    suspend fun linkGuestWithFacebook(token: String): Result<LoginResult> {
        val linkResult = authManager.linkWithFacebook(token)
        return linkResult.map { firebaseUser ->
            val name = firebaseUser.displayName ?: "Facebook User"
            val photo = firebaseUser.photoUrl?.toString() ?: ""
            val result = LoginResult(
                userId = firebaseUser.uid,
                name = name,
                photoUri = photo
            )
            result.completeLogin("facebook")
            result
        }
    }

    companion object {
        private const val TAG = "LoginHandler"
    }
}
