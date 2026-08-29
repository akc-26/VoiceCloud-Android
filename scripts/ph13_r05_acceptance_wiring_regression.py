from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
accept=(ROOT/'scripts/VC-ANDROID-PH13-R05-ACCEPTANCE.cmd').read_text(encoding='utf-8')
diag=(ROOT/'scripts/VC-ANDROID-PH13-R05-SOURCE-DIAGNOSTIC.ps1').read_text(encoding='utf-8')
build=(ROOT/'scripts/VC-ANDROID-PH13-R05-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
checks=[]
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
for token in ['VC-ANDROID-PH13-R05-SOURCE-DIAGNOSTIC.ps1','VC-ANDROID-JAVA-ENV.cmd','VC-ANDROID-SDK-ENV.cmd','VC-ANDROID-PH13-R05-ISOLATED-BUILD.ps1','ph13_r05_delivery_hygiene.py']:
    ck(f'R05 acceptance includes {token}',token in accept)
for token in ['ph13_r05_uiux_preservation_regression.py','ph13_r01_backend_contract_regression.py','ph13_r01_compile_surface_regression.py','ph11_r02_titlecase_toast_regression.py','ph06_r03_hosting_compile_regression.py']:
    ck(f'R05 source diagnostic includes {token}',token in diag)
for gate in ['[GATE 1] compileDebugKotlin','[GATE 2] compileStagingKotlin','[GATE 3] compileReleaseKotlin','[GATE 4] Unit tests','[GATE 5] Lint','[GATE 6] Assemblies','[GATE 7] Physical-device instrumentation']:
    ck(f'R05 isolated build includes {gate}',gate in build)
ck('R05 uses isolated workspace', 'VoiceCloud-PH13-R05-' in build and 'robocopy' in build)
ck('R05 Gradle invocations disable daemon', "'--no-daemon'" in build)
ck('R05 acceptance has delivery integrity gate', '[GATE 8] Delivery-tree integrity' in accept)
ck('R05 final acceptance is fail-closed', 'exit /b 1' in accept and '[PASS] VC-ANDROID-PH13-R05 acceptance commands completed successfully.' in accept)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R05 acceptance wiring: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R05 acceptance wiring: {passed}/{len(checks)} PASS')
