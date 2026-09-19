from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; p=ROOT/'scripts/VC-ANDROID-PH12-R01-ACCEPTANCE.cmd'
if not p.exists(): raise SystemExit('[FAIL] PH12 acceptance runner missing')
t=p.read_text(encoding='utf-8'); checks=[]
def ck(n,o): checks.append(bool(o)); print(('[PASS] ' if o else '[FAIL] ')+n)
for s in ['ph12_r01_source_check.py','ph12_r01_backend_contract_regression.py','ph12_r01_compile_surface_regression.py','ph12_r01_unit_contract_regression.py','ph12_r01_preservation_regression.py']: ck('runner wires '+s,s in t)
order=[':app:compileDebugKotlin',':app:compileStagingKotlin',':app:compileReleaseKotlin','call gradlew.bat test','lintDebug lintStaging lintRelease',':app:assembleDebug :app:assembleStaging :app:assembleRelease','VC-ANDROID-DEVICE-INSTRUMENTATION.ps1','ph12_r01_package_hygiene.py']
pos=[t.find(x) for x in order]; ck('mandatory acceptance gates all present',all(x>=0 for x in pos)); ck('mandatory acceptance gates are ordered',pos==sorted(pos))
ck('runner bootstraps Java SDK and wrapper','VC-ANDROID-JAVA-ENV.cmd' in t and 'VC-ANDROID-SDK-ENV.cmd' in t and 'BOOTSTRAP-GRADLE-WRAPPER.cmd' in t)
ck('runner fails closed on Gradle gate errors','|| exit /b 1' in t)
if sum(checks)!=len(checks): raise SystemExit(f'[FAIL] PH12 acceptance wiring: {sum(checks)}/{len(checks)} PASS')
print(f'[PASS] VC-ANDROID-PH12-R01 acceptance wiring: {sum(checks)}/{len(checks)} PASS')
