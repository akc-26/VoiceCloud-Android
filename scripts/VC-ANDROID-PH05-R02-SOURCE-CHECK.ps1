$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$app = Get-Content -LiteralPath (Join-Path $root 'app/build.gradle.kts') -Raw
$design = Get-Content -LiteralPath (Join-Path $root 'core/designsystem/build.gradle.kts') -Raw
$count = 0
function P([string]$name, [bool]$ok) {
    if (-not $ok) { Write-Host "[FAIL] $name"; exit 1 }
    $script:count++
    Write-Host "[PASS] $name"
}
P 'app resValue feature enabled' ($app -match 'resValues\s*=\s*true')
P 'designsystem resValue feature enabled' ($design -match 'resValues\s*=\s*true')
P 'designsystem Android resources explicitly enabled' ($design -match 'androidResources\s*\{[\s\S]*?enable\s*=\s*true')
P 'invalid AGP9 sourceSets cast removed' (-not ($design -match 'sourceSets\.getByName\("main"\)\.res'))
P 'AGP9 variant resource source API retained' ($design -match 'variant\.sources\.res\?\.addStaticSourceDirectory\("\.\./\.\./branding/res"\)')
P 'central brand splash retained' (Test-Path (Join-Path $root 'branding/res/drawable/vc_brand_splash.xml'))
P 'central brand icon retained' (Test-Path (Join-Path $root 'branding/res/drawable/vc_brand_app_icon_foreground.xml'))
Write-Host "VC-ANDROID-PH05-R02 Windows corrective authority: $count/$count PASS"
