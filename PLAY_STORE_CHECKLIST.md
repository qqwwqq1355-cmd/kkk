# Play Store Publishing Checklist

**Package:** com.jeeey.shopin  
**Version:** 1.0.0 (build 3046)

Condensed checklist from README Section 14 for Play Store submission.

---

## Pre-Release Verification

- [ ] **1. SDK Version Validation**
  - Verify `targetSdkVersion` is API 35 or above (as required by Play policy)
  - Confirm `compileSdk` matches targetSdk
  - Test on latest Android version (API 35+)

- [ ] **2. Firebase Configuration**
  - `google-services.json` correctly configured for `com.jeeey.shopin`
  - SHA-1 fingerprint of **release** signing key registered in Firebase Console
  - FCM messaging enabled and tested

- [ ] **3. Build Signing**
  - Release build signed with production `key.jks`
  - Keystore backed up securely
  - SHA-256 fingerprint ready for assetlinks.json

---

## Play Console Setup

- [ ] **4. Store Listing Assets**
  - [ ] App icon (512x512 PNG)
  - [ ] Feature graphic (1024x500 PNG)
  - [ ] Screenshots - Phone (at least 2)
  - [ ] Screenshots - 7" Tablet (recommended)
  - [ ] Screenshots - 10" Tablet (recommended)
  - [ ] Short description (80 chars max)
  - [ ] Full description (4000 chars max)

- [ ] **5. Privacy & Legal**
  - [ ] Privacy Policy URL (live, accessible)
  - [ ] Terms of Service URL (if applicable)
  - [ ] Data Safety form completed accurately:
    - Data collection disclosures
    - Data sharing practices
    - Security practices
    - Data deletion options

---

## Testing Tracks

- [ ] **6. Internal Test Track**
  - Upload `.aab` to Internal Testing
  - Add internal testers (email list)
  - Complete internal test cycle
  - Fix any critical issues

- [ ] **7. Test Accounts for Reviewers**
  - Provide test credentials in Play Console
  - Ensure test account covers typical user flows
  - Document any special instructions

---

## Release Progression

- [ ] **8. Closed Testing (Beta)**
  - Promote from Internal to Closed Testing
  - Expand tester group
  - Collect feedback and crash reports
  - Address significant issues

- [ ] **9. Production Release**
  - Review all store listing details
  - Verify content rating questionnaire
  - Set rollout percentage (staged rollout recommended)
  - Submit for review

---

## Post-Release

- [ ] **10. Monitoring**
  - [ ] Firebase Crashlytics configured and monitored
  - [ ] Analytics tracking key events
  - [ ] Play Console vitals dashboard reviewed
  - [ ] User reviews monitored for issues
  - [ ] Ready to push hotfix if critical issues found

---

## Common Issues to Avoid

| Issue | Solution |
|-------|----------|
| SHA-1 mismatch | Re-register fingerprint in Firebase & Google Console |
| WebView-only app rejection | Ensure native features documented (push, native login, share) |
| Missing privacy policy | Host policy at accessible URL before submission |
| Wrong keystore for updates | Always use same release keystore |
| Data safety inaccuracies | Review all data collection points |

---

## Release Credentials Reference

| Item | Location | Notes |
|------|----------|-------|
| Release Keystore | `android/app/key.jks` | DO NOT commit - use CI secrets |
| Firebase Config | `android/app/google-services.json` | Must match package & SHA-1 |
| Asset Links | `.well-known/assetlinks.json` | Host at `https://m.jeeey.com/.well-known/` |

---

## Sign-off

| Step | Completed By | Date |
|------|--------------|------|
| Build verified | | |
| Store listing reviewed | | |
| Data safety submitted | | |
| Internal test passed | | |
| Production submitted | | |
