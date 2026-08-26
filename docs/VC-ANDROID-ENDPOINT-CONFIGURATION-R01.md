# VoiceCloud Android Endpoint Configuration — R01

## Permanent development authority from PH01-R08 onward

The Android app uses the deployed Raspberry Pi VoiceCloud host for REST, Socket.IO and public Web/deep-link authorities.

### Primary file to edit

`gradle.properties` in the Android project root.

Current values:

```properties
VOICECLOUD_DEBUG_API_BASE_URL=https://voicecloud.tailfca77b.ts.net
VOICECLOUD_DEBUG_SOCKET_BASE_URL=https://voicecloud.tailfca77b.ts.net
VOICECLOUD_DEBUG_WEB_BASE_URL=https://voicecloud.tailfca77b.ts.net
```

When the server host changes, change **only those three values**. Do not add `/realtime` to the Socket property and do not need to add `/api/v1/` to the API property.

### Optional Windows per-user override

`%USERPROFILE%\.gradle\gradle.properties`

The same three names may be defined there. Gradle user-level values override the project root values. If the user-level file contains old endpoints, update or remove them to avoid overriding the project authority.

## Runtime source flow

1. `app/build.gradle.kts`
   - reads `VOICECLOUD_DEBUG_API_BASE_URL`
   - reads `VOICECLOUD_DEBUG_SOCKET_BASE_URL`
   - reads `VOICECLOUD_DEBUG_WEB_BASE_URL`
   - publishes `BuildConfig.API_BASE_URL`, `BuildConfig.SOCKET_BASE_URL`, `BuildConfig.WEB_BASE_URL`

2. `app/src/main/java/app/voicecloud/android/network/ApiEndpointResolver.kt`
   - API root `https://host` -> `https://host/api/v1/`
   - API `https://host/api/v1/` -> preserved
   - Socket root -> `https://host`
   - Web root -> `https://host/`

3. `app/src/main/java/app/voicecloud/android/di/FoundationModule.kt`
   - Retrofit consumes `endpoints.apiBaseUrl`
   - Socket.IO consumes `endpoints.socketBaseUrl`

4. `core/realtime/src/main/java/app/voicecloud/core/realtime/RealtimeClient.kt`
   - namespace: `/realtime`
   - path: `/socket.io`
   - secure access token from `TokenVault`
   - backend readiness event: `connection_established`
   - backend failure event: `auth_error`

## Current resolved URLs

```text
REST Bootstrap:
https://voicecloud.tailfca77b.ts.net/api/v1/config/mobile?platform=android

Socket.IO namespace:
https://voicecloud.tailfca77b.ts.net/realtime
Socket.IO engine path:
/socket.io

Web:
https://voicecloud.tailfca77b.ts.net/
```
