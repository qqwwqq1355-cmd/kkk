package com.jeeey.shopin

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Utility object to track app foreground/background state.
 * Used to determine how to handle FCM messages.
 */
object AppStateHolder : DefaultLifecycleObserver {

    private var isAppForeground = false
    private var isOnHomePage = true  // Assume home page by default

    fun init(application: Application) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Check if the app is currently in the foreground.
     */
    fun isAppInForeground(): Boolean = isAppForeground

    /**
     * Check if user is on the home page.
     * This should be updated by the MainActivity when navigation changes.
     */
    fun isOnHomePage(): Boolean = isOnHomePage

    /**
     * Set whether the user is on the home page.
     * Called from MainActivity when WebView URL changes.
     */
    fun setOnHomePage(onHome: Boolean) {
        isOnHomePage = onHome
    }

    override fun onStart(owner: LifecycleOwner) {
        isAppForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        isAppForeground = false
    }

    /**
     * Alternative method to check foreground state using ActivityManager.
     * Use this as a fallback if ProcessLifecycleOwner is not available.
     */
    fun isAppInForegroundFallback(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }
}
