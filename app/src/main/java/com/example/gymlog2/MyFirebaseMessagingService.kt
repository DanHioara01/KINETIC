package com.example.gymlog2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "kinetic_notifications"
        private const val CHANNEL_NAME = "Kinetic Notifications"
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from Kinetic"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = UserProfileManager(applicationContext).getOwnUserId()
        if (userId != "local_user") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    FirestoreHelper().saveFcmToken(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    NetworkClient.api.upsertUser(mapOf(
                        "id" to userId,
                        "fcmToken" to token
                    ))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        ensureChannel()

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Kinetic"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val type = remoteMessage.data["type"] ?: ""
        val senderName = remoteMessage.data["fromUserName"] ?: ""
        val fromUserId = remoteMessage.data["fromUserId"] ?: ""

        if (type == "friend_request") {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showFriendRequestNotification(
                targetUserId = UserProfileManager(applicationContext).getOwnUserId(),
                senderName = senderName.ifEmpty { body }
            )
        } else {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_friends", true)
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            try {
                NotificationManagerCompat.from(this).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
            } catch (_: SecurityException) { }
        }
    }
}
