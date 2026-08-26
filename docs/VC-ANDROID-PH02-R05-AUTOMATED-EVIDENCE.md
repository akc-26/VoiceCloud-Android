# VC-ANDROID-PH02-R05 Automated Evidence

## Baseline

- Repository: `akc-26/VoiceCloud-Android`
- Parent branch: `VoiceCloud-Android-VC-ANDROID-PH01-R09`
- Parent commit: `ce56b1e36a4b2b563a65e4699df9193751f0978d`
- Phase: `VC-ANDROID-PH02-R05`

## User-workstation evidence incorporated

The R02 Windows run passed all accumulated source regressions, live Tailscale REST/Socket/Web connectivity, cached Gradle checksum verification, and Gradle wrapper generation. It then failed in `:feature:auth:compileDebugKotlin` on DataStore public-ABI leakage and an invalid Compose `align` use. The same log also showed Room schema-location and deprecated Hilt ViewModel Compose warnings. R03 corrects all four findings and audits the next direct dependency/public ABI surface before packaging.

## Packaging-environment source/regression gates

| Gate | Result |
|---|---:|
| Retained PH02-R01 source authority | 85/85 PASS |
| PH02-R02 Gradle Kotlin DSL regression | 8/8 PASS |
| PH02-R02 clean-delivery regression | 5/5 PASS |
| PH02-R05 compile-surface regression | 30/30 PASS |
| PH01-R09 source authority | 43/43 PASS |
| PH01-R02 Windows acceptance regression | 13/13 PASS |
| PH01-R03 AGP9 built-in Kotlin regression | 12/12 PASS |
| PH01-R04 SDK/logging regression | 12/12 PASS |
| PH01-R05 module ABI/runtime regression | 16/16 PASS |
| PH01-R08 live-server regression | 23/23 PASS |
| PH01-R09 wrapper/connectivity regression | 13/13 PASS |

Additional package audit verifies XML well-formedness, Python regression-script syntax, no runtime TODO/FIXME placeholders, no generated/machine-specific directories/files, one-level ZIP extraction, and retention of the corrected R09 Gradle-wrapper bootstrap.

## Required Windows acceptance — not claimed in this environment

This packaging environment does not contain the Android SDK/Gradle dependency cache needed to truthfully claim Android compilation. Run:

```bat
scripts\VC-ANDROID-PH02-R05-ACCEPTANCE.cmd
```

R03 intentionally runs the three Kotlin compile variants in one Gradle `--continue` matrix. If a compile failure remains, the same run should expose every reachable Debug/Staging/Release problem rather than forcing one failure per iteration. PH02 is accepted only after compile, tests, lint, assemblies, connected instrumentation (when a device is attached), and the manual QA checklist pass.

## R05 preservation correction

- PH02-R05 targeted preservation regression: `5/5 PASS`.
- Production-source delta from R03: only `feature/bootstrap/.../BootstrapRoute.kt`.
- R04 `@param:ApplicationContext` provider-function change is explicitly absent.
- R03 Google authentication implementation is preserved unchanged.
- Windows Android SDK compile/test/lint/assembly gates remain authoritative and must be run with `scripts\VC-ANDROID-PH02-R05-ACCEPTANCE.cmd`.
