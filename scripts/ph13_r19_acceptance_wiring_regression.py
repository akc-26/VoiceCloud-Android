from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
c=(ROOT/'scripts/VC-ANDROID-PH13-R19-ACCEPTANCE.cmd').read_text(encoding='utf-8')
b=(ROOT/'scripts/VC-ANDROID-PH13-R19-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
d=(ROOT/'scripts/VC-ANDROID-PH13-R19-SOURCE-DIAGNOSTIC.ps1').read_text(encoding='utf-8')
checks={
'R19 source diagnostics wired':'VC-ANDROID-PH13-R19-SOURCE-DIAGNOSTIC.ps1' in c,
'R19 full isolated build wired':'VC-ANDROID-PH13-R19-ISOLATED-BUILD.ps1' in c,
'R19 delivery integrity wired':'ph13_r19_delivery_integrity.py' in c,
'R19 video/product architecture regression wired':'ph13_r19_video_product_regression.py' in d,
'R19 product preservation wired':'ph13_r19_product_preservation_regression.py' in d,
'R19 compile surface wired':'ph13_r19_compile_surface_regression.py' in d,
'R18 component-signature closure retained':'ph13_r18_component_signature_regression.py' in d,
'R16 approved-design fidelity retained':'ph13_r16_design_fidelity_regression.py' in d,
'Python UTF-8 mode forced by CMD':'set "PYTHONUTF8=1"' in c,
'Debug compile mandatory':'[GATE 1] compileDebugKotlin' in b,
'Staging compile mandatory':'[GATE 2] compileStagingKotlin' in b,
'Release compile mandatory':'[GATE 3] compileReleaseKotlin' in b,
'Unit tests mandatory':'[GATE 4] Unit tests' in b,
'Lint mandatory':'[GATE 5] Lint' in b,
'Assemblies mandatory':'[GATE 6] Assemblies' in b,
'Physical-device instrumentation mandatory':'[GATE 7] Physical-device instrumentation' in b,
'Device pending distinct':'exit 2' in b and 'VC_DEVICE_PENDING' in c,
'Full acceptance only after device success':'FULL ACCEPTANCE' in c,
}
failed=[]
for k,v in checks.items(): print('[PASS]' if v else '[FAIL]',k); failed += [] if v else [k]
if failed: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R19 acceptance wiring: {len(checks)}/{len(checks)} PASS')
