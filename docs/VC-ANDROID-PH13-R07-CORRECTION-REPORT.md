# VC-ANDROID-PH13-R07 — Kotlin Compile Correction

Parent: VC-ANDROID-PH13-R06

## Workstation evidence addressed
PH13-R06 successfully reached real Kotlin compilation. `:core:designsystem:compileDebugKotlin` failed because:
1. `VoiceCloudBrandMark.kt` passed `Stroke` positionally to `drawArc`, where the current Compose overload expects `alpha: Float` before `style`.
2. `VoiceCloudPremium.kt` imported/called `matchParentSize`, which did not resolve in the project's current Compose API surface.

## Correction
- All three cloud `drawArc` calls now use named arguments including `style = cloudStroke`.
- Speaking-ring Canvas now uses `Modifier.fillMaxSize()`.
- Touched Canvas line/circle calls use named arguments to remove overload ambiguity.
- No feature/API/business/navigation/data/realtime/security behavior was changed.
- Added regression checks for the exact compiler signatures reported by the workstation.
- Acceptance now skips dependent tests/lint/assemblies only if mandatory Kotlin compile gates fail, avoiding wasted time while remaining fail-closed.
