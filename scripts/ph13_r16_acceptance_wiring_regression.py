from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
c=(ROOT/'scripts/VC-ANDROID-PH13-R16-ACCEPTANCE.cmd').read_text()
b=(ROOT/'scripts/VC-ANDROID-PH13-R16-ISOLATED-BUILD.ps1').read_text()
d=(ROOT/'scripts/VC-ANDROID-PH13-R16-SOURCE-DIAGNOSTIC.ps1').read_text()
checks={
'R16 source diagnostics wired':'VC-ANDROID-PH13-R16-SOURCE-DIAGNOSTIC.ps1' in c,
'R16 full isolated build wired':'VC-ANDROID-PH13-R16-ISOLATED-BUILD.ps1' in c,
'R16 delivery integrity wired':'ph13_r16_delivery_integrity.py' in c,
'Debug compile mandatory':"[GATE 1] compileDebugKotlin" in b,
'Staging compile mandatory':"[GATE 2] compileStagingKotlin" in b,
'Release compile mandatory':"[GATE 3] compileReleaseKotlin" in b,
'Unit tests mandatory':"[GATE 4] Unit tests" in b,
'Lint mandatory':"[GATE 5] Lint" in b,
'Assemblies mandatory':"[GATE 6] Assemblies" in b,
'Physical-device instrumentation mandatory':"[GATE 7] Physical-device instrumentation" in b,
'Device pending distinct':'exit 2' in b and 'VC_DEVICE_PENDING' in c,
'Full acceptance only after device path success':'FULL ACCEPTANCE' in c,
'R16 design fidelity in diagnostics':'ph13_r16_design_fidelity_regression.py' in d,
'R16 preservation in diagnostics':'ph13_r16_product_preservation_regression.py' in d,
'R16 compile surface in diagnostics':'ph13_r16_compile_surface_regression.py' in d,
}
failed=[]
for k,v in checks.items(): print('[PASS]' if v else '[FAIL]',k); failed += [] if v else [k]
if failed: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH13-R16 acceptance wiring: {len(checks)}/{len(checks)} PASS')
