# Test Matrix - Shopin WebView App

**Package:** com.jeeey.shopin  
**Version:** 1.0.0 (build 3046)

This document enumerates all functional test cases as specified in README Section 10.

---

## Device / API Level Coverage

Test on physical devices or reliable emulators at the following API levels:

| API Level | Android Version | Priority |
|-----------|-----------------|----------|
| API 21    | Android 5.0     | High (minSdk) |
| API 23    | Android 6.0     | Medium |
| API 26    | Android 8.0     | Medium |
| API 29    | Android 10      | High |
| API 31    | Android 12      | High |
| API 33    | Android 13      | High |
| API 34    | Android 14      | High |
| API 35+   | Android 15+     | High (targetSdk) |

---

## Functional Test Cases

### 1. Cold Start (Splash & Preload)

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| CS-01 | Launch app from cold state | Splash screen displays with maroon (#7B1B2B) background | ⬜ Pending |
| CS-02 | Logo visibility on splash | Logo centered and clearly visible | ⬜ Pending |
| CS-03 | Progress indicator animates | Circular progress indicator reflects actual load progress | ⬜ Pending |
| CS-04 | WebView preloads in background | m.jeeey.com/tabs/ loads while splash shows | ⬜ Pending |
| CS-05 | Transition at 65-80% progress | Smooth fade transition from splash to WebView | ⬜ Pending |
| CS-06 | No white flash/flicker | Transition is seamless without visual artifacts | ⬜ Pending |

### 2. Back Navigation Behavior

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| BN-01 | Back from nested page | webView.goBack() navigates to previous page | ⬜ Pending |
| BN-02 | No reload on back | Page state preserved, no full reload | ⬜ Pending |
| BN-03 | Back at root page | "Press again to exit" snackbar appears | ⬜ Pending |
| BN-04 | Snackbar styling | White text on translucent maroon background | ⬜ Pending |
| BN-05 | Snackbar duration | Visible for 2 seconds | ⬜ Pending |
| BN-06 | Double back within 2s | App exits/closes | ⬜ Pending |
| BN-07 | Back after snackbar timeout | Shows snackbar again, does not exit | ⬜ Pending |

### 3. Google Sign-In Flow (Native)

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| GS-01 | JS bridge detection | App receives social-login message from web | ⬜ Pending |
| GS-02 | Native sign-in triggers | Google Sign-In SDK launches | ⬜ Pending |
| GS-03 | Token obtained | idToken successfully obtained from Google | ⬜ Pending |
| GS-04 | Session binding | Token sent to backend, session created | ⬜ Pending |
| GS-05 | Error handling | Graceful error shown if sign-in fails | ⬜ Pending |

### 4. Facebook Sign-In Flow (Native)

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| FS-01 | JS bridge detection | App receives social-login message with provider='facebook' | ⬜ Pending |
| FS-02 | Facebook SDK launches | Facebook login dialog appears | ⬜ Pending |
| FS-03 | Access token obtained | accessToken successfully retrieved | ⬜ Pending |
| FS-04 | Server verification | Token sent to backend for verification | ⬜ Pending |
| FS-05 | Error handling | Graceful error if login cancelled or failed | ⬜ Pending |

### 5. Push Notifications

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| PN-01 | Token generation | FCM token generated on first launch | ⬜ Pending |
| PN-02 | Foreground on home - dialog | Custom center dialog with title, body, image | ⬜ Pending |
| PN-03 | Dialog Open button | Deep links to specified URL in-app | ⬜ Pending |
| PN-04 | Foreground not on home - queue | Notification queued, not displayed immediately | ⬜ Pending |
| PN-05 | Return to home - show queued | Queued notifications displayed on return | ⬜ Pending |
| PN-06 | Background - system notification | Standard Android notification shown | ⬜ Pending |
| PN-07 | Notification tap - open app | App resumes and navigates to URL | ⬜ Pending |
| PN-08 | Notification channel | Channel created with correct importance | ⬜ Pending |

### 6. Deep Link Handling

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| DL-01 | HTTPS deep link | Opens corresponding page in WebView | ⬜ Pending |
| DL-02 | App scheme (shopin://) | Handled correctly by app | ⬜ Pending |
| DL-03 | External link from notification | Navigates to correct URL | ⬜ Pending |
| DL-04 | Invalid deep link | Gracefully handled, no crash | ⬜ Pending |

### 7. File Uploads

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| FU-01 | input[type=file] single | File picker opens, file selected | ⬜ Pending |
| FU-02 | input[type=file] multiple | Multiple files can be selected | ⬜ Pending |
| FU-03 | Camera capture option | Camera option available if configured | ⬜ Pending |
| FU-04 | Upload completes | Selected file uploaded successfully | ⬜ Pending |

### 8. Share Action

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| SH-01 | Share button visible | FAB or share icon accessible | ⬜ Pending |
| SH-02 | Share sheet opens | Native Android share sheet appears | ⬜ Pending |
| SH-03 | Correct URL shared | Current page URL is shared content | ⬜ Pending |
| SH-04 | Share to app | URL successfully shared to target app | ⬜ Pending |

### 9. Offline Fallback & Cache Behavior

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| OF-01 | Initial load offline | Offline screen with retry button shown | ⬜ Pending |
| OF-02 | Connection restored | Retry loads page successfully | ⬜ Pending |
| OF-03 | Connection lost mid-session | Appropriate offline message shown | ⬜ Pending |
| OF-04 | Cached content available | Previously loaded content accessible offline (if service worker) | ⬜ Pending |
| OF-05 | Auto-retry on reconnect | Page auto-reloads when connection restored | ⬜ Pending |

### 10. UI Responsiveness

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| UI-01 | Small phone (≤5") | Layout adapts correctly | ⬜ Pending |
| UI-02 | Standard phone (5-6") | Layout looks good | ⬜ Pending |
| UI-03 | Phablet (6-7") | Layout adapts correctly | ⬜ Pending |
| UI-04 | Tablet (7"+) | Layout scales appropriately | ⬜ Pending |
| UI-05 | Portrait orientation | All elements visible and functional | ⬜ Pending |
| UI-06 | Landscape orientation | Layout adapts, no cut-off content | ⬜ Pending |
| UI-07 | Scroll performance | Smooth scrolling, no jank | ⬜ Pending |
| UI-08 | Touch responsiveness | Taps register accurately | ⬜ Pending |

### 11. Dynamic Theme

| Test ID | Test Case | Expected Result | Status |
|---------|-----------|-----------------|--------|
| TH-01 | Initial theme from endpoint | Status/nav bars match theme on startup | ⬜ Pending |
| TH-02 | Theme meta tag reading | App reads and applies meta theme-color | ⬜ Pending |
| TH-03 | Runtime theme change | JS postMessage updates system bars | ⬜ Pending |
| TH-04 | Theme fallback | Default maroon if theme fetch fails | ⬜ Pending |

---

## Test Execution Notes

> **⚠️ Test executions are pending real environment:**
> - FCM push tokens require actual Firebase configuration
> - OAuth testing requires configured Google/Facebook app credentials
> - Theme endpoint (https://m.jeeey.com/app/theme) availability required
> - assetlinks.json must be hosted on production domain for deep link verification

## Status Legend

- ⬜ Pending - Not yet tested
- ✅ Passed - Test passed successfully
- ❌ Failed - Test failed, needs investigation
- ⏭️ Skipped - Test skipped (N/A or blocked)

---

## QA Sign-off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| QA Engineer | | | |
| Developer | | | |
| Product Owner | | | |
