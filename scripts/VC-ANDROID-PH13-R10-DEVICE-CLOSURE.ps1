$ErrorActionPreference = 'Continue'
$sourceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$productManifest = Join-Path $sourceRoot 'contracts\VC-ANDROID-PH13-R10-R09-PRODUCT-CANONICAL.sha256'
$deviceScript = Join-Path $sourceRoot 'scripts\VC-ANDROID-DEVICE-INSTRUMENTATION.ps1'
$cachedGradleBat = Join-Path $env:USERPROFILE '.voicecloud\gradle-bootstrap\gradle-9.5.0\bin\gradle.bat'
$originalGradleUserHome = $env:GRADLE_USER_HOME
$originalGradleOpts = $env:GRADLE_OPTS

function Test-ProductManifest {
    param([Parameter(Mandatory=$true)][string]$Root)
    if (-not (Test-Path -LiteralPath $productManifest)) { return $false }
    try {
        foreach ($line in (Get-Content -LiteralPath $productManifest)) {
            if ([string]::IsNullOrWhiteSpace($line)) { continue }
            $parts = $line -split '  ', 2
            if ($parts.Count -ne 2) { return $false }
            $expected = $parts[0].Trim().ToLowerInvariant()
            $relative = $parts[1].Trim().Replace('/', '\')
            $path = Join-Path $Root $relative
            if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { return $false }
            $actual = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
            if ($actual -ne $expected) { return $false }
        }
        return $true
    } catch {
        return $false
    }
}

function Test-DebugArtifacts {
    param([Parameter(Mandatory=$true)][string]$Root)
    $appMeta = Join-Path $Root 'app\build\outputs\apk\debug\output-metadata.json'
    $testMeta = Join-Path $Root 'app\build\outputs\apk\androidTest\debug\output-metadata.json'
    return ((Test-Path -LiteralPath $appMeta -PathType Leaf) -and (Test-Path -LiteralPath $testMeta -PathType Leaf))
}

function Find-VerifiedR09Workspace {
    $dirs = @(Get-ChildItem -LiteralPath $env:TEMP -Directory -Filter 'VoiceCloud-PH13-R09-*' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending)
    foreach ($dir in $dirs) {
        if (-not (Test-DebugArtifacts -Root $dir.FullName)) { continue }
        Write-Host "[INFO] Found prior R09 acceptance workspace with debug artifacts: $($dir.FullName)"
        Write-Host '[INFO] Verifying its complete Android product/build-input hashes before reuse...'
        if (Test-ProductManifest -Root $dir.FullName) {
            Write-Host '[PASS] Prior R09 workspace product source matches the canonical R09 product manifest.'
            return $dir.FullName
        }
        Write-Host '[WARN] Prior R09 workspace was rejected because its product hashes do not match the canonical manifest.'
    }
    return $null
}

function Remove-TreeBestEffort {
    param([string]$Path)
    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) { return }
    for ($attempt=1; $attempt -le 5; $attempt++) {
        try { Remove-Item -LiteralPath $Path -Recurse -Force -ErrorAction Stop } catch {
            try { & cmd.exe /d /c "rd /s /q `"$Path`"" 2>$null | Out-Null } catch { }
        }
        if (-not (Test-Path -LiteralPath $Path)) { return }
        Start-Sleep -Milliseconds (300 * $attempt)
    }
    Write-Host "[INFO] Temporary corrective workspace remains outside the source tree and may be removed later: $Path"
}

if (-not (Test-Path -LiteralPath $deviceScript)) {
    Write-Host '[FAIL] R10 resilient device instrumentation script is missing.'
    exit 1
}
if (-not (Test-ProductManifest -Root $sourceRoot)) {
    Write-Host '[FAIL] R10 product files are not byte-identical to the workstation-build-verified R09 product authority.'
    exit 1
}
Write-Host '[PASS] R10 Android product/build inputs are byte-identical to R09; no app/UI/runtime source was changed.'

$reuse = Find-VerifiedR09Workspace
if ($reuse) {
    Write-Host '[FAST PATH] Reusing the already-built, hash-verified R09 Debug + androidTest APKs.'
    Write-Host '[FAST PATH] Compile/test/lint/assembly are not being repeated because those exact product sources already passed Gates 1-6 in the supplied R09 workstation log.'
    & powershell -NoProfile -ExecutionPolicy Bypass -File $deviceScript -ProjectRoot $reuse
    $rc = $LASTEXITCODE
    if ($rc -eq 0) {
        Write-Host '[PASS] R10 resilient physical-device closure passed using verified R09 build artifacts.'
        exit 0
    }
    if ($rc -eq 2) {
        Write-Host '[WARN] No healthy authorized device was available for the R10 device closure.'
        exit 2
    }
    Write-Host '[WARN] Fast-path device closure did not pass. Building fresh Debug/androidTest prerequisites once and retrying from a clean corrective workspace.'
}

$runId = [Guid]::NewGuid().ToString('N')
$workspace = Join-Path $env:TEMP ("VoiceCloud-PH13-R10-DEVICE-$runId")
$gradleUserHome = Join-Path $env:USERPROFILE '.voicecloud\gradle-r10-device-cache'
$gradlew = Join-Path $workspace 'gradlew.bat'
try {
    New-Item -ItemType Directory -Force -Path $workspace | Out-Null
    New-Item -ItemType Directory -Force -Path $gradleUserHome | Out-Null
    $excludeDirs = @('.git','.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__')
    $excludeFiles = @('local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties','*.apk','*.aab','*.pyc','*.pyo','*.iml','*.hprof')
    $roboArgs = @($sourceRoot, $workspace, '/E', '/COPY:DAT', '/DCOPY:DAT', '/R:1', '/W:1', '/NFL', '/NDL', '/NP', '/XD') + $excludeDirs + @('/XF') + $excludeFiles
    & robocopy @roboArgs | Out-Host
    if ($LASTEXITCODE -ge 8) { throw "robocopy failed with code $LASTEXITCODE" }
    if (-not (Test-ProductManifest -Root $workspace)) { throw 'fresh corrective workspace failed product hash verification' }
    Write-Host '[PASS] Fresh R10 corrective workspace preserves the exact R09 Android product source.'

    $env:GRADLE_USER_HOME = $gradleUserHome
    if ([string]::IsNullOrWhiteSpace($originalGradleOpts)) { $env:GRADLE_OPTS='-Dorg.gradle.daemon=false' } else { $env:GRADLE_OPTS="$originalGradleOpts -Dorg.gradle.daemon=false" }
    Set-Location $workspace
    Write-Host '[BOOTSTRAP] Standard Gradle Wrapper for quick device prerequisite build'
    & powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\BOOTSTRAP-GRADLE-WRAPPER.ps1
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $gradlew)) { throw 'verified local Gradle wrapper bootstrap failed' }
    $wrapperText = Get-Content -LiteralPath (Join-Path $workspace 'gradle\wrapper\gradle-wrapper.properties') -Raw
    if (-not $wrapperText.Contains('distributionUrl=file\:///')) { throw 'generated wrapper is not pinned to the verified local Gradle ZIP' }

    Write-Host '[QUICK BUILD] assembleDebug + assembleDebugAndroidTest only'
    & $gradlew ':app:assembleDebug' ':app:assembleDebugAndroidTest' '--no-daemon' '--stacktrace'
    if ($LASTEXITCODE -ne 0) { throw "quick debug prerequisite build failed with code $LASTEXITCODE" }
    if (-not (Test-DebugArtifacts -Root $workspace)) { throw 'quick debug prerequisite build did not produce required output metadata' }
    Write-Host '[PASS] Quick Debug/androidTest prerequisites built successfully.'

    & powershell -NoProfile -ExecutionPolicy Bypass -File $deviceScript -ProjectRoot $workspace
    $deviceRc = $LASTEXITCODE
    if ($deviceRc -eq 0) {
        Write-Host '[PASS] R10 resilient physical-device closure passed after one fresh quick prerequisite build.'
        exit 0
    }
    if ($deviceRc -eq 2) { exit 2 }
    throw "resilient physical-device closure failed with exit $deviceRc"
} catch {
    Write-Host "[FAIL] R10 device closure failed: $($_.Exception.Message)"
    exit 1
} finally {
    Set-Location $sourceRoot
    if (Test-Path -LiteralPath $cachedGradleBat) {
        try { & $cachedGradleBat --stop 2>&1 | Out-Null } catch { }
    }
    if ($null -eq $originalGradleUserHome) { Remove-Item Env:GRADLE_USER_HOME -ErrorAction SilentlyContinue } else { $env:GRADLE_USER_HOME = $originalGradleUserHome }
    if ($null -eq $originalGradleOpts) { Remove-Item Env:GRADLE_OPTS -ErrorAction SilentlyContinue } else { $env:GRADLE_OPTS = $originalGradleOpts }
    Remove-TreeBestEffort -Path $workspace
}
