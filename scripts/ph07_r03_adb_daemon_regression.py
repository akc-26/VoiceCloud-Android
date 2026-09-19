from pathlib import Path
p = Path(__file__).with_name('VC-ANDROID-DEVICE-INSTRUMENTATION.ps1')
s = p.read_text(encoding='utf-8')
checks = {
    'adb devices uses captured process helper': "Invoke-CapturedProcess -FilePath $adb -Arguments @('devices')" in s,
    'normal adb stderr is informational': '[INFO] adb:' in s,
    'direct PowerShell adb devices invocation removed': '& $adb devices 2>&1' not in s,
    'no-device exit remains nonfatal code 2': "exit 2" in s and 'No healthy authorized Android device' in s,
}
failed=[]
for name, ok in checks.items():
    print(('[PASS] ' if ok else '[FAIL] ') + name)
    if not ok: failed.append(name)
if failed:
    raise SystemExit(1)
print(f"VC-ANDROID-PH07-R03 adb daemon regression: {len(checks)}/{len(checks)} PASS")
