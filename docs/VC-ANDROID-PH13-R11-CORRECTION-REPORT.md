# VC-ANDROID-PH13-R11 Correction Report

## Purpose
PH13-R11 corrects the final acceptance-harness defect exposed by the PH13-R10 workstation log. It does not change Android product behavior or UI/UX.

## Proven product state retained
The prior PH13-R09 workstation acceptance already completed Debug, Staging and Release Kotlin compilation, unit tests, all lint variants, and all required assemblies successfully. PH13-R10 again completed the quick Debug + androidTest prerequisite build successfully.

## R10 defect
The shared device instrumentation script correctly returns exit code 2 when no healthy authorized API 26+ Android device is available. That condition is intentionally distinct from an instrumentation failure. PH13-R10 incorrectly counted exit code 2 as a failing acceptance group after spending time on the fallback Debug/androidTest build.

## R11 correction
1. Preflight device availability before artifact search, workspace copy, wrapper bootstrap, or Gradle build.
2. If no healthy authorized device exists, return the device gate as PENDING and keep host/build acceptance valid.
3. If a device exists, prefer hash-verified R10/R09 build artifacts.
4. If reusable artifacts are unavailable, build only Debug + DebugAndroidTest prerequisites once.
5. Real ADB, installation, APK identity, runner-target, or instrumentation failures remain fail-closed.

## Product-change boundary
The build-proven Android product/build-input manifest remains byte-identical. No Kotlin product source is added or modified in R11.
