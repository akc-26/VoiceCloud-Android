param(
    [Parameter(Mandatory=$false)]
    [string]$OutputPath = (Join-Path (Split-Path $PSScriptRoot -Parent | Split-Path -Parent) 'VoiceCloud-Android-VC-ANDROID-PH04-R01.zip')
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$stage = Join-Path ([IO.Path]::GetTempPath()) ("voicecloud-package-" + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $stage | Out-Null

$excludeDirs = @('.git','.gradle','.idea','build','captures','.externalNativeBuild','.cxx','__pycache__')
$excludeFiles = @('local.properties','.DS_Store','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar')

try {
    Get-ChildItem -LiteralPath $root -Force | ForEach-Object {
        if ($_.PSIsContainer) {
            if ($excludeDirs -contains $_.Name) { return }
            $dest = Join-Path $stage $_.Name
            Copy-Item -LiteralPath $_.FullName -Destination $dest -Recurse -Force
        } else {
            if ($excludeFiles -contains $_.Name -or $_.Name -like '*.iml' -or $_.Name -like '*.jks' -or $_.Name -like '*.keystore' -or $_.Name -like '*.zip') { return }
            Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $stage $_.Name) -Force
        }
    }

    # Remove forbidden directories/files that may exist below copied module trees.
    Get-ChildItem -LiteralPath $stage -Directory -Recurse -Force | Sort-Object FullName -Descending | ForEach-Object {
        if ($excludeDirs -contains $_.Name) { Remove-Item -LiteralPath $_.FullName -Recurse -Force }
    }
    Get-ChildItem -LiteralPath $stage -File -Recurse -Force | ForEach-Object {
        if ($excludeFiles -contains $_.Name -or $_.Name -like '*.iml' -or $_.Name -like '*.jks' -or $_.Name -like '*.keystore' -or $_.Name -like '*.zip') {
            Remove-Item -LiteralPath $_.FullName -Force
        }
    }

    if (Test-Path -LiteralPath $OutputPath) { Remove-Item -LiteralPath $OutputPath -Force }
    # IMPORTANT: archive the CONTENTS of the staging directory, not the project directory itself.
    # This prevents Downloads\Project\Project\... after normal Windows extraction.
    Compress-Archive -Path (Join-Path $stage '*') -DestinationPath $OutputPath -CompressionLevel Optimal

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [IO.Compression.ZipFile]::OpenRead($OutputPath)
    try {
        $names = @($zip.Entries | ForEach-Object FullName)
        if (-not ($names -contains 'settings.gradle.kts')) { throw 'Package layout invalid: settings.gradle.kts is not at ZIP root.' }
        $bad = $names | Where-Object { $_ -match '(^|/)(\.git|\.gradle|\.idea|build|captures|\.externalNativeBuild|\.cxx|__pycache__)(/|$)' -or $_ -match '(^|/)(local\.properties|secrets\.properties|gradlew|gradlew\.bat|gradle-wrapper\.jar)$' }
        if ($bad) { throw "Package contains generated/machine-specific artifacts: $($bad -join ', ')" }
    } finally {
        $zip.Dispose()
    }

    Write-Host "[PASS] Clean ZIP created with project files at archive root: $OutputPath"
} finally {
    if (Test-Path -LiteralPath $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
}
