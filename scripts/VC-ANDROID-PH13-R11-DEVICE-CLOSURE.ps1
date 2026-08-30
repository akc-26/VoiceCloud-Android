$ErrorActionPreference = 'Stop'
$sourceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$deviceScript = Join-Path $sourceRoot 'scripts\VC-ANDROID-DEVICE-INSTRUMENTATION.ps1'
$productManifest = Join-Path $sourceRoot 'contracts\VC-ANDROID-PH13-R11-R09-PRODUCT-CANONICAL.sha256'
$cachedGradleBat = Join-Path $env:USERPROFILE '.voicecloud\gradle-bootstrap\gradle-9.5.0\bin\gradle.bat'
$originalGradleUserHome = $env:GRADLE_USER_HOME
$originalGradleOpts = $env:GRADLE_OPTS

function Test-ProductManifest {
    param([Parameter(Mandatory=$true)][string]$Root)
    if (-not (Test-Path -LiteralPath $productManifest)) { return $false }
    foreach ($line in (Get-Content -LiteralPath $productManifest)) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $parts = $line -split '  ', 2
        if ($parts.Count -ne 2) { return $false }
        $expected = $parts[0].Trim().ToLowerInvariant()
        $rel = $parts[1].Trim().Replace('/', [IO.Path]::DirectorySeparatorChar)
        $path = Join-Path $Root $rel
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { return $false }
        $actual = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($actual -ne $expected) { return $false }
    }
    return $true
}

function Test-DebugArtifacts {
    param([Parameter(Mandatory=$true)][string]$Root)
    return ((Test-Path -LiteralPath (Join-Path $Root 'app\build\outputs\apk\debug\output-metadata.json') -PathType Leaf) -and
            (Test-Path -LiteralPath (Join-Path $Root 'app\build\outputs\apk\androidTest\debug\output-metadata.json') -PathType Leaf))
}

function Find-VerifiedPriorWorkspace {
    $patterns = @('VoiceCloud-PH13-R10-DEVICE-*','VoiceCloud-PH13-R09-*')
    foreach ($pattern in $patterns) {
        $dirs = @(Get-ChildItem -LiteralPath $env:TEMP -Directory -Filter $pattern -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending)
        foreach ($dir in $dirs) {
            if (-not (Test-DebugArtifacts -Root $dir.FullName)) { continue }
            Write-Host "[INFO] Found prior build workspace with Debug/androidTest artifacts: $($dir.FullName)"
            Write-Host '[INFO] Verifying complete Android product/build-input hashes before reuse...'
            if (Test-ProductManifest -Root $dir.FullName) {
                Write-Host '[PASS] Prior workspace product source matches the canonical build-proven R09 product manifest.'
                return $dir.FullName
            }
            Write-Host '[WARN] Prior workspace rejected because its product hashes do not match the canonical product authority.'
        }
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
    Write-Host '[FAIL] R11 resilient device instrumentation script is missing.'
    exit 1
}
if (-not (Test-ProductManifest -Root $sourceRoot)) {
    Write-Host '[FAIL] R11 product files are not byte-identical to the workstation-build-verified R09 product authority.'
    exit 1
}
Write-Host '[PASS] R11 Android product/build inputs are byte-identical to R09/R10; no app/UI/runtime source was changed.'

# Critical R11 correction: check device availability BEFORE any build or artifact preparation.
Write-Host '[DEVICE PREFLIGHT] Checking for a healthy authorized API 26+ device before any build work...'
& powershell -NoProfile -ExecutionPolicy Bypass -File $deviceScript -PreflightOnly
$preflightRc = $LASTEXITCODE
if ($preflightRc -eq 2) {
    Write-Host '[DEVICE-PENDING] No healthy authorized Android device is available right now.'
    Write-Host '[DEVICE-PENDING] Skipping artifact preparation/build entirely; no-device is an environment-pending condition, not a product failure.'
    exit 2
}
if ($preflightRc -ne 0) {
    Write-Host "[FAIL] Device preflight encountered a real adb/device error (exit $preflightRc)."
    exit 1
}

$reuse = Find-VerifiedPriorWorkspace
if ($reuse) {
    Write-Host '[FAST PATH] Reusing already-built, hash-verified Debug + androidTest APKs.'
    Write-Host '[FAST PATH] Compile/test/lint/assembly are not repeated because the exact product source already passed R09 Gates 1-6.'
    & powershell -NoProfile -ExecutionPolicy Bypass -File $deviceScript -ProjectRoot $reuse
    $rc = $LASTEXITCODE
    if ($rc -eq 0) {
        Write-Host '[PASS] R11 resilient physical-device closure passed using verified prior build artifacts.'
        exit 0
    }
    if ($rc -eq 2) {
        Write-Host '[DEVICE-PENDING] Device became unavailable after preflight; closure remains pending, not failed.'
        exit 2
    }
    Write-Host '[WARN] Verified-artifact device attempt failed. One fresh Debug/androidTest prerequisite build will be attempted before the final device retry.'
}

$runId = [Guid]::NewGuid().ToString('N')
$workspace = Join-Path $env:TEMP ("VoiceCloud-PH13-R11-DEVICE-$runId")
$gradleUserHome = Join-Path $env:USERPROFILE '.voicecloud\gradle-r11-device-cache'
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
    Write-Host '[PASS] Fresh R11 corrective workspace preserves the exact build-proven Android product source.'

    # Device can disappear while preparing workspace; avoid a long build if that happened.
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $workspace 'scripts\VC-ANDROID-DEVICE-INSTRUMENTATION.ps1') -PreflightOnly
    $secondPreflightRc = $LASTEXITCODE
    if ($secondPreflightRc -eq 2) {
        Write-Host '[DEVICE-PENDING] Device became unavailable before fallback build; skipping build.'
        exit 2
    }
    if ($secondPreflightRc -ne 0) { throw "second device preflight failed with exit $secondPreflightRc" }

    $env:GRADLE_USER_HOME = $gradleUserHome
    if ([string]::IsNullOrWhiteSpace($originalGradleOpts)) { $env:GRADLE_OPTS='-Dorg.gradle.daemon=false' } else { $env:GRADLE_OPTS="$originalGradleOpts -Dorg.gradle.daemon=false" }
    Set-Location $workspace
    Write-Host '[BOOTSTRAP] Standard Gradle Wrapper for one quick device prerequisite build'
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
        Write-Host '[PASS] R11 resilient physical-device closure passed after one fresh quick prerequisite build.'
        exit 0
    }
    if ($deviceRc -eq 2) {
        Write-Host '[DEVICE-PENDING] Device became unavailable before instrumentation; closure remains pending, not failed.'
        exit 2
    }
    throw "resilient physical-device closure failed with exit $deviceRc"
} catch {
    Write-Host "[FAIL] R11 device closure failed: $($_.Exception.Message)"
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
