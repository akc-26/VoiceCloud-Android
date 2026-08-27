$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$main = Get-Content -LiteralPath (Join-Path $root 'app/src/main/java/app/voicecloud/android/MainActivity.kt') -Raw
$app = Get-Content -LiteralPath (Join-Path $root 'app/build.gradle.kts') -Raw
$count = 0
function P([string]$name, [bool]$ok) {
    if (-not $ok) { Write-Host "[FAIL] $name"; exit 1 }
    $script:count++
    Write-Host "[PASS] $name"
}
P 'SplashScreen dependency retained' ($app -match 'implementation\(libs\.androidx\.core\.splashscreen\)')
P 'correct SplashScreen companion import retained' ($main -match 'import androidx\.core\.splashscreen\.SplashScreen\.Companion\.installSplashScreen')
P 'invalid package-level SplashScreen import removed' (-not ($main -match 'import androidx\.core\.splashscreen\.installSplashScreen'))
P 'installSplashScreen invocation retained' ($main -match 'installSplashScreen\(\)')
Write-Host "VC-ANDROID-PH05-R03 Windows corrective authority: $count/$count PASS"
