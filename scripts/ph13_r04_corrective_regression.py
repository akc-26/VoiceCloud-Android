from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
a=(ROOT/'scripts/VC-ANDROID-PH13-R04-ACCEPTANCE.cmd').read_text(encoding='utf-8')
s=(ROOT/'scripts/VC-ANDROID-PH13-R04-SOURCE-DIAGNOSTIC.ps1').read_text(encoding='utf-8')
b=(ROOT/'scripts/VC-ANDROID-PH13-R04-ISOLATED-BUILD.ps1').read_text(encoding='utf-8')
h=(ROOT/'scripts/ph13_r04_delivery_hygiene.py').read_text(encoding='utf-8') if (ROOT/'scripts/ph13_r04_delivery_hygiene.py').exists() else ''
checks=[]
def ck(name, ok):
    checks.append(bool(ok)); print(('[PASS] ' if ok else '[FAIL] ')+name)
ck('R04 solves R03 fail-fast discovery defect', '|| exit /b 1' not in a and 'VC_FAILURE_GROUPS' in a)
ck('Gate 8 executes even when isolated build returns nonzero', 'VC-ANDROID-PH13-R04-ISOLATED-BUILD.ps1\n  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1' in a.replace('\r','') and '[GATE 8] Original delivery-tree integrity' in a)
ck('source diagnostics continue through complete Python check list', 'foreach ($scriptName in $pythonChecks)' in s and 'exit 1' in s and s.index('foreach ($scriptName in $pythonChecks)') < s.rindex('exit 1'))
ck('compile Debug/Staging/Release each execute independently', all(token in b for token in ["Invoke-GradleGate -Label '[GATE 1] compileDebugKotlin'","Invoke-GradleGate -Label '[GATE 2] compileStagingKotlin'","Invoke-GradleGate -Label '[GATE 3] compileReleaseKotlin'"]))
ck('tests lint assemblies execute after compile gates regardless of earlier exit codes', all(token in b for token in ["Invoke-GradleGate -Label '[GATE 4] Unit tests'","Invoke-GradleGate -Label '[GATE 5] Lint'","Invoke-GradleGate -Label '[GATE 6] Assemblies'"]))
ck('device gate is attempted after assemblies', b.index('[GATE 6] Assemblies') < b.index('[GATE 7] Physical-device instrumentation'))
ck('R04 uses unique workspace and unique Gradle user home', b.count('[Guid]::NewGuid()')>=1 and 'VoiceCloud-PH13-R04-' in b and 'VoiceCloud-PH13-R04-GRADLE-' in b)
ck('R04 stops Gradle before deleting isolated workspace', '--stop' in b and 'Remove-TreeRobust' in b)
ck('R04 cleanup retries both PowerShell and cmd removal', 'Remove-Item -LiteralPath $Path -Recurse -Force' in b and 'rd /s /q' in b)
ck('R04 cleanup warning is separated from product/source failures', '[GATE-WARN]' in b and '[GATE-FAIL]' in b)
ck('R04 final result remains fail-closed for genuine gate failures', 'if ($failures.Count -gt 0)' in b and 'exit 1' in b and 'if not "%VC_FAILURE_GROUPS%"=="0"' in a)
ck('R04 retains strict original delivery hygiene after all diagnostics', 'ph13_r04_delivery_hygiene.py' in a)
if h:
    ck('R04 delivery hygiene forbids build/cache/generated wrapper material', all(x in h for x in ["'.gradle'","'build'","'gradlew'","'gradle-daemon-jvm.properties'"]))
if not all(checks): raise SystemExit('[FAIL] VC-ANDROID-PH13-R04 corrective regression')
print(f'[PASS] VC-ANDROID-PH13-R04 corrective regression: {sum(checks)}/{len(checks)} PASS')
