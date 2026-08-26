# VoiceCloud Android — VC-ANDROID-PH02-R05

PH02-R05 is the preservation-first targeted corrective implementation revision of the locked **Dual Portal Authentication & Account Lifecycle** phase. It starts only from the accepted Git baseline `VoiceCloud-Android-VC-ANDROID-PH01-R09` at exact commit `ce56b1e36a4b2b563a65e4699df9193751f0978d` and preserves the PH01 Raspberry Pi/Tailscale, R06 design, API, security, and regression authorities.

## Locked parent baseline
- Repository: `https://github.com/akc-26/VoiceCloud-Android`
- Parent branch: `VoiceCloud-Android-VC-ANDROID-PH01-R09`
- Parent commit: `ce56b1e36a4b2b563a65e4699df9193751f0978d`
- Finalized backend: `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06`
- Backend commit: `7bee8d463786d21a7200b5663a499408bb229115`
- Android backend readiness contract: `VC-ANDROID-A0-R01`
- User UI authority: finalized R06 Consumer Website, Light-first
- Creator UI authority: finalized R06 Creator Studio
- Master plan: `docs/VC-ANDROID-R06-MASTER-DEVELOPMENT-PLAN-R01.md`

## PH02 locked scope implemented

### User Portal
- portal selector
- email/username + password login
- registration
- phone OTP login
- Google login through Google → Firebase Auth → Firebase ID token → VoiceCloud backend
- guest login
- guest upgrade by email, phone OTP, or Google
- forgot password
- HTTPS App Link reset-password handling
- first-time onboarding
- restricted-account handling
- session-expired handling

### Creator Portal
- separate Creator sign-in presentation
- Creator email/password flow aligned with Creator Studio
- hard server-role validation: only `role == CREATOR` can enter
- public Creator access application entry
- backend maintenance handling

### Shared session/security foundation
- separate public and authenticated Retrofit/OkHttp clients
- Keystore AES-GCM access/refresh token authority retained
- synchronized single-flight refresh-token rotation
- realtime re-authentication after token rotation
- secure `/auth/me` session restore
- logout and logout-all
- per-device metadata foundation
- FCM token registration API foundation
- configured-host-only HTTPS password-reset deep link

PH02 intentionally does **not** implement Consumer Home, discovery, communities, messages, rooms, RTC, wallet/economy, or later Creator product modules. Those remain in their locked later phases.

## Raspberry Pi / Tailscale development authority

```text
Server origin: https://voicecloud.tailfca77b.ts.net
REST API:      https://voicecloud.tailfca77b.ts.net/api/v1/
Socket.IO:     https://voicecloud.tailfca77b.ts.net/realtime
Socket path:   /socket.io
Web:           https://voicecloud.tailfca77b.ts.net/
```

The Windows workstation and physical Android device must have access to the same Tailscale tailnet while this `.ts.net` server remains tailnet-only.

## Google/Firebase client configuration

The backend verifies **Firebase Authentication ID tokens**, not a raw Google OAuth token. PH02 therefore performs the same semantic flow as the finalized Website: obtain Google identity → authenticate with Firebase → obtain a fresh Firebase ID token → call `/auth/google/login`.

Public Android client configuration is centralized in root `gradle.properties` (or preferably a workstation-level Gradle override):

```properties
VOICECLOUD_GOOGLE_WEB_CLIENT_ID=
VOICECLOUD_FIREBASE_API_KEY=
VOICECLOUD_FIREBASE_APPLICATION_ID=
VOICECLOUD_FIREBASE_PROJECT_ID=
```

These are public client identifiers. **Never place Firebase Admin private keys, service-account private keys, or backend secrets in the Android project.** If the public client configuration is incomplete, Google sign-in fails closed while email, phone and guest authentication remain available according to `/config/mobile`.

The Android application signing SHA-1 must also be registered for the Android app in the Firebase project before Google authentication is expected to pass on a physical device.

## Password-reset App Link

The app accepts only:

```text
https://<configured VoiceCloud Web host>/auth/reset-password?token=<one-time-token>
```

The incoming host is checked against `BuildConfig.WEB_BASE_URL`, and undersized/missing tokens are rejected. `onNewIntent` is handled for links received while the app is already running.

Android `autoVerify` additionally depends on the matching deployed Digital Asset Links file (`/.well-known/assetlinks.json`) for the installed signing certificate. The Android source is prepared for verification; deployed-domain verification remains an environment acceptance item.


## PH02 cumulative corrective protection

The first R01 Windows acceptance exposed a Gradle Kotlin DSL symbol-resolution defect in `app/build.gradle.kts`: package-qualified `java.net.URI(...)` was shadowed by Gradle's `java` DSL symbol and failed with `Unresolved reference 'net'` before Android compilation. R02 imports `java.net.URI`, centralizes host extraction in `webHost(url)`, and permanently protects the correction with an accumulated regression check.

R02 also introduced `scripts/CREATE-CLEAN-PACKAGE.ps1`, which creates ZIPs with the project files directly at archive root and verifies that generated/machine-specific artifacts are absent.


### R03 compile-surface hardening

The R02 Windows run successfully passed Gradle wrapper generation and reached `:app:compileDebugKotlin`, where it exposed two PH02 compile defects: DataStore `Preferences` leaked through inferred expression-body return types in `VoiceCloudPreferences`, and `TextAction` used `Modifier.align` outside the required `ColumnScope`. R03 fixes both and then audits the next compiler surface rather than stopping at those lines.

R03 additionally:
- gives every DataStore mutation an explicit block/`Unit` API so DataStore implementation types cannot leak across module boundaries;
- makes `TextAction` scope-independent;
- declares Activity Compose and lifecycle-runtime-compose at the modules that directly use them;
- exports Retrofit/coroutine types where public PH02 APIs expose them;
- exports `TokenVault`/`StateFlow` ABI from `:core:realtime`;
- migrates `hiltViewModel()` to the non-deprecated Hilt 1.3 lifecycle artifact/package;
- configures durable Room schema export through the Room Gradle plugin;
- changes acceptance to a Debug/Staging/Release `--continue` compile matrix so one failed run reports every reachable variant compile problem rather than stopping after the first one.

These rules are protected by `scripts/ph02_r03_compile_surface_regression.py` and the R03 Windows source gate and must be retained by later phases.

## Full Windows acceptance

From the extracted project root run:

```bat
scripts\VC-ANDROID-PH02-R05-ACCEPTANCE.cmd
```

The gate order is intentionally strict:
1. PH02 source-contract verification (85 invariants in the cross-platform checker plus the Windows source gate)
2. PH01 durable source/regression checks
3. Raspberry Pi REST / Socket.IO / Web connectivity
4. one `--continue` compile matrix running `:app:compileDebugKotlin`, `:app:compileStagingKotlin`, and `:app:compileReleaseKotlin` so a failed workstation run exposes every reachable variant error
5. unit tests
6. lint Debug / Staging / Release
7. assemble Debug / Staging / Release
8. connected debug instrumentation when an authorized device is attached

A source/static verifier is not phase acceptance. PH02 is ready for manual approval only after the compile, test, lint, assembly, device, and manual-QA gates have passed.

## Manual QA
See `docs/VC-ANDROID-PH02-R05-MANUAL-QA.md`.

## Clean repository policy
Generated and machine-specific files remain excluded: `.gradle/`, `.idea/`, `local.properties`, all `build/` directories, captures, native caches, local keystores, Gradle bootstrap artifacts and secrets. Durable regression scripts are retained because they protect accepted PH01 corrections.
