# VC-ANDROID-PH09-R07 Closure Correction Report

## Why R07 exists
The R06 workstation run proved Debug/Staging/Release Kotlin compilation, unit tests, lint and assemblies all succeed. The final physical-device gate failed only after both APKs installed because the inherited device harness hard-coded the debug package and did not prove app/test installation for the Android user in which instrumentation ran.

## Device closure
R07 replaces hard-coded instrumentation identity with AGP `output-metadata.json`, resolves `am get-current-user`, installs app/test APKs explicitly for that user, verifies package enablement for that user, discovers the installed runner dynamically, requires runner target == AGP-derived app ID, and runs `am instrument --user <id>`.

## Warning closure
R07 also reviews every warning printed by the R06 compiler/assembly run. Safe warnings are corrected directly: explicit Hilt qualifier targets, redundant Elvis/safe-call cleanup, and explicit JNI keep-debug-symbol rules for the three third-party libraries AGP could not strip. Google Sign-In and legacy FCM registration-token deprecations remain intentionally compatible with the current backend contract and are explicitly suppressed/documented pending coordinated contract migration.

## No scope expansion
PH09 Settings/Security/Safety/CMS/Support behavior and PH08 payment authority remain unchanged.
