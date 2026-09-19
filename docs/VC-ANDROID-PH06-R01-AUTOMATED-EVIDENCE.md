# VC-ANDROID-PH06-R01 Automated Evidence

## Assistant-side gates
The package contains and runs:
- `ph06_r01_source_check.py`
- `ph06_r01_ui_motion_spacing_regression.py`
- `ph06_r01_module_dependency_audit.py`
- `ph06_r01_host_runtime_regression.py`
- `VC-ANDROID-PH06-R01-SOURCE-CHECK.ps1`
- all retained PH05/PH04/PH03/PH02/PH01 regression scripts.

## Workstation acceptance
Run:

```powershell
scripts\VC-ANDROID-PH06-R01-ACCEPTANCE.cmd
```

The command performs:
1. PH06 + inherited source/regression gates.
2. Java 17 / Android SDK discovery.
3. live Raspberry Pi REST / Socket.IO / Web connectivity.
4. verified Gradle 9.5.0 wrapper bootstrap when absent.
5. `clean`.
6. one broad `--continue` sweep containing, in order:
   - `:app:compileDebugKotlin`
   - `:app:compileStagingKotlin`
   - `:app:compileReleaseKotlin`
   - unit `test`
   - `lintDebug`, `lintStaging`, `lintRelease`
   - `:app:assembleDebug`, `:app:assembleStaging`, `:app:assembleRelease`
   - `:app:assembleDebugAndroidTest`
7. healthy-device isolated instrumentation.

## Instrumentation
The retained foundation instrumentation verifies white-label ARGB rendering and now also verifies that PH06's installed application declares `RECORD_AUDIO`. Host UI additionally keeps runtime permission request behind explicit microphone user intent.

## Acceptance interpretation
A source/regression PASS is not a substitute for Android compilation or physical-device QA. PH06 must not be Git-frozen/advanced until the Windows acceptance command and the manual QA checklist are confirmed.

## Final assistant-side package verification
Before ZIP creation, the exact Python regression sequence referenced by `VC-ANDROID-PH06-R01-ACCEPTANCE.cmd` passed `30/30` scripts together. PH06-specific gates passed:
- source authority: `79/79`
- UI motion/spacing: `36/36`
- module dependency audit: `21/21`
- Host runtime regression: `13/13`

Additional package checks:
- XML parse: `28/28`
- manual physical-device QA cases: `57`
- forbidden generated/machine artifacts in source package: `0`

These are assistant-side source/package checks only; Windows Gradle compilation/lint/assemblies and device instrumentation remain required for build acceptance.
