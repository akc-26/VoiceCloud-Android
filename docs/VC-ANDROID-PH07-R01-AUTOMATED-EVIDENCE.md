# VC-ANDROID-PH07-R01 Automated Evidence
- PH07 source authority: run `python scripts/ph07_r01_source_check.py`.
- PH06-R03 preservation source gate is chained by the Windows acceptance runner.
- Required workstation gates: `compileDebugKotlin`, `compileStagingKotlin`, `compileReleaseKotlin`, unit tests, lint Debug/Staging/Release, Debug/Staging/Release assemblies, AndroidTest APK, physical-device instrumentation.
- Financial invariant: Google Play purchase results are never treated as VoiceCloud entitlement until backend validation/verification succeeds.
