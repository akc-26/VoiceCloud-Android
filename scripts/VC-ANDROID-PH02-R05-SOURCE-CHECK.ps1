$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'VC-ANDROID-PH02-R03-SOURCE-CHECK.ps1')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
$route=[IO.File]::ReadAllText((Join-Path $root 'feature/bootstrap/src/main/java/app/voicecloud/feature/bootstrap/BootstrapRoute.kt'))
$nav=[IO.File]::ReadAllText((Join-Path $root 'app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'))
$foundation=[IO.File]::ReadAllText((Join-Path $root 'app/src/main/java/app/voicecloud/android/di/FoundationModule.kt'))
$count=0
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }
P 'BootstrapRoute ViewModel precedes callback' ($route.Contains('fun BootstrapRoute(viewModel: BootstrapViewModel = hiltViewModel(), onReady:'))
P 'BootstrapRoute trailing-lambda call retained' ($nav.Contains('BootstrapRoute { config ->'))
P 'Rejected R04 provider @param target absent' (-not $foundation.Contains('@param:ApplicationContext context: Context'))
Write-Host "VC-ANDROID-PH02-R05 Windows targeted corrective authority: $script:count/$script:count PASS"
