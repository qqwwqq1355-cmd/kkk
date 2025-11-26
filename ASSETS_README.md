# Asset Placement Guide / دليل وضع الملفات

This document explains where to place your app assets (icons, logos, keys, etc.).

هذا الدليل يشرح أين تضع ملفات التطبيق (الأيقونات، الشعار، المفاتيح، إلخ).

---

## 📁 مسارات الملفات المهمة

### 1. ملف key.properties (إعدادات التوقيع)

**المسار:** `android/key.properties`

**المحتوى:**
```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=YOUR_KEY_ALIAS
storeFile=key.jks
```

**ملاحظة:** 
- استبدل القيم أعلاه بقيمك الفعلية
- هذا الملف موجود بالفعل في `.gitignore` ولن يتم رفعه

---

### 2. ملف key.jks (مفتاح التوقيع)

**المسار:** `android/app/key.jks`

**ملاحظة مهمة:** 
- ضع ملف `key.jks` في المجلد `android/app/`
- هذا الملف لن يُرفع لأنه موجود في `.gitignore`
- احتفظ بنسخة احتياطية في مكان آمن

---

### 3. شعار شاشة البداية (Splash Screen Logo)

**المسار:** `assets/logo.png`

**المتطلبات:**
- الصيغة: PNG مع شفافية
- الحجم: 512×512 بكسل (موصى به)
- سيظهر الشعار على خلفية عنابية (#7B1B2B)

---

### 4. أيقونة التطبيق (App Icon)

**المسارات:**

| الكثافة | الحجم | المسار |
|---------|-------|--------|
| mdpi | 48×48 بكسل | `android/app/src/main/res/mipmap-mdpi/ic_launcher.png` |
| hdpi | 72×72 بكسل | `android/app/src/main/res/mipmap-hdpi/ic_launcher.png` |
| xhdpi | 96×96 بكسل | `android/app/src/main/res/mipmap-xhdpi/ic_launcher.png` |
| xxhdpi | 144×144 بكسل | `android/app/src/main/res/mipmap-xxhdpi/ic_launcher.png` |
| xxxhdpi | 192×192 بكسل | `android/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` |

**الأيقونات الدائرية:**
نفس المجلدات، لكن باسم `ic_launcher_round.png`

**طريقة سهلة للإنشاء:**
استخدم Android Asset Studio: https://romannurik.github.io/AndroidAssetStudio/

---

### 5. أيقونة الإشعارات (Notification Icon)

**المسارات:** أنشئ هذه الملفات في `android/app/src/main/res/`:

| الكثافة | الحجم | المسار |
|---------|-------|--------|
| mdpi | 24×24 بكسل | `drawable-mdpi/ic_notification.png` |
| hdpi | 36×36 بكسل | `drawable-hdpi/ic_notification.png` |
| xhdpi | 48×48 بكسل | `drawable-xhdpi/ic_notification.png` |
| xxhdpi | 72×72 بكسل | `drawable-xxhdpi/ic_notification.png` |
| xxxhdpi | 96×96 بكسل | `drawable-xxxhdpi/ic_notification.png` |

**المتطلبات:**
- يجب أن يكون **شكل أبيض فقط** على خلفية شفافة
- نظام أندرويد سيلونها تلقائياً

**بعد إنشاء الأيقونات، عدّل AndroidManifest.xml:**
```xml
<meta-data
    android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@drawable/ic_notification" />
```

---

## 📋 ملخص المسارات

| الملف | المسار الكامل |
|-------|---------------|
| مفتاح التوقيع | `android/app/key.jks` |
| إعدادات التوقيع | `android/key.properties` |
| شعار السبلاش | `assets/logo.png` |
| أيقونات التطبيق | `android/app/src/main/res/mipmap-*/ic_launcher.png` |
| الأيقونات الدائرية | `android/app/src/main/res/mipmap-*/ic_launcher_round.png` |
| أيقونة الإشعارات | `android/app/src/main/res/drawable-*/ic_notification.png` |

---

## 🔧 إعداد Facebook

لإتمام إعداد Facebook، تحتاج إلى:

1. **الحصول على Client Token:**
   - اذهب إلى https://developers.facebook.com/
   - اختر تطبيقك
   - Settings > Advanced > Client Token
   
2. **عدّل الملف:** `android/app/src/main/res/values/strings.xml`
   - استبدل `YOUR_FACEBOOK_CLIENT_TOKEN` بالقيمة الفعلية

3. **أضف Key Hash:**
   - في Facebook Developer Console
   - Settings > Basic > Key Hashes

---

## 🎨 Play Store Assets

| العنصر | الحجم | الوصف |
|--------|-------|-------|
| App Icon | 512×512 بكسل | أيقونة عالية الدقة |
| Feature Graphic | 1024×500 بكسل | بانر المتجر |
| Screenshots | متعددة | على الأقل لقطتين |
