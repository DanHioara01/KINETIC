package com.example.gymlog2

import java.util.UUID

object AppConstants {
    const val DEFAULT_USER_ID = "simple"
    const val LOCAL_USER_ID = "local_user"
    const val GUEST_USER_NAME = "Guest"
    const val GOOGLE_USER_NAME = "Google User"
    const val FACEBOOK_USER_NAME = "Facebook User"

    fun generateUuid(): String = UUID.randomUUID().toString()
}
