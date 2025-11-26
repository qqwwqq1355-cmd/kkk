# WebView Mobile App — README

**Project:** m.jeeey.com (WebView Mobile App)  
**Package Name:** com.jeeey.shopin  
**Version:** 1.0.0+3046

## Overview

This README is a full, production-ready specification intended to be followed verbatim by the developer.
It describes the full WebView mobile app architecture, UX behavior, required files, Android / Play
Console requirements, Firebase push behavior, native sign-in integration, security, testing matrix, and
publishing checklist. Deliverables, paths, and exact code snippets are included.

> **Important enforcement note for publisher:** The app must provide native value beyond
> rendering the website (push notifications, native sign-in, offline caching, in-app share,
> improved navigation/back logic, native dialogs) to comply with Google Play minimum
> functionality policies.

## Table of Contents

1. Project metadata
2. Required inputs (what you must provide)
3. File placement and repository structure
4. Google Play and Android compatibility requirements
5. UX & behavior specifications (Splash, back logic, dynamic theme, touch & scrolling)
6. Firebase Cloud Messaging (FCM) behavior & payload format
7. Native Google & Facebook sign-in (recommended architecture)
8. WebView security settings
9. Caching, preloading & offline behavior
10. Testing matrix and test cases
11. Build configuration and signing
12. Asset Links (digital assetlinks.json)
13. Example code snippets (Flutter + Kotlin examples)
14. Play Store publishing checklist
15. Common pitfalls & recommendations
16. Appendix: sample payloads & configs

## 1. Project metadata

- **Hosted site:** https://m.jeeey.com/tabs/...
- **Application ID:** com.jeeey.shopin
- **Current version:** 1.0.0 (build 3046)
- **Delivery format expected:** Android App Bundle (`.aab`) and optionally APKs for internal testing

## 2. Required inputs (must be provided before development/publishing)

Provide the following items to the developer exactly as listed:

- **Signing keystore file** (`key.jks`) and credentials: `keystorePassword`, `keyAlias`, `keyPassword`. Place in `android/app/key.jks` (or share securely).
- **Firebase config file:** `google-services.json` (must be configured for `com.jeeey.shopin` and include SHA-1 of the signing key).
- **Firebase project details:** Project ID, Sender ID, Server key (for server push integration).
- **Google OAuth client IDs:** Android client (with correct SHA-1), Web client (if site uses OAuth on web). If using Google Sign-In natively give client id(s).
- **Facebook App ID & App Secret** (if using Facebook Login). Also Facebook OAuth redirect URIs configured.
- **Privacy Policy URL** and **Terms of Service URL**.
- **App icons and artwork** (512x512 feature graphic + multi-res icons). Provide PNG/SVG assets.
- **assetlinks.json content** or permission to publish it under `https://m.jeeey.com/.well-known/assetlinks.json`.
- **Test accounts** (for reviewer and QA): login credentials covering typical user roles.
- **IDs / classes for sign-in buttons** in the web app (recommended): examples `id="jeeey-google-btn"`, `id="jeeey-facebook-btn"` so native code can intercept clicks via JS bridge.

## 3. File placement and repository structure

Recommended minimal project structure (Android / Flutter hybrid example):

```
project-root/
├─ android/
│  ├─ app/
│  │  ├─ key.jks              # signing key (secure!)
│  │  ├─ google-services.json # Firebase config
│  │  └─ src/main/AndroidManifest.xml
│  └─ build.gradle
├─ lib/ (if Flutter) or app/src/... (native)
├─ assets/
│  ├─ logo.png
│  └─ splash_background.png
├─ README.md
└─ .well-known/assetlinks.json # published on site under m.jeeey.com
```

Place `key.jks` strictly under `android/app/` or in a secure location referenced by `build.gradle` signing config.

## 4. Google Play & Android compatibility requirements

- **targetSdkVersion:** Must target the latest Play-required SDK (Android 15 / API 35 as of this file). Update `compileSdk` & `targetSdk` accordingly.
- **minSdkVersion:** Recommended 21 (Android 5.0) for widest device coverage. Note: some modern Android libraries may require minSdk 23+; choose and document trade-offs.
- **Bundle format:** Upload `.aab` to Play Console.
- **Data Safety & Privacy:** Complete Play Console Data Safety form, and provide a live privacy policy URL.
- **Minimum functionality:** The app must provide native capabilities beyond mere WebView rendering (notifications, native login hooks, offline caching, native share, etc.).

## 5. UX & behavior specifications

Implement the following behaviors exactly to guarantee acceptance and UX quality.

### Splash Screen

- **Background color:** maroon / brand color (example: `#7B1B2B`).
- **Center logo:** large, crisp PNG/SVG.
- Around the logo show a circular loading indicator that animates while the WebView preloads the site.
- Preload the WebView page in background during splash. Show the WebView only when `loadingProgress >= 65-80%` and then perform a smooth fade transition from splash to WebView (no flicker).

### Preload & App Entry

- Use hidden/offstage WebView (or headless) to load `https://m.jeeey.com/tabs/` while splash displays.
- Only attach/visualize the WebView when the ready threshold is passed.

### Dynamic Theme (Status Bar & Navigation Bar)

Two supported methods (implement one or both):

1. Read `<meta name="theme-color">` from the page and apply it to Android status/navigation bars via JS bridge when page starts/updates.
2. REST endpoint: fetch `GET https://m.jeeey.com/app/theme` returning `{"primary":"#7B1B2B","accent":"#ffffff"}`, and apply those values on app startup and when web page emits a theme change event.

When theme changes occur in the website, the site must call the JS bridge API (e.g. `window.postMessage({type:'theme', color:'#7B1B2B'})`) and the native app applies the new color seamlessly.

### Back Button Logic (No flicker, consistent)

- If `webView.canGoBack()` → call `webView.goBack()` and do NOT reload the entire WebView view hierarchy.
- If `!webView.canGoBack()` (i.e., at the app root/homepage):
  - Show a centered toast/snackbar/badge: **"Press again to exit"** (text color white on translucent maroon bar), visible for 2 seconds.
  - If back pressed again within 2 seconds → call `finish()` (close app).

### Copy, Touch, Scroll & Performance

- Enable hardware acceleration for the Activity/Window.
- WebSettings: `javaScriptEnabled=true`, `domStorageEnabled=true`, `setUseWideViewPort(true)`, `setLoadWithOverviewMode(true)`.
- Enable `setLayerType(View.LAYER_TYPE_HARDWARE)` where applicable for smoother rendering.

### Share URL

Provide a native share action that opens the Android share sheet with the current page URL (`webView.getUrl()`).

## 6. Firebase Cloud Messaging (FCM) behavior

Implementation detail for notifications and in-app dialogs:

### Recommended message type

Use **data messages** for maximum control. Data messages are always delivered to `onMessageReceived` and managed by the app, enabling custom behavior.

### Foreground behavior

- If the app is foreground and the user is on the app's home page → show a custom center-screen dialog with:
  - Notification image (optional) loaded from payload image URL.
  - Title and body text.
  - An action button **Open** that deep-links into the specified URL in-app.
- If the user is not on the home page → store the notification payload locally (e.g., Room DB or SharedPreferences queue) and do NOT show the dialog until the user returns to home page.

### Background behavior

Let Android show the normal system notification (use a Notification Channel). When the user taps the notification, open the app (resume) and navigate to the provided in-app URL.

### FCM Service Implementation

Implement `FirebaseMessagingService` to handle `onMessageReceived(remoteMessage)`. When in foreground, broadcast the payload to the Activity/Fragment via `LocalBroadcastManager` or other IPC.

### Notification Channel

Create at least one notification channel for promotional/transactional messages with appropriate importance.

## 7. Native Google & Facebook sign-in (recommended architecture)

Do not rely only on OAuth inside the WebView for Google Sign-In—use the native SDKs for better security, UX and compliance.

### Google Sign-In (Native recommended)

- Register an Android OAuth client in Google Cloud Console with the correct package name and SHA-1 fingerprint of the signing key.
- Implement native Google Sign-In.
- After obtaining `idToken` or `authCode`, POST that token to backend `POST /auth/mobile` endpoint to create or link server-side session.

### Facebook Login (Native recommended)

- Use Facebook Android SDK and login flow.
- Obtain `accessToken`, send it to backend for server-side verification and session creation.

### Web buttons coordination

- If sign-in buttons are present on the web page, the site should emit a JS message when they are clicked (e.g. `window.postMessage({type: 'social-login', provider: 'google'})`).
- The native app must listen to that message and trigger native sign-in. This maintains consistent UX and ensures Google's policies.

## 8. WebView security & privacy

- `setAllowFileAccess(false)`, `setAllowContentAccess(false)` unless file access is explicitly required.
- `mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW` (or evaluate on case-by-case basis).
- Enable Safe Browsing if available: `WebView.enableSafeBrowsing(context, callback)`.
- Validate and restrict `shouldOverrideUrlLoading` to only allow safe schemes (`https://`, `app://`, `intent://`) and deep links that are whitelisted.
- Store sensitive tokens securely (Android Keystore / EncryptedSharedPreferences) and avoid persisting raw session tokens unencrypted.

## 9. Caching, preloading & offline support

- Enable HTTP caching and use server-side `Cache-Control` headers.
- If the site supports PWA + service workers, modern WebView (Chromium) will benefit from service workers for offline content.
- Implement a local cache fallback strategy: when offline, load cached HTML/CSS/JS or show a friendly offline page and retry logic.
- Use preload approach in splash screen to fetch initial HTML + critical assets.

## 10. Testing matrix and test cases

**Devices / API levels to test on physical devices or reliable emulators:**
- API 21, 23, 26, 29, 31, 33, 34, 35+

**Functional test cases:**
- Cold start (splash & preload) → verify fade and load progress.
- Back navigation behavior (navigation stack & double-back exit at root).
- Google sign-in flow (native) and session binding on website.
- Facebook sign-in flow (native).
- Push notification: foreground dialog on home, queue when in other pages, system notification in background.
- Deep link handling from notification and external links.
- File uploads (`input[type=file]`) from within web pages.
- Share action triggers native share sheet with the correct URL.
- Offline fallback & cache behavior.
- UI responsiveness across different screen sizes and orientations (small phones, phablets, tablets).

## 11. Build configuration and signing

Add signing configuration to `android/app/build.gradle` or Flutter `android/app/build.gradle`:

```gradle
android {
    compileSdk 35
    defaultConfig {
        applicationId "com.jeeey.shopin"
        minSdk 21
        targetSdk 35
        versionCode 3046
        versionName "1.0.0"
    }
    signingConfigs {
        release {
            storeFile file("key.jks")
            storePassword "STORE_PASSWORD"
            keyAlias "KEY_ALIAS"
            keyPassword "KEY_PASSWORD"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            // Optionally add proguard or R8 rules
        }
    }
}
```

> **Note:** Use `gradle.properties` or CI secrets to store passwords. Never commit raw passwords to version control.

## 12. Asset Links (digital assetlinks.json)

Create and host an `assetlinks.json` to enable verified links and app-site association.

**Location:** `https://m.jeeey.com/.well-known/assetlinks.json`

**Example:**
```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.jeeey.shopin",
      "sha256_cert_fingerprints": ["SHA-256-FINGERPRINT-HERE"]
    }
  }
]
```

Replace `SHA-256-FINGERPRINT-HERE` with the actual fingerprint of the signing key.

## 13. Example code snippets

These are minimal, copy-ready examples for a Flutter WebView implementation and Android FCM service. They illustrate the preload + splash logic and the FCM broadcast flow.

### 13.1 Flutter example (splash + preload WebView)

```dart
// minimal main.dart concept
import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';
import 'dart:async';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget { @override _MyAppState createState() => _MyAppState(); }

class _MyAppState extends State<MyApp> {
  final Completer<WebViewController> _ctrl = Completer();
  bool _showWeb = false;
  double _progress = 0.0;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Stack(children: [
          Offstage(
            offstage: !_showWeb,
            child: WebView(
              initialUrl: 'https://m.jeeey.com/tabs/',
              javascriptMode: JavascriptMode.unrestricted,
              onWebViewCreated: (c) => _ctrl.complete(c),
              onProgress: (p) {
                setState(() => _progress = p / 100.0);
                if (p >= 70 && !_showWeb) setState(() => _showWeb = true);
              },
              javascriptChannels: {
                JavascriptChannel(name: 'Theme', onMessageReceived: (m) { /* handle theme change */ })
              },
            ),
          ),
          Offstage(
            offstage: _showWeb,
            child: Container(
              color: Color(0xFF7B1B2B),
              child: Center(
                child: Column(mainAxisSize: MainAxisSize.min, children: [
                  Stack(alignment: Alignment.center, children: [
                    SizedBox(width: 180, height: 180, child: CircularProgressIndicator(value: _progress)),
                    Image.asset('assets/logo.png', width: 140, height: 140),
                  ]),
                  SizedBox(height: 20),
                  Text('Loading...', style: TextStyle(color: Colors.white)),
                ]),
              ),
            ),
          ),
        ]),
      ),
    );
  }
}
```

### 13.2 Android (Kotlin) FCM example

```kotlin
class MyFirebaseMessagingService: FirebaseMessagingService() {
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val data = remoteMessage.data
    if (isAppInForeground()) {
      val i = Intent("FCM_PUSH")
      i.putExtra("title", data["title"])
      i.putExtra("body", data["body"])
      i.putExtra("image", data["image"])
      LocalBroadcastManager.getInstance(this).sendBroadcast(i)
    } else {
      showNotification(remoteMessage)
    }
  }
}
```

In the Activity register a `BroadcastReceiver` for the `FCM_PUSH` action and show the custom dialog as specified in Section 6.

## 14. Play Store publishing checklist (step-by-step)

1. Validate `targetSdkVersion` (API 35 or above if required by Play policy).
2. Ensure `google-services.json` matches package and SHA-1.
3. Sign the release build with `key.jks`.
4. Prepare Play Console assets: screenshots (multiple device sizes), feature graphic, short and full description, and privacy policy URL.
5. Complete Data Safety form accurately.
6. Upload `.aab` to Internal Test track first.
7. Provide test accounts in the Play Console for reviewers.
8. After internal test validation, proceed to closed testing and then production.
9. Monitor Crashlytics and analytics after release and be ready to push fixes.

## 15. Common pitfalls & recommendations

- **Do not depend only on WebView OAuth for Google sign-in.** Many Google features are blocked in embedded WebViews; use native sign-in.
- **Avoid committing keystore or secrets in the repository.** Use CI secrets for building release artifacts.
- **Pre-register SHA-1 fingerprints** in Google Console (both debug and release keys) to avoid auth failures.
- **Test push notifications thoroughly** in all app states.
- **Make sure the app provides native value** beyond a simple web wrapper.

## 16. Appendix: sample notification payloads & assetlinks

### FCM data message example

```json
{
  "to": "<FCM_TOKEN>",
  "data": {
    "type": "promo",
    "title": "Today's Offer",
    "body": "30% off on selected items!",
    "image": "https://m.jeeey.com/uploads/offer.jpg",
    "url": "https://m.jeeey.com/tabs/deals"
  },
  "priority": "high"
}
```

### assetlinks.json (example)

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.jeeey.shopin",
      "sha256_cert_fingerprints": ["SHA-256-FINGERPRINT-HERE"]
    }
  }
]
```

## Deliverables for developer

- Fully working Android app implementing all features in this README.
- `README.md` (this file) included in the repo root.
- `assetlinks.json` published on site.
- AAB file signed with `key.jks` ready for Play upload.
- QA test report with pass/fail results for all test cases in Section 10.

## Final note

This README is intentionally exhaustive and designed so a developer can follow it line-by-line. If you want, I can also generate:
- A ready-to-download README.md file
- A PDF version
- A separate concise checklist for the Play Store reviewer
- Full Kotlin and Flutter reference implementations (complete Activities/Services/Handlers)

---

## QA Note

> **Test executions are pending real environment setup:** Push tokens, OAuth keys, and theme endpoint availability are required for full testing. The app has been structured with all placeholders clearly marked. Once credentials are provided, update the placeholder files and run full integration tests against the real endpoints.
