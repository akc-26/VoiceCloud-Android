# VoiceCloud Android Change Log

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
