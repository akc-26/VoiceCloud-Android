from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
acc=(ROOT/'scripts/VC-ANDROID-PH13-R20-ACCEPTANCE.cmd').read_text(encoding='utf-8')
bld=(ROOT/'scripts/VC-ANDROID-PH13-R20-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
diag=(ROOT/'scripts/VC-ANDROID-PH13-R20-SOURCE-DIAGNOSTIC.ps1').read_text(encoding='utf-8')
checks={
 'R20 source diagnostic wired':'VC-ANDROID-PH13-R20-SOURCE-DIAGNOSTIC.ps1' in acc,
 'R20 isolated full build wired':'VC-ANDROID-PH13-R20-ISOLATED-BUILD.ps1' in acc,
 'R20 integrity wired':'ph13_r20_delivery_integrity.py' in acc,
 'UI root cause regression wired':'ph13_r20_ui_root_cause_regression.py' in diag,
 'product preservation wired':'ph13_r20_product_preservation_regression.py' in diag,
 'component signature closure retained':'ph13_r18_component_signature_regression.py' in diag,
 'expanded compile surface retained':'ph13_r18_compile_surface_regression.py' in diag,
 'approved design fidelity retained':'ph13_r16_design_fidelity_regression.py' in diag,
 'debug compile mandatory':':app:compileDebugKotlin' in bld,
 'staging compile mandatory':':app:compileStagingKotlin' in bld,
 'release compile mandatory':':app:compileReleaseKotlin' in bld,
 'unit tests mandatory':"'test'" in bld,
 'lint all variants mandatory':"'lintDebug','lintStaging','lintRelease'" in bld,
 'assemblies and androidTest mandatory':':app:assembleDebugAndroidTest' in bld,
 'physical device instrumentation mandatory':'Physical-device instrumentation' in bld,
 'device pending remains distinct':'exit 2' in bld,
 'delayed ERRORLEVEL capture':'!ERRORLEVEL!' in acc,
}
fail=0
for name,ok in checks.items():
 print('[PASS]' if ok else '[FAIL]',name)
 fail += 0 if ok else 1
if fail: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R20 acceptance wiring: {len(checks)}/{len(checks)} PASS')
