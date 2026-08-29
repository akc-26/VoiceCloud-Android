from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
accept=(ROOT/'scripts/VC-ANDROID-PH13-R06-ACCEPTANCE.cmd').read_text(encoding='utf-8')
diag=(ROOT/'scripts/VC-ANDROID-PH13-R06-SOURCE-DIAGNOSTIC.ps1').read_text(encoding='utf-8')
build=(ROOT/'scripts/VC-ANDROID-PH13-R06-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
checks=[]
def ck(name,ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
for token in ['VC-ANDROID-PH13-R06-SOURCE-DIAGNOSTIC.ps1','VC-ANDROID-JAVA-ENV.cmd','VC-ANDROID-SDK-ENV.cmd','VC-ANDROID-PH13-R06-ISOLATED-BUILD.ps1','ph13_r06_delivery_integrity.py']:
    ck(f'R06 acceptance includes {token}',token in accept)
for token in ['ph13_r06_harness_correction_regression.py','ph13_r06_product_preservation_regression.py','ph13_r01_backend_contract_regression.py','ph13_r01_compile_surface_regression.py','ph11_r02_titlecase_toast_regression.py','ph06_r03_hosting_compile_regression.py']:
    ck(f'R06 source diagnostic includes {token}',token in diag)
for gate in ['[GATE 1] compileDebugKotlin','[GATE 2] compileStagingKotlin','[GATE 3] compileReleaseKotlin','[GATE 4] Unit tests','[GATE 5] Lint','[GATE 6] Assemblies','[GATE 7] Physical-device instrumentation']:
    ck(f'R06 isolated build includes {gate}',gate in build)
ck('R06 uses isolated workspace', 'VoiceCloud-PH13-R06-' in build and 'robocopy' in build)
ck('R06 wrapper runtime is verified local before compile', "Contains('distributionUrl=file\\:///')" in build and '$script:buildReady = $true' in build)
ck('R06 Gradle invocations disable daemon', "'--no-daemon'" in build)
ck('R06 acceptance has product/delivery integrity gate', '[GATE 8] Product/delivery integrity' in accept)
ck('R06 final acceptance is fail-closed', 'exit /b 1' in accept and '[PASS] VC-ANDROID-PH13-R06 acceptance commands completed successfully.' in accept)
passed=sum(checks)
if passed!=len(checks): raise SystemExit(f'[FAIL] VC-ANDROID-PH13-R06 acceptance wiring: {passed}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH13-R06 acceptance wiring: {passed}/{len(checks)} PASS')
