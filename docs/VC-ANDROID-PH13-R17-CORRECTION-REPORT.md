# VC-ANDROID-PH13-R17 — Consolidated Compiler + Windows UTF-8 Closure

## Parent and scope

- Parent package: **VC-ANDROID-PH13-R16**.
- R16 remains the approved-design UI/UX implementation authority.
- R17 does **not** redesign or roll back the R16 presentation.
- Product-source change is limited to one file: `core/designsystem/.../VoiceCloudPremium.kt`.
- API, repository, ViewModel, model, navigation authority, RTC/realtime, security, permissions, payment/economy and backend business rules remain protected.

## Why R16 failed

Two independent defects were proven by the Windows/device workflow:

1. The R16 source diagnostic crashed under Windows Python 3.14 because `ph13_r16_compile_surface_regression.py` used locale-dependent `Path.read_text()` calls. Windows selected `cp1252`, while `DiscoveryScreens.kt` contains UTF-8 text that cannot be decoded by that codec. The source gate therefore stopped before the full Android acceptance runner.
2. A direct Gradle compile then exposed a real Kotlin compiler defect in `VoiceCloudPremium.kt`. `VoiceCloudSpeakingAvatar` has a public parameter named `size: Dp`; inside its `Canvas` block, unqualified `size.width`, `size.height` and `size.minDimension` resolved to that outer `Dp` parameter rather than `DrawScope.size`.

## R17 corrections

### Kotlin compiler closure

`VoiceCloudSpeakingAvatar` keeps its public `size: Dp` parameter for API/call-site compatibility, but its Canvas now captures the drawing-scope dimensions explicitly:

```kotlin
val canvasSize = this.size
```

All speaking-avatar geometry uses `canvasSize.width`, `canvasSize.height` and `canvasSize.minDimension`.

R17 also scans the full project for functions that combine a `size: Dp` parameter with Canvas drawing and rejects any recurrence of unqualified `size.width`, `size.height` or `size.minDimension`.

### Windows Python locale closure

R17 acceptance sets:

```text
PYTHONUTF8=1
PYTHONIOENCODING=utf-8
```

The PowerShell source diagnostic sets the same environment for every child Python regression. The exact R16 compile-surface checker that crashed is also corrected to use explicit `encoding='utf-8'` reads, so it is locale-independent even when run directly.

### Expanded compile-surface audit

R17 audits every Kotlin file changed by the R16 design implementation plus the foundation instrumentation test for:

- merge markers;
- braces, parentheses and brackets;
- Compose modifier-extension imports;
- drawable resource resolution;
- Canvas/DrawScope size-shadowing;
- known nullable Host-stage safety;
- Title Case whitespace preservation;
- R15 Hosting `clip` compiler closure;
- R16 Portal Selector guest callback wiring;
- R16 Creator analytics callback wiring.

## Acceptance authority

R17 does not weaken the Android acceptance gate. The Windows runner still requires:

1. `:app:compileDebugKotlin`
2. `:app:compileStagingKotlin`
3. `:app:compileReleaseKotlin`
4. unit tests
5. Debug/Staging/Release lint
6. Debug/Staging/Release + Debug AndroidTest assemblies
7. physical-device instrumentation
8. final delivery integrity

No claim is made that these Android Gradle/device gates ran in the Linux packaging environment. They must pass on the Android workstation before Git freeze.
