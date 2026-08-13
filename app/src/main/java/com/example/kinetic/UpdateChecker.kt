package com.example.kinetic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Verifică pe GitHub Releases dacă există o versiune mai nouă a aplicației.
 * Folosit la pornire pentru a anunța utilizatorul despre actualizări.
 */
object UpdateChecker {

    private const val RELEASES_URL =
        "https://api.github.com/repos/DanHioara01/KINETIC/releases/latest"

    data class LatestRelease(
        val tagName: String,
        val htmlUrl: String,
        val publishedAt: String,
        val name: String
    )

    /**
     * Verifică ultima versiune de pe GitHub. Returnează [LatestRelease] dacă
     * există o versiune mai nouă decât cea instalată, altfel null.
     */
    suspend fun checkForUpdate(): LatestRelease? = withContext(Dispatchers.IO) {
        try {
            // Folosim un client OkHttp simplu (releases publice, fără token)
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = okhttp3.Request.Builder().url(RELEASES_URL).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val tag = json.optString("tag_name", "").trim()
                if (tag.isEmpty()) return@withContext null

                val installed = BuildConfig.VERSION_NAME.trim()
                if (isNewer(tag, installed)) {
                    LatestRelease(
                        tagName = tag,
                        htmlUrl = json.optString("html_url", "https://github.com/DanHioara01/KINETIC/releases"),
                        publishedAt = json.optString("published_at", ""),
                        name = json.optString("name", tag)
                    )
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null // offline sau eroare de rețea — verificarea nu trebuie să deranjeze
        }
    }

    /**
     * Compară versiunea de pe GitHub (ex: "v2.1" sau "2.1.0") cu cea instalată
     * (ex: "2.1"). Extrage numerele și compară numeric.
     */
    fun isNewer(githubTag: String, installedVersion: String): Boolean {
        val gh = parseNumbers(githubTag)
        val inst = parseNumbers(installedVersion)
        if (gh.isEmpty() || inst.isEmpty()) return false
        val maxLen = maxOf(gh.size, inst.size)
        for (i in 0 until maxLen) {
            val a = if (i < gh.size) gh[i] else 0
            val b = if (i < inst.size) inst[i] else 0
            if (a > b) return true
            if (a < b) return false
        }
        return false // egale
    }

    private fun parseNumbers(tag: String): List<Int> {
        return tag.lowercase()
            .trimStart('v')
            .split('.', '-', '_', ' ')
            .mapNotNull { it.toIntOrNull() }
            .filter { it >= 0 }
    }
}
