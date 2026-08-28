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
