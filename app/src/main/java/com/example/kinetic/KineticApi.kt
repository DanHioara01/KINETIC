package com.example.kinetic

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth

interface KineticApi {
    // Users
    @POST("users")
    suspend fun upsertUser(@Body body: Any): Map<String, Any>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Map<String, Any>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): List<Map<String, Any>>

    // Friendships
    @POST("friends/request")
    suspend fun sendFriendRequest(@Body body: Any): Map<String, Any>

    @GET("friends/incoming/{userId}")
    suspend fun getIncomingRequests(@Path("userId") userId: String): List<FriendshipEntity>

    @POST("friends/accept")
    suspend fun acceptFriendRequest(@Body body: Any): Map<String, Any>

    @POST("friends/reject")
    suspend fun rejectFriendRequest(@Body body: Any): Map<String, Any>

    @POST("friends/remove")
    suspend fun removeFriend(@Body body: Any): Map<String, Any>

    @GET("friends/{userId}")
    suspend fun getFriends(@Path("userId") userId: String): List<FriendshipEntity>

    // Feed & Posts
    @POST("posts")
    suspend fun createPost(@Body body: Any): Map<String, Any>

    @GET("feed")
    suspend fun getFeed(@Query("limit") limit: Int): List<FeedPostEntity>

    @GET("posts/author/{authorId}")
    suspend fun getPostsByAuthor(@Path("authorId") authorId: String): List<FeedPostEntity>

    // Comments & Likes
    @POST("comments")
    suspend fun comment(@Body body: Any): Map<String, Any>

    @GET("comments/{postId}")
    suspend fun getComments(@Path("postId") postId: Long): List<CommentEntity>

    @POST("posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: Long, @Body body: Any): Map<String, Any>

    @DELETE("posts/{postId}/like")
    suspend fun unlikePost(@Path("postId") postId: Long, @Query("userId") userId: String): Map<String, Any>

    @GET("posts/{postId}/likes/count")
    suspend fun getLikesCount(@Path("postId") postId: Long): Map<String, Int>

    @GET("posts/{postId}/liked/{userId}")
    suspend fun isLikedByUser(@Path("postId") postId: Long, @Path("userId") userId: String): Map<String, Boolean>

    // Leaderboard
    @POST("leaderboard")
    suspend fun upsertLeaderboardEntry(@Body body: Any): Map<String, Any>

    @GET("leaderboard")
    suspend fun getLeaderboard(@Query("metric") metric: String, @Query("limit") limit: Int): LeaderboardResponse

    // Streaks & Workout Logging (Gamification)
    @POST("workouts/log")
    suspend fun logWorkout(@Body body: Any): WorkoutLogResponse

    @GET("streaks/{userId}")
    suspend fun getStreak(@Path("userId") userId: String): StreakEntity

    // Badges
    @GET("badges")
    suspend fun getAllBadges(): List<BadgeEntity>

    @GET("badges/user/{userId}")
    suspend fun getBadgesForUser(@Path("userId") userId: String): List<UserBadgeEntity>

    @POST("badges/award")
    suspend fun awardBadge(@Body body: Any): Map<String, Any>

    // === DATA SYNC ===
    @GET("sync/antrenamente/{userId}")
    suspend fun syncAntrenamente(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/antrenamente/upsert")
    suspend fun upsertAntrenament(@Body body: Any): Map<String, Any>

    @POST("sync/antrenamente/bulk")
    suspend fun bulkAntrenamente(@Body body: Any): Map<String, Any>

    @DELETE("sync/antrenamente/{uuid}")
    suspend fun deleteAntrenament(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/exercitii/{userId}")
    suspend fun syncExercitii(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/exercitii/upsert")
    suspend fun upsertExercitiu(@Body body: Any): Map<String, Any>

    @POST("sync/exercitii/bulk")
    suspend fun bulkExercitii(@Body body: Any): Map<String, Any>

    @DELETE("sync/exercitii/{uuid}")
    suspend fun deleteExercitiu(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/exercises/{userId}")
    suspend fun syncExercises(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/exercises/upsert")
    suspend fun upsertExercise(@Body body: Any): Map<String, Any>

    @POST("sync/exercises/bulk")
    suspend fun bulkExercises(@Body body: Any): Map<String, Any>

    @DELETE("sync/exercises/{uuid}")
    suspend fun deleteExercise(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/templates/{userId}")
    suspend fun syncTemplates(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/templates/upsert")
    suspend fun upsertTemplate(@Body body: Any): Map<String, Any>

    @POST("sync/templates/bulk")
    suspend fun bulkTemplates(@Body body: Any): Map<String, Any>

    @DELETE("sync/templates/{uuid}")
    suspend fun deleteTemplate(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/template_exercises/{userId}")
    suspend fun syncTemplateExercises(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/template_exercises/upsert")
    suspend fun upsertTemplateExercise(@Body body: Any): Map<String, Any>

    @POST("sync/template_exercises/bulk")
    suspend fun bulkTemplateExercises(@Body body: Any): Map<String, Any>

    @DELETE("sync/template_exercises/{uuid}")
    suspend fun deleteTemplateExercise(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/personal_records/{userId}")
    suspend fun syncPersonalRecords(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/personal_records/upsert")
    suspend fun upsertPersonalRecord(@Body body: Any): Map<String, Any>

    @POST("sync/personal_records/bulk")
    suspend fun bulkPersonalRecords(@Body body: Any): Map<String, Any>

    @DELETE("sync/personal_records/{uuid}")
    suspend fun deletePersonalRecord(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/muscle_recovery/{userId}")
    suspend fun syncMuscleRecovery(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/muscle_recovery/upsert")
    suspend fun upsertMuscleRecovery(@Body body: Any): Map<String, Any>

    @POST("sync/muscle_recovery/bulk")
    suspend fun bulkMuscleRecovery(@Body body: Any): Map<String, Any>

    @DELETE("sync/muscle_recovery/{uuid}")
    suspend fun deleteMuscleRecovery(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/exercise_metadata/{userId}")
    suspend fun syncExerciseMetadata(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/exercise_metadata/upsert")
    suspend fun upsertExerciseMetadata(@Body body: Any): Map<String, Any>

    @POST("sync/exercise_metadata/bulk")
    suspend fun bulkExerciseMetadata(@Body body: Any): Map<String, Any>

    @DELETE("sync/exercise_metadata/{uuid}")
    suspend fun deleteExerciseMetadata(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/biometric_entries/{userId}")
    suspend fun syncBiometricEntries(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/biometric_entries/upsert")
    suspend fun upsertBiometricEntry(@Body body: Any): Map<String, Any>

    @POST("sync/biometric_entries/bulk")
    suspend fun bulkBiometricEntries(@Body body: Any): Map<String, Any>

    @DELETE("sync/biometric_entries/{uuid}")
    suspend fun deleteBiometricEntry(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/food_entries/{userId}")
    suspend fun syncFoodEntries(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/food_entries/upsert")
    suspend fun upsertFoodEntry(@Body body: Any): Map<String, Any>

    @POST("sync/food_entries/bulk")
    suspend fun bulkFoodEntries(@Body body: Any): Map<String, Any>

    @DELETE("sync/food_entries/{uuid}")
    suspend fun deleteFoodEntry(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/cardio_routes/{userId}")
    suspend fun syncCardioRoutes(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/cardio_routes/upsert")
    suspend fun upsertCardioRoute(@Body body: Any): Map<String, Any>

    @POST("sync/cardio_routes/bulk")
    suspend fun bulkCardioRoutes(@Body body: Any): Map<String, Any>

    @DELETE("sync/cardio_routes/{uuid}")
    suspend fun deleteCardioRoute(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/rest_days/{userId}")
    suspend fun syncRestDays(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/rest_days/upsert")
    suspend fun upsertRestDay(@Body body: Any): Map<String, Any>

    @POST("sync/rest_days/bulk")
    suspend fun bulkRestDays(@Body body: Any): Map<String, Any>

    @DELETE("sync/rest_days/{uuid}")
    suspend fun deleteRestDay(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/ai_chat_history/{userId}")
    suspend fun syncAiChatHistory(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/ai_chat_history/upsert")
    suspend fun upsertAiChatHistory(@Body body: Any): Map<String, Any>

    @POST("sync/ai_chat_history/bulk")
    suspend fun bulkAiChatHistory(@Body body: Any): Map<String, Any>

    @DELETE("sync/ai_chat_history/{uuid}")
    suspend fun deleteAiChatHistory(@Path("uuid") uuid: String): Map<String, Any>

    @GET("sync/subscriptions/{userId}")
    suspend fun syncSubscriptions(@Path("userId") userId: String, @Query("since") since: Long = 0): List<Map<String, Any>>

    @POST("sync/subscriptions/upsert")
    suspend fun upsertSubscription(@Body body: Any): Map<String, Any>

    @POST("sync/subscriptions/bulk")
    suspend fun bulkSubscriptions(@Body body: Any): Map<String, Any>

    @DELETE("sync/subscriptions/{uuid}")
    suspend fun deleteSubscription(@Path("uuid") uuid: String): Map<String, Any>
}

data class WorkoutLogResponse(
    val success: Boolean,
    val stats: Map<String, Int>,
    val streak: StreakResponse,
    val newlyAwardedBadges: List<String>
)

/**
 * Răspunsul backend-ului pentru /leaderboard: { entries: [...], total, hasMore }.
 * Default-urile fac parsing-ul tolerant la versiuni vechi ale backend-ului.
 */
data class LeaderboardResponse(
    val entries: List<Map<String, Any>> = emptyList(),
    val total: Int = 0,
    val hasMore: Boolean = false
)

data class StreakResponse(
    val currentStreak: Int,
    val bestStreak: Int,
    val lastDate: Long
)

object NetworkClient {
    private const val DEFAULT_URL = "https://kinetic-backend-3ff6.onrender.com"
    private var currentUrl: String = DEFAULT_URL
    private var currentApi: KineticApi? = null

    /**
     * Atașează token-ul de ID Firebase (Authorization: Bearer) la fiecare cerere,
     * pentru ca backend-ul să poată verifica identitatea utilizatorului.
     */
    private fun authInterceptor(): okhttp3.Interceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val token = runCatching {
            runBlocking {
                val user = FirebaseAuth.getInstance().currentUser ?: return@runBlocking null
                val task = user.getIdToken(false)
                Tasks.await(task).token
            }
        }.getOrNull()

        if (token == null) {
            chain.proceed(original)
        } else {
            val authed = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(authed)
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor())
        .build()

    fun getApi(serverUrl: String? = null): KineticApi {
        val url = (serverUrl?.takeIf { it.isNotBlank() } ?: currentUrl).trimEnd('/') + "/"
        if (url == currentUrl && currentApi != null) return currentApi!!
        currentUrl = url
        currentApi = Retrofit.Builder()
            .baseUrl(url)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KineticApi::class.java)
        return currentApi!!
    }

    val api: KineticApi get() = getApi()
}
