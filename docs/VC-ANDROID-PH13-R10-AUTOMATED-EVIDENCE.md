# VC-ANDROID-PH13-R10 Automated Evidence

## Workstation evidence inherited from byte-identical R09 product source
The user's supplied PH13-R09 acceptance run established:

| R09 gate | Result |
|---|---|
| Gradle wrapper bootstrap | PASS |
| compileDebugKotlin | PASS |
| compileStagingKotlin | PASS |
| compileReleaseKotlin | PASS |
| Unit tests | PASS |
| Lint Debug/Staging/Release | PASS |
| Assemblies incl. Debug AndroidTest | PASS |
| Physical-device instrumentation | FAIL — ADB app install client timeout |
| Product/delivery integrity | PASS |

R10 cryptographically preserves the Android product/build inputs used by those successful gates.

## R10 regression authority
- `ph13_r10_product_preservation_regression.py` — proves R09 Android product/build-input byte identity.
- `ph13_r10_device_timeout_regression.py` — proves the timeout/recovery/fallback logic is wired.
- `ph13_r10_acceptance_wiring_regression.py` — proves the corrective acceptance uses source/integrity gates and the fast/fallback device closure.
- Complete applicable R09/R08 inherited source, backend, security, payment, RTC and compile-risk diagnostics remain wired.

## Important acceptance rule
R10 is accepted only when `scripts\VC-ANDROID-PH13-R10-ACCEPTANCE.cmd` reaches its final PASS. A real physical-device failure remains fatal; the correction only prevents a single ADB client timeout from being misclassified before exact package state/retry diagnostics are attempted.

## Assistant-side closure before packaging
- R10 + applicable inherited source/regression scripts: **27/27 PASS**.
- R10 device timeout/recovery regression: **15/15 PASS**.
- R10 acceptance wiring regression: **11/11 PASS**.
- R10 product preservation: **185/185 Android product/build-input files byte-identical to R09**.
- R10 delivery integrity is fail-closed and is re-run after archive extraction.

The assistant environment cannot execute Windows ADB/PowerShell physical-device commands, so the only remaining runtime action is the corrected device closure on the user's Windows workstation. The R10 command is intentionally optimized to reuse the verified R09 build artifacts or build only Debug/androidTest prerequisites.
