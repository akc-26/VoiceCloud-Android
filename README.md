# VoiceCloud Android — VC-ANDROID-PH03-R02

Corrective PH03 package for the Home/discovery scroll crash caused by duplicate Compose lazy-list keys. Built from PH03-R01 while preserving PH02-R05 auth/bootstrap authority.

## Acceptance

Run on the Windows Android workstation:

```powershell
scripts\VC-ANDROID-PH03-R02-ACCEPTANCE.cmd
```

PH03-R02 adds section-qualified collision-safe lazy-list keys plus repository/render-boundary deduplication. It retains all PH01/PH02/PH03-R01 regressions before the Android build gates.

# VoiceCloud Android — VC-ANDROID-PH03-R01

Native Android implementation of the locked **Consumer Home, Discovery, Search, Profile & Social** phase.

## Authoritative parent
- Repository: `https://github.com/akc-26/VoiceCloud-Android`
- Parent branch: `VoiceCloud-Android-VC-ANDROID-PH02-R05`
- Parent commit: `ae84dc7ea408c5d0948f2064e0955e088cbad957`
- PH01 frozen ancestor: `VoiceCloud-Android-VC-ANDROID-PH01-R09`
- PH01 commit: `ce56b1e36a4b2b563a65e4699df9193751f0978d`
- Backend: `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06`
- Backend commit: `7bee8d463786d21a7200b5663a499408bb229115`
- Roadmap: `docs/VC-ANDROID-R06-MASTER-DEVELOPMENT-PLAN-R01.md`

## PH03 implemented scope
- Consumer Home
- Explore
- live-room discovery/listing
- People discovery
- Creator discovery
- user + room Search
- public profile
- My Profile shell
- follow / unfollow
- followers / following
- friends list
- incoming/outgoing friend requests
- friend suggestions
- public identity filtering
- current-user self exclusion
- username-based human-readable profile navigation

PH03 does **not** implement PH04 communities/events/messages/notifications or PH05 room detail/join/RTC.

## Preservation rule
PH03 adds an isolated `:feature:discovery` module and consumer navigation. PH02 authentication, Google/Firebase exchange, preferences, token vault, refresh rotation, realtime re-authentication, bootstrap, database and Creator Portal implementation are retained rather than refactored.

Every accumulated PH01/PH02 workstation regression is run before PH03 Android build gates.

## Consumer identity policy
Consumer-facing discovery renders only normal `USER`/`CREATOR` identities. Guest, Admin and Super Admin identities are excluded, and the logged-in user is excluded by both ID and normalized username.

Some finalized R06 relationship DTOs omit `role`. Android therefore resolves an authoritative profile before rendering a role-less friend/request/suggestion identity and fails closed when that authority cannot be established.

## Raspberry Pi / Tailscale authority

```text
Server origin: https://voicecloud.tailfca77b.ts.net
REST API:      https://voicecloud.tailfca77b.ts.net/api/v1/
Socket.IO:     https://voicecloud.tailfca77b.ts.net/realtime
Socket path:   /socket.io
Web:           https://voicecloud.tailfca77b.ts.net/
```

Windows and the physical Android device must be able to access the same tailnet while the server remains tailnet-only.

## Full Windows acceptance
From project root:

```bat
scripts\VC-ANDROID-PH03-R02-ACCEPTANCE.cmd
```

The acceptance gate runs:
1. PH03 source and module-dependency authority
2. accumulated PH01/PH02 durable regressions
3. Java and Android SDK resolution
4. live REST / Socket.IO / Web connectivity
5. clean
6. compile Debug / Staging / Release
7. unit tests
8. lint Debug / Staging / Release
9. assemble Debug / Staging / Release
10. `connectedDebugAndroidTest` when an authorized device is attached

The compile/test/lint/assembly tasks run in one Gradle `--continue` sweep so one workstation execution exposes every reachable failure instead of stopping after the first task category.

Source/static verification is **not** Android build acceptance.

## QA and implementation evidence
- `docs/VC-ANDROID-PH03-R01-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH03-R01-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH03-R01-MANUAL-QA.md`

## Clean repository policy
Do not commit `.gradle/`, `.idea/`, `.kotlin/`, `local.properties`, generated `build/` directories, generated wrapper bootstrap files, captures, local keystores, or secrets. Durable regression scripts are source and must be retained.
