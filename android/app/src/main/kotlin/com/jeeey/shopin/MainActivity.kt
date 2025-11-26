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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

/**
 * Main Activity for the Shopin Flutter WebView app.
 * 
 * Handles:
 * - Flutter engine configuration
 * - Platform channels for native social login (Google & Facebook)
 * - FCM broadcast receiver for foreground notifications
 * - Deep link handling from notifications
 */
class MainActivity : FlutterActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val SOCIAL_LOGIN_CHANNEL = "com.jeeey.shopin/social_login"
        private const val NOTIFICATION_CHANNEL = "com.jeeey.shopin/notifications"
        // Web client ID from google-services.json (client_type: 3)
        private const val WEB_CLIENT_ID = "806186232733-mavt0rshih2qgli5qiaj0gkfktj2be18.apps.googleusercontent.com"
    }

    private var flutterEngine: FlutterEngine? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var callbackManager: CallbackManager
    private var pendingResult: MethodChannel.Result? = null

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

        // Initialize Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleGoogleSignInResult(result.resultCode, result.data)
        }

        // Initialize Facebook
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val token = result.accessToken.token
                    Log.d(TAG, "Facebook login success")
                    pendingResult?.success(mapOf("token" to token))
                    pendingResult = null
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook login cancelled")
                    pendingResult?.error("CANCELLED", "User cancelled login", null)
                    pendingResult = null
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Facebook login error", error)
                    pendingResult?.error("ERROR", error.message, null)
                    pendingResult = null
                }
            }
        )

        // Register FCM broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            fcmReceiver,
            IntentFilter(MyFirebaseMessagingService.ACTION_FCM_PUSH)
        )

        // Handle deep link from notification if present
        handleDeepLinkIntent(intent)
    }

    private fun handleGoogleSignInResult(resultCode: Int, data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            
            if (idToken != null) {
                Log.d(TAG, "Google Sign-In success")
                pendingResult?.success(mapOf("token" to idToken))
            } else {
                pendingResult?.error("NO_TOKEN", "No ID token received", null)
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed: ${e.statusCode}", e)
            when (e.statusCode) {
                12501 -> pendingResult?.error("CANCELLED", "User cancelled login", null)
                else -> pendingResult?.error("ERROR", "Sign-in failed: ${e.message}", null)
            }
        }
        pendingResult = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Facebook callback
        callbackManager.onActivityResult(requestCode, resultCode, data)
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
     */
    private fun setupSocialLoginChannel(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, SOCIAL_LOGIN_CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "signIn" -> {
                        val provider = call.argument<String>("provider")
                        Log.d(TAG, "Social login requested for provider: $provider")
                        pendingResult = result

                        when (provider) {
                            "google" -> {
                                // Sign out first to allow account selection
                                googleSignInClient.signOut().addOnCompleteListener {
                                    val signInIntent = googleSignInClient.signInIntent
                                    googleSignInLauncher.launch(signInIntent)
                                }
                            }
                            "facebook" -> {
                                LoginManager.getInstance().logInWithReadPermissions(
                                    this,
                                    listOf("email", "public_profile")
                                )
                            }
                            else -> {
                                pendingResult = null
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
                        when (provider) {
                            "google" -> googleSignInClient.signOut()
                            "facebook" -> LoginManager.getInstance().logOut()
                        }
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
