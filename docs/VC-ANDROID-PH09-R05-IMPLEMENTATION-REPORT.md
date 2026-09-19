# VC-ANDROID-PH09-R05 Implementation Report

## Baseline

VC-ANDROID-PH09-R05 preserves the PH09 product implementation directly on the frozen Android baseline:

- Repository: `https://github.com/akc-26/VoiceCloud-Android`
- Parent branch: `VoiceCloud-Android-VC-ANDROID-PH08-R02`
- Exact parent commit: `1739f081ac386acca9a24b37863b72c4db952089`
- Parent of PH08-R02: `fe2e7857802861e2ace2733a313c00c5d27d331d` (`VC-ANDROID-PH07-R03`)

The PH08 generic authority manifest is based on the accepted PH08-R02 delivery. Root `gradle.properties` is excluded from that generic manifest and verified separately by the dedicated R05 classified Gradle-property authority.


## R05 acceptance/toolchain correction

R05 corrects the R04 workstation false failure around `org.gradle.tooling.parallel`. The project is pinned to Gradle 9.5 and now explicitly declares `org.gradle.tooling.parallel=true` alongside `org.gradle.parallel=true`.

Root `gradle.properties` remains outside generic PH08 preservation and is verified by one classified authority: 22 frozen PH08 product/build keys remain exact, the R05 tooling key remains exact, additional `org.gradle.*` / `systemProp.*` workstation-runtime properties are non-source authority, and unexpected VoiceCloud/Android/Kotlin or arbitrary project properties remain fail-closed. No Kotlin/product behavior changed from R04.

## Locked phase scope

PH09 implements **Preferences, Security, Safety, CMS & Support**:

- Settings overview
- notification preferences
- privacy
- voice/audio preferences
- Appearance: Light / Dark / System
- English-only current language state
- security overview
- sessions/devices
- session/device detail and revoke
- login activity
- Help / FAQ / legal
- report User / Room
- Safety Center
- dynamic CMS pages
- Contact Support
- About
- maintenance / restricted / session-expired global handling

At successful PH09 workstation/device acceptance, Consumer Portal feature parity is complete according to the locked R06 master plan.

## Implementation architecture

### New isolated feature module

PH09 adds `:feature:settings` with:

- `SettingsApi.kt`
- `SettingsRepository.kt`
- `SettingsNetworkModule.kt`
- `SettingsModels.kt`
- `SettingsScreens.kt`
- `SettingsViewModel.kt`
- `SettingsModelsTest.kt`

The module is registered in `settings.gradle.kts` and consumed from `:app`.

### Settings and preferences

Backend-authoritative settings use:

- `GET /users/settings`
- `PATCH /users/settings`
- `GET /users/privacy`
- `PATCH /users/privacy`

Implemented settings include:

- Email, push, in-app and sound notification preferences
- Messaging, follow, invitation and visitor permissions
- Visitor tracking and anonymous visiting
- Audio preset, noise suppression, echo cancellation and automatic gain control
- Light / Dark / System appearance
- English-only current language state
- Backend timezone display when available

All mutations refresh canonical server state after success.

### Appearance authority

The previous fixed root appearance is replaced with a persisted application theme flow using `VoiceCloudPreferences` and `ThemePreference`.

- `LIGHT` always renders light theme.
- `DARK` always renders dark theme.
- `SYSTEM` follows `isSystemInDarkTheme()`.
- Server settings reconcile the local appearance preference when loaded.
- Theme updates are persisted server-side and locally only through the supported settings workflow.

No PH09 screen hard-codes branding hex/RGB values; the existing centralized Royal Sapphire theme remains the styling authority.

## Security & devices

Implemented backend authority:

- `GET /auth/sessions`
- `GET /auth/sessions/{sessionId}`
- `DELETE /auth/sessions/{sessionId}`
- `GET /auth/devices`
- `GET /auth/devices/{deviceId}`
- `DELETE /auth/devices/{deviceId}`
- `GET /auth/history?limit=100`
- `POST /auth/logout-all`

### Privacy-safe Android models

Android models intentionally do not contain:

- session tokens
- refresh-token hashes
- backend owner user IDs
- device push tokens

Only account-facing session/device fields are projected into UI models.

### Server-authoritative revoke

Session/device revoke calls the canonical backend DELETE operation and refreshes the corresponding server list after success.

`Sign out all devices` has been hardened so local credentials are cleared **only after the backend logout-all request succeeds**. A failed backend revoke cannot falsely appear to have terminated all remote sessions.

Normal single-session logout retains the existing recoverable behavior needed when the user is offline.

### Login activity

Authentication history is bounded to a maximum of 100 entries and renders account-facing metadata such as action, login method, platform, IP/location when supplied by the backend, user agent and timestamp.

## Safety and reporting

Canonical report authority:

- `POST /reports`
- `GET /reports/my-reports`
- `GET /reports/targets/search`
- `GET /reports/targets/{targetType}/{targetId}`

Android consumer reporting exposes only normal `USER` and `ROOM` targets. Internal IDs are kept inside the selected target model and are not presented as a UUID entry workflow.

Supported reasons match backend authority:

- SPAM
- ABUSE
- HARASSMENT
- FAKE_PROFILE
- SEXUAL_CONTENT
- VIOLENCE
- OTHER

Contextual Report actions are integrated into:

- public profile
- live room

The Safety Center links to reporting, blocked users, privacy controls, communities and published safety/community CMS content.

No evidence-file upload, device-ban mutation, Admin moderation action or fabricated safety capability is exposed.

## Help, CMS, support and About

CMS authority:

- `GET /cms/pages`
- `GET /cms/pages/{slug}`

PH09 provides a CMS-backed Help Center and individual dynamic content pages for Admin-published help, FAQ, terms, privacy, legal and safety/community material.

Contact Support uses the persisted backend endpoint:

- `POST /contact`

The form sends name, email, optional phone and description. The app does not expose provider credentials, invent a live-chat system or promise a fixed support-response time.

About provides the VoiceCloud product identity and application version without fabricating backend statistics.

## Global account/runtime handling

PH09 integrates authenticated failure handling into navigation:

- HTTP 401 -> session-expired state and sign-in recovery
- maintenance response / HTTP 503 -> maintenance state using backend-safe messaging
- restricted/suspended/disabled account state -> restricted-account route after authentication

This is global handling, not a settings-only visual message.

## Existing feature integration

Small controlled integration changes were made only where required:

- My Profile -> Settings, Security & devices, Safety Center
- Public Profile -> contextual Report
- Live Room -> contextual Report
- Root -> appearance preference collection
- Auth repository/viewmodel -> backend-success-first logout-all and restricted-account handling
- Navigation -> PH09 routes and global account-state handling

PH08 replay/profile functionality and PH07 economy/payment authority remain preserved.

## Deliberately not implemented

PH09 does **not** fabricate unsupported or privileged capabilities:

- self-service password mutation when no approved Android client contract exists
- 2FA setup/recovery
- trusted-device semantics
- Admin moderation/report review
- device-ban operations
- report evidence upload
- fake live support/chat
- unsupported product languages

## Acceptance authority

Run on the Windows Android workstation:

```powershell
scripts\VC-ANDROID-PH09-R05-ACCEPTANCE.cmd
```

The locked order is:

1. PH09 source/product/security/preservation/compile-risk regressions
2. preserved PH08 payment and compiler-risk regressions
3. preserved PH07 ADB regression
4. preserved PH06 hosting compile regression
5. `:app:compileDebugKotlin`
6. `:app:compileStagingKotlin`
7. `:app:compileReleaseKotlin`
8. unit tests
9. Debug/Staging/Release lint
10. Debug/Staging/Release assemblies + Debug AndroidTest APK
11. physical-device instrumentation when a healthy device is available

This package must not be called build/device accepted until the workstation gate succeeds.
