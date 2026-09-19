from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
cmd=(ROOT/'scripts/VC-ANDROID-PH13-R14-ACCEPTANCE.cmd').read_text(encoding='utf-8')
ps=(ROOT/'scripts/VC-ANDROID-PH13-R14-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
diag=(ROOT/'scripts/VC-ANDROID-PH13-R14-SOURCE-DIAGNOSTIC.ps1').read_text(encoding='utf-8')
delivery=(ROOT/'scripts/ph13_r14_delivery_integrity.py').read_text(encoding='utf-8') if (ROOT/'scripts/ph13_r14_delivery_integrity.py').exists() else ''
checks=[]
def check(name,ok):
    checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
check('R14 source diagnostics wired before Android build', 'VC-ANDROID-PH13-R14-SOURCE-DIAGNOSTIC.ps1' in cmd and cmd.index('SOURCE-DIAGNOSTIC') < cmd.index('ISOLATED-BUILD'))
check('R14 full isolated build wired', 'VC-ANDROID-PH13-R14-ISOLATED-BUILD.ps1' in cmd)
check('R14 delivery integrity wired', 'ph13_r14_delivery_integrity.py' in cmd)
check('Windows delayed expansion enabled', 'EnableDelayedExpansion' in cmd)
check('Android build return code captured with delayed expansion', 'set "VC_ANDROID_RC=!ERRORLEVEL!"' in cmd)
check('Device pending remains distinct exit code 2', 'exit /b 2' in cmd and 'VC_DEVICE_PENDING' in cmd)
check('R14 does not mislabel pending device as full acceptance', '[PENDING] VC-ANDROID-PH13-R14 is NOT fully accepted yet.' in cmd)
check('R14 only labels full acceptance after device path returns success', 'VC-ANDROID-PH13-R14 FULL ACCEPTANCE' in cmd)
check('Full build Gate 1 compileDebugKotlin present', ':app:compileDebugKotlin' in ps)
check('Full build Gate 2 compileStagingKotlin present', ':app:compileStagingKotlin' in ps)
check('Full build Gate 3 compileReleaseKotlin present', ':app:compileReleaseKotlin' in ps)
check('Full build unit tests present', "@('test')" in ps)
check('Full build all-variant lint present', "@('lintDebug','lintStaging','lintRelease')" in ps)
check('Full build Debug/Staging/Release/androidTest assemblies present', all(x in ps for x in (':app:assembleDebug',':app:assembleStaging',':app:assembleRelease',':app:assembleDebugAndroidTest')))
check('Physical-device instrumentation remains mandatory final executable gate', 'VC-ANDROID-DEVICE-INSTRUMENTATION.ps1' in ps and '[GATE 7] Physical-device instrumentation' in ps)
check('Full build uses isolated workspace', 'VoiceCloud-PH13-R14-' in ps and 'robocopy' in ps)
check('Gradle wrapper remains verified local bootstrap', 'BOOTSTRAP-GRADLE-WRAPPER.ps1' in ps and "distributionUrl=file\\:///" in ps)
check('Room generated schema directory is excluded from isolated source copy', 'app.voicecloud.core.database.VoiceCloudDatabase' in ps)
check('Source diagnostic uses R14 preservation, visual, hygiene and contamination gates', all(x in diag for x in ('ph13_r14_product_preservation_regression.py','ph13_r14_visual_device_regression.py','ph13_r14_kotlin_compile_risk_regression.py','ph13_r14_windows_harness_structure_regression.py','ph13_r14_source_hygiene.py','ph13_r14_workstation_artifact_regression.py')))
check('Stale R13 product/workstation and old whitespace gates are not used', all(x not in diag for x in ('ph13_r13_product_preservation_regression.py','ph13_r13_workstation_artifact_regression.py','ph13_r01_whitespace_regression.py')))
check('Existing device resilience authority retained', 'ph09_r08_device_closure_regression.py' in diag and 'VC-ANDROID-DEVICE-INSTRUMENTATION.ps1' in ps)
failed=len(checks)-sum(checks)
if failed: raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R14 acceptance wiring: {sum(checks)}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R14 acceptance wiring: {len(checks)}/{len(checks)} PASS')
