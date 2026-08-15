package com.example.kinetic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SocialRepository(private val db: AppDatabase) {
    private val api = NetworkClient.api

    suspend fun sendFriendRequest(fromUserId: String, toUserId: String, fromUserName: String, fromUserPhoto: String = "") {
        withContext(Dispatchers.IO) {
            val existing = db.friendshipDao().getBetween(fromUserId, toUserId)
            if (existing != null && existing.status == "accepted") return@withContext

            val reverse = db.friendshipDao().getBetween(toUserId, fromUserId)
            if (reverse != null && reverse.status == "accepted") return@withContext

            if (existing != null) db.friendshipDao().deleteById(existing.id)
            if (reverse != null) db.friendshipDao().deleteById(reverse.id)

            val local = FriendshipEntity(userId = fromUserId, friendId = toUserId, status = "pending")
            db.friendshipDao().upsert(local)

            try {
                api.sendFriendRequest(mapOf("fromUserId" to fromUserId, "toUserId" to toUserId))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun acceptFriendRequest(requestId: Long) {
        withContext(Dispatchers.IO) {
            val f = db.friendshipDao().getById(requestId) ?: return@withContext
            db.friendshipDao().accept(requestId)

            val reverse = db.friendshipDao().getBetween(f.friendId, f.userId)
            if (reverse != null) {
                db.friendshipDao().accept(reverse.id)
            } else {
                db.friendshipDao().upsert(FriendshipEntity(userId = f.friendId, friendId = f.userId, status = "accepted"))
            }

            // Backend-ul cere userId = utilizatorul autentificat (acceptorul). Cererea
            // primită are userId = expeditor, deci trimitem invers: acceptorul ca userId.
            try {
                api.acceptFriendRequest(mapOf("userId" to f.friendId, "friendId" to f.userId))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun rejectFriendRequest(requestId: Long) {
        withContext(Dispatchers.IO) {
            val f = db.friendshipDao().getById(requestId) ?: return@withContext
            db.friendshipDao().deleteById(requestId)

            val reverse = db.friendshipDao().getBetween(f.friendId, f.userId)
            if (reverse != null) db.friendshipDao().deleteById(reverse.id)

            try {
                api.rejectFriendRequest(mapOf("userId" to f.userId, "friendId" to f.friendId))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun getIncomingRequests(userId: String): List<FriendshipEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val serverRequests = api.getIncomingRequests(userId)
                val serverRequestIds = serverRequests.map { it.userId }.toSet()
                // Ștergem doar dacă serverul a răspuns cu o listă non-goală: un răspuns gol
                // tranzitoriu (rețea/proxy) nu trebuie să șteargă toate cererile locale.
                if (serverRequests.isNotEmpty()) {
                    db.friendshipDao().getIncomingRequests(userId).forEach { local ->
                        if (local.userId !in serverRequestIds) {
                            db.friendshipDao().deleteById(local.id)
                        }
                    }
                }
                for (req in serverRequests) {
                    val existing = db.friendshipDao().getBetween(req.userId, req.friendId)
                    if (existing == null || existing.status != "pending") {
                        if (existing != null) db.friendshipDao().deleteById(existing.id)
                        // Row nou cu id local (nu id-ul de pe server) ca să nu suprascrie alte relații.
                        db.friendshipDao().upsert(
                            FriendshipEntity(
                                userId = req.userId,
                                friendId = req.friendId,
                                status = "pending",
                                createdAt = req.createdAt
                            )
                        )
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            // Păstrăm o singură cerere per expeditor (cea mai recentă).
            db.friendshipDao().getIncomingRequests(userId)
                .sortedByDescending { it.createdAt }
                .distinctBy { it.userId }
        }
    }

    suspend fun getFriends(userId: String): List<FriendshipEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val serverFriends = api.getFriends(userId)
                val serverFriendIds = serverFriends.map { if (it.userId == userId) it.friendId else it.userId }.toSet()
                // Ștergem local doar când serverul a răspuns cu o listă non-goală — altfel un
                // răspuns gol tranzitoriu ar șterge TOȚI prietenii de pe device.
                if (serverFriends.isNotEmpty()) {
                    db.friendshipDao().getFriendsFor(userId).forEach { local ->
                        val localFriendId = if (local.userId == userId) local.friendId else local.userId
                        if (localFriendId !in serverFriendIds) {
                            db.friendshipDao().deleteById(local.id)
                        }
                    }
                }
                for (f in serverFriends) {
                    val actualFriendId = if (f.userId == userId) f.friendId else f.userId
                    val existing = db.friendshipDao().getBetween(userId, actualFriendId)
                    if (existing == null) {
                        db.friendshipDao().upsert(
                            FriendshipEntity(userId = userId, friendId = actualFriendId, status = "accepted")
                        )
                    } else if (existing.status != "accepted") {
                        db.friendshipDao().accept(existing.id)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            // Curăță duplicatele rămase (de la versiunile vechi): o singură intrare per prieten,
            // preferând rândul canonic (userId == mine) — altfel lista ar rămâne goală pentru că
            // afișarea filtrează rândurile inverse (friendId == mine).
            val all = db.friendshipDao().getFriendsFor(userId).sortedBy { it.id }
            val byFriend = LinkedHashMap<String, FriendshipEntity>()
            for (f in all) {
                val key = if (f.userId == userId) f.friendId else f.userId
                val prev = byFriend[key]
                val keep: FriendshipEntity
                val drop: FriendshipEntity?
                when {
                    prev == null -> { keep = f; drop = null }
                    f.userId == userId -> { keep = f; drop = prev }
                    prev.userId == userId -> { keep = prev; drop = f }
                    else -> { keep = prev; drop = f } // ambele inverse: păstrăm cel mai vechi
                }
                byFriend[key] = keep
                if (drop != null) db.friendshipDao().deleteById(drop.id)
            }
            byFriend.values.toList()
        }
    }

    suspend fun unfollow(userId: String, friendId: String) {
        withContext(Dispatchers.IO) {
            val friends = db.friendshipDao().getFriendsFor(userId)
            val match = friends.firstOrNull { it.friendId == friendId }
            match?.let {
                db.friendshipDao().deleteById(it.id)
                val reverse = db.friendshipDao().getBetween(friendId, userId)
                if (reverse != null) db.friendshipDao().deleteById(reverse.id)
            }

            try {
                api.removeFriend(mapOf("userId" to userId, "friendId" to friendId))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun searchUsersOnline(query: String): List<Pair<String, String>> {
        if (query.isBlank()) return emptyList()
        return withContext(Dispatchers.IO) {
            try {
                val results = api.searchUsers(query.trim())
                results.map { (it["id"] as? String ?: "") to (it["name"] as? String ?: "") }
                    .filter { it.first.isNotBlank() }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun syncUserProfile(userId: String, name: String, photoUri: String = "") {
        withContext(Dispatchers.IO) {
            try {
                // Token-ul FCM nu trebuie să blocheze sync-ul profilului: dacă fetch-ul eșuează
                // sau durează prea mult, trimitem profilul fără token ca utilizatorul să apară
                // oricum în căutare (o persoană logată trebuie să fie găsită prin nume).
                val token = runCatching {
                    withTimeoutOrNull(3000) {
                        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                    }
                }.getOrNull() ?: ""
                // Doar URL-urile accesibile (http/https) se sincronizează: un path local
                // file:// e specific device-ului și inutil pentru alți utilizatori/leaderboard.
                val remotePhoto = if (photoUri.startsWith("http://") || photoUri.startsWith("https://")) photoUri else ""
                api.upsertUser(mapOf("id" to userId, "name" to name, "photoUri" to remotePhoto, "fcmToken" to token))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    @Deprecated("Use syncUserProfile suspend function instead")
    fun syncUserProfileBlocking(userId: String, name: String, photoUri: String = "") {
        try {
            kotlinx.coroutines.runBlocking {
                syncUserProfile(userId, name, photoUri)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun syncVolumeToFirestore(userId: String, totalVolume: Double, workoutCount: Int) {
        withContext(Dispatchers.IO) {
            try {
                api.upsertUser(mapOf("id" to userId, "totalVolume" to totalVolume.toString(), "workoutCount" to workoutCount.toString()))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun getUserVolume(userId: String): Pair<Double, Int> {
        return withContext(Dispatchers.IO) {
            try {
                val user = api.getUser(userId)
                val vol = (user["totalVolume"] as? Number)?.toDouble() ?: 0.0
                val wc = (user["workoutCount"] as? Number)?.toInt() ?: 0
                vol to wc
            } catch (e: Exception) {
                0.0 to 0
            }
        }
    }

    suspend fun getLeaderboardFirestore(limit: Int = 50): List<LeaderboardEntry> {
        return withContext(Dispatchers.IO) {
            try {
                val entries = api.getLeaderboard("volume", limit).entries
                entries.mapIndexed { index, entry ->
                    LeaderboardEntry(
                        userId = (entry["userId"] as? String) ?: "",
                        name = (entry["name"] as? String) ?: (entry["userId"] as? String) ?: "",
                        photoUri = (entry["photoUri"] as? String) ?: "",
                        totalVolume = (entry["value"] as? Number)?.toDouble() ?: 0.0,
                        workoutCount = (entry["workoutCount"] as? Number)?.toInt() ?: 0,
                        rank = index + 1
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getFriendLeaderboard(userId: String, limit: Int = 50): List<LeaderboardEntry> {
        val friends = getFriends(userId)
        val friendIds = friends.map { it.friendId }.toMutableSet()
        friendIds.add(userId)
        if (friendIds.size <= 1) return emptyList()

        return withContext(Dispatchers.IO) {
            try {
                val entries = api.getLeaderboard("volume", 200).entries
                val filtered = entries.filter { (it["userId"] as? String) in friendIds }.take(limit)
                filtered.mapIndexed { index, entry ->
                    LeaderboardEntry(
                        userId = (entry["userId"] as? String) ?: "",
                        name = (entry["name"] as? String) ?: (entry["userId"] as? String) ?: "",
                        photoUri = (entry["photoUri"] as? String) ?: "",
                        totalVolume = (entry["value"] as? Number)?.toDouble() ?: 0.0,
                        workoutCount = (entry["workoutCount"] as? Number)?.toInt() ?: 0,
                        rank = index + 1
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun createPost(authorId: String, content: String, activityType: String = "post") {
        withContext(Dispatchers.IO) {
            try {
                api.createPost(mapOf("authorId" to authorId, "content" to content, "activityType" to activityType))
            } catch (_: Exception) {}
        }
    }

    suspend fun getFeed(limit: Int = 50): List<FeedPostEntity> {
        return withContext(Dispatchers.IO) {
            try {
                api.getFeed(limit)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun comment(postId: Long, authorId: String, content: String) {
        withContext(Dispatchers.IO) {
            try {
                api.comment(mapOf("postId" to postId.toString(), "authorId" to authorId, "content" to content))
            } catch (_: Exception) {}
        }
    }

    suspend fun like(postId: Long, userId: String) {
        withContext(Dispatchers.IO) {
            try {
                api.likePost(postId, mapOf("userId" to userId))
            } catch (_: Exception) {}
        }
    }

    suspend fun unlike(postId: Long, userId: String) {
        withContext(Dispatchers.IO) {
            try {
                api.unlikePost(postId, userId)
            } catch (_: Exception) {}
        }
    }

    suspend fun isLikedByUser(postId: Long, userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val result = api.isLikedByUser(postId, userId)
                result["liked"] ?: false
            } catch (_: Exception) { false }
        }
    }

    suspend fun likesCount(postId: Long): Int {
        return withContext(Dispatchers.IO) {
            try {
                val result = api.getLikesCount(postId)
                result["count"] ?: 0
            } catch (_: Exception) { 0 }
        }
    }
}

data class LeaderboardEntry(
    val userId: String,
    val name: String,
    val photoUri: String = "",
    val totalVolume: Double = 0.0,
    val workoutCount: Int = 0,
    val rank: Int = 0
)
