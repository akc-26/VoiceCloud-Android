$ErrorActionPreference = 'Stop'
$sourceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$deviceScript = Join-Path $sourceRoot 'scripts\VC-ANDROID-DEVICE-INSTRUMENTATION.ps1'
$runtimeManifest = Join-Path $sourceRoot 'contracts\VC-ANDROID-PH13-R13-R09-RUNTIME-CANONICAL.sha256'
$testRel = 'app\src\androidTest\java\app\voicecloud\android\VoiceCloudFoundationInstrumentedTest.kt'
$sourceTest = Join-Path $sourceRoot $testRel
$cachedGradleBat = Join-Path $env:USERPROFILE '.voicecloud\gradle-bootstrap\gradle-9.5.0\bin\gradle.bat'
$originalGradleUserHome = $env:GRADLE_USER_HOME
$originalGradleOpts = $env:GRADLE_OPTS

function Test-RuntimeManifest {
    param([Parameter(Mandatory=$true)][string]$Root)
    if (-not (Test-Path -LiteralPath $runtimeManifest -PathType Leaf)) { return $false }
    foreach ($line in (Get-Content -LiteralPath $runtimeManifest)) {
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

function Test-AppArtifact {
    param([Parameter(Mandatory=$true)][string]$Root)
    return (Test-Path -LiteralPath (Join-Path $Root 'app\build\outputs\apk\debug\output-metadata.json') -PathType Leaf)
}

function Test-TestArtifact {
    param([Parameter(Mandatory=$true)][string]$Root)
    return (Test-Path -LiteralPath (Join-Path $Root 'app\build\outputs\apk\androidTest\debug\output-metadata.json') -PathType Leaf)
}

function Ensure-Wrapper {
    param([Parameter(Mandatory=$true)][string]$Root)
    $gradlew = Join-Path $Root 'gradlew.bat'
    $props = Join-Path $Root 'gradle\wrapper\gradle-wrapper.properties'
    if ((Test-Path -LiteralPath $gradlew -PathType Leaf) -and (Test-Path -LiteralPath $props -PathType Leaf)) {
        $text = Get-Content -LiteralPath $props -Raw
        if ($text.Contains('distributionUrl=file\:///')) { return $gradlew }
    }
    Write-Host '[BOOTSTRAP] Generating verified local Gradle wrapper for androidTest-only correction build...'
    Push-Location $Root
    try {
        & powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\BOOTSTRAP-GRADLE-WRAPPER.ps1
        if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $gradlew -PathType Leaf)) { throw 'verified local Gradle wrapper bootstrap failed' }
    } finally { Pop-Location }
    $text = Get-Content -LiteralPath $props -Raw
    if (-not $text.Contains('distributionUrl=file\:///')) { throw 'generated wrapper is not pinned to the verified local Gradle ZIP' }
    return $gradlew
}

function Patch-And-Build-TestApk {
    param([Parameter(Mandatory=$true)][string]$Root,[Parameter(Mandatory=$true)][bool]$NeedAppBuild)
    if (-not (Test-RuntimeManifest -Root $Root)) { throw 'candidate workspace runtime hashes do not match build-proven R09 production authority' }
    $destTest = Join-Path $Root $testRel
    $destDir = Split-Path -Parent $destTest
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    Copy-Item -LiteralPath $sourceTest -Destination $destTest -Force
    $gradlew = Ensure-Wrapper -Root $Root
    $env:GRADLE_USER_HOME = Join-Path $env:USERPROFILE '.voicecloud\gradle-r13-test-cache'
    New-Item -ItemType Directory -Force -Path $env:GRADLE_USER_HOME | Out-Null
    if ([string]::IsNullOrWhiteSpace($originalGradleOpts)) { $env:GRADLE_OPTS='-Dorg.gradle.daemon=false' } else { $env:GRADLE_OPTS="$originalGradleOpts -Dorg.gradle.daemon=false" }
    Push-Location $Root
    try {
        if ($NeedAppBuild) {
            Write-Host '[R13 TEST BUILD] assembleDebug + assembleDebugAndroidTest fallback'
            & $gradlew ':app:assembleDebug' ':app:assembleDebugAndroidTest' '--no-daemon' '--stacktrace'
        } else {
            Write-Host '[R13 FAST TEST BUILD] Rebuilding androidTest APK only; production app APK is reused unchanged.'
            & $gradlew ':app:assembleDebugAndroidTest' '--no-daemon' '--stacktrace'
        }
        if ($LASTEXITCODE -ne 0) { throw "R13 androidTest correction build failed with code $LASTEXITCODE" }
    } finally { Pop-Location }
    if (-not (Test-AppArtifact -Root $Root)) { throw 'Debug app output metadata is missing after R13 test build' }
    if (-not (Test-TestArtifact -Root $Root)) { throw 'Debug androidTest output metadata is missing after R13 test build' }
    Write-Host '[PASS] Corrected R13 androidTest APK is ready.'
}

function Find-PriorWorkspace {
    $patterns = @('VoiceCloud-PH13-R11-DEVICE-*','VoiceCloud-PH13-R10-DEVICE-*','VoiceCloud-PH13-R09-*')
    foreach ($pattern in $patterns) {
        $dirs = @(Get-ChildItem -LiteralPath $env:TEMP -Directory -Filter $pattern -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending)
        foreach ($dir in $dirs) {
            if (-not (Test-AppArtifact -Root $dir.FullName)) { continue }
            if (Test-RuntimeManifest -Root $dir.FullName) {
                Write-Host "[FAST PATH] Found build-proven prior workspace: $($dir.FullName)"
                return $dir.FullName
            }
        }
    }
    return $null
}

function Remove-TreeBestEffort {
    param([string]$Path)
    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) { return }
    for ($attempt=1; $attempt -le 4; $attempt++) {
        try { Remove-Item -LiteralPath $Path -Recurse -Force -ErrorAction Stop } catch { }
        if (-not (Test-Path -LiteralPath $Path)) { return }
        Start-Sleep -Milliseconds (250 * $attempt)
    }
    Write-Host "[INFO] Temporary R13 fallback workspace remains outside source tree: $Path"
}

if (-not (Test-Path -LiteralPath $sourceTest -PathType Leaf)) { Write-Host '[FAIL] R13 corrected instrumentation source is missing.'; exit 1 }
if (-not (Test-RuntimeManifest -Root $sourceRoot)) { Write-Host '[FAIL] R13 production/runtime source drifted from build-proven R09 authority.'; exit 1 }
Write-Host '[PASS] R13 production app/UI/API/runtime inputs remain byte-identical to build-proven R09 authority.'

Write-Host '[DEVICE PREFLIGHT] Checking healthy authorized API 26+ device before any build...'
& powershell -NoProfile -ExecutionPolicy Bypass -File $deviceScript -PreflightOnly
$preflightRc = $LASTEXITCODE
if ($preflightRc -eq 2) { Write-Host '[DEVICE-PENDING] No healthy authorized Android device is currently available; test correction remains pending.'; exit 2 }
if ($preflightRc -ne 0) { Write-Host "[FAIL] Device preflight failed with exit $preflightRc."; exit 1 }

$fallbackWorkspace = $null
try {
    $workspace = Find-PriorWorkspace
    if ($workspace) {
        Patch-And-Build-TestApk -Root $workspace -NeedAppBuild:$false
        & powershell -NoProfile -ExecutionPolicy Bypass -File $deviceScript -ProjectRoot $workspace
        $rc = $LASTEXITCODE
        if ($rc -eq 0) { Write-Host '[PASS] R13 corrected instrumentation passed using the unchanged prior app APK.'; exit 0 }
        if ($rc -eq 2) { Write-Host '[DEVICE-PENDING] Device became unavailable before corrected instrumentation completed.'; exit 2 }
        Write-Host '[FAIL] Corrected instrumentation still reported a genuine test/device failure.'
        exit 1
    }

    Write-Host '[FALLBACK] No reusable build workspace found. Building only Debug + androidTest prerequisites in an isolated workspace.'
    $fallbackWorkspace = Join-Path $env:TEMP ("VoiceCloud-PH13-R13-TEST-" + [Guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Force -Path $fallbackWorkspace | Out-Null
    $excludeDirs = @('.git','.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__')
    $excludeFiles = @('local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties','*.apk','*.aab','*.pyc','*.pyo','*.iml','*.hprof')
    $roboArgs = @($sourceRoot, $fallbackWorkspace, '/E', '/COPY:DAT', '/DCOPY:DAT', '/R:1', '/W:1', '/NFL', '/NDL', '/NP', '/XD') + $excludeDirs + @('/XF') + $excludeFiles
    & robocopy @roboArgs | Out-Host
    if ($LASTEXITCODE -ge 8) { throw "robocopy failed with code $LASTEXITCODE" }
    Patch-And-Build-TestApk -Root $fallbackWorkspace -NeedAppBuild:$true
    & powershell -NoProfile -ExecutionPolicy Bypass -File $deviceScript -ProjectRoot $fallbackWorkspace
    $rc = $LASTEXITCODE
    if ($rc -eq 0) { Write-Host '[PASS] R13 corrected instrumentation passed after fallback prerequisite build.'; exit 0 }
    if ($rc -eq 2) { Write-Host '[DEVICE-PENDING] Device became unavailable before corrected instrumentation completed.'; exit 2 }
    throw "corrected instrumentation failed with exit $rc"
} catch {
    Write-Host "[FAIL] R13 device/test closure failed: $($_.Exception.Message)"
    exit 1
} finally {
    if (Test-Path -LiteralPath $cachedGradleBat) { try { & $cachedGradleBat --stop 2>&1 | Out-Null } catch { } }
    if ($null -eq $originalGradleUserHome) { Remove-Item Env:GRADLE_USER_HOME -ErrorAction SilentlyContinue } else { $env:GRADLE_USER_HOME = $originalGradleUserHome }
    if ($null -eq $originalGradleOpts) { Remove-Item Env:GRADLE_OPTS -ErrorAction SilentlyContinue } else { $env:GRADLE_OPTS = $originalGradleOpts }
    if ($fallbackWorkspace) { Remove-TreeBestEffort -Path $fallbackWorkspace }
}
