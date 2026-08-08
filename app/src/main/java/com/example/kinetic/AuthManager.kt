package com.example.kinetic

import android.content.Context
import android.util.Patterns
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isAnonymous: Boolean get() = currentUser?.isAnonymous == true
    val currentUid: String? get() = currentUser?.uid

    fun isLoggedIn(): Boolean = currentUser != null

    // ── Email / Password ──────────────────────────────────────────────

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "email_required"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "email_invalid"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "password_required"
        if (password.length < 6) return "password_too_short"
        return null
    }

    fun validateName(name: String): String? {
        if (name.isBlank()) return "name_required"
        if (name.trim().length < 2) return "name_too_short"
        return null
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            // Clear any lingering Firebase session (e.g. a previous user that was never
            // signed out) so a fresh email/password sign-in isn't blocked by a collision.
            if (auth.currentUser != null) auth.signOut()
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthUserCollisionException) {
            val methods = try {
                auth.fetchSignInMethodsForEmail(email).await().signInMethods ?: emptyList()
            } catch (_: Exception) { emptyList() }
            val providerNames = methods.map { providerIdToDisplayName(it) }
            Result.failure(AuthException(
                "account_exists_with_different_credential",
                "This email is registered with: ${providerNames.joinToString(", ")}. Use that method to sign in."
            ))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(AuthException("invalid-credential", AuthManager.getFriendlyMessage("invalid-credential")))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(AuthException("user-not-found", AuthManager.getFriendlyMessage("user-not-found")))
        } catch (e: FirebaseAuthException) {
            if (AuthManager.extractErrorCode(e) == "too-many-requests") {
                Result.failure(AuthException("too_many_requests", AuthManager.getFriendlyMessage("too-many-requests")))
            } else {
                Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
            }
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(AuthException("email-already-in-use", AuthManager.getFriendlyMessage("email-already-in-use")))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(AuthException("weak-password", AuthManager.getFriendlyMessage("weak-password")))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(AuthException("invalid-email", AuthManager.getFriendlyMessage("invalid-email")))
        } catch (e: FirebaseAuthException) {
            if (AuthManager.extractErrorCode(e) == "too-many-requests") {
                Result.failure(AuthException("too-many-requests", AuthManager.getFriendlyMessage("too-many-requests")))
            } else {
                Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
            }
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    // ── Google Sign-In ────────────────────────────────────────────────

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthUserCollisionException) {
            val email = e.email
            if (email != null) {
                val methods = try {
                    auth.fetchSignInMethodsForEmail(email).await().signInMethods ?: emptyList()
                } catch (_: Exception) { emptyList() }
                if (methods.contains(EmailAuthProvider.PROVIDER_ID)) {
                    Result.failure(AuthException(
                        "account_exists_with_email",
                        "An account already exists with this email. Log in with email & password first, then link Google from settings."
                    ))
                } else {
                    Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
                }
            } else {
                Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(AuthException("invalid_credential", "Invalid Google credential. Please try again."))
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    // ── Facebook Login ────────────────────────────────────────────────

    suspend fun signInWithFacebook(token: String): Result<FirebaseUser> {
        return try {
            val credential = FacebookAuthProvider.getCredential(token)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthUserCollisionException) {
            val email = e.email
            if (email != null) {
                val methods = try {
                    auth.fetchSignInMethodsForEmail(email).await().signInMethods ?: emptyList()
                } catch (_: Exception) { emptyList() }
                if (methods.contains(EmailAuthProvider.PROVIDER_ID)) {
                    Result.failure(AuthException(
                        "account_exists_with_email",
                        "An account already exists with this email. Log in with email & password first, then link Facebook from settings."
                    ))
                } else {
                    Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
                }
            } else {
                Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
            }
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    // ── Anonymous / Guest ─────────────────────────────────────────────

    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    // ── Account Linking (preserve UID) ────────────────────────────────

    suspend fun linkWithEmailCredential(email: String, password: String): Result<FirebaseUser> {
        val user = auth.currentUser
            ?: return Result.failure(AuthException("no_user", "No user signed in"))
        if (!user.isAnonymous) {
            return Result.failure(AuthException("not_anonymous", "Only anonymous accounts can be linked."))
        }
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val result = user.linkWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(AuthException(
                "email-already-in-use",
                "This email is already registered. Choose a different email or sign in with the existing account."
            ))
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    suspend fun linkWithGoogle(idToken: String): Result<FirebaseUser> {
        val user = auth.currentUser
            ?: return Result.failure(AuthException("no_user", "No user signed in"))
        if (!user.isAnonymous) {
            return Result.failure(AuthException("not_anonymous", "Only anonymous accounts can be linked."))
        }
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = user.linkWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(AuthException(
                "credential-already-in-use",
                "This Google account is already linked to another user."
            ))
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    suspend fun linkWithFacebook(token: String): Result<FirebaseUser> {
        val user = auth.currentUser
            ?: return Result.failure(AuthException("no_user", "No user signed in"))
        if (!user.isAnonymous) {
            return Result.failure(AuthException("not_anonymous", "Only anonymous accounts can be linked."))
        }
        return try {
            val credential = FacebookAuthProvider.getCredential(token)
            val result = user.linkWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(AuthException(
                "credential-already-in-use",
                "This Facebook account is already linked to another user."
            ))
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    // ── Profile & Session ─────────────────────────────────────────────

    suspend fun updateDisplayName(name: String): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(AuthException("no_user", "No user signed in"))
        return try {
            user.updateProfile(userProfileChangeRequest { displayName = name }).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), "Failed to update profile"))
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(AuthException("no_user", "No user signed in"))
        val email = user.email
            ?: return Result.failure(AuthException("no_email", "No email associated with this account"))
        return try {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(AuthException("wrong-password", "Current password is incorrect."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(AuthException("weak-password", "New password must be at least 6 characters."))
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    suspend fun deleteAccount(password: String? = null): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(AuthException("no_user", "No user signed in"))
        return try {
            if (user.isAnonymous) {
                user.delete().await()
            } else {
                val email = user.email
                if (email != null && password != null) {
                    val credential = EmailAuthProvider.getCredential(email, password)
                    user.reauthenticate(credential).await()
                }
                user.delete().await()
            }
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(AuthException("wrong-password", "Password is incorrect."))
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AuthException(AuthManager.extractErrorCode(e), AuthManager.getFriendlyMessage(AuthManager.extractErrorCode(e))))
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getDisplayName(): String = currentUser?.displayName ?: ""
    fun getPhotoUrl(): String = currentUser?.photoUrl?.toString() ?: ""
    fun getEmail(): String = currentUser?.email ?: ""
    fun getUserId(): String = currentUser?.uid ?: ""

    companion object {
        private fun providerIdToDisplayName(providerId: String): String = when (providerId) {
            EmailAuthProvider.PROVIDER_ID -> "Email"
            GoogleAuthProvider.PROVIDER_ID -> "Google"
            FacebookAuthProvider.PROVIDER_ID -> "Facebook"
            else -> providerId
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
                message.contains("user-disabled") -> "user-disabled"
                else -> "unknown"
            }
        }

        fun getFriendlyMessage(errorCode: String): String = when (errorCode) {
            "email-already-in-use" -> "An account with this email already exists."
            "invalid-email" -> "Invalid email address format."
            "weak-password" -> "Password must be at least 6 characters."
            "network-request-failed" -> "Network error. Check your connection and try again."
            "too-many-requests" -> "Too many attempts. Please wait a moment and try again."
            "user-not-found" -> "No account found with this email."
            "wrong-password" -> "Incorrect password. Please try again."
            "invalid-credential" -> "Invalid credentials. Please check your email and password."
            "credential-already-in-use" -> "This credential is already linked to another account."
            "account_exists_with_different_credential" -> "An account already exists with this email using a different sign-in method."
            "account_exists_with_email" -> "An account already exists with this email. Use email sign-in."
            "email_required" -> "Email is required."
            "email_invalid" -> "Please enter a valid email address."
            "password_required" -> "Password is required."
            "password_too_short" -> "Password must be at least 6 characters."
            "name_required" -> "Name is required."
            "name_too_short" -> "Name must be at least 2 characters."
            "no_user" -> "No user signed in."
            "not_anonymous" -> "Only guest accounts can be upgraded."
            else -> "Something went wrong. Please try again."
        }

        fun fromException(e: Throwable?): AuthException? {
            if (e == null) return null
            val message = when (e) {
                is FirebaseAuthInvalidCredentialsException -> getFriendlyMessage("invalid-credential")
                is FirebaseAuthInvalidUserException -> getFriendlyMessage("user-not-found")
                is FirebaseAuthUserCollisionException -> getFriendlyMessage("email-already-in-use")
                is FirebaseAuthWeakPasswordException -> getFriendlyMessage("weak-password")
                is FirebaseAuthException -> {
                    val code = extractErrorCode(e)
                    if (code == "too-many-requests") getFriendlyMessage("too-many-requests") else getFriendlyMessage(code)
                }
                else -> getFriendlyMessage(extractErrorCode(e))
            }
            return AuthException(extractErrorCode(e), message)
        }
    }

    class AuthException(val code: String, override val message: String) : Exception(message)
}
