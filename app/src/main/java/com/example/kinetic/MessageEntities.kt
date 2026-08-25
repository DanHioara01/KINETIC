package com.example.kinetic

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Entity ---
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val type: String,       // INFO, WARNING, SUCCESS, REMINDER
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// --- DAO ---
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isRead = 0 ORDER BY timestamp DESC")
    fun observeUnread(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isRead = 1 ORDER BY timestamp DESC")
    fun observeRead(): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE messages SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()

    @Query("DELETE FROM messages WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun count(): Int
}
