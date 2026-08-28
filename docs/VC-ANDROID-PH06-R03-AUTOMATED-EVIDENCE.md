# VC-ANDROID-PH06-R03 Automated Evidence

## R03 corrective gates
- `scripts/VC-ANDROID-PH06-R03-SOURCE-CHECK.ps1`
- `scripts/ph06_r03_hosting_compile_regression.py`
- `scripts/VC-ANDROID-PH06-R03-ACCEPTANCE.cmd`

## Corrected invariant
- `RoomStageState?` remains nullable.
- Host audience rendering never dereferences nullable `stage` directly.
- Speaker filtering uses `stage?.speakers.orEmpty()` snapshot.
- Participant rendering uses `stage?.participants.orEmpty()`.
- The previous unsafe `stage.speakers` expression is forbidden.

## Retained broad gate
The Windows R03 acceptance retains:
1. accumulated PH01-PH06 source/regression checks;
2. live Raspberry Pi REST/Socket/Web connectivity;
3. Gradle wrapper verification/bootstrap;
4. `clean`;
5. `:app:compileDebugKotlin`;
6. `:app:compileStagingKotlin`;
7. `:app:compileReleaseKotlin`;
8. unit tests;
9. Debug/Staging/Release lint;
10. Debug/Staging/Release assemblies;
11. Debug AndroidTest APK;
12. isolated healthy-device instrumentation.

Workstation compiler output remains the authoritative evidence for real AGP/Kotlin compilation.
