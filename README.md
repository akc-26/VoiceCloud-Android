# VoiceCloud Android — VC-ANDROID-PH01-R09

PH01-R09 is the final clean PH01 foundation baseline for the native VoiceCloud Android Studio project. It targets the deployed Raspberry Pi VoiceCloud authority through Tailscale, retains the locked PH01 architecture and design/API contracts, and includes the Windows-safe Gradle wrapper bootstrap correction validated during physical-device setup.

## Locked authority
- Finalized platform: `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06`
- Finalized platform commit: `7bee8d463786d21a7200b5663a499408bb229115`
- Android backend readiness contract: `VC-ANDROID-A0-R01`
- User design authority: finalized R06 Consumer Website UI/UX, Light-first
- Creator design authority: finalized R06 Creator Studio UI/UX
- Android master plan: `docs/VC-ANDROID-R06-MASTER-DEVELOPMENT-PLAN-R01.md`

## PH01 scope
Foundation only: Android build variants, Compose/Hilt/navigation/network/database/security/preferences/logging/realtime foundations, centralized dual-portal design authority, `/config/mobile` bootstrap, maintenance/force-update/error states, and machine-readable API/Socket/screen/design anti-drift contracts. Authentication and product/business modules begin in PH02.

## Raspberry Pi live server authority

```text
Server origin: https://voicecloud.tailfca77b.ts.net
REST API:      https://voicecloud.tailfca77b.ts.net/api/v1/
Socket.IO:     https://voicecloud.tailfca77b.ts.net/realtime
Socket path:   /socket.io
Web:           https://voicecloud.tailfca77b.ts.net/
```

The Windows development machine and physical Android device must have access to the same Tailscale tailnet while this `.ts.net` authority remains tailnet-only.

No `10.0.2.2`, `127.0.0.1`, or `adb reverse` is required for the normal PH01-R09 path.

## Endpoint configuration
The project-level endpoint authority is the root `gradle.properties` file:

```properties
VOICECLOUD_DEBUG_API_BASE_URL=https://voicecloud.tailfca77b.ts.net
VOICECLOUD_DEBUG_SOCKET_BASE_URL=https://voicecloud.tailfca77b.ts.net
VOICECLOUD_DEBUG_WEB_BASE_URL=https://voicecloud.tailfca77b.ts.net
```

See `docs/VC-ANDROID-ENDPOINT-CONFIGURATION-R01.md` for the exact runtime flow and optional user-level Gradle override behavior.

## Build/runtime wiring
- `app/build.gradle.kts` publishes the configured API, Socket and Web authorities through BuildConfig.
- `ApiEndpointResolver.kt` normalizes REST to `/api/v1/`, Socket to the server origin, and Web to a canonical trailing-slash URL.
- `FoundationModule.kt` injects the normalized REST and Socket endpoints.
- `core:realtime` connects to namespace `/realtime` using Socket.IO path `/socket.io` and secure token authority.

## Full Windows acceptance
From the project root run:

```bat
scripts\VC-ANDROID-PH01-R09-ACCEPTANCE.cmd
```

The gate order is:
1. source and durable regression authority
2. live Raspberry Pi REST / Socket.IO / Web connectivity
3. `compileDebugKotlin`
4. `compileStagingKotlin`
5. `compileReleaseKotlin`
6. unit tests
7. lint Debug / Staging / Release
8. assemble Debug / Staging / Release
9. connected debug instrumentation when an authorized device is attached

If the standard Gradle wrapper JAR/scripts are absent from a clean package, the acceptance flow generates them from the verified cached Gradle 9.5.0 distribution. The Windows bootstrap explicitly resolves the cached ZIP to its filesystem `ProviderPath` before constructing the local file URI, avoiding PowerShell `PathInfo` -> `System.Uri` casting failure.

## Clean repository policy
Generated and machine-specific files are intentionally excluded by `.gitignore`, including `.gradle/`, `.idea/`, `local.properties`, module/root `build/` directories, captures, native build caches and local keystores/secrets.

Historical per-revision delivery manifests, verification TXT files and superseded PH01 R02-R08 implementation/QA documents are not part of this clean Git baseline. Durable regression scripts are retained because they protect previously corrected behavior from regression.
