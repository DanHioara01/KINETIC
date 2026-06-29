package com.example.gymlog2

import android.content.Context
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isAnonymous: Boolean get() = currentUser?.isAnonymous == true

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkWithEmailCredential(email: String, password: String): Result<FirebaseUser> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val result = user.linkWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkWithGoogle(idToken: String): Result<FirebaseUser> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = user.linkWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun linkWithFacebook(token: String): Result<FirebaseUser> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
        return try {
            val credential = FacebookAuthProvider.getCredential(token)
            val result = user.linkWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithFacebook(token: String): Result<FirebaseUser> {
        return try {
            val credential = FacebookAuthProvider.getCredential(token)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithTwitter(token: String, secret: String): Result<FirebaseUser> {
        return try {
            val credential = TwitterAuthProvider.getCredential(token, secret)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(name: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user signed in"))
        return try {
            val profileUpdates = userProfileChangeRequest {
                displayName = name
            }
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getDisplayName(): String = currentUser?.displayName ?: ""
    fun getPhotoUrl(): String = currentUser?.photoUrl?.toString() ?: ""
    fun getUserId(): String = currentUser?.uid ?: "local_user"

    companion object {
        fun getFriendlyErrorMessage(errorCode: String?): String {
            return when (errorCode) {
                "email-already-in-use" -> "email_in_use"
                "invalid-email" -> "email_invalid"
                "weak-password" -> "weak_password"
                "network-request-failed" -> "network_error"
                "too-many-requests" -> "too_many_requests"
                "user-not-found" -> "generic_auth_error"
                "wrong-password" -> "generic_auth_error"
                "invalid-credential" -> "generic_auth_error"
                "credential-already-in-use" -> "email_in_use"
                else -> "generic_auth_error"
            }
        }

        fun extractErrorCode(e: Throwable): String {
            val message = e.message?.lowercase() ?: ""
            return when {
                message.contains("email-already-in-use") -> "email-already-in-use"
                message.contains("invalid-email") -> "invalid-email"
                message.contains("weak-password") -> "weak-password"
                message.contains("network-request-failed") -> "network-request-failed"
                message.contains("too-many-requests") -> "too-many-requests"
                message.contains("user-not-found") -> "user-not-found"
                message.contains("wrong-password") -> "wrong-password"
                message.contains("invalid-credential") -> "invalid-credential"
                message.contains("credential-already-in-use") -> "credential-already-in-use"
                else -> "unknown"
            }
        }
    }
}
