from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
cmd=(ROOT/'scripts/VC-ANDROID-PH11-R02-ACCEPTANCE.cmd').read_text(encoding='utf-8')
checks=[]
def ck(label,ok):
    if not ok: raise SystemExit(f'[FAIL] {label}')
    checks.append(label); print(f'[PASS] {label}')
ck('acceptance identifies PH11 R02','VoiceCloud Android PH11 R02 - Full Acceptance' in cmd)
ck('acceptance pins exact PH10-R01 Git parent','999a3c310c9e40bf1a47972551050bfd5b00e54d' in cmd)
required=[
 'VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1','ph11_r02_acceptance_wiring_regression.py','ph11_r02_unit_contract_regression.py','ph11_r02_source_check.py','ph11_r02_backend_contract_regression.py','ph11_r02_titlecase_toast_regression.py','ph11_r02_corrections_regression.py','ph11_r02_compile_surface_regression.py','ph11_r02_compile_risk_regression.py','ph11_r02_economy_payment_regression.py','ph11_r02_preservation_regression.py',
 'ph09_r08_gradle_properties_authority.py','ph09_r08_compile_risk_regression.py','ph09_r08_warning_closure_regression.py','ph09_r08_device_closure_regression.py','ph07_r03_adb_daemon_regression.py','ph06_r03_hosting_compile_regression.py']
for dep in required:
    ck(f'acceptance dependency exists: {dep}', (ROOT/'scripts'/dep).exists())
for dep in [x for x in required if x.startswith('ph11_r02_')]:
    ck(f'acceptance invokes current PH11 R02 gate: {dep}', dep in cmd)
locked=[':app:compileDebugKotlin',':app:compileStagingKotlin',':app:compileReleaseKotlin','gradlew.bat test','lintDebug lintStaging lintRelease',':app:assembleDebug :app:assembleStaging :app:assembleRelease :app:assembleDebugAndroidTest','VC-ANDROID-DEVICE-INSTRUMENTATION.ps1']
pos=[cmd.find(x) for x in locked]
ck('mandatory build/device gates remain in locked order', all(x>=0 for x in pos) and pos==sorted(pos))
first=pos[0]
ck('all PH11 R02 authority gates run before Gradle', all(cmd.find(x)<first for x in [x for x in required if x.startswith('ph11_r02_')]))
ck('PowerShell parser executes before Python and Gradle',cmd.find('VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1')<cmd.find('ph11_r02_acceptance_wiring_regression.py')<first)
ck('acceptance final success label is PH11 R02','[PASS] VC-ANDROID-PH11-R02 acceptance commands completed successfully.' in cmd)
print(f'[PASS] VC-ANDROID-PH11-R02 acceptance wiring regression: {len(checks)}/{len(checks)} PASS')
