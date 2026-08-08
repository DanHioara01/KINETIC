package com.example.kinetic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom

object SpotifyManager {

    const val CLIENT_ID = "4cd2b83ae9094ecbad1f27af3e65b0f4"
    private const val REDIRECT_URI = "kinetic://callback"
    private const val AUTH_URL = "https://accounts.spotify.com/authorize"
    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"
    private const val API_BASE = "https://api.spotify.com/v1"
    private const val PREFS_NAME = "spotify_prefs"

    var pendingCallbackUri: android.net.Uri? = null

    var accessToken: String? = null
        private set
    var refreshToken: String? = null
        private set
    var tokenExpiry: Long = 0L
        private set
    private var codeVerifier: String = ""
    private const val CODE_VERIFIER_KEY = "code_verifier"

    fun isAuthorized(): Boolean =
        !accessToken.isNullOrEmpty() && System.currentTimeMillis() < tokenExpiry

    fun isSpotifyInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.spotify.music", 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun buildAuthIntent(context: Context): Intent {
        codeVerifier = generateCodeVerifier()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(CODE_VERIFIER_KEY, codeVerifier).apply()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        val scope = "playlist-read-private playlist-read-collaborative user-read-playback-state"
        val authUri = Uri.parse(AUTH_URL).buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("scope", scope)
            .build()
        return Intent(Intent.ACTION_VIEW, authUri)
    }

    suspend fun handleRedirect(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val code = uri.getQueryParameter("code")
        if (code.isNullOrEmpty()) return@withContext false
        try {
            val body = "grant_type=authorization_code&code=$code&redirect_uri=$REDIRECT_URI&client_id=$CLIENT_ID&code_verifier=$codeVerifier"
            val conn = URL(TOKEN_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true
            conn.outputStream.write(body.toByteArray())
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            accessToken = json.getString("access_token")
            refreshToken = json.optString("refresh_token", refreshToken)
            tokenExpiry = System.currentTimeMillis() + json.getLong("expires_in") * 1000
            saveTokens(context)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun refreshAccessToken(context: Context): Boolean = withContext(Dispatchers.IO) {
        val refresh = refreshToken ?: return@withContext false
        try {
            val body = "grant_type=refresh_token&refresh_token=$refresh&client_id=$CLIENT_ID"
            val conn = URL(TOKEN_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true
            conn.outputStream.write(body.toByteArray())
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            accessToken = json.getString("access_token")
            if (json.has("refresh_token")) refreshToken = json.getString("refresh_token")
            tokenExpiry = System.currentTimeMillis() + json.getLong("expires_in") * 1000
            saveTokens(context)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchPlaylists(context: Context): List<SpotifyPlaylist> = withContext(Dispatchers.IO) {
        if (accessToken.isNullOrEmpty()) return@withContext emptyList()
        if (System.currentTimeMillis() > tokenExpiry - 60_000) refreshAccessToken(context)
        val playlists = mutableListOf<SpotifyPlaylist>()
        var url: String? = "$API_BASE/me/playlists?limit=50"
        var retried = false
        try {
            while (url != null) {
                val token = accessToken ?: break
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.setRequestProperty("Authorization", "Bearer $token")
                conn.connect()
                if (conn.responseCode == 401 && !retried) {
                    conn.disconnect()
                    if (refreshAccessToken(context)) { retried = true; continue }
                    break
                }
                if (conn.responseCode != 200) { conn.disconnect(); break }
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                conn.disconnect()
                val items = json.getJSONArray("items")
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val images = item.getJSONArray("images")
                    val imageUrl = if (images.length() > 0) images.getJSONObject(0).getString("url") else null
                    playlists.add(SpotifyPlaylist(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        description = item.optString("description", ""),
                        imageUrl = imageUrl,
                        trackCount = item.getJSONObject("tracks").getInt("total"),
                        uri = item.getString("uri")
                    ))
                }
                url = if (json.has("next") && !json.isNull("next")) json.getString("next") else null
            }
        } catch (e: Exception) { e.printStackTrace() }
        playlists
    }

    fun logout(context: Context) {
        accessToken = null; refreshToken = null; tokenExpiry = 0L
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun saveTokens(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("token_expiry", tokenExpiry).apply()
    }

    fun loadTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        accessToken = prefs.getString("access_token", null)
        refreshToken = prefs.getString("refresh_token", null)
        tokenExpiry = prefs.getLong("token_expiry", 0L)
        codeVerifier = prefs.getString(CODE_VERIFIER_KEY, "") ?: ""
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32); SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}

fun openSpotifyApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        openSpotifyInPlayStore(context)
    }
}

private fun openSpotifyInPlayStore(context: Context) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.spotify.music")
            )
        )
    } catch (_: Exception) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")
            )
        )
    }
}

data class SpotifyPlaylist(
    val id: String, val name: String, val description: String,
    val imageUrl: String?, val trackCount: Int, val uri: String
)