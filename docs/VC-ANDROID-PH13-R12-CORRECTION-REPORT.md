# VC-ANDROID-PH13-R12 Correction Report

## Scope
Acceptance-harness correction only. Android application/product source remains byte-identical to the workstation-build-proven R09/R10/R11 authority.

## R11 failure reproduced
R11 product-preservation treated normal workstation-generated files (`local.properties`, `gradle/gradle-daemon-jvm.properties`, and `.gradle/**`) as unauthorized new files, while delivery integrity correctly ignored them. This contradictory classification blocked device closure before it ran.

## R12 corrections
1. Product preservation and delivery integrity now share the same generated-artifact authority.
2. Exact R11 workstation artifacts are simulated by an automated regression before acceptance proceeds.
3. Unknown untracked files still fail closed.
4. Windows acceptance enables delayed expansion and captures the device PowerShell return code with `!ERRORLEVEL!`, preventing stale `%ERRORLEVEL%` / `%VC_DEVICE_RC%` values inside parenthesized batch blocks.
5. No Android product/API/UI/runtime source was changed.

## Build authority
R09 workstation evidence remains the build authority for Debug/Staging/Release Kotlin compilation, unit tests, lint, and assemblies. R12 does not repeat those expensive already-proven gates.
