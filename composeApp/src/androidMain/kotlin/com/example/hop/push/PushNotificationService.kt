package com.example.hop.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.hop.MainActivity
import com.example.hop.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Handles incoming FCM messages and token refreshes for Hop.
 *
 * - New token → POST /users/push-token via [UserRepository.savePushToken]
 * - Message received → show a system notification via [NotificationManager]
 */
class PushNotificationService : FirebaseMessagingService() {

    private val userRepository: UserRepository by inject()

    // Scoped to the service; cancelled in onDestroy to avoid leaks.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch {
            userRepository.savePushToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: return // nothing to show
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        showNotification(title = title, body = body)
    }

    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure the channel exists (required Android 8+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Hop Notifications",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Hop ride and booking updates"
        }
        manager.createNotificationChannel(channel)

        // Tap opens the app at MainActivity
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // replace with app icon
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "hop_notifications"
    }
}
