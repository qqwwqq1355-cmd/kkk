package com.jeeey.shopin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * Application class for Shopin
 * Initializes notification channels and app-wide state.
 */
class App : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "shopin_notifications"
        const val NOTIFICATION_CHANNEL_NAME = "Shopin Notifications"
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "Promotional and transactional notifications from Shopin"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channel (required for Android O+)
        createNotificationChannel()
        
        // Initialize AppStateHolder for foreground detection
        AppStateHolder.init(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NOTIFICATION_CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
