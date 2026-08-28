# VC-ANDROID-PH09-R06 Correction Report

## Baseline
- Frozen parent: `VC-ANDROID-PH08-R02` @ `1739f081ac386acca9a24b37863b72c4db952089`.
- R06 is a compiler corrective revision of PH09-R05.

## Workstation finding
R05 passed all PH09/inherited pre-Gradle gates, Java/SDK detection and Gradle-wrapper bootstrap, then failed at `:app:compileDebugKotlin` in `VoiceCloudRoot.kt` with two unresolved `Modifier` references.

## Root cause
PH08-R02 already imported `androidx.compose.ui.Modifier`. PH09 modified `VoiceCloudRoot.kt` for persisted Light/Dark/System appearance and accidentally removed that inherited import while leaving two `Modifier` usages in the function body. No other import was removed from the PH09-modified inherited Kotlin files.

## Correction
R06 restores exactly:

```kotlin
import androidx.compose.ui.Modifier
```

No PH09 feature/API/security/payment/RTC behavior is changed.

## Durable regression
R06 adds `scripts/ph09_r06_compile_surface_regression.py` and `contracts/VC-ANDROID-PH09-R06-INHERITED-IMPORT-AUTHORITY.json`. The gate verifies inherited imports that remain referenced, required Compose/navigation imports, new Settings Retrofit/Hilt imports, and modified callback boundaries. It passes 171/171 checks. Removing the restored import causes the regression to fail immediately.
