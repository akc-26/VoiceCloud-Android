# VoiceCloud Android Changelog

## VC-ANDROID-PH05-R04
- Corrects the post-splash startup crash `ArrayIndexOutOfBoundsException: length=20; index=46` from Compose `ColorSpace` conversion.
- Changes the centralized brand parser from `Color(argb.toULong())` to the 32-bit ARGB `Color(argb.toInt())` constructor.
- Adds a full centralized Consumer/Creator/Common palette instrumentation test using `toArgb()` so invalid packed colors fail the device gate before manual QA.
- Makes the instrumentation package identity assertion follow generated `BuildConfig.APPLICATION_ID` for white-label application-id changes.
- Preserves PH05-R03 compile correction, PH05-R02 AGP9 resource wiring, PH05-R01 runtime scope and all PH01-PH04 regressions.

## VC-ANDROID-PH05-R03
- Corrected AndroidX Core SplashScreen Kotlin extension import so `:app` Debug, Staging and Release Kotlin compilation can resolve `installSplashScreen()`.
- Preserved PH05-R02 AGP9 white-label resource wiring and all PH05 listener/runtime behavior.
- Added durable SplashScreen compile regression and R03 Windows acceptance entry point.

# VC-ANDROID-PH05-R02

- Preserves PH05-R01 product functionality and the frozen PH04-R04 parent lineage.
- Corrects Windows acceptance failure `defaultConfig contains custom resource values, but the feature is disabled` by enabling AGP `resValues` in `:app` and `:core:designsystem`.
- Corrects Android Studio/Gradle configuration `AndroidLibrarySourceSet` class-cast failure by replacing legacy typed `sourceSets` resource wiring with the AGP 9 public `androidComponents` variant sources API.
- Keeps splash/app-icon assets centralized under root `branding/res` and white-label properties under `branding/voicecloud-brand.properties`.
- Adds PH05-R02 Windows corrective authority and a durable AGP9 branding Gradle regression.
- Production-source/build delta from PH05-R01 is restricted to `app/build.gradle.kts` and `core/designsystem/build.gradle.kts`; no feature/API/auth/RTC/runtime implementation is changed.

# VC-ANDROID-PH05-R01

- Starts strictly from frozen PH04-R04 commit `702cf4c1ae966cf826ddbc64c6cd86487f6d91e9`.
- Adds isolated `:feature:live` for the locked listener-only live-room phase.
- Implements room preview, RTC join/rejoin/leave, remote-audio LiveKit subscription, participants, room access states, raise hand, speaker invitation response, chat/reactions, gifts, saved rooms, room activity and pause/resume/end handling.
- Keeps local LiveKit audio/video capture disabled and does not request microphone permission; PH06 publishing/host controls remain deferred.
- Hardens rapid leave/retry/rejoin/room-switch races with session generations, coroutine cancellation and shared RTC-engine connection ownership.
- Replaces bottom-tab placeholder glyphs with proper vector icons.
- Integrates AndroidX SplashScreen with a temporary replaceable splash drawable.
- Centralizes white-label identity, application ID, colors, gradients, radii, motion and brand assets under the root `branding/` authority.
- Adds PH05 source, lifecycle/concurrency and module-dependency regressions while retaining every accumulated PH01-PH04 protection.

# VC-ANDROID-PH04-R04

- Corrects secondary-route status/navigation-bar crowding with one application-level safe drawing inset boundary.
- Adds centralized `VoiceCloudPageTopBar` with icon-only back navigation, centered single-line title, stable geometry, optional subtitle and right action.
- Converts discovery/profile and PH04 engagement secondary pages to the shared page chrome.
- Aligns Android consumer gradients and Material surface-container roles with the current Website premium presentation authority.
- Adds centralized 12/18/24/30dp Material shape radii.
- Hardens Home and PH04 equal-width actions against text wrapping and corrects paired community-action label sizing.
- Removes implementation/roadmap language from consumer-facing UI while changing historical regressions to assert the underlying product boundary instead of debug copy.
- Adds a 91-check durable UI-quality regression while retaining PH03 scroll-crash, PH04-R02 Compose-scope and PH04-R03 healthy-device protections.
- Changes only seven UI/design-system/root/build production files; API/repository/auth/security/database/realtime behavior is preserved.

# VC-ANDROID-PH04-R03

- Preserves PH04-R02 production source unchanged.
- Corrects the workstation Gate 6 failure caused by stale wireless/mDNS ADB aliases being enumerated by Gradle/UTP as Unknown/API 1 devices.
- Builds `:app:assembleDebugAndroidTest` inside the broad host build sweep.
- Adds healthy-device preflight with an 8-second SDK probe timeout and minSdk 26 enforcement.
- Prefers a direct/non-mDNS device when multiple healthy transports are visible.
- Installs the app and test APKs directly to one selected serial and runs instrumentation with `adb -s`.
- Requires proof that at least one instrumentation test executed and passed.
- Adds a durable 15-check device-gate regression and R03 Windows source authority.

# VC-ANDROID-PH04-R02

- Corrected PH04-R01 `EngagementScreens.kt:73` Compose scope compile failure by declaring `QuickAction` as a `RowScope` extension.
- Added a durable PH04-R02 Compose-scope regression and R02 Windows acceptance entry point.
- Preserved all PH01-PH04-R01 behavior and regressions.

# VC-ANDROID-PH04-R01

- Starts from PH03-R02 exact parent `3665f606e43c677d7d2e4652e2a6451d3fe459a4`.
- Adds isolated `:feature:engagement` for Communities, Events, Messaging and Notifications.
- Implements public/private community membership, invite-code join/rotation, community roles/member management and community CRUD.
- Implements scheduled-event listing/detail/reminders without pulling premium purchase or PH05 RTC forward.
- Implements direct-message inbox/conversation/send/read-state with HTTP conversation refresh.
- Implements notification lifecycle plus authenticated realtime notification refresh.
- Adds Firebase Messaging service/token synchronization using the existing PH02 centralized public Firebase configuration and fail-closed initialization.
- Adds Android 13+ notification permission and allowlisted notification deep-route resolution.
- Preserves PH03-R02 duplicate-key crash protection and all accumulated PH01-PH03 regressions.

# VC-ANDROID-PH03-R02

- Fixed fatal Home scroll crash caused by the same user UUID being used as a Compose `LazyColumn` key in both People and Creators sections.
- Replaced raw UUID-only PH03 lazy-list keys with section-qualified collision-safe keys.
- Added render-boundary deduplication for Home, Explore, Rooms, Search, Social and Friends lists.
- Added repository-level deduplication for room/search/friend/request/suggestion payloads.
- Added unit coverage for duplicate consumer identities and a durable `ph03_r02_lazy_list_key_regression.py` gate reproducing the reported UUID collision scenario.
- Preserved PH02 authentication/bootstrap implementation unchanged.

## VC-ANDROID-PH03-R01

- Starts from PH02-R05 exact parent `ae84dc7ea408c5d0948f2064e0955e088cbad957`.
- Adds isolated `:feature:discovery` module for Home, Explore, live-room discovery, People/Creators, Search, profiles, follows and friends.
- Adds centralized consumer identity filtering: USER/CREATOR only, guest/privileged exclusion and defensive self-exclusion.
- Resolves role-less relationship DTOs through authoritative profile lookup before rendering.
- Adds human-readable `profile/{username}` navigation and keeps raw IDs internal to backend operations.
- Preserves PH02 auth/bootstrap implementation and sends the PH02 UserReady handoff to PH03 Home.
- Adds PH03 source, dependency, Windows acceptance, implementation report and manual QA gates while retaining all prior regressions.

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
