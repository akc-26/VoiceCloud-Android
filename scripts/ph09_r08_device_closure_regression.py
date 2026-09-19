from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
s=(ROOT/'scripts/VC-ANDROID-DEVICE-INSTRUMENTATION.ps1').read_text(encoding='utf-8')
checks=[]
def ck(label, cond):
    checks.append((label,bool(cond)))

ck('device gate derives Debug app identity from AGP output metadata', "app\\build\\outputs\\apk\\debug\\output-metadata.json" in s and 'Read-ApkOutputMetadata' in s)
ck('device gate derives Debug androidTest APK from AGP output metadata', "app\\build\\outputs\\apk\\androidTest\\debug\\output-metadata.json" in s)
ck('device gate does not hard-code VoiceCloud debug target package', 'app.voicecloud.android.debug' not in s)
ck('device gate resolves foreground Android user', "'am', 'get-current-user'" in s)
ck('foreground Android user must parse as a non-negative integer', '[int]::TryParse' in s and '$currentUser -lt 0' in s)
ck('app and test APK installs explicitly target the foreground user', "'install', '--user', [string]$currentUser, '-r', '-t'" in s)
ck('app package is verified enabled for the same Android user', 'Assert-PackageEnabledForUser' in s and "'pm', 'list', 'packages', '-e', '--user'" in s)
ck('test package is verified enabled for the same Android user', "-PackageName $runnerPackage -Label 'Debug androidTest'" in s)
ck('runner is discovered dynamically from installed instrumentation', "'pm', 'list', 'instrumentation'" in s and '$candidateRunner' in s)
ck('runner target must equal AGP-derived app applicationId', '$candidateTarget -eq $appArtifact.ApplicationId' in s)
ck('runner package is cross-checked with test metadata when available', "$runnerPackage -ne $testArtifact.ApplicationId" in s)
ck('instrumentation executes for the explicit foreground Android user', "'am', 'instrument', '--user', [string]$currentUser, '-w', $runner" in s)
ck('device gate identifies itself as Gate 7', '[GATE 7] Instrumentation on selected device/user only' in s)
ck('stale Gate 6 instrumentation label is removed', '[GATE 6] Instrumentation on selected device only' not in s)
ck('target-package-not-found is a first-class rejected failure', 'Unable to find instrumentation target package' in s)
ck('instrumentation failure diagnostics include app/test package state', '[INFO] App package state:' in s and '[INFO] Test package state:' in s)
ck('device gate still requires at least one executed test', "OK \\((\\d+) tests?\\)" in s and 'Value -lt 1' in s)
ck('no healthy device remains a distinct nonfatal code 2', 'exit 2' in s and 'No healthy authorized Android device' in s)
ck('wired/wireless candidate health and minimum API checks remain', '$sdk -lt 26' in s and 'transportRank' in s and 'TimeoutSeconds 8' in s)
ck('APK path comes from output metadata rather than fixed filenames', 'elements[0].outputFile' in s and "app-debug.apk'" not in s and "app-debug-androidTest.apk'" not in s)

ck('device PowerShell interpolation braces UserId before colon', 'Android user ${UserId}: $PackageName' in s)
ck('device PowerShell interpolation braces currentUser before colon', 'Android user ${currentUser}: $($match.Groups[1].Value)' in s)
ck('device process capture drains redirected output asynchronously', 'ReadToEndAsync()' in s and s.index('ReadToEndAsync()') < s.index('WaitForExit($TimeoutSeconds * 1000)'))
failed=[label for label,ok in checks if not ok]
for label,ok in checks: print(f"[{'PASS' if ok else 'FAIL'}] {label}")
if failed: raise SystemExit(1)
print(f'[PASS] VC-ANDROID-PH09-R08 device/instrumentation closure regression: {len(checks)}/{len(checks)} PASS')
