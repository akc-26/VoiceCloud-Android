## VC-ANDROID-PH02-R05

- Rebased corrective delivery on PH02-R03, rejecting the unnecessary R04 refactor.
- Preserved all R01-R03 functionality and prior workstation fixes.
- Changed only BootstrapRoute parameter order so the existing trailing lambda binds to onReady.
- Added a targeted regression that forbids the rejected R04 provider-function @param annotation change.

# VoiceCloud Android Change Log

## VC-ANDROID-PH02-R03
- Fixes the R02 `:feature:auth:compileDebugKotlin` failure caused by DataStore `Preferences` leaking through inferred public return types in `VoiceCloudPreferences`.
- Fixes the Compose `TextAction` compilation failure caused by `Modifier.align` being used outside a scope that provides the align parent-data modifier.
- Audits and hardens direct dependencies before the next workstation run: Activity Compose in `:feature:auth`, lifecycle-runtime-compose in `:app`, and public Retrofit/coroutine ABI exposure in auth/bootstrap/realtime.
- Exports `TokenVault` and `StateFlow` dependencies from `:core:realtime` because they are part of its public API.
- Migrates `hiltViewModel()` to the Hilt 1.3 `hilt-lifecycle-viewmodel-compose` artifact/package, removing the deprecation warning seen in R02.
- Configures Room schema export with the Room Gradle plugin, removing the Room KSP schema-location warning without disabling schema history.
- Adds a 30-check compile-surface regression and cumulative Windows source gate.
- Changes the compile acceptance to one `--continue` Debug/Staging/Release matrix so a failed workstation run exposes all reachable variant compile errors in the same iteration.

## VC-ANDROID-PH02-R02
- Corrects the R01 Windows Gradle Kotlin DSL configuration failure `Unresolved reference 'net'` in `app/build.gradle.kts`.
- Imports `java.net.URI` explicitly and centralizes Web-host parsing in a fail-closed `webHost(url)` helper.
- Adds durable R02 regression coverage so shadow-prone `java.net.URI(...)` usage cannot silently return in future phases.
- R02 acceptance preserves the full PH02-R01 source authority and all accumulated PH01 regressions before compile Debug/Staging/Release, tests, lint and assemblies.
- Adds a reusable clean-package script that archives project contents at ZIP root and rejects generated/machine-specific artifacts, preventing duplicate nested extraction folders.

## VC-ANDROID-PH02-R01
- Starts strictly from Git parent `VoiceCloud-Android-VC-ANDROID-PH01-R09` / `ce56b1e36a4b2b563a65e4699df9193751f0978d`.
- Adds the locked PH02 Dual Portal Authentication & Account Lifecycle module.
- Implements User email/username login, registration, phone OTP, Google, guest, guest upgrade, forgot/reset password, onboarding, restricted and session-expired states.
- Implements separate Creator Studio sign-in with Creator email semantics and hard `role == CREATOR` enforcement, plus Creator access application and maintenance states.
- Separates public and authenticated HTTP clients and adds synchronized single-flight refresh-token rotation using the Keystore token authority.
- Adds secure session restore, logout/logout-all, device/session metadata, realtime re-authentication after token rotation, and FCM device-token registration foundation.
- Persists PH02 onboarding completion per user on-device so backend profile-completion scoring cannot trap completed users in a repeat onboarding loop.
- Adds configured-host-only HTTPS reset-password App Link handling including `onNewIntent`.
- Corrects Google authentication to exchange the Google OAuth token through Firebase Authentication before submitting the Firebase ID token required by the VoiceCloud backend.
- Pins Firebase Android BoM 34.18.0 / Firebase Auth 24.2.0 and Play Services Auth 21.5.1.
- Preserves User Light-first design authority and applies Creator Studio dark design authority to Creator authentication.
- Adds PH02 source/Windows acceptance gates while retaining all durable PH01 regressions and compile Debug/Staging/Release gates.

## VC-ANDROID-PH01-R09
- Final PH01 clean baseline for Git freeze.
- Hardened Gradle wrapper bootstrap around the verified cached Gradle 9.5.0 distribution.
- Fixed Windows PowerShell `Resolve-Path` `PathInfo` to `System.Uri` conversion by resolving `.ProviderPath` first.
- Pins the official Gradle 9.5.0 binary distribution SHA-256 and retains a 60-second wrapper network timeout.
- Retains 3-attempt Raspberry Pi/Tailscale REST, Socket.IO and Web connectivity probes.
- Removes superseded revision/verification/manifests and historical R02-R08 delivery documents from the clean Git package while retaining durable regressions.

## VC-ANDROID-PH01-R08
- Fixed stale `ApiEndpointResolverTest.kt` references to removed emulator/single-URL resolver APIs.
- Switched Android development authority from local/ADB loopback to deployed Raspberry Pi server `https://voicecloud.tailfca77b.ts.net`.
- Centralized API/Socket/Web endpoint values in root `gradle.properties`.
- Added live REST, Socket.IO Engine.IO handshake and Web connectivity preflight.

## VC-ANDROID-PH01-R07
- Added explicit API, Socket and Web endpoint support and authenticated Socket.IO foundation.

## VC-ANDROID-PH01-R05
- Corrected public module ABI dependency exposure and Android Studio JBR auto-detection.

## VC-ANDROID-PH01-R04
- Added command-line Android SDK discovery and fixed logging regex compilation.

## VC-ANDROID-PH01-R03
- Migrated to AGP 9 built-in Kotlin and removed the legacy Kotlin Android plugin/DSL.

## VC-ANDROID-PH01-R02
- Corrected Windows bootstrap-state acceptance regression.

## VC-ANDROID-PH01-R01
- Created the native VoiceCloud Android PH01 foundation and locked R06/A0 API/design/screen authorities.
