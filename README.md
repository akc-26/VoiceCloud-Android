# VoiceCloud Android — VC-ANDROID-PH13-R10

PH13-R10 is a **device-acceptance corrective revision only** on top of PH13-R09. The Android product/UI/API/runtime/build-input source is byte-identical to R09. The supplied R09 workstation acceptance already proved Debug/Staging/Release Kotlin compilation, unit tests, lint and all assemblies; its only failing gate was the wireless ADB physical-device APK install timing out.

## Fast corrective acceptance
Run only:
```powershell
scripts\VC-ANDROID-PH13-R10-ACCEPTANCE.cmd
```

R10 first proves the product/build-input hashes are identical to R09. It then reuses the hash-verified R09 temporary build artifacts when they still exist. If they are unavailable, it builds **Debug + androidTest prerequisites only** and runs the corrected device gate. It does not make you repeat the already-proven Staging/Release compile, unit-test, lint and full-assembly chain.

## Device-gate correction
- disables incremental ADB install for the corrective path;
- verifies the exact installed APK by SHA-256, including after an ADB client timeout;
- performs one bounded ADB transport recovery;
- falls back to `adb push` + `pm install` when direct install cannot be proven;
- retries instrumentation once only when the ADB client itself times out;
- preserves USB-first device preference;
- remains fail-closed for real install/instrumentation failures.

## Documentation
- `docs/VC-ANDROID-PH13-R10-CORRECTION-REPORT.md`
- `docs/VC-ANDROID-PH13-R10-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH13-R10-DEVICE-RETEST.md`

---

# VoiceCloud Android — VC-ANDROID-PH13-R09

PH13-R09 is the full page-by-page premium UI/UX implementation on the working PH13-R08 baseline. It upgrades both End User and Creator/Host portals to the approved glossy ivory + emerald/teal + gold visual quality while preserving the R08 feature/API/business/runtime authority.

## Acceptance
Run only:
```powershell
scripts\VC-ANDROID-PH13-R09-ACCEPTANCE.cmd
```

Do not Git-freeze R09 until the workstation acceptance reaches the final PASS and the manual UI/UX checklist has been reviewed on device.

## R09 documentation
- `docs/VC-ANDROID-PH13-R09-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH13-R09-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH13-R09-MANUAL-QA.md`
- `docs/VC-ANDROID-PH13-R09-SCREEN-COVERAGE.md`
- `docs/reference/VC-ANDROID-PH13-R09-HOME-QUALITY-REFERENCE.png`

---

# VoiceCloud Android — VC-ANDROID-PH13-R07

## PH13-R07 compile correction

Use `scripts\VC-ANDROID-PH13-R07-ACCEPTANCE.cmd` on Windows. R07 is a compile-only correction over PH13-R06: the VoiceCloud premium UI/UX and all feature/API/business behavior remain unchanged.


R06 is an **acceptance-harness corrective revision only** on top of PH13-R05. The complete R05 premium UI/UX and all Android product/API/runtime behavior remain unchanged.

## Why R06 exists
The R05 workstation log showed that the cached Gradle 9.5.0 ZIP was successfully verified and used to generate the wrapper, but the bootstrap then rewrote the wrapper URL back to `services.gradle.org`. The subsequent compile/test/lint/assembly gates timed out attempting a redundant network download. R05 also classified Android Studio/Gradle machine files such as `.gradle` and `local.properties` as new product source.

R06 keeps the acceptance wrapper pinned to the verified local Gradle ZIP and makes source integrity workstation-aware without weakening tracked-source or unmanifested-product checks.

## Acceptance
Run only:
```powershell
.\scripts\VC-ANDROID-PH13-R06-ACCEPTANCE.cmd
```
Do not freeze R06 until this command reaches its final PASS.

## Documentation
- `docs/VC-ANDROID-PH13-R06-CORRECTION-REPORT.md`
- `docs/VC-ANDROID-PH13-R06-AUTOMATED-EVIDENCE.md`
- R05 implementation report/manual QA remain the product/UI authority.

---

# VoiceCloud Android — VC-ANDROID-PH13-R04

R04 is the acceptance-harness closure revision for PH13. **PH13 product/Kotlin/API behavior is unchanged from PH13-R01/R02/R03.** R04 fixes the test-process defect exposed by the R03 workstation run: an isolated-workspace cleanup error terminated the runner before the remaining independent validation could execute.

## Acceptance — full diagnostic mode
Run only:
```powershell
.\scripts\VC-ANDROID-PH13-R04-ACCEPTANCE.cmd
```

R04 does **not** stop discovery at the first independent failure. It attempts every applicable source/regression gate, resolves Java and Android SDK preconditions, then runs Debug/Staging/Release Kotlin compilation, unit tests, lint, assemblies, device instrumentation, cleanup, and final original-delivery-tree integrity. All failures are collected and summarized at the end. The final process still exits nonzero when any genuine mandatory gate fails.

## Windows build isolation and cleanup
- Gradle runs only in a unique `%TEMP%` source copy.
- Every run receives its own private `GRADLE_USER_HOME`.
- Gradle invocations use `--no-daemon`; the harness also runs `gradlew --stop` before cleanup.
- Workspace and private Gradle-home cleanup use bounded retries plus Windows `rd /s /q` fallback.
- A residual temporary-directory cleanup problem is reported distinctly as an operational warning because it is outside the immutable distributable/Git source tree; it cannot hide or bypass source/build failures.
- Gate 8 always executes against the untouched original delivery directory, even if an earlier diagnostic group failed.

## R03 evidence that led to R04
The R03 workstation run contained no `BUILD FAILED` result: Debug/Staging/Release compile, unit tests, lint and assemblies completed successfully. The sole fatal result was isolated `%TEMP%` workspace cleanup, which caused the R03 fail-fast runner to terminate before final delivery-tree validation. R04 corrects the runner architecture rather than changing PH13 application code.

## Documentation
- `docs/VC-ANDROID-PH13-R04-CORRECTION-REPORT.md`
- `docs/VC-ANDROID-PH13-R04-AUTOMATED-EVIDENCE.md`
- PH13 functional implementation and manual QA remain documented by the retained PH13-R01 reports.

---

# VoiceCloud Android — VC-ANDROID-PH13-R02

R02 is the acceptance-harness corrective revision of PH13-R01. Product/Kotlin behavior is unchanged from PH13-R01. R02 fixes the workstation Gate 8 false failure caused by Gradle-wrapper bootstrap line-ending mutation and by applying distributable-ZIP cleanliness rules to expected post-build artifacts.

## Acceptance
Run only:
```powershell
scripts\VC-ANDROID-PH13-R02-ACCEPTANCE.cmd
```
Do not Git-freeze PH13 until this R02 command reaches its final PASS.

## Correction authority
- Tracked delivery source must be byte-identical after the build.
- Wrapper bootstrap is restored to the canonical committed `gradle-wrapper.properties` bytes before Gate 8 source-integrity verification.
- Generated `gradlew`, `gradlew.bat` and `gradle-wrapper.jar` are removed after the build so they cannot leak into Git freeze.
- Build outputs/caches are allowed only as known workstation-generated artifacts during post-build verification; they remain forbidden from the distributable ZIP.

## Documentation
- `docs/VC-ANDROID-PH13-R02-CORRECTION-REPORT.md`
- `docs/VC-ANDROID-PH13-R02-AUTOMATED-EVIDENCE.md`
- PH13 functional implementation/QA remains documented by the retained PH13-R01 reports.

---

# VoiceCloud Android — VC-ANDROID-PH13-R01

PH13 completes the locked Creator Portal feature scope on the PH12-R01 package baseline: Analytics, Creator Wallet, Earnings, Gifts, Payout Requests, Creator Notifications, Host eligibility/progression, Host application and private verification assets.

Android authority explicitly forbids exposing Creator stream keys, RTMP URLs, provider secrets or credential reveal/regeneration controls. Those web-only credential surfaces are intentionally absent.

## Acceptance
Run only:
```powershell
scripts\VC-ANDROID-PH13-R01-ACCEPTANCE.cmd
```
Do not Git-freeze PH13 until the workstation/device command reaches its final PASS.

## Documentation
- `docs/VC-ANDROID-PH13-R01-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH13-R01-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH13-R01-MANUAL-QA.md`

---

# VoiceCloud Android — VC-ANDROID-PH11-R02

PH11 implements the locked **Creator Live Rooms & Schedule** phase on frozen PH10-R01 commit `999a3c310c9e40bf1a47972551050bfd5b00e54d` together with the requested application-wide UI/UX/runtime corrections.

R02 corrects the R01 workstation unit-test authority failure. All three Kotlin compile variants already passed in R01; R02 updates stale PH10-era Creator expectations and compiler-generated JVM-field-sensitive Settings assertions without changing PH11 production behavior.

## Acceptance
Run only:
```powershell
scripts\VC-ANDROID-PH11-R02-ACCEPTANCE.cmd
```

Do not Git-freeze PH11 until Windows/device acceptance reaches:
```text
[PASS] VC-ANDROID-PH11-R02 acceptance commands completed successfully.
```

## Documentation
- `docs/VC-ANDROID-PH11-R02-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH11-R02-CORRECTION-REPORT.md`
- `docs/VC-ANDROID-PH11-R02-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH11-R02-MANUAL-QA.md`

---

# VoiceCloud Android — VC-ANDROID-PH10-R01

PH10 implements the locked **Creator Portal Core, Dashboard, Profile, Settings & Support** phase on frozen PH09-R08 commit `10e820a005d363643bcfc5568e661de96a41dbc9`.

## PH10 scope
- dedicated `:feature:creator` module
- separate `creator/*` portal shell/navigation
- backend `CREATOR` role guard
- Creator dashboard from `/creator/dashboard`
- Creator profile from `/users/profile/me` + `/users/profile`
- Creator settings from `/users/settings`
- Creator/Host audience CMS from `/cms/creator/pages`
- persisted Contact Support through `/contact`
- Creator access application linkage from Creator Sign In
- maintenance/restricted/session-expired handling
- CREATOR-only `Switch to Creator` / `Switch to VoiceCloud` using the same authenticated account/token

PH11–PH13 functionality is intentionally **not** pulled forward.

## Acceptance
Run only:
```powershell
scripts\VC-ANDROID-PH10-R01-ACCEPTANCE.cmd
```

The acceptance command retains the PH09-R08/PH08/PH07/PH06 critical regression gates and then executes the locked order: `compileDebugKotlin` → `compileStagingKotlin` → `compileReleaseKotlin` → unit tests → lint → assemblies → physical-device instrumentation.

Do not Git-freeze PH10 until the Windows/device acceptance reaches:
```text
[PASS] VC-ANDROID-PH10-R01 acceptance commands completed successfully.
```

## Documentation
- `docs/VC-ANDROID-PH10-R01-IMPLEMENTATION-REPORT.md`
- `docs/VC-ANDROID-PH10-R01-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH10-R01-MANUAL-QA.md`

---

# VoiceCloud Android — VC-ANDROID-PH09-R08

PH09 implements the locked **Preferences, Security, Safety, CMS & Support** phase on frozen PH08-R02 commit `1739f081ac386acca9a24b37863b72c4db952089`.

R08 is the closure corrective revision from the complete R07 Windows acceptance output. R07 proved Debug/Staging/Release Kotlin compilation, unit tests, lint and assemblies all succeed, but Gate 7 never executed because the PowerShell device script had three invalid unbraced variable-before-colon interpolations. The same R07 compiler output also still contained one FCM override-deprecation warning in each variant.

## R08 closure
- Adds a **real Windows PowerShell parser gate** across every `scripts/*.ps1` before Python, Gradle or device work begins.
- Corrects all three invalid PowerShell interpolations by using `${UserId}:` / `${currentUser}:`.
- Audits the complete current PowerShell surface for the same parser hazard.
- Hardens redirected process capture with asynchronous stdout/stderr draining so verbose ADB/instrumentation output cannot deadlock the harness.
- Retains AGP-metadata-derived app/test APK identity, foreground-user-aware install/package verification and dynamic runner-target validation from R07.
- Corrects the remaining Kotlin compiler warning with `@Suppress("OVERRIDE_DEPRECATION")` on the retained FCM callback compatibility path.
- Preserves PH08 payment/live/profile authority and all inherited compile/runtime regressions.

## Acceptance
Run only:
```powershell
scripts\VC-ANDROID-PH09-R08-ACCEPTANCE.cmd
```

R08 first parses every PowerShell script with `System.Management.Automation.Language.Parser`. Only after that succeeds does it run PH09/inherited regressions and the locked order: `compileDebugKotlin` → `compileStagingKotlin` → `compileReleaseKotlin` → unit tests → lint → assemblies → physical-device instrumentation.

Do not freeze PH09 to Git until the Windows/device acceptance reaches the final R08 PASS line.

## VC-ANDROID-PH12-R01
Creator Audience, Messaging, Followers & Subscribers is implemented from PH11-R02 (`fad14e236a780cbe14a74a4676a3ad94237d9bbe`). Run `scripts\VC-ANDROID-PH12-R01-ACCEPTANCE.cmd` on the Windows Android workstation before phase acceptance or Git freeze.


## VC-ANDROID-PH13-R03
Acceptance-harness correction only. PH13 product/Kotlin source is unchanged from R01/R02. R03 executes all real Gradle compile/test/lint/assembly/device gates in an isolated `%TEMP%` copy and validates the original extracted source tree only after that workspace is removed. Use `scripts\VC-ANDROID-PH13-R03-ACCEPTANCE.cmd`.

### PH13-R08 acceptance
Run `scripts\VC-ANDROID-PH13-R08-ACCEPTANCE.cmd` from a freshly extracted PH13-R08 folder. This revision closes the Auth UI compiler defect exposed by PH13-R07 and performs a broader premium-UI contract audit before the mandatory Debug/Staging/Release Kotlin compile gates.
