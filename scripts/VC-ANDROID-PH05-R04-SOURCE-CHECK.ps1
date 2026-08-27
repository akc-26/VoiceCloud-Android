$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$brand = Get-Content -LiteralPath (Join-Path $root 'core/designsystem/src/main/java/app/voicecloud/core/designsystem/theme/VoiceCloudBrand.kt') -Raw
$instrumented = Get-Content -LiteralPath (Join-Path $root 'app/src/androidTest/java/app/voicecloud/android/VoiceCloudFoundationInstrumentedTest.kt') -Raw
$branding = Get-Content -LiteralPath (Join-Path $root 'branding/voicecloud-brand.properties') -Raw
$count = 0
function P([string]$name, [bool]$ok) {
    if (-not $ok) { Write-Host "[FAIL] $name"; exit 1 }
    $script:count++
    Write-Host "[PASS] $name"
}
P 'central brand parser uses ARGB Int Color constructor' ($brand -match 'Color\(argb\.toInt\(\)\)')
P 'invalid packed ULong Color constructor removed' (-not ($brand -match 'Color\(argb\.toULong\(\)\)'))
P 'consumer text crash color remains centralized' ($branding -match '(?m)^consumer\.text=#10262E$')
P 'instrumentation exercises centralized brand palette' ($instrumented -match 'centralizedBrandPaletteUsesValidArgbColors')
P 'instrumentation calls Color.toArgb runtime conversion' ($instrumented -match '\.toArgb\(\)')
P 'reported consumer text ARGB is asserted' ($instrumented -match '0xFF10262E\.toInt\(\)')
P 'sapphire ARGB is asserted' ($instrumented -match '0xFF0B7C86\.toInt\(\)')
P 'instrumentation package assertion follows white-label BuildConfig' ($instrumented -match 'BuildConfig\.APPLICATION_ID')
P 'instrumentation no longer hardcodes debug application id' (-not ($instrumented -match '"app\.voicecloud\.android\.debug"'))
Write-Host "VC-ANDROID-PH05-R04 Windows corrective authority: $count/$count PASS"
