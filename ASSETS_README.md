# Asset Placement Guide / دليل وضع الصور

This document explains where to place your app assets (icons, logos, etc.).

هذا الدليل يشرح أين تضع صور التطبيق (الأيقونات، الشعار، إلخ).

---

## 1. Splash Screen Logo / شعار شاشة البداية

**Location / الموقع:** `assets/logo.png`

**Requirements / المتطلبات:**
- Format: PNG with transparency / صيغة PNG مع شفافية
- Size: 512x512 pixels (recommended) / الحجم: 512×512 بكسل (موصى به)
- The logo will be displayed on the maroon (#7B1B2B) splash screen
- الشعار سيظهر على شاشة البداية العنابية

**Current file:** Replace the placeholder `assets/logo.png` with your actual logo.

---

## 2. App Icon / أيقونة التطبيق

**Locations / المواقع:**

| Density | Size | Path |
|---------|------|------|
| mdpi | 48×48 px | `android/app/src/main/res/mipmap-mdpi/ic_launcher.png` |
| hdpi | 72×72 px | `android/app/src/main/res/mipmap-hdpi/ic_launcher.png` |
| xhdpi | 96×96 px | `android/app/src/main/res/mipmap-xhdpi/ic_launcher.png` |
| xxhdpi | 144×144 px | `android/app/src/main/res/mipmap-xxhdpi/ic_launcher.png` |
| xxxhdpi | 192×192 px | `android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` |

**Round Icons / الأيقونات الدائرية:**
Same folders, but named `ic_launcher_round.png`

**Easy way to generate / طريقة سهلة للإنشاء:**
1. Use Android Asset Studio: https://romannurik.github.io/AndroidAssetStudio/
2. Or use Flutter launcher_icons package

---

## 3. Notification Icon / أيقونة الإشعارات

**Locations / المواقع:**

Create these files in `android/app/src/main/res/`:

| Density | Size | Path |
|---------|------|------|
| mdpi | 24×24 px | `drawable-mdpi/ic_notification.png` |
| hdpi | 36×36 px | `drawable-hdpi/ic_notification.png` |
| xhdpi | 48×48 px | `drawable-xhdpi/ic_notification.png` |
| xxhdpi | 72×72 px | `drawable-xxhdpi/ic_notification.png` |
| xxxhdpi | 96×96 px | `drawable-xxxhdpi/ic_notification.png` |

**Requirements / المتطلبات:**
- Must be **white silhouette** on transparent background
- يجب أن يكون **شكل أبيض** على خلفية شفافة
- Android will tint it with the notification color

**After creating the icons / بعد إنشاء الأيقونات:**
Update `AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@drawable/ic_notification" />
```

---

## 4. Play Store Assets / صور متجر بلاي

**Required / مطلوب:**

| Asset | Size | Description |
|-------|------|-------------|
| App Icon | 512×512 px | High resolution app icon |
| Feature Graphic | 1024×500 px | Banner displayed on Play Store |
| Screenshots | Various | At least 2 phone screenshots |

---

## 5. Quick Setup with flutter_launcher_icons

Add to `pubspec.yaml`:

```yaml
dev_dependencies:
  flutter_launcher_icons: ^0.13.1

flutter_launcher_icons:
  android: true
  ios: false
  image_path: "assets/app_icon.png"
  adaptive_icon_background: "#7B1B2B"
  adaptive_icon_foreground: "assets/app_icon_foreground.png"
```

Then run:
```bash
flutter pub get
flutter pub run flutter_launcher_icons
```

---

## Summary / ملخص

| Asset | Location |
|-------|----------|
| Splash Logo | `assets/logo.png` |
| App Icons | `android/app/src/main/res/mipmap-*/ic_launcher.png` |
| Round Icons | `android/app/src/main/res/mipmap-*/ic_launcher_round.png` |
| Notification Icon | `android/app/src/main/res/drawable-*/ic_notification.png` |
