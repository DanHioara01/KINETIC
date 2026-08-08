package com.example.kinetic

import com.google.firebase.auth.FirebaseAuth

/**
 * Admin access manager. Grants full feature access to app creator(s).
 * Hardcoded list of admin emails for simplicity.
 */
object AdminManager {
    private val adminEmails = setOf("dan.hioara@gmail.com".lowercase())

    /** Returns true if the current signed-in user is an admin. */
    fun isCurrentUserAdmin(): Boolean {
        val email = FirebaseAuth.getInstance().currentUser?.email?.lowercase()
        return email != null && adminEmails.contains(email)
    }

    /** Returns true if the given email is an admin. */
    fun isAdmin(email: String?): Boolean {
        return email != null && adminEmails.contains(email.lowercase())
    }
}