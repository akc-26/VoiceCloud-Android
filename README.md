# VoiceCloud Android — VC-ANDROID-PH06-R03

PH06-R03 is the corrective delivery for **Host/Speaker Room Management, Scheduling, Stage, Polls & Quiz**, preserving the PH06-R02 premium UI/UX refinement and correcting the nullable stage compiler failure exposed by the real Windows AGP/Kotlin build.

## Parent authority
- Git baseline before PH06: `VoiceCloud-Android-VC-ANDROID-PH05-R04`
- Exact parent commit: `85eef801187243453e84bbf7e71af67ba73aef4b`

## R03 correction
The PH06-R02 Windows broad build reached real Kotlin compilation and found one defect in Host Live Console audience rendering: nullable `RoomStageState?` was dereferenced as `stage.speakers`. R03 uses null-safe `orEmpty()` snapshots for both speakers and participants and forbids direct nullable `stage.` dereferences in the Hosting UI.

## Full Windows acceptance
```powershell
scripts\VC-ANDROID-PH06-R03-ACCEPTANCE.cmd
```

The command retains the complete PH01-PH06 regression chain, live backend connectivity, Debug/Staging/Release compilation, tests, lint, all assemblies, AndroidTest APK and isolated connected-device instrumentation.

## Documentation
- `docs/VC-ANDROID-PH06-R03-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH06-R03-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH06-R03-MANUAL-QA.md`

R03 is not build/device accepted until the Windows acceptance command and physical-device QA pass.
