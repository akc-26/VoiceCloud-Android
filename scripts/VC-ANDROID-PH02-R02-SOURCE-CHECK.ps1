$ErrorActionPreference='Stop'
$root=Resolve-Path (Join-Path $PSScriptRoot '..')
$script:count=0
function T([string]$p){ [IO.File]::ReadAllText((Join-Path $root $p)) }
function P([string]$n,[bool]$c){ if(-not $c){Write-Host "[FAIL] $n";exit 1};$script:count++;Write-Host "[PASS] $n" }

# Preserve every PH02-R01 Windows source authority invariant first.
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'VC-ANDROID-PH02-R01-SOURCE-CHECK.ps1')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$app=T 'app/build.gradle.kts'
P 'Gradle Kotlin DSL imports java.net.URI explicitly' $app.Contains('import java.net.URI')
P 'Gradle Kotlin DSL avoids shadow-prone java.net.URI qualification' (-not $app.Contains('java.net.URI('))
P 'centralized webHost helper uses imported URI type' $app.Contains('fun webHost(url: String): String = URI(url).host')
P 'default manifest placeholder uses webHost helper' $app.Contains('defaultConfig.manifestPlaceholders["voicecloudWebHost"] = webHost(debugWebUrl)')
P 'staging manifest placeholder uses webHost helper' $app.Contains('manifestPlaceholders["voicecloudWebHost"] = webHost(stagingWebUrl)')
P 'release manifest placeholder uses webHost helper' $app.Contains('manifestPlaceholders["voicecloudWebHost"] = webHost(releaseWebUrl)')
Write-Host "VC-ANDROID-PH02-R02 Windows corrective authority: $script:count/$script:count PASS"
