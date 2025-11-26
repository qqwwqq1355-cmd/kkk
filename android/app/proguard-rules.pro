# Shopin ProGuard Rules
# ======================
# Add project specific ProGuard rules here.

# Flutter
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.** { *; }
-keep class io.flutter.util.** { *; }
-keep class io.flutter.view.** { *; }
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# WebView
-keep class android.webkit.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep custom classes
-keep class com.jeeey.shopin.** { *; }

# Prevent obfuscation of types used by JavaScript interfaces
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Prevent removal of native methods
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
