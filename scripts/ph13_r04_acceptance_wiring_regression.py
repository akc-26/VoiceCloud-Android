from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
a=(ROOT/'scripts/VC-ANDROID-PH13-R04-ACCEPTANCE.cmd').read_text(encoding='utf-8')
s=(ROOT/'scripts/VC-ANDROID-PH13-R04-SOURCE-DIAGNOSTIC.ps1').read_text(encoding='utf-8')
b=(ROOT/'scripts/VC-ANDROID-PH13-R04-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
checks=[]
def ck(name, ok):
    checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
for token in ['VC-ANDROID-PH13-R04-SOURCE-DIAGNOSTIC.ps1','VC-ANDROID-JAVA-ENV.cmd','VC-ANDROID-SDK-ENV.cmd','VC-ANDROID-PH13-R04-ISOLATED-BUILD.ps1','ph13_r04_delivery_hygiene.py']:
    ck(f'R04 acceptance includes {token}', token in a)
ck('R04 command runner has no fail-fast || exit pattern', '|| exit /b 1' not in a)
ck('R04 always reaches Gate 8 after isolated build invocation', a.index('VC-ANDROID-PH13-R04-ISOLATED-BUILD.ps1') < a.index('[GATE 8] Original delivery-tree integrity'))
ck('R04 aggregates failure groups before final exit', 'VC_FAILURE_GROUPS' in a and 'full diagnostic acceptance completed with' in a)
ck('R04 final PASS marker unique', a.count('[PASS] VC-ANDROID-PH13-R04 acceptance commands completed successfully.')==1)
ck('source diagnostic records multiple failures instead of throwing first', 'System.Collections.Generic.List[string]' in s and '[SUMMARY-FAIL]' in s and 'foreach ($scriptName in $pythonChecks)' in s)
for name in ['ph13_r01_source_check.py','ph13_r01_backend_contract_regression.py','ph13_r01_compile_surface_regression.py','ph12_r01_backend_contract_regression.py','ph11_r02_titlecase_toast_regression.py','ph09_r08_device_closure_regression.py','ph07_r03_adb_daemon_regression.py','ph06_r03_hosting_compile_regression.py']:
    ck(f'source diagnostic retains {name}', name in s)
for label in ['[GATE 1] compileDebugKotlin','[GATE 2] compileStagingKotlin','[GATE 3] compileReleaseKotlin','[GATE 4] Unit tests','[GATE 5] Lint','[GATE 6] Assemblies','[GATE 7] Physical-device instrumentation']:
    ck(f'isolated diagnostic retains {label}', label in b)
ck('Android gates aggregate failures', 'System.Collections.Generic.List[string]' in b and '[SUMMARY-FAIL]' in b and 'Add-Failure' in b)
ck('Gradle gate failures do not throw/terminate diagnostic sequence', 'Gradle exited with code' in b and 'throw "$Label failed' not in b)
ck('private Gradle user home isolates daemon/cache lifecycle', 'VoiceCloud-PH13-R04-GRADLE-' in b and 'GRADLE_USER_HOME' in b)
ck('Gradle invocations disable persistent daemon', "'--no-daemon'" in b and '-Dorg.gradle.daemon=false' in b)
ck('isolated Gradle daemons are stopped before cleanup', '--stop' in b and b.index('--stop') < b.index('Remove-TreeRobust -Path $workspace'))
ck('workspace cleanup has bounded retry strategy', 'for ($attempt = 1; $attempt -le 12; $attempt++)' in b and 'rd /s /q' in b)
ck('cleanup problem is reported without hiding other acceptance results', 'Isolated Android workspace cleanup' in b and '[SUMMARY-WARN]' in b)
if not all(checks): raise SystemExit('[FAIL] VC-ANDROID-PH13-R04 acceptance wiring regression')
print(f'[PASS] VC-ANDROID-PH13-R04 acceptance wiring: {sum(checks)}/{len(checks)} PASS')
