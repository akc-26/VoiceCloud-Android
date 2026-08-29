from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
accept=(ROOT/'scripts/VC-ANDROID-PH13-R08-ACCEPTANCE.cmd').read_text(encoding='utf-8')
build=(ROOT/'scripts/VC-ANDROID-PH13-R08-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
diag=(ROOT/'scripts/VC-ANDROID-PH13-R08-SOURCE-DIAGNOSTIC.ps1')
checks=[]
def check(label, cond):
    checks.append(cond); print('[PASS]' if cond else '[FAIL]', label)
check('R08 source diagnostic wired', 'VC-ANDROID-PH13-R08-SOURCE-DIAGNOSTIC.ps1' in accept)
check('R08 isolated build wired', 'VC-ANDROID-PH13-R08-ISOLATED-BUILD.ps1' in accept)
check('R08 delivery integrity wired', 'ph13_r08_delivery_integrity.py' in accept)
check('full UI compile-risk regression wired', diag.exists() and 'ph13_r08_full_ui_compile_risk_regression.py' in diag.read_text(encoding='utf-8'))
check('product preservation regression wired', diag.exists() and 'ph13_r08_product_preservation_regression.py' in diag.read_text(encoding='utf-8'))
for gate, task in [('1',':app:compileDebugKotlin'),('2',':app:compileStagingKotlin'),('3',':app:compileReleaseKotlin')]:
    check(f'compile gate {gate} retained', f"[GATE {gate}]" in build and task in build)
check('debug compile uses --continue to expose independent module errors in one pass', "@(':app:compileDebugKotlin','--continue')" in build)
check('staging compile uses --continue', "@(':app:compileStagingKotlin','--continue')" in build)
check('release compile uses --continue', "@(':app:compileReleaseKotlin','--continue')" in build)
check('unit tests retained', "[GATE 4] Unit tests" in build and "@('test')" in build)
check('lint retained', "[GATE 5] Lint" in build and 'lintDebug' in build and 'lintStaging' in build and 'lintRelease' in build)
check('assemblies retained', "[GATE 6] Assemblies" in build and ':app:assembleDebug' in build and ':app:assembleStaging' in build and ':app:assembleRelease' in build)
check('device gate retained', '[GATE 7] Physical-device instrumentation' in build)
check('verified local wrapper required', "distributionUrl=file\\:///" in build)
check('dependent gates stop after mandatory compile failure', 'if ($script:compileGatesClean)' in build)
check('final acceptance fail-closed', 'exit /b 1' in accept)
if not all(checks): raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R08 acceptance wiring: {sum(checks)}/{len(checks)} PASS')
