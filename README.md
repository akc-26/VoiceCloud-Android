# VoiceCloud Android — VC-ANDROID-PH05-R04

PH05-R04 is the runtime corrective delivery for the startup crash found after the PH05-R03 build installed successfully. The crash was caused by centralized 32-bit ARGB branding values being passed to Compose through the packed `Color(ULong)` representation. R04 converts those values through the ARGB `Color(Int)` constructor, adds full-palette device instrumentation coverage, and preserves the complete PH05-R03 implementation and frozen PH04-R04 parent lineage.

Run `scripts\VC-ANDROID-PH05-R04-ACCEPTANCE.cmd` on Windows.


Corrective delivery of the locked **Live Room Listener, Access, Realtime & Engagement** phase. R04 preserves the full PH05 implementation while correcting the centralized brand-color startup crash and retaining the earlier Gradle/SplashScreen corrective protections.

## Authoritative parent
- Repository: `https://github.com/akc-26/VoiceCloud-Android`
- Parent branch: `VoiceCloud-Android-VC-ANDROID-PH04-R04`
- Parent commit: `702cf4c1ae966cf826ddbc64c6cd86487f6d91e9`
- PH03 parent: `VoiceCloud-Android-VC-ANDROID-PH03-R02` / `3665f606e43c677d7d2e4652e2a6451d3fe459a4`
- PH02 ancestor: `VoiceCloud-Android-VC-ANDROID-PH02-R05` / `ae84dc7ea408c5d0948f2064e0955e088cbad957`
- PH01 frozen ancestor: `VoiceCloud-Android-VC-ANDROID-PH01-R09` / `ce56b1e36a4b2b563a65e4699df9193751f0978d`
- Backend: `VoiceCloud-Backend-VC-PLATFORM-CLOSURE-R06`
- Backend commit: `7bee8d463786d21a7200b5663a499408bb229115`
- Roadmap: `docs/VC-ANDROID-R06-MASTER-DEVELOPMENT-PLAN-R01.md`

## PH05-R04 runtime corrective scope
- corrects the startup `ArrayIndexOutOfBoundsException` in Compose color-space conversion by constructing centralized branding colors from 32-bit ARGB `Int` values rather than the packed `ULong` representation
- keeps every Consumer, Creator and Common color token centralized in `branding/voicecloud-brand.properties`
- adds Android instrumentation coverage that calls `toArgb()` across the full centralized palette, including the reported `consumer.text=#10262E` crash path
- makes the package-identity instrumentation assertion white-label safe by comparing against generated `BuildConfig.APPLICATION_ID` instead of a hard-coded VoiceCloud application id
- preserves PH05-R03 SplashScreen, PH05-R02 AGP9 branding resource wiring, PH05-R01 listener/runtime implementation and all PH01-PH04 protections

## PH05-R02 corrective scope
- enables AGP `resValues` generation in every module that declares `resValue(...)` branding resources
- explicitly keeps Android resource processing enabled in `:core:designsystem`
- removes the failing AGP 9 `AndroidLibrarySourceSet` typed source-set access
- registers the centralized root `branding/res` directory through the public `androidComponents` variant sources API
- adds R02 regressions for both exact workstation configuration failures while preserving PH05-R01 production behavior

## PH05 implemented scope
- room preview/detail before joining
- listener-only LiveKit room connection and remote-audio subscription
- authoritative RTC join/rejoin/leave lifecycle
- participant/presence rendering
- room access-state presentation for locked, invite, ticket/premium, subscriber, verified and community restrictions
- raise/lower hand
- speaker invitation accept/reject without pulling PH06 microphone publishing forward
- room chat, message reactions and room reactions
- in-room gift catalogue/send foundation
- saved-room state
- activity join/leave tracking
- pause/resume/end handling
- reconnect handling
- foreground/background listener lifecycle correctness
- stale join/rejoin/session race protection at ViewModel and shared RTC-engine layers

PH05 remains **listener-only**. PH06 microphone/host/co-host/publishing controls are deliberately not introduced.

## Additional approved PH05 UI/white-label work
### Real bottom-tab icons
Home, Explore, Search, Friends and Profile use proper vector navigation icons rather than placeholder circles/glyphs.

### Replaceable splash
The app uses AndroidX SplashScreen and a temporary vector splash asset. Replace the asset later without changing splash/navigation code:
- `branding/res/drawable/vc_brand_splash.xml`

### Single white-label branding authority
Brand identity, application ID, visible app name, colors, gradients, radii, motion and replaceable brand assets are centralized under:

```text
branding/
├── voicecloud-brand.properties
└── res/drawable/
    ├── vc_brand_splash.xml
    └── vc_brand_app_icon_foreground.xml
```

Screen code consumes the centralized design-system authority. Do not introduce customer-brand colors/names/assets directly inside feature screens. The internal Kotlin namespace/package stays stable and is not a white-label setting.

## Listener RTC safety
LiveKit connects with auto-subscription enabled and local audio/video capture disabled. PH05 does not request `RECORD_AUDIO` and does not enable the microphone. Join/rejoin operations are generation-owned so an old suspended connection cannot overwrite a newer room session after leave, retry, rapid room switching or navigation away.

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
scripts\VC-ANDROID-PH05-R04-ACCEPTANCE.cmd
```

The acceptance gate runs PH05 source/lifecycle/dependency checks plus every retained PH01-PH04 durable regression, validates live connectivity, generates the standard Gradle wrapper from the verified Gradle 9.5.0 cache when needed, then runs `compileDebugKotlin`, `compileStagingKotlin`, `compileReleaseKotlin`, unit tests, lint and Debug/Staging/Release assemblies. It also builds the instrumentation APK and uses the hardened healthy-device gate when a valid Android device is available.

Source/static verification is not final Android build acceptance. Final phase acceptance requires the Windows build gate plus the manual device QA checklist.

## PH05 evidence and QA
- `docs/VC-ANDROID-PH05-R01-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH05-R01-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH05-R01-MANUAL-QA.md`

## Preserved corrective protections
PH05 retains, among others:
- PH03 duplicate-key Home scrolling crash protection
- PH04 Compose scope regression
- PH04 healthy-device instrumentation selection
- PH04 professional safe-area/header/theme/button UI rules
- all PH02 auth/session/Firebase/token protections
- all PH01 Gradle/SDK/live-server/ABI protections

## Clean repository policy
Do not commit `.gradle/`, `.idea/`, `.kotlin/`, `local.properties`, generated `build/` directories, generated wrapper JAR/scripts, captures, keystores or secrets. Keep `gradle/wrapper/gradle-wrapper.properties` and all durable regression scripts because they are source authority.
