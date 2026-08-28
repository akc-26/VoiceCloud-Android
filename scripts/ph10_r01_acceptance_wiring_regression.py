from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
cmd=(ROOT/'scripts/VC-ANDROID-PH10-R01-ACCEPTANCE.cmd').read_text(encoding='utf-8')
checks=[]
def ck(name,ok): checks.append(ok); print(('[PASS] ' if ok else '[FAIL] ')+name)
ck('acceptance identifies PH10 R01', 'VoiceCloud Android PH10 R01 - Full Acceptance' in cmd)
ck('acceptance pins exact PH09-R08 Git parent', '10e820a005d363643bcfc5568e661de96a41dbc9' in cmd)
for script in [
 'VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1',
 'ph10_r01_acceptance_wiring_regression.py','ph10_r01_source_check.py','ph10_r01_portal_authority_regression.py','ph10_r01_compile_surface_regression.py','ph10_r01_compile_risk_regression.py','ph10_r01_preservation_regression.py',
 'ph09_r08_product_quality_regression.py','ph09_r08_security_authority_regression.py','ph09_r08_gradle_properties_authority.py','ph09_r08_windows_preflight_regression.py','ph09_r08_preservation_regression.py','ph09_r08_compile_surface_regression.py','ph09_r08_compile_risk_regression.py','ph09_r08_warning_closure_regression.py','ph09_r08_device_closure_regression.py',
 'ph08_r02_payment_regression.py','ph08_r02_compile_risk_regression.py','ph07_r03_adb_daemon_regression.py','ph06_r03_hosting_compile_regression.py',
]: ck(f'acceptance dependency exists: {script}', (ROOT/'scripts'/script).exists())
for name in ['ph10_r01_source_check.py','ph10_r01_portal_authority_regression.py','ph10_r01_compile_surface_regression.py','ph10_r01_compile_risk_regression.py','ph10_r01_preservation_regression.py']:
    ck(f'acceptance invokes current PH10 gate: {name}', name in cmd)
positions=[cmd.find('[GATE 1] compileDebugKotlin'),cmd.find('[GATE 2] compileStagingKotlin'),cmd.find('[GATE 3] compileReleaseKotlin'),cmd.find('[GATE 4] Unit tests'),cmd.find('[GATE 5] Lint'),cmd.find('[GATE 6] Assemblies'),cmd.find('[GATE 7] Physical-device instrumentation')]
ck('mandatory compile/test/lint/assembly/device gates are present in locked order', all(x>=0 for x in positions) and positions==sorted(positions))
first_gradle=positions[0]
ck('all PH10 source/portal/compile/preservation gates run before Gradle', all(cmd.find(x)<first_gradle for x in ['ph10_r01_source_check.py','ph10_r01_portal_authority_regression.py','ph10_r01_compile_surface_regression.py','ph10_r01_compile_risk_regression.py','ph10_r01_preservation_regression.py']))
ck('PowerShell parser runs before Python/Gradle', cmd.find('VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1') < cmd.find('ph10_r01_acceptance_wiring_regression.py') < first_gradle)
ck('acceptance final success label is PH10 R01', '[PASS] VC-ANDROID-PH10-R01 acceptance commands completed successfully.' in cmd)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH10-R01 acceptance wiring regression: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH10-R01 acceptance wiring regression: {passed}/{len(checks)} PASS')
