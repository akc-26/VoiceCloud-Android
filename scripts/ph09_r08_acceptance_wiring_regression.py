from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
acc=(ROOT/'scripts/VC-ANDROID-PH09-R08-ACCEPTANCE.cmd').read_text(encoding='utf-8')
checks=0
def ok(label,cond):
    global checks
    if not cond: raise SystemExit(f'[FAIL] {label}')
    checks+=1; print(f'[PASS] {label}')
ok('acceptance identifies PH09 R08', 'VoiceCloud Android PH09 R08 - Full Acceptance' in acc)
ok('acceptance has no superseded PH09 R01-R07 python entrypoints', not re.search(r'python scripts\\ph09_r0[1-7]_', acc, re.I))
required=[
 'ph09_r08_acceptance_wiring_regression.py','ph09_r08_powershell_surface_regression.py','ph09_r08_source_check.py','ph09_r08_product_quality_regression.py','ph09_r08_security_authority_regression.py',
 'ph09_r08_gradle_properties_authority.py','ph09_r08_windows_preflight_regression.py','ph09_r08_preservation_regression.py','ph09_r08_compile_surface_regression.py','ph09_r08_compile_risk_regression.py',
 'ph09_r08_device_closure_regression.py','ph09_r08_warning_closure_regression.py',
 'ph08_r02_payment_regression.py','ph08_r02_compile_risk_regression.py','ph07_r03_adb_daemon_regression.py','ph06_r03_hosting_compile_regression.py']
for name in required: ok(f'acceptance dependency exists: {name}', (ROOT/'scripts'/name).exists())
ok('PowerShell parser checker exists', (ROOT/'scripts/VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1').exists())
ok('acceptance requires Windows PowerShell', 'where powershell' in acc.lower())
ok('acceptance executes real PowerShell parser gate', 'VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1' in acc)
parser_pos=acc.index('VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1')
python_pos=acc.index('python scripts\\ph09_r08_acceptance_wiring_regression.py')
ok('PowerShell parser gate runs before Python/static regressions', parser_pos < python_pos)
ordered=[
 'ph09_r08_powershell_surface_regression.py','ph09_r08_gradle_properties_authority.py','ph09_r08_windows_preflight_regression.py','ph09_r08_preservation_regression.py',
 'ph09_r08_compile_surface_regression.py','ph09_r08_compile_risk_regression.py','ph09_r08_warning_closure_regression.py','ph09_r08_device_closure_regression.py']
pos=[acc.index(x) for x in ordered]
ok('PowerShell, authority, compile, warning and device closure gates run before Gradle compilation', pos==sorted(pos))
compile_markers=['[GATE 1] compileDebugKotlin','[GATE 2] compileStagingKotlin','[GATE 3] compileReleaseKotlin','[GATE 4] Unit tests','[GATE 5] Lint','[GATE 6] Assemblies','[GATE 7] Physical-device instrumentation']
positions=[acc.index(x) for x in compile_markers]
ok('mandatory Gradle/device gates remain in locked order', positions==sorted(positions))
ok('acceptance invokes current R08 device closure regression', 'ph09_r08_device_closure_regression.py' in acc)
ok('acceptance invokes current R08 warning closure regression', 'ph09_r08_warning_closure_regression.py' in acc)
ok('acceptance final success label is R08', '[PASS] VC-ANDROID-PH09-R08 acceptance commands completed successfully.' in acc)
print(f'[PASS] VC-ANDROID-PH09-R08 acceptance wiring regression: {checks}/{checks} PASS')
