$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$accept = Get-Content (Join-Path $PSScriptRoot 'VC-ANDROID-PH04-R03-ACCEPTANCE.cmd') -Raw
$device = Get-Content (Join-Path $PSScriptRoot 'VC-ANDROID-DEVICE-INSTRUMENTATION.ps1') -Raw
$checks = @(
  @('R03 acceptance uses explicit device gate', $accept.Contains('VC-ANDROID-DEVICE-INSTRUMENTATION.ps1')),
  @('R03 builds androidTest APK before device gate', $accept.Contains(':app:assembleDebugAndroidTest')),
  @('R03 no longer invokes Gradle connectedDebugAndroidTest', -not $accept.Contains(':app:connectedDebugAndroidTest')),
  @('device gate validates API >= 26', $device.Contains('$sdk -lt 26')),
  @('device gate explicitly selects adb serial', $device.Contains("@('-s', `$device.Serial")),
  @('device gate installs app APK directly', $device.Contains("'install', '-r', '-t', `$apk")),
  @('device gate resolves instrumentation runner', $device.Contains("'pm', 'list', 'instrumentation'")),
  @('device gate requires at least one executed test', $device.Contains("'OK \((\d+) tests?\)'")),
  @('device gate ignores unhealthy adb entries', $device.Contains('Ignoring unhealthy adb entry')),
  @('PH04 production marker retained', (Get-Content (Join-Path $root 'app\build.gradle.kts') -Raw).Contains('1.0.0-ph04'))
)
$passed = 0
foreach ($check in $checks) {
  if ($check[1]) { Write-Host "[PASS] $($check[0])"; $passed++ } else { Write-Host "[FAIL] $($check[0])"; exit 1 }
}
Write-Host "VC-ANDROID-PH04-R03 Windows corrective authority: $passed/$($checks.Count) PASS"
