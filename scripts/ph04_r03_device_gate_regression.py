from pathlib import Path
root = Path(__file__).resolve().parents[1]
accept = (root / 'scripts' / 'VC-ANDROID-PH04-R03-ACCEPTANCE.cmd').read_text(encoding='utf-8')
device = (root / 'scripts' / 'VC-ANDROID-DEVICE-INSTRUMENTATION.ps1').read_text(encoding='utf-8')
checks = {
    'androidTest APK is built in broad sweep': ':app:assembleDebugAndroidTest' in accept,
    'fragile Gradle all-device instrumentation removed': ':app:connectedDebugAndroidTest' not in accept,
    'explicit healthy-device runner is called': 'VC-ANDROID-DEVICE-INSTRUMENTATION.ps1' in accept,
    'adb device lines parsed by tab': '-split "`t", 2' in device,
    'unhealthy adb SDK query has timeout': 'TimeoutSeconds 8' in device,
    'minimum supported API enforced': '$sdk -lt 26' in device,
    'non-mDNS transport preferred': 'transportRank' in device,
    'app APK path is explicit': "app\\build\\outputs\\apk\\debug\\app-debug.apk" in device,
    'test APK path is explicit': "app\\build\\outputs\\apk\\androidTest\\debug\\app-debug-androidTest.apk" in device,
    'APK install targets selected serial': "@('-s', $device.Serial, 'install'" in device,
    'runner discovery targets selected serial': "@('-s', $device.Serial, 'shell', 'pm', 'list', 'instrumentation')" in device,
    'instrumentation targets selected serial': "@('-s', $device.Serial, 'shell', 'am', 'instrument'" in device,
    'instrumentation rejects failures': 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=Process crashed' in device,
    'instrumentation requires nonzero executed tests': '[int]$match.Groups[1].Value -lt 1' in device,
    'no-device condition is distinguished from test failure': 'exit 2' in device,
}
failed = [name for name, ok in checks.items() if not ok]
for name, ok in checks.items():
    print(f"[{'PASS' if ok else 'FAIL'}] {name}")
if failed:
    raise SystemExit(1)
print(f"VC-ANDROID-PH04-R03 device-gate regression: {len(checks)}/{len(checks)} PASS")
