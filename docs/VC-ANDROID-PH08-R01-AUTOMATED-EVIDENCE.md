# VC-ANDROID-PH08-R01 Automated Evidence

## Static/source authority
Run:
```powershell
python scripts\ph08_r01_source_check.py
```
Expected final line:
`[PASS] VC-ANDROID-PH08-R01 source authority complete`

The checker verifies module registration, PH08 version, PH07 economy preservation, exact R06 API-contract endpoint families, backend-supported profile mutation fields, multipart media, Coil fallback, guarded/lifecycle-safe replay player, access states, safe ID presentation, navigation, guest gating and absence of mock PH08 production payloads.

## PH07 preservation
Run:
```powershell
python scripts\ph08_r01_preservation_regression.py
```
Expected final line:
`[PASS] 87 inherited PH01-PH07 Kotlin source files match the PH07-R03 baseline`

## Compile-risk regression
Run:
```powershell
python scripts\ph08_r01_compile_risk_regression.py
```
Expected final line:
`[PASS] VC-ANDROID-PH08-R01 compile-risk regression: 10/10 PASS`

This permanently guards the PH08-specific Kotlin/Compose risks found during implementation, including Coil painter-state collection, guarded MediaPlayer creation/release, invalid nullable `ifBlank` usage, explicit navigation mutation lambdas and non-fabricated replay access.

## Inherited corrective regressions
The full PH08 acceptance runner also executes:
- `ph07_r03_adb_daemon_regression.py`
- `ph06_r03_hosting_compile_regression.py`

## Full Windows gate
Run:
```powershell
scripts\VC-ANDROID-PH08-R01-ACCEPTANCE.cmd
```
Gate order is locked:
1. `:app:compileDebugKotlin`
2. `:app:compileStagingKotlin`
3. `:app:compileReleaseKotlin`
4. unit tests
5. Debug/Staging/Release lint
6. Debug/Staging/Release + AndroidTest assemblies
7. physical-device instrumentation

A source/static PASS alone is not Android build acceptance.
