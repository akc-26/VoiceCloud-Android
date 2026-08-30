# VoiceCloud Android VC-ANDROID-PH13-R19 — Consolidated Video-Driven Product Correction

R19 uses the build-proven R15 core carried through R18 and closes the product/navigation problems exposed by the latest End User + Creator physical-device videos. Listener navigation is now Home / Discover / Live / Messages / Profile, the center live/microphone control no longer opens Search, Speaker is removed as a pseudo-portal, Creator Studio is Host-approval aware, and Creator Profile opens as an overview before editing. The approved R16 board reconstruction remains the visual authority.

Run the full Windows acceptance from a freshly extracted package:

```bat
scripts\VC-ANDROID-PH13-R19-ACCEPTANCE.cmd
```

Do not Git-freeze or call R19 fully accepted until the full compile/test/lint/assembly/device gate and manual device QA pass.

## PH13 R18 acceptance
R18 preserves the approved R16 design implementation and R17 compiler/UTF-8 fixes while closing Economy/Hosting/Creator component API signature drift. Run `scripts\VC-ANDROID-PH13-R18-ACCEPTANCE.cmd` on Windows for the full Debug/Staging/Release, tests, lint, assemblies and device gate.

# VoiceCloud Android VC-ANDROID-PH13-R16 — Approved Design Fidelity Candidate

R16 freezes **VC-ANDROID-PH13-R15** as the functional/build baseline and reconstructs the Android presentation against the nine approved End User + Creator/Host design boards bundled under `docs/reference/approved-r16/`. The approved boards—not the rejected R14/R15 generic premium-shell composition—are the visual authority for this revision.

Functional authority remains protected: API, repository, ViewModel, model, RTC, security, payment/economy and backend business-rule source is preserved unless explicitly listed in the R16 approved presentation manifest. Run the complete Windows acceptance only from a freshly extracted package:

```bat
scripts\VC-ANDROID-PH13-R16-ACCEPTANCE.cmd
```

R16 is not BUILD/DEVICE ACCEPTED until that command completes all Debug/Staging/Release compile gates, tests, lint, assemblies and physical-device instrumentation.

# VC-ANDROID-PH13-R15

R15 is the compiler-closure revision for the full physical-device-driven R14 UI/UX implementation. Run `scripts\VC-ANDROID-PH13-R15-ACCEPTANCE.cmd` on Windows. Full compile/test/lint/assembly/device acceptance remains mandatory.

# VoiceCloud Android — VC-ANDROID-PH13-R14

R14 is the consolidated physical-device-driven UI/UX correction over PH13-R13. It fixes the systemic text-spacing defect, replaces placeholder-like media behavior with real backend cover/avatar/image rendering plus polished fallbacks, strengthens Home/Auth/Profile/Live/Economy/Settings/Creator/Host presentation, and protects the existing functional/API/RTC/payment/navigation authority.

Run only:
```bat
scripts\VC-ANDROID-PH13-R14-ACCEPTANCE.cmd
```

R14 uses the full required acceptance chain: Debug/Staging/Release Kotlin compilation, unit tests, all-variant lint, Debug/Staging/Release and DebugAndroidTest assemblies, physical-device instrumentation, and final delivery integrity. A missing device is **PENDING**, not full acceptance.

See `docs/VC-ANDROID-PH13-R14-IMPLEMENTATION-REPORT.md` and `docs/VC-ANDROID-PH13-R14-MANUAL-DEVICE-QA.md`.

---

# PH13 R12 Workstation-Safe Acceptance Closure

R12 preserves the build-proven R09/R10/R11 Android product source and corrects only acceptance-harness behavior for workstation-generated artifacts and Windows batch device return-code handling. Run `scripts\VC-ANDROID-PH13-R12-ACCEPTANCE.cmd`.

# VoiceCloud Android — VC-ANDROID-PH13-R11

PH13-R11 is the final **acceptance/device-gate correction** on top of the build-proven PH13-R09/R10 product. Android app/UI/API/runtime/build-input source remains byte-identical to the R09 product authority.

The supplied R09 workstation acceptance already proved:
- Debug Kotlin compile — PASS
- Staging Kotlin compile — PASS
- Release Kotlin compile — PASS
- Unit tests — PASS
- Debug/Staging/Release lint — PASS
- Debug/Staging/Release + DebugAndroidTest assemblies — PASS

R10 then proved the quick Debug + androidTest prerequisite build again, but no healthy authorized device was available by the time device closure ran. R11 corrects that workflow contradiction.

## Acceptance
Run only:
```powershell
scripts\VC-ANDROID-PH13-R11-ACCEPTANCE.cmd
```

R11 performs the device availability preflight **before any artifact preparation or Gradle build**.

- If no healthy authorized API 26+ device is available, R11 reports physical-device QA as **PENDING**, performs no fallback build, preserves the already-proven host/build acceptance, and exits successfully.
- If a device is available, R11 first reuses hash-verified R10/R09 Debug + androidTest artifacts when possible.
- Only when a device is available and reusable artifacts are unavailable does R11 build `assembleDebug + assembleDebugAndroidTest` once.
- Genuine ADB/install/instrumentation failures remain fail-closed.

## Product preservation
R11 does not change the premium UI/UX, application code, APIs, repositories, ViewModels, models, navigation authority, RTC/realtime, security, payment/economy logic or business rules.

## Documentation
- `docs/VC-ANDROID-PH13-R11-CORRECTION-REPORT.md`
- `docs/VC-ANDROID-PH13-R11-AUTOMATED-EVIDENCE.md`
- `docs/VC-ANDROID-PH13-R11-DEVICE-CLOSURE.md`

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

### PH13-R13 acceptance
R13 is an androidTest-only palette-authority correction. Run on Windows with:

`scripts\VC-ANDROID-PH13-R13-ACCEPTANCE.cmd`

When the prior R12/R11 temporary build workspace is still available and hash-valid, R13 reuses the unchanged production Debug APK and rebuilds only the corrected androidTest APK before physical-device instrumentation.
## PH13 R17 corrective acceptance

R17 preserves the approved R16 UI/UX and closes two concrete R16 validation defects: the Windows Python locale crash in the source compile-surface diagnostic and the real Kotlin `DrawScope.size` shadowing compiler failure in `VoiceCloudSpeakingAvatar`. Run `scripts\VC-ANDROID-PH13-R17-ACCEPTANCE.cmd` from Windows. The command forces Python UTF-8 mode and then executes the complete Debug/Staging/Release compile, tests, lint, assemblies and physical-device instrumentation sequence.


### PH13 R20
R20 is the consolidated physical-device UI/UX root-cause correction over R19. Run `scripts\VC-ANDROID-PH13-R20-ACCEPTANCE.cmd` from a freshly extracted package for the mandatory full Windows acceptance sequence.
