from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
SCRIPTS=ROOT/'scripts'
checks=0
def ok(label,cond):
    global checks
    if not cond: raise SystemExit(f'[FAIL] {label}')
    checks+=1; print(f'[PASS] {label}')
def text(name): return (SCRIPTS/name).read_text(encoding='utf-8')
ps_files=sorted(SCRIPTS.glob('*.ps1'))
ok('PowerShell surface is non-empty', len(ps_files) >= 10)
# PowerShell treats "$name:" as a scoped-variable token and raises InvalidVariableReferenceWithDrive.
scopes={'env','global','script','local','private','using','variable','function','alias'}
risky=[]
for p in ps_files:
    for lineno,line in enumerate(p.read_text(encoding='utf-8').splitlines(),1):
        for m in re.finditer(r'(?<!\$)\$([A-Za-z_][A-Za-z0-9_]*):', line):
            if m.group(1).lower() not in scopes:
                risky.append(f'{p.name}:{lineno}:${m.group(1)}:')
ok('all PowerShell variable-before-colon interpolation is braced or scope-qualified', not risky)
parser=text('VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1')
ok('real PowerShell parser API is used', '[System.Management.Automation.Language.Parser]::ParseFile' in parser)
ok('parser gate scans every scripts/*.ps1 file', "Get-ChildItem -LiteralPath $PSScriptRoot -Filter '*.ps1'" in parser)
ok('parser gate records parse errors and fails closed', '$parseErrors.Count -gt 0' in parser and 'exit 1' in parser)
ok('parser diagnostics include script line and column', '${line}:${column}' in parser)
device=text('VC-ANDROID-DEVICE-INSTRUMENTATION.ps1')
ok('device failure interpolation braces Android user id before colon', 'Android user ${UserId}: $PackageName' in device)
ok('device success interpolation braces Android user id before colon', '[PASS] $Label package enabled for Android user ${UserId}: $PackageName' in device)
ok('final instrumentation interpolation braces current user before colon', 'Android user ${currentUser}: $($match.Groups[1].Value)' in device)
ok('device process capture drains stdout asynchronously', '$stdoutTask = $process.StandardOutput.ReadToEndAsync()' in device)
ok('device process capture drains stderr asynchronously', '$stderrTask = $process.StandardError.ReadToEndAsync()' in device)
ok('async output draining starts before timed WaitForExit', device.index('ReadToEndAsync()') < device.index('WaitForExit($TimeoutSeconds * 1000)'))
ok('timeout kills and joins child process', 'try { $process.Kill() } catch { }' in device and 'try { $process.WaitForExit() } catch { }' in device)
ok('timeout returns captured diagnostics instead of discarding output', 'StdOut = $stdout' in device and 'Timed out' in device)
ok('device harness remains explicit-user aware', 'get-current-user' in device and "'--user', [string]$currentUser" in device)
ok('device harness does not hard-code debug VoiceCloud application id', 'app.voicecloud.android.debug' not in device)
ok('device harness keeps target-package-not-found as explicit failure', 'Unable to find instrumentation target package' in device)
# Current acceptance must parse scripts before any Python/Gradle work.
acc=(SCRIPTS/'VC-ANDROID-PH09-R08-ACCEPTANCE.cmd').read_text(encoding='utf-8')
ok('R08 acceptance invokes parser gate', 'VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1' in acc)
ok('R08 parser gate precedes first Python regression', acc.index('VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1') < acc.index('python scripts\\ph09_r08_acceptance_wiring_regression.py'))
print(f'[PASS] VC-ANDROID-PH09-R08 PowerShell surface regression: {checks}/{checks} PASS')
