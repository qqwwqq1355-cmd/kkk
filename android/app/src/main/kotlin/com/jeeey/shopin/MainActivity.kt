package com.jeeey.shopin

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * Main Activity for the Shopin Flutter WebView app.
 * 
 * Handles:
 * - Flutter engine configuration
 * - Platform channels for native social login
 * - FCM broadcast receiver for foreground notifications
 * - Deep link handling from notifications
 */
class MainActivity : FlutterActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val SOCIAL_LOGIN_CHANNEL = "com.jeeey.shopin/social_login"
        private const val NOTIFICATION_CHANNEL = "com.jeeey.shopin/notifications"
    }

    private var flutterEngine: FlutterEngine? = null

    /**
     * Broadcast receiver for FCM messages when app is in foreground.
     */
    private val fcmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MyFirebaseMessagingService.ACTION_FCM_PUSH) {
                val title = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_TITLE) ?: ""
                val body = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_BODY) ?: ""
                val image = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_IMAGE)
                val url = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_URL)

                Log.d(TAG, "FCM broadcast received: $title")

                // Send to Flutter via method channel
                flutterEngine?.dartExecutor?.binaryMessenger?.let { messenger ->
                    MethodChannel(messenger, NOTIFICATION_CHANNEL).invokeMethod(
                        "showDialog",
                        mapOf(
                            "title" to title,
                            "body" to body,
                            "image" to image,
                            "url" to url
                        )
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register FCM broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            fcmReceiver,
            IntentFilter(MyFirebaseMessagingService.ACTION_FCM_PUSH)
        )

        // Handle deep link from notification if present
        handleDeepLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        this.flutterEngine = flutterEngine

        // Setup social login platform channel
        setupSocialLoginChannel(flutterEngine)

        // Setup notification platform channel
        setupNotificationChannel(flutterEngine)
    }

    /**
     * Setup platform channel for native social login.
     * 
     * TODO: Implement actual Google/Facebook sign-in when credentials are available.
     * The current implementation returns stub responses for development.
     */
    private fun setupSocialLoginChannel(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, SOCIAL_LOGIN_CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "signIn" -> {
                        val provider = call.argument<String>("provider")
                        Log.d(TAG, "Social login requested for provider: $provider")

                        when (provider) {
                            "google" -> {
                                // TODO: Implement Google Sign-In
                                // 1. Launch GoogleSignInClient with correct client ID
                                // 2. Handle activity result
                                // 3. Get idToken and return it
                                // For now, return error indicating not implemented
                                result.error(
                                    "NOT_IMPLEMENTED",
                                    "Google Sign-In not yet implemented. Add Google Sign-In SDK and configure OAuth client ID.",
                                    null
                                )
                            }
                            "facebook" -> {
                                // TODO: Implement Facebook Login
                                // 1. Initialize FacebookSdk
                                // 2. Use LoginManager to request permissions
                                // 3. Get access token and return it
                                result.error(
                                    "NOT_IMPLEMENTED",
                                    "Facebook Login not yet implemented. Add Facebook SDK and configure App ID.",
                                    null
                                )
                            }
                            else -> {
                                result.error(
                                    "INVALID_PROVIDER",
                                    "Unknown sign-in provider: $provider",
                                    null
                                )
                            }
                        }
                    }
                    "signOut" -> {
                        val provider = call.argument<String>("provider")
                        Log.d(TAG, "Sign out requested for provider: $provider")
                        // TODO: Implement sign out for respective providers
                        result.success(true)
                    }
                    else -> {
                        result.notImplemented()
                    }
                }
            }
    }

    /**
     * Setup platform channel for notification handling.
     */
    private fun setupNotificationChannel(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, NOTIFICATION_CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "getQueuedNotifications" -> {
                        // Return any queued notifications
                        val notifications = NotificationQueue.dequeueAll(this)
                        result.success(notifications.map {
                            mapOf(
                                "title" to it.title,
                                "body" to it.body,
                                "image" to it.imageUrl,
                                "url" to it.deepLink,
                                "timestamp" to it.timestamp
                            )
                        })
                    }
                    "setOnHomePage" -> {
                        val onHome = call.argument<Boolean>("onHome") ?: true
                        AppStateHolder.setOnHomePage(onHome)
                        result.success(true)
                    }
                    else -> {
                        result.notImplemented()
                    }
                }
            }
    }

    /**
     * Handle deep link from notification.
     */
    private fun handleDeepLinkIntent(intent: Intent?) {
        intent?.getStringExtra("deep_link")?.let { deepLink ->
            Log.d(TAG, "Handling deep link: $deepLink")
            // Send deep link to Flutter
            flutterEngine?.dartExecutor?.binaryMessenger?.let { messenger ->
                MethodChannel(messenger, NOTIFICATION_CHANNEL).invokeMethod(
                    "openDeepLink",
                    mapOf("url" to deepLink)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Notify Flutter that we're back on the home page to check queued notifications
        flutterEngine?.dartExecutor?.binaryMessenger?.let { messenger ->
            MethodChannel(messenger, NOTIFICATION_CHANNEL).invokeMethod("onResume", null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fcmReceiver)
    }
}
