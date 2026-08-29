# VoiceCloud Android VC-ANDROID-PH13-R10 — Device Acceptance Closure

## Parent
`VC-ANDROID-PH13-R09`

## Why R10 exists
The supplied R09 Windows acceptance output proves that the R09 Android product is build-valid through the complete host build chain:

- `compileDebugKotlin` — PASS
- `compileStagingKotlin` — PASS
- `compileReleaseKotlin` — PASS
- unit tests — PASS
- Debug/Staging/Release lint — PASS
- Debug/Staging/Release + Debug AndroidTest assemblies — PASS
- product/delivery integrity — PASS

The only failing group was physical-device instrumentation. The selected OnePlus API 30 device was healthy and both package identities were resolved, but the first `adb install` call exceeded the old 90-second client timeout and the gate aborted without checking whether installation had actually completed.

## Correction boundary
R10 intentionally does **not** modify Android product/UI/API/runtime/build-input source. This preserves the exact R09 source that already passed the expensive compile/test/lint/assembly chain on the user's workstation.

Only acceptance/device-harness authority, revision metadata and R10 evidence are changed/added.

## Device correction
The shared physical-device gate now:

1. prefers USB transport over wireless ADB when both are healthy;
2. skips redundant install only when the exact local APK SHA-256 matches the installed APK SHA-256;
3. uses `adb install --no-incremental` with a bounded 180-second first attempt;
4. if the ADB client times out, checks the exact installed APK hash before deciding failure;
5. performs one bounded ADB reconnect / `wait-for-device` recovery;
6. falls back once to `adb push` + `pm install` so transfer and package-manager execution are separated;
7. removes the temporary pushed APK;
8. dynamically revalidates the instrumentation runner and package state;
9. retries instrumentation once only when the ADB client itself times out;
10. remains fail-closed for genuine install, runner, crash or test failures.

## Time-saving corrective acceptance
`VC-ANDROID-PH13-R10-ACCEPTANCE.cmd` does not repeat R09's already-proven 25–30 minute full host build chain.

It first verifies the complete R09 Android product/build-input hashes. Then:

- **Fast path:** reuse a remaining R09 isolated acceptance workspace only after every Android product/build-input file matches the R09 canonical SHA-256 manifest and both Debug APK metadata files exist.
- **Fallback:** if no reusable workspace exists, create one clean corrective workspace and build only `assembleDebug` + `assembleDebugAndroidTest`, then run the resilient physical-device gate.

This preserves build rigor without forcing the user to repeatedly rerun unchanged Staging/Release/test/lint work.
