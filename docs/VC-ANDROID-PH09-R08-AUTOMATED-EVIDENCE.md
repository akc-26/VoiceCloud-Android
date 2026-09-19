# VC-ANDROID-PH09-R08 Automated Evidence

Pre-Gradle delivery gates run on the packaged source tree:
- acceptance wiring: **27/27 PASS**
- PowerShell surface: **19/19 PASS**
- PH09 source authority: **PASS**
- product quality: **15/15 PASS**
- security/privacy: **15/15 PASS**
- Gradle properties authority: **PASS**
- Windows/tooling preflight: **20/20 PASS**
- PH08 preservation: **PASS**
- PH09 compile-surface/import: **171/171 PASS**
- PH09 compile-risk: **26/26 PASS**
- warning/packaging closure: **11/11 PASS**
- device/instrumentation closure: **23/23 PASS**
- inherited PH08 payment: **33/33 PASS**
- inherited PH08 compile-risk: **19/19 PASS**
- inherited PH07 ADB: **4/4 PASS**
- inherited PH06 hosting: **13/13 PASS**

R07 workstation evidence already proves Debug/Staging/Release Kotlin compilation, unit tests, lint and assemblies succeed. R08 changes only the failing PowerShell/device acceptance surface plus the remaining FCM compiler-warning annotation. The real Windows PowerShell parser gate is intentionally first in R08 acceptance so script syntax is proven before another long Gradle run.

Destructive regressions used during R08 preparation:
- reintroducing `$UserId:` / `$currentUser:` -> PowerShell-surface regression FAIL;
- removing asynchronous redirected-output draining -> PowerShell-surface regression FAIL;
- reverting `OVERRIDE_DEPRECATION` -> warning/compile-risk regression FAIL;
- reintroducing hard-coded debug app ID -> device closure regression FAIL.
