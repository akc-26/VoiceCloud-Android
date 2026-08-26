# VC-ANDROID-PH02-R05 Implementation Report

## 1. Baseline and authority

PH02-R05 was implemented from the frozen parent:

- Repository: `akc-26/VoiceCloud-Android`
- Branch: `VoiceCloud-Android-VC-ANDROID-PH01-R09`
- Exact parent commit: `ce56b1e36a4b2b563a65e4699df9193751f0978d`
- Backend authority: `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06`
- Backend authority commit: `7bee8d463786d21a7200b5663a499408bb229115`
- Android readiness contract: `VC-ANDROID-A0-R01`
- Roadmap authority: `VC-ANDROID-R06-MASTER-DEVELOPMENT-PLAN-R01`

No PH03+ business module was introduced.

## 2. Implemented module boundary

New physical module:

```text
:feature:auth
```

PH01 modules remain intact. The new feature owns PH02 auth/account presentation, request models, repositories and auth-specific DI. Shared security, network, preferences, logging, realtime and design-system authorities remain in their existing `:core:*` modules.

## 3. Backend contract alignment

PH02 consumes the finalized backend routes directly:

### Public
- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/phone/send-otp`
- `POST /auth/phone/login`
- `POST /auth/google/login`
- `POST /auth/guest/login`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`
- `POST /auth/refresh`
- `POST /creator-access/applications`

### Authenticated
- `GET /auth/me`
- `POST /auth/guest/upgrade`
- `POST /auth/logout`
- `POST /auth/logout-all`
- `PATCH /users/profile`
- `PATCH /users/settings`
- `POST /notifications/register-device`

Email/username User login sends exactly one backend identifier. Creator login sends an email and then independently verifies the returned backend role is exactly `CREATOR`. `ADMIN` and `SUPER_ADMIN` accounts are blocked from public Android portal access.

## 4. Session/security implementation

- Public bootstrap/auth Retrofit client never inherits a bearer token.
- Authenticated client reads tokens only from `TokenVault`.
- Existing Android Keystore AES-GCM token storage is preserved.
- OkHttp 401 authentication is routed through one synchronized refresh coordinator.
- If another request already rotated the failed access token, concurrent callers reuse the newly rotated token instead of issuing another refresh call.
- Successful refresh atomically stores both the new access token and the rotated refresh token.
- An active Socket.IO connection is re-authenticated after refresh-token rotation.
- Invalid refresh clears secure local auth and realtime state.
- App startup restores a persisted session by calling authoritative `GET /auth/me`.
- Logout/logout-all attempt server revocation and always clear local session authority.

## 5. User Portal

Implemented PH02 screens/states:
- portal selection
- User sign-in
- registration
- phone input
- OTP verification/resend
- forgot password
- reset password
- first-time onboarding (About / Interests / Preferences), with per-user device-local completion history so the three-step flow does not repeat after logout/relogin
- Guest authenticated handoff
- Guest upgrade (email / phone / Google)
- restricted account
- session expired
- authenticated User PH02 handoff

The authenticated handoff explicitly stops before PH03 Home/discovery functionality.

## 6. Creator Portal

- Separate Creator sign-in presentation.
- Creator email/password semantics aligned to the finalized Creator Studio.
- Creator visual route consumes centralized Creator design tokens and dark Creator presentation.
- Entry is denied unless the backend-returned role is exactly `CREATOR`.
- Non-Creator credentials are logged out/cleared before the role error is returned.
- Public Creator access application form is wired to `/creator-access/applications` with backend DTO bounds.
- 503 maintenance state is handled without bypassing backend authority.
- Creator authenticated handoff intentionally stops before later dashboard/room/RTC/earnings modules.

## 7. Google/Firebase correction

Backend source verification showed that `GoogleAuthService.verifyGoogleIdToken()` uses Firebase Admin `verifyIdToken()`. Therefore a raw Google OAuth ID token is not the final VoiceCloud backend credential.

PH02 implements:

```text
Google Sign-In OAuth ID token
    -> FirebaseAuth.signInWithCredential(GoogleAuthProvider credential)
    -> FirebaseUser.getIdToken(true)
    -> Firebase Authentication ID token
    -> POST /auth/google/login
```

The same Firebase-ID-token rule is used for Guest → Google upgrade.

Client public configuration is centralized through Gradle BuildConfig properties and fails closed when incomplete. No Firebase Admin/private credential is included in Android source.

## 8. Password recovery App Link

The manifest declares an HTTPS `autoVerify` route for `/auth/reset-password`. MainActivity:
- accepts HTTPS only;
- validates the host against the configured `WEB_BASE_URL`;
- requires a nontrivial one-time token;
- supports both cold-start intents and `onNewIntent` while already running.

Actual OS auto-verification additionally requires the deployed VoiceCloud web origin to publish the correct Digital Asset Links association for the installed signing certificate.

## 9. Device/FCM foundation

- Stable Android device ID is derived as a non-raw SHA-256-based identifier from `ANDROID_ID`.
- Device name/type, Android version and application version are included where the finalized backend DTO supports them.
- `/notifications/register-device` is wired after authenticated account establishment.
- `FcmTokenRegistrationFoundation.onNewToken()` provides the PH02 integration boundary for an actual Firebase Messaging token source in the notification phase without changing API authority.

## 10. Design authority

- User auth remains Light-first and consumes `:core:designsystem` centralized Consumer tokens.
- Creator auth consumes centralized Creator tokens and Creator dark presentation.
- No hard-coded replacement branding system or generic product redesign was introduced.
- Screen copy/workflow is derived from finalized Website and Creator Studio auth behavior where applicable.

## 11. Automated evidence available in this package

The retained PH02-R01 source verifier checks 85 individual invariants covering scope, endpoints, security/session behavior, routes, App Links, Google/Firebase exchange, design boundary and later-phase exclusion.

All PH01 regression gates invoked by the PH02 acceptance script were rerun after PH02 implementation and passed in the packaging environment.

The packaging environment does not contain the Android SDK/Windows Android Studio JBR toolchain. Therefore Android compilation, Android lint, APK assembly and connected-device instrumentation are **not claimed as passed here**. They are mandatory in the Windows acceptance script before PH02 can be marked accepted.
## 12. Cumulative corrective regressions

The first Windows R01 acceptance reached Gradle wrapper generation, then Gradle Kotlin DSL compilation failed in `app/build.gradle.kts` because `java.net.URI(...)` was resolved through Gradle's `java` DSL symbol rather than the Java package, producing `Unresolved reference 'net'`.

R02 corrects this by importing `java.net.URI` explicitly and routing all manifest-host extraction through one fail-closed `webHost(url)` helper. The four debug/staging/release/default manifest placeholder assignments now use that helper.

This issue is permanently guarded by:

- `scripts/ph02_r02_gradle_kotlin_dsl_regression.py`
- `scripts/VC-ANDROID-PH02-R05-SOURCE-CHECK.ps1`
- `scripts/VC-ANDROID-PH02-R05-ACCEPTANCE.cmd`

The corrective acceptance script runs the complete retained PH02-R01 authority and all accumulated PH01 regressions before the Android compile gates. It also includes a clean-delivery regression so ZIPs are created with project files directly at archive root rather than an extra duplicate project folder.

## 13. Required acceptance command

```bat
scripts\VC-ANDROID-PH02-R05-ACCEPTANCE.cmd
```

That script explicitly runs `compileDebugKotlin`, `compileStagingKotlin`, and `compileReleaseKotlin` before unit tests, lint and assemblies, then runs connected debug instrumentation when a device is available.


## 14. R03 compile-surface correction

The R02 Windows acceptance proved that the R02 Gradle URI fix was successful: wrapper generation completed and the run entered `:app:compileDebugKotlin`. The compiler then identified two PH02 source/module issues.

1. `VoiceCloudPreferences` mutation functions used expression bodies whose inferred return type is DataStore `Preferences`. Because DataStore is intentionally an `implementation` dependency of `:core:preferences`, callers in `:feature:auth` could not resolve that leaked implementation type. R03 converts all DataStore mutations to explicit block-body `Unit` APIs, preserving encapsulation rather than exporting DataStore.
2. `TextAction` used `Modifier.align(Alignment.CenterHorizontally)` from a standalone composable that does not have a `ColumnScope` receiver. R03 uses a scope-independent full-width `TextButton`, whose content is centered normally.

The same audit then checked the next module/classpath surface instead of waiting for another workstation iteration. R03 adds the direct dependencies consumed by app/auth, exports public coroutine/Retrofit/security ABI where required, moves Hilt ViewModel Compose to the current lifecycle artifact/package, and enables Room schema export through the supported Room Gradle plugin.

The new `ph02_r03_compile_surface_regression.py` permanently protects these rules. The Windows compile gate also uses Gradle `--continue` across Debug/Staging/Release so one unsuccessful run reveals all reachable variant compile failures.
