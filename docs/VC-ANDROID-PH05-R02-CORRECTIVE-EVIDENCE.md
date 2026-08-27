# VC-ANDROID-PH05-R02 Corrective Evidence

## Evidence from PH05-R01 workstation run
1. Windows acceptance passed the complete retained source/regression chain and live connectivity, then wrapper generation failed during project configuration because `:app` declared `resValue(...)` while AGP `resValues` generation was disabled.
2. Android Studio reported a configuration-time `ClassCastException` at `core/designsystem/build.gradle.kts` line 20 where typed `sourceSets.getByName("main").res` access attempted to cast AGP 9's hidden implementation to `AndroidLibrarySourceSet`.

## R02 correction
- `:app` explicitly sets `buildFeatures.resValues = true`.
- `:core:designsystem` explicitly enables Android resources and `buildFeatures.resValues = true`.
- centralized `branding/res` static resources are registered with `androidComponents { onVariants { variant.sources.res?.addStaticSourceDirectory(...) } }`.
- legacy typed source-set resource access is removed.

## Durable regression
- `scripts/ph05_r02_agp9_branding_gradle_regression.py`
- `scripts/VC-ANDROID-PH05-R02-SOURCE-CHECK.ps1`
- `scripts/VC-ANDROID-PH05-R02-ACCEPTANCE.cmd`

R02 does not alter PH05 live-room, authentication, networking, realtime, database, Firebase, navigation, or UI feature behavior.
