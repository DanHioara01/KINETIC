package com.example.kinetic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for the currently active userId.
 * Exposed as a reactive [StateFlow] so that ViewModels and Composables
 * can re-query data automatically when the account changes (login / logout / switch).
 *
 * Usage:
 *  - Call [refresh] immediately after every successful login or logout.
 *  - Collect [currentUserId] in ViewModels to react to account switches.
 *  - Read [currentUserId].value for one-shot access (non-reactive).
 */
class CurrentUserProvider private constructor() {

    private val _currentUserId = MutableStateFlow(resolveUserId())
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    /**
     * Re-reads the userId from [UserProfileManager] / Firebase Auth
     * and pushes the new value into the flow.
     */
    fun refresh() {
        _currentUserId.value = resolveUserId()
    }

    /**
     * Best-effort resolution without Context.
     * Firebase Auth is globally available; UserProfileManager needs a Context
     * so we fall back to the stored SharedPreferences value.
     */
    private fun resolveUserId(): String {
        val firebaseUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        return firebaseUid ?: "local_user"
    }

    companion object {
        @Volatile
        private var INSTANCE: CurrentUserProvider? = null

        fun getInstance(): CurrentUserProvider {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CurrentUserProvider().also { INSTANCE = it }
            }
        }
    }
}
