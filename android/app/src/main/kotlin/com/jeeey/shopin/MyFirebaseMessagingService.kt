package com.jeeey.shopin

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL

/**
 * Firebase Cloud Messaging Service for Shopin.
 * 
 * Handles data messages with custom logic:
 * - Foreground on home page: Show in-app dialog via LocalBroadcast
 * - Foreground not on home page: Queue notification for later display
 * - Background: Show system notification
 * 
 * Expected data payload format:
 * {
 *   "type": "promo" | "transactional",
 *   "title": "Notification Title",
 *   "body": "Notification body text",
 *   "image": "https://example.com/image.jpg",  // optional
 *   "url": "https://m.jeeey.com/tabs/deals"    // deep link URL
 * }
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val ACTION_FCM_PUSH = "com.jeeey.shopin.FCM_PUSH"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_IMAGE = "image"
        const val EXTRA_URL = "url"
        const val EXTRA_TYPE = "type"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        // TODO: Send token to your backend server for push targeting
        // Example: sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Extract data payload
        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: ""
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val imageUrl = data["image"]
        val deepLink = data["url"]
        val notificationType = data["type"] ?: "promo"

        Log.d(TAG, "Notification - Title: $title, Body: $body, URL: $deepLink")

        when {
            AppStateHolder.isAppInForeground() && AppStateHolder.isOnHomePage() -> {
                // Foreground + on home page: Broadcast to show in-app dialog
                Log.d(TAG, "Foreground on home - broadcasting for dialog")
                broadcastForDialog(title, body, imageUrl, deepLink, notificationType)
            }
            AppStateHolder.isAppInForeground() && !AppStateHolder.isOnHomePage() -> {
                // Foreground but not on home page: Queue for later
                Log.d(TAG, "Foreground not on home - queueing notification")
                NotificationQueue.enqueue(this, title, body, imageUrl, deepLink)
            }
            else -> {
                // Background: Show system notification
                Log.d(TAG, "Background - showing system notification")
                showSystemNotification(title, body, imageUrl, deepLink)
            }
        }
    }

    /**
     * Broadcast notification data for in-app dialog display.
     */
    private fun broadcastForDialog(
        title: String,
        body: String,
        imageUrl: String?,
        deepLink: String?,
        type: String
    ) {
        val intent = Intent(ACTION_FCM_PUSH).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_IMAGE, imageUrl)
            putExtra(EXTRA_URL, deepLink)
            putExtra(EXTRA_TYPE, type)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * Show a system notification (for background state).
     */
    private fun showSystemNotification(
        title: String,
        body: String,
        imageUrl: String?,
        deepLink: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open app with deep link
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deepLink?.let { putExtra("deep_link", it) }
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, App.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with app icon
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        // Load and set large image if provided
        imageUrl?.let { url ->
            val bitmap = loadImageFromUrl(url)
            bitmap?.let {
                notificationBuilder.setLargeIcon(it)
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(it)
                        .bigLargeIcon(null as Bitmap?)
                )
            }
        }

        // Show notification
        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }

    /**
     * Load image from URL for notification.
     */
    private fun loadImageFromUrl(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            val inputStream = connection.inputStream
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image from URL: $urlString", e)
            null
        }
    }
}
