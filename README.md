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
