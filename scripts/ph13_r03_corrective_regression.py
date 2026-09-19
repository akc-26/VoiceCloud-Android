from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
p=(ROOT/'scripts/VC-ANDROID-PH13-R03-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
h=(ROOT/'scripts/ph13_r03_delivery_hygiene.py').read_text(encoding='utf-8')
checks=[]
def ck(name, ok): checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
ck('isolated workspace is created under TEMP', '$env:TEMP' in p and 'VoiceCloud-PH13-R03-' in p)
ck('isolated workspace uses unique GUID', '[Guid]::NewGuid()' in p)
ck('copy excludes .gradle/build/cache directories', all(x in p for x in ("'.gradle'","'build'","'__pycache__'")))
ck('copy excludes generated wrapper files', all(x in p for x in ("'gradlew'","'gradlew.bat'","'gradle-wrapper.jar'")))
ck('copy excludes Gradle daemon JVM criteria file', "'gradle-daemon-jvm.properties'" in p)
ck('wrapper bootstrap executes inside isolated workspace', 'Set-Location $workspace' in p and 'BOOTSTRAP-GRADLE-WRAPPER.ps1' in p)
for label, token in [
 ('Debug compile isolated', "':app:compileDebugKotlin'"),('Staging compile isolated', "':app:compileStagingKotlin'"),
 ('Release compile isolated', "':app:compileReleaseKotlin'"),('Unit tests isolated', "'test'"),
 ('Lint isolated', "'lintDebug'"),('Assemblies isolated', "':app:assembleDebugAndroidTest'"),
 ('Device instrumentation isolated','VC-ANDROID-DEVICE-INSTRUMENTATION.ps1')]: ck(label, token in p)
ck('workspace cleanup is guaranteed by finally', 'finally {' in p and 'Remove-Item -LiteralPath $workspace -Recurse -Force' in p)
order_tokens=['[GATE 1] compileDebugKotlin','[GATE 2] compileStagingKotlin','[GATE 3] compileReleaseKotlin','[GATE 4] Unit tests','[GATE 5] Lint','[GATE 6] Assemblies','[GATE 7] Physical-device instrumentation']
ck('isolated Android gate order is Debug-Staging-Release-tests-lint-assemblies-device', all(p.index(a) < p.index(b) for a,b in zip(order_tokens,order_tokens[1:])))
ck('isolated failure path still reaches cleanup', '$exitCode = 1' in p and 'exit $exitCode' in p)
ck('delivery hygiene forbids Gradle daemon JVM file', 'gradle-daemon-jvm.properties' in h)
ck('delivery hygiene remains manifest-strict', 'unmanifested delivery file present' in h)
if not all(checks): raise SystemExit('[FAIL] PH13-R03 corrective regression')
print(f'[PASS] VC-ANDROID-PH13-R03 corrective regression: {sum(checks)}/{len(checks)} PASS')
