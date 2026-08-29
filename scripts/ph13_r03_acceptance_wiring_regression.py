from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
a=(ROOT/'scripts/VC-ANDROID-PH13-R03-ACCEPTANCE.cmd').read_text(encoding='utf-8')
checks=[]
def ck(name, ok):
    checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
required=[
 'ph13_r03_corrective_regression.py','ph13_r03_preservation_regression.py','ph13_r01_source_check.py',
 'ph13_r01_backend_contract_regression.py','ph13_r01_compile_surface_regression.py',
 'VC-ANDROID-PH13-R03-ISOLATED-BUILD.ps1','ph13_r03_delivery_hygiene.py'
]
for token in required: ck(f'acceptance includes {token}', token in a)
ck('Python bytecode generation disabled in original tree', 'PYTHONDONTWRITEBYTECODE=1' in a)
ck('Java environment resolved before isolated build', a.index('VC-ANDROID-JAVA-ENV.cmd') < a.index('VC-ANDROID-PH13-R03-ISOLATED-BUILD.ps1'))
ck('SDK environment resolved before isolated build', a.index('VC-ANDROID-SDK-ENV.cmd') < a.index('VC-ANDROID-PH13-R03-ISOLATED-BUILD.ps1'))
ck('Gate 8 runs only after isolated build', a.index('VC-ANDROID-PH13-R03-ISOLATED-BUILD.ps1') < a.index('[GATE 8] Original delivery-tree integrity'))
ck('R03 no longer runs R02 restore gate', 'ph13_r02_restore_tracked_source.py ||' not in a)
ck('R03 no longer runs R02 post-build workspace classifier', 'ph13_r02_post_build_hygiene.py ||' not in a)
ck('final R03 PASS marker is unique', a.count('[PASS] VC-ANDROID-PH13-R03 acceptance commands completed successfully.')==1)
if not all(checks): raise SystemExit('[FAIL] PH13-R03 acceptance wiring regression')
print(f'[PASS] VC-ANDROID-PH13-R03 acceptance wiring: {sum(checks)}/{len(checks)} PASS')
