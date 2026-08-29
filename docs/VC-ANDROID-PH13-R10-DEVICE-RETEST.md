# VC-ANDROID-PH13-R10 — Device Retest

Run from a freshly extracted R10 folder:

```powershell
scripts\VC-ANDROID-PH13-R10-ACCEPTANCE.cmd
```

## Expected fast path
Because the R09 acceptance reported that its isolated `%TEMP%\VoiceCloud-PH13-R09-*` workspace could not be removed, R10 first searches for that workspace. It will reuse it **only** if:

- both Debug app and Debug androidTest output metadata exist; and
- every Android product/build-input file matches the canonical R09 SHA-256 manifest.

If valid, R10 proceeds directly to the corrected device gate and avoids another full build.

## Expected fallback
If the prior workspace is missing or does not verify, R10 automatically creates a clean corrective workspace, bootstraps the verified local Gradle 9.5 distribution, and runs only:

```text
:app:assembleDebug
:app:assembleDebugAndroidTest
```

It then performs the corrected device gate.

## Device install behavior
On the selected device, the gate first checks whether the exact APK is already installed. If not, it attempts non-incremental ADB install. A client timeout triggers exact installed-APK SHA verification, one ADB transport recovery, and one `adb push` + `pm install` fallback before failure is declared.
