$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$hostingPath = Join-Path $root 'feature\hosting\src\main\java\app\voicecloud\feature\hosting\ui\HostingScreens.kt'
$acceptPath = Join-Path $root 'scripts\VC-ANDROID-PH06-R03-ACCEPTANCE.cmd'
$hostingScreens = Get-Content $hostingPath -Raw
$accept = Get-Content $acceptPath -Raw
$script:count = 0
function P([string]$name, [bool]$ok) {
  if (-not $ok) { Write-Host "[FAIL] $name"; exit 1 }
  $script:count++
  Write-Host "[PASS] $name"
}
P 'reported nullable stage speaker dereference removed' (-not $hostingScreens.Contains('stage?.participants.orEmpty().filter { p -> stage.speakers.none'))
P 'nullable stage speaker snapshot is safe' $hostingScreens.Contains('val currentSpeakers = stage?.speakers.orEmpty()')
P 'nullable stage participant iteration is safe' $hostingScreens.Contains('stage?.participants.orEmpty()')
P 'audience filtering uses safe speaker snapshot' $hostingScreens.Contains('currentSpeakers.none { speaker -> speaker.userId == participant.userId }')
P 'R03 acceptance retains all three app compile variants' ($accept.Contains(':app:compileDebugKotlin') -and $accept.Contains(':app:compileStagingKotlin') -and $accept.Contains(':app:compileReleaseKotlin'))
P 'R03 acceptance retains R02 premium UI regression' $accept.Contains('ph06_r02_premium_ui_regression.py')
Write-Host "VC-ANDROID-PH06-R03 Windows corrective authority: $script:count/$script:count PASS"
