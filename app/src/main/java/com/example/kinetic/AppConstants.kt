package com.example.kinetic

import androidx.compose.ui.unit.dp
import java.util.UUID

object AppConstants {
    const val DEFAULT_USER_ID = "simple"
    const val LOCAL_USER_ID = "local_user"
    const val GUEST_USER_NAME = "Guest"
    const val GOOGLE_USER_NAME = "Google User"
    const val FACEBOOK_USER_NAME = "Facebook User"

    val BOTTOM_NAV_PADDING = 88.dp

    // Supabase Storage (profile photos) — bucket public "profile_photos".
    // Anon key e public din design; accesul e limitat de policy-urile din SQL.
    const val SUPABASE_URL = "https://wexwjurnuasqlnsitosy.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndleHdqdXJudWFzcWxuc2l0b3N5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY2NDU5MjQsImV4cCI6MjEwMjIyMTkyNH0.eMF42IrOG04UoRtWf7YEjmme7Lo4__ZlJ3G_cpuMEPc"
    const val SUPABASE_PHOTO_BUCKET = "profile_photos"

    fun generateUuid(): String = UUID.randomUUID().toString()
}
