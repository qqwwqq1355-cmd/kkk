package com.jeeey.shopin

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Queue for storing notification payloads when user is not on home page.
 * Notifications are stored in SharedPreferences and retrieved when user returns to home.
 */
object NotificationQueue {

    private const val PREFS_NAME = "notification_queue"
    private const val KEY_QUEUE = "queued_notifications"
    private const val MAX_QUEUE_SIZE = 20

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Add a notification payload to the queue.
     */
    fun enqueue(context: Context, title: String?, body: String?, imageUrl: String?, deepLink: String?) {
        val prefs = getPrefs(context)
        val queue = getQueueAsJsonArray(prefs)
        
        val notification = JSONObject().apply {
            put("title", title ?: "")
            put("body", body ?: "")
            put("image", imageUrl ?: "")
            put("url", deepLink ?: "")
            put("timestamp", System.currentTimeMillis())
        }
        
        queue.put(notification)
        
        // Limit queue size
        while (queue.length() > MAX_QUEUE_SIZE) {
            queue.remove(0)
        }
        
        prefs.edit().putString(KEY_QUEUE, queue.toString()).apply()
    }

    /**
     * Get and clear all queued notifications.
     */
    fun dequeueAll(context: Context): List<NotificationPayload> {
        val prefs = getPrefs(context)
        val queue = getQueueAsJsonArray(prefs)
        val notifications = mutableListOf<NotificationPayload>()
        
        for (i in 0 until queue.length()) {
            val obj = queue.getJSONObject(i)
            notifications.add(
                NotificationPayload(
                    title = obj.optString("title"),
                    body = obj.optString("body"),
                    imageUrl = obj.optString("image"),
                    deepLink = obj.optString("url"),
                    timestamp = obj.optLong("timestamp")
                )
            )
        }
        
        // Clear the queue
        prefs.edit().remove(KEY_QUEUE).apply()
        
        return notifications
    }

    /**
     * Check if there are queued notifications.
     */
    fun hasQueuedNotifications(context: Context): Boolean {
        val prefs = getPrefs(context)
        val queueStr = prefs.getString(KEY_QUEUE, null) ?: return false
        return try {
            JSONArray(queueStr).length() > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun getQueueAsJsonArray(prefs: SharedPreferences): JSONArray {
        val queueStr = prefs.getString(KEY_QUEUE, null)
        return if (queueStr != null) {
            try {
                JSONArray(queueStr)
            } catch (e: Exception) {
                JSONArray()
            }
        } else {
            JSONArray()
        }
    }

    /**
     * Data class representing a notification payload.
     */
    data class NotificationPayload(
        val title: String,
        val body: String,
        val imageUrl: String,
        val deepLink: String,
        val timestamp: Long
    )
}
