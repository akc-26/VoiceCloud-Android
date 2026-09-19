# VC-ANDROID-PH05-R01 Automated Evidence

## Assistant-side source/regression evidence
The PH05 working tree was checked with:
- `scripts/ph05_r01_source_check.py` — PH05 endpoint/scope/RTC/UI/branding authority
- `scripts/ph05_r01_lifecycle_regression.py` — join/rejoin/leave stale-result and RTC ownership regression
- `scripts/ph05_r01_module_dependency_audit.py` — direct dependency/public ABI audit
- all current PH04-R01/R02/R03/R04 regressions
- PH03-R01/R02 source/dependency/duplicate-key regressions
- PH02-R01/R02/R03/R05 regressions
- PH01-R02/R03/R04/R05/R08/R09 and PH01-R09 source authority
- XML parse/integrity and clean-package scans

The durable PH05 checks are part of `VC-ANDROID-PH05-R01-ACCEPTANCE.cmd` and must remain in later phases.

## Workstation acceptance command
```bat
scripts\VC-ANDROID-PH05-R01-ACCEPTANCE.cmd
```

The Windows gate must execute in this order:
1. accumulated source/regression authority;
2. Java/Android SDK resolution and Raspberry Pi/Tailscale connectivity;
3. verified Gradle wrapper bootstrap when required;
4. clean;
5. `:app:compileDebugKotlin`;
6. `:app:compileStagingKotlin`;
7. `:app:compileReleaseKotlin`;
8. unit tests;
9. lint Debug/Staging/Release;
10. assemble Debug/Staging/Release and AndroidTest APK;
11. healthy-device instrumentation when a valid device is available.

## Acceptance status
Assistant-side static/regression/package checks can establish package integrity but do not substitute for the user's Windows/Android SDK build and physical-device runtime validation. PH05 becomes build-accepted only after that gate passes.
