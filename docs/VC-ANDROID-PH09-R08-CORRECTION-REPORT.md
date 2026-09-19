# VC-ANDROID-PH09-R08 Closure Correction Report

## R07 evidence reviewed as a whole
R07 successfully passed all PH09/inherited source gates, all three Kotlin compile variants, unit tests, lint and assemblies. The only compiler warning remaining in the supplied run was the FCM `onNewToken` override-deprecation diagnostic, repeated once per build variant. Gate 7 then stopped before device logic because Windows PowerShell rejected three strings containing unbraced `$variable:` interpolation.

## PowerShell closure
R08 fixes all three parser hazards and adds a first-class parser authority using `System.Management.Automation.Language.Parser::ParseFile` across every PowerShell script before Python or Gradle work. A separate static surface regression scans the entire PowerShell script set for the same variable-before-colon hazard. Redirected ADB output is drained asynchronously to remove a latent pipe-deadlock risk during verbose instrumentation.

## Compiler-warning closure
The retained FCM callback now suppresses the exact `OVERRIDE_DEPRECATION` diagnostic. Other warning classes corrected in R07 remain preserved.

## Device closure retained
R08 retains AGP output-metadata-derived app/test APK paths, foreground Android user resolution, explicit-user installs/package checks, dynamic runner discovery, runner-target validation and explicit-user instrumentation.

## No PH09 scope expansion
Settings/Security/Safety/CMS/Support behavior and PH08 payment authority remain unchanged.
