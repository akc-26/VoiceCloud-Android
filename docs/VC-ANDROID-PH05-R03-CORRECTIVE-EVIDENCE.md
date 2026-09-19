# VC-ANDROID-PH05-R03 Corrective Evidence

## Trigger
PH05-R02 passed Gradle configuration, wrapper bootstrap and clean, then all three app Kotlin variants failed on `MainActivity.kt` because `installSplashScreen` was imported from the wrong Kotlin package path.

## Correction
- Retained `androidx.core:core-splashscreen:1.2.0` on the `:app` compile classpath.
- Replaced `import androidx.core.splashscreen.installSplashScreen` with the AndroidX Kotlin API import `import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen`.
- Kept `installSplashScreen()` before `super.onCreate(...)`.
- No PH05 runtime/API/LiveKit/repository/navigation/branding behavior changed.

## Durable regression
- `scripts/ph05_r03_splashscreen_compile_regression.py`
- `scripts/VC-ANDROID-PH05-R03-SOURCE-CHECK.ps1`
- `scripts/VC-ANDROID-PH05-R03-ACCEPTANCE.cmd`

The R03 acceptance retains all PH01-PH05 accumulated checks and the broad Debug/Staging/Release compile, test, lint, assembly and AndroidTest gates.
