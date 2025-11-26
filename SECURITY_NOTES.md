# Security Implementation Notes

**Package:** com.jeeey.shopin  
**Version:** 1.0.0 (build 3046)

This document summarizes the security settings and best practices implemented in the Shopin WebView app.

---

## WebView Security Settings

### File Access Restrictions

The following restrictions are applied to prevent unauthorized file system access:

| Setting | Value | Purpose |
|---------|-------|---------|
| `setAllowFileAccess` | `false` | Prevents WebView from accessing local files |
| `setAllowContentAccess` | `false` | Prevents access to content providers |
| `setAllowFileAccessFromFileURLs` | `false` | Blocks file:// to file:// access |
| `setAllowUniversalAccessFromFileURLs` | `false` | Blocks file:// to any origin access |

> **Note:** These are Flutter webview_flutter defaults. If using native WebView wrappers, ensure these are explicitly set.

### Mixed Content Policy

```
mixedContentMode = MIXED_CONTENT_NEVER_ALLOW
```

- HTTPS pages cannot load HTTP resources
- Prevents man-in-the-middle attacks on secure pages
- All content must be served over TLS

### Safe Browsing

- Android WebView Safe Browsing is enabled by default on Android 8.1+
- Warns users about malicious sites
- No additional configuration required for basic protection

---

## URL Scheme Whitelisting

Navigation is restricted to approved URL schemes only:

| Scheme | Allowed | Purpose |
|--------|---------|---------|
| `https://` | ✅ Yes | Secure web content |
| `intent://` | ✅ Yes | Android app intents (controlled) |
| `app://` | ✅ Yes | Custom app schemes (internal) |
| `http://` | ❌ No | Insecure - blocked |
| `javascript:` | ❌ No | XSS vector - blocked |
| `file://` | ❌ No | Local file access - blocked |
| `data:` | ❌ No | Potential XSS - blocked |

Implementation in `main.dart`:
```dart
bool _isAllowedScheme(String scheme) {
  const allowedSchemes = ['https', 'intent', 'app'];
  return allowedSchemes.contains(scheme.toLowerCase());
}
```

---

## Network Security

### Network Security Config (`network_security_config.xml`)

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

- All cleartext (HTTP) traffic is blocked
- Only system-trusted certificates accepted
- No custom CA certificates pinned (standard trust store)

### Domain Restrictions

- Primary domain: `m.jeeey.com`
- Deep links verified via `assetlinks.json`

---

## Token & Credential Storage

### Recommendations (TODO for Implementation)

| Data Type | Storage Method | Notes |
|-----------|----------------|-------|
| FCM Token | SharedPreferences | Non-sensitive, can be regenerated |
| OAuth Tokens | EncryptedSharedPreferences | Use AndroidX Security library |
| Session Tokens | EncryptedSharedPreferences | Never store in plain SharedPrefs |
| Keystore | Android Keystore | For encryption keys |

### Implementation Guidance

```kotlin
// Recommended: Use EncryptedSharedPreferences for sensitive data
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val securePrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

## Signing Key Security

### DO NOT COMMIT SECRETS

The following files must **never** be committed to version control:

- `android/app/key.jks` - Production signing keystore
- `key.properties` - Keystore passwords
- Real `google-services.json` with production keys

### Secure Credential Handling

1. **CI/CD Secrets**: Store passwords in CI environment variables
2. **Local Development**: Use `gradle.properties` (gitignored)
3. **Team Sharing**: Use secure password manager or vault

### .gitignore Entries

```gitignore
# Keystore files
*.jks
*.keystore
key.properties

# Keep placeholder for documentation
!*.placeholder.txt
```

---

## Firebase Security

### FCM Message Handling

- Only data messages are used (not notification messages)
- Enables app-controlled handling in foreground
- No automatic notification display without app logic

### Token Refresh

- New tokens are logged for development
- TODO: Implement secure token submission to backend

---

## JavaScript Bridge Security

### Channel Isolation

Separate JS channels for different purposes:
- `ThemeBridge` - Theme color updates only
- `SocialLoginBridge` - Social login triggers only

### Message Validation

All incoming messages are validated:
```dart
void _handleThemeMessage(String message) {
  try {
    final data = jsonDecode(message);
    if (data['type'] == 'theme' && data['color'] != null) {
      // Validate color format before applying
      _applyThemeColor(data['color']);
    }
  } catch (e) {
    debugPrint('Error parsing theme message: $e');
  }
}
```

---

## Third-Party SDK Security

### Current Dependencies

| Dependency | Purpose | Security Notes |
|------------|---------|----------------|
| webview_flutter | WebView | Official Flutter plugin |
| connectivity_plus | Network state | Read-only network info |
| share_plus | Native share | No sensitive data handling |
| firebase_messaging | Push notifications | Encrypted transport |

### Future SDKs (TODO)

| SDK | Purpose | Required Configuration |
|-----|---------|----------------------|
| Google Sign-In | Native auth | OAuth client ID, SHA-1 |
| Facebook Login | Native auth | App ID, App Secret |

---

## Audit Checklist

| Item | Status | Notes |
|------|--------|-------|
| File access disabled | ✅ | Default in Flutter webview |
| Mixed content blocked | ✅ | Configured |
| Cleartext blocked | ✅ | network_security_config.xml |
| Scheme whitelist | ✅ | https, intent, app only |
| No committed secrets | ✅ | .gitignore configured |
| Token encryption | ⬜ | TODO: EncryptedSharedPreferences |
| Safe Browsing | ✅ | System default |

---

## Vulnerability Disclosure

If you discover a security vulnerability in this application:

1. Do NOT create a public issue
2. Contact the security team privately
3. Provide detailed reproduction steps
4. Allow time for patch before disclosure

---

## References

- [Android WebView Security](https://developer.android.com/guide/webapps/best-practices)
- [Network Security Config](https://developer.android.com/training/articles/security-config)
- [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
