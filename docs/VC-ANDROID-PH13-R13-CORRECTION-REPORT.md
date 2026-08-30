# VC-ANDROID-PH13-R13 Correction Report

## Cause
R12 reached real physical-device instrumentation successfully. The installed app and test package were healthy, but `VoiceCloudFoundationInstrumentedTest.centralizedBrandPaletteUsesValidArgbColors()` still asserted the legacy pre-redesign VoiceCloud colors.

The runtime design authority is the approved premium palette in `branding/voicecloud-brand.properties`:
- `consumer.text=#18312D`
- `consumer.sapphire=#006C63`

The stale instrumentation test still asserted:
- `0xFF10262E` for `ConsumerColors.Text`
- `0xFF0B7C86` for `ConsumerColors.Sapphire`

R12 therefore failed the first stale assertion even though the runtime color resolved correctly to `#18312D`.

## R13 correction
Only `app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt` is changed in Kotlin source.

The two exact palette assertions now match the centralized approved branding authority:
- `0xFF18312D` for `ConsumerColors.Text`
- `0xFF006C63` for `ConsumerColors.Sapphire`

The full runtime palette conversion guard, package identity test, and microphone permission test remain unchanged.

## Production preservation
R13 preserves every build-proven R09 production/runtime input byte-for-byte. There is no change to application UI, APIs, repositories, ViewModels, navigation, RTC/realtime, security, wallet/payment logic, permissions, or business rules.

## Retest optimization
R13 checks for the retained R12/R11 temporary build workspace. If found and hash-verified against the build-proven runtime manifest, it copies only the corrected androidTest source and runs `:app:assembleDebugAndroidTest`. The production Debug APK is reused unchanged.

If no prior workspace exists, R13 falls back to an isolated Debug + androidTest prerequisite build.
