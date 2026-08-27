# VoiceCloud Android — VC-ANDROID-PH04-R04

Native Android implementation of the locked **Communities, Events, Messaging & Notifications** phase.

## Authoritative parent
- Repository: `https://github.com/akc-26/VoiceCloud-Android`
- Parent branch: `VoiceCloud-Android-VC-ANDROID-PH03-R02`
- Parent commit: `3665f606e43c677d7d2e4652e2a6451d3fe459a4`
- PH02 parent: `VoiceCloud-Android-VC-ANDROID-PH02-R05` / `ae84dc7ea408c5d0948f2064e0955e088cbad957`
- PH01 frozen ancestor: `VoiceCloud-Android-VC-ANDROID-PH01-R09` / `ce56b1e36a4b2b563a65e4699df9193751f0978d`
- Backend: `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06`
- Backend commit: `7bee8d463786d21a7200b5663a499408bb229115`
- Roadmap: `docs/VC-ANDROID-R06-MASTER-DEVELOPMENT-PLAN-R01.md`

## PH04 implemented scope
- Communities list/detail/create/edit/delete
- public/private membership and private invite-code joining
- invite-code rotation
- Owner/Admin/Moderator/Member role workflows and member management
- community scheduled events and event detail/reminders
- direct-message inbox and conversation
- message send and conversation read state
- notification list/unread/read/read-all/delete lifecycle
- authenticated realtime notification refresh
- FCM service/token registration foundation and notification deep routing
- Android 13+ notification permission flow
- Public Profile -> Message entry
- Home -> Communities / Messages / Notifications entry points

PH04 does **not** implement PH05 live-room detail/join/listener RTC functionality.

## Preservation rule
PH04 adds an isolated `:feature:engagement` module plus only the app/navigation/FCM wiring needed for the phase. PH01-PH03 authentication, bootstrap, discovery, token/security, database, realtime foundation and the PH03-R02 duplicate-key runtime fix remain protected by accumulated regressions.

## Firebase/FCM authority
The project intentionally does not depend on a committed `google-services.json`. PH04 initializes the default Firebase app from the same centralized public BuildConfig Firebase client values established in PH02 and fails closed if those public values are incomplete. Never place Firebase Admin/service-account/private credentials in the Android project.

## Raspberry Pi / Tailscale authority
```text
Server origin: https://voicecloud.tailfca77b.ts.net
REST API:      https://voicecloud.tailfca77b.ts.net/api/v1/
Socket.IO:     https://voicecloud.tailfca77b.ts.net/realtime
Socket path:   /socket.io
Web:           https://voicecloud.tailfca77b.ts.net/
```

## Full Windows acceptance
From project root:

```bat
scripts\VC-ANDROID-PH04-R04-ACCEPTANCE.cmd
```

The acceptance gate runs accumulated PH01-PH03 source/regression checks first, then resolves Java/Android SDK, validates live server connectivity, generates the standard wrapper from the verified Gradle 9.5.0 cache when required, and runs clean + Debug/Staging/Release Kotlin compilation + unit tests + lint + assemblies. The retained R03 device gate builds the debug instrumentation APK during the broad sweep, rejects stale/unhealthy ADB aliases, selects one healthy API 26+ device, installs both APKs explicitly to that serial, runs instrumentation only on that device, and requires at least one passing test.

Source/static verification is **not** Android build acceptance. The user-provided PH04-R02 workstation log already proves Debug/Staging/Release compilation, unit tests, lint and assemblies complete successfully; R03 corrects only the connected-device acceptance runner that was defeated by stale wireless-ADB aliases. Manual device QA still remains required before phase approval.

## Evidence and QA
- `docs/VC-ANDROID-PH04-R01-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH04-R01-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH04-R01-MANUAL-QA.md`

## Clean repository policy
Do not commit `.gradle/`, `.idea/`, `.kotlin/`, `local.properties`, generated `build/` directories, generated wrapper binary/scripts, captures, local keystores or secrets. Durable regression scripts are source and must be retained.


## PH04-R02 corrective revision

Corrects the Windows Kotlin compile failure in `EngagementScreens.kt` by making `QuickAction` a `RowScope` extension so `Modifier.weight()` is used in a valid Compose layout scope. No unrelated production behavior is changed.


## PH04-R03 corrective revision

R03 is an acceptance-infrastructure correction only. It does **not** modify VoiceCloud production Kotlin/source behavior. The PH04-R02 workstation build completed the entire host build sweep successfully, then Gradle/UTP failed because stale mDNS ADB aliases reported Unknown/API 1 while the real OnePlus device was also connected. R03 replaces the all-discovered-device `connectedDebugAndroidTest` invocation with a healthy-device preflight and serial-isolated ADB instrumentation run.

The gate explicitly requires API 26+, prefers a direct/USB transport when available, installs the debug app and androidTest APK only to the selected device, discovers the installed instrumentation runner, runs it with `adb -s`, and fails unless at least one test executes successfully.


## PH04-R04 professional UI corrective revision

R04 is a shared UI-system correction based on physical-device video review. It adds one app-level safe drawing boundary for edge-to-edge Android, a centralized icon-only secondary-page top bar, Website-derived consumer gradients/surface roles, centralized Material radii, single-line equal-width shortcut actions, and production-copy cleanup. The correction is intentionally centralized so Communities, public profiles, community inner pages, events, messages, conversations, notifications and future secondary pages use the same geometry instead of receiving page-by-page patches.

R04 changes no API/repository/auth/security/database/realtime behavior. See `docs/VC-ANDROID-PH04-R04-UI-QUALITY-CORRECTIVE-REPORT.md`, `docs/VC-ANDROID-PH04-R04-AUTOMATED-EVIDENCE.md`, and `docs/VC-ANDROID-PH04-R04-MANUAL-QA.md`.
