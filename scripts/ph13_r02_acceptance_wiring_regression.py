from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
t=(ROOT/'scripts/VC-ANDROID-PH13-R02-ACCEPTANCE.cmd').read_text(encoding='utf-8')
checks=[]
def ck(n,o): checks.append(bool(o)); print(('[PASS] ' if o else '[FAIL] ')+n)
ordered=[
 'ph13_r02_corrective_regression.py','ph13_r02_preservation_regression.py',
 'ph13_r01_source_check.py','ph13_r01_backend_contract_regression.py','ph13_r01_compile_surface_regression.py',
 ':app:compileDebugKotlin',':app:compileStagingKotlin',':app:compileReleaseKotlin','gradlew.bat test',
 'lintDebug lintStaging lintRelease',':app:assembleDebug :app:assembleStaging :app:assembleRelease',
 'VC-ANDROID-DEVICE-INSTRUMENTATION.ps1','ph13_r02_restore_tracked_source.py','ph13_r02_post_build_hygiene.py'
]
pos=[]
for token in ordered:
 i=t.find(token); ck('acceptance includes '+token,i>=0); pos.append(i)
ck('mandatory R02 acceptance order is fail-closed',all(a>=0 and b>a for a,b in zip(pos,pos[1:])))
ck('all Gradle/build gates remain fail-closed',t.count('|| exit /b 1') >= 20)
ck('R02 final PASS marker present','[PASS] VC-ANDROID-PH13-R02 acceptance commands completed successfully.' in t)
if not all(checks): raise SystemExit('[FAIL] PH13-R02 acceptance wiring regression')
print(f'[PASS] VC-ANDROID-PH13-R02 acceptance wiring: {sum(checks)}/{len(checks)} PASS')
