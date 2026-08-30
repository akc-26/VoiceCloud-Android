$ErrorActionPreference = 'Continue'
$sourceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$runId = [Guid]::NewGuid().ToString('N')
$workspace = Join-Path $env:TEMP ("VoiceCloud-PH13-R17-$runId")
$gradleUserHome = Join-Path $env:USERPROFILE '.voicecloud\gradle-r14-cache'
$originalGradleUserHome = $env:GRADLE_USER_HOME
$originalGradleOpts = $env:GRADLE_OPTS

$failures = New-Object System.Collections.Generic.List[string]
$warnings = New-Object System.Collections.Generic.List[string]
$passes = 0
$total = 0
$devicePending = $false
$gradlew = Join-Path $workspace 'gradlew.bat'
$cachedGradleBat = Join-Path $env:USERPROFILE '.voicecloud\gradle-bootstrap\gradle-9.5.0\bin\gradle.bat'
$buildReady = $false
$compileGatesClean = $true
$assemblyGateClean = $true

function Add-Failure {
    param([string]$Label,[string]$Detail)
    $script:failures.Add("$Label :: $Detail")
    Write-Host "[GATE-FAIL] $Label :: $Detail"
}
function Add-Warning {
    param([string]$Label,[string]$Detail)
    $script:warnings.Add("$Label :: $Detail")
    Write-Host "[GATE-WARN] $Label :: $Detail"
}
function Invoke-GradleGate {
    param(
        [Parameter(Mandatory=$true)][string]$Label,
        [Parameter(Mandatory=$true)][string[]]$Arguments
    )
    $script:total++
    Write-Host $Label
    if (-not $script:buildReady -or -not (Test-Path -LiteralPath $script:gradlew)) {
        Add-Failure -Label $Label -Detail 'blocked because verified local Gradle wrapper bootstrap did not complete.'
        if ($Label -match '^\[GATE [123]\]') { $script:compileGatesClean = $false }
        if ($Label -match '^\[GATE 6\]') { $script:assemblyGateClean = $false }
        return
    }
    try {
        & $script:gradlew @Arguments '--no-daemon' '--stacktrace'
        $rc = $LASTEXITCODE
        if ($rc -eq 0) {
            $script:passes++
            Write-Host "[GATE-PASS] $Label"
        } else {
            Add-Failure -Label $Label -Detail "Gradle exited with code $rc"
            if ($Label -match '^\[GATE [123]\]') { $script:compileGatesClean = $false }
            if ($Label -match '^\[GATE 6\]') { $script:assemblyGateClean = $false }
        }
    } catch {
        Add-Failure -Label $Label -Detail $_.Exception.Message
        if ($Label -match '^\[GATE [123]\]') { $script:compileGatesClean = $false }
        if ($Label -match '^\[GATE 6\]') { $script:assemblyGateClean = $false }
    }
}
function Remove-TreeRobust {
    param([Parameter(Mandatory=$true)][string]$Path,[Parameter(Mandatory=$true)][string]$Label)
    if (-not (Test-Path -LiteralPath $Path)) { return $true }
    for ($attempt = 1; $attempt -le 12; $attempt++) {
        try { & cmd.exe /d /c "attrib -R `"$Path\*`" /S /D" 2>$null | Out-Null } catch {}
        try { Remove-Item -LiteralPath $Path -Recurse -Force -ErrorAction Stop } catch {
            try { & cmd.exe /d /c "rd /s /q `"$Path`"" 2>$null | Out-Null } catch {}
        }
        if (-not (Test-Path -LiteralPath $Path)) {
            Write-Host "[PASS] $Label removed."
            return $true
        }
        Start-Sleep -Milliseconds ([Math]::Min(2500, 250 * $attempt))
    }
    Add-Warning -Label $Label -Detail "Could not fully remove after retries: $Path"
    return $false
}

try {
    New-Item -ItemType Directory -Force -Path $workspace | Out-Null
    New-Item -ItemType Directory -Force -Path $gradleUserHome | Out-Null
    Write-Host "[INFO] Creating isolated R17 full-acceptance workspace: $workspace"
    Write-Host "[INFO] Using persistent dependency cache outside source tree: $gradleUserHome"

    $excludeDirs = @('.git','.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__','app.voicecloud.core.database.VoiceCloudDatabase')
    $excludeFiles = @('local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties','*.apk','*.aab','*.pyc','*.pyo','*.iml','*.hprof')
    $roboArgs = @($sourceRoot, $workspace, '/E', '/COPY:DAT', '/DCOPY:DAT', '/R:1', '/W:1', '/NFL', '/NDL', '/NP', '/XD') + $excludeDirs + @('/XF') + $excludeFiles
    & robocopy @roboArgs
    $robocopyExit = $LASTEXITCODE
    if ($robocopyExit -ge 8) {
        Add-Failure -Label 'Isolated source copy' -Detail "robocopy exited with code $robocopyExit"
    } else {
        Write-Host '[PASS] Isolated source copy created; Gradle/Room outputs cannot contaminate the delivered source tree.'
    }

    $env:GRADLE_USER_HOME = $gradleUserHome
    if ([string]::IsNullOrWhiteSpace($originalGradleOpts)) {
        $env:GRADLE_OPTS = '-Dorg.gradle.daemon=false'
    } else {
        $env:GRADLE_OPTS = "$originalGradleOpts -Dorg.gradle.daemon=false"
    }

    if (Test-Path -LiteralPath $workspace) {
        Set-Location $workspace
        $script:total++
        Write-Host '[BOOTSTRAP] Standard Gradle Wrapper from verified local Gradle 9.5 distribution'
        try {
            & powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\BOOTSTRAP-GRADLE-WRAPPER.ps1
            $bootstrapExit = $LASTEXITCODE
            if ($bootstrapExit -eq 0 -and (Test-Path -LiteralPath $gradlew)) {
                $wrapperProps = Join-Path $workspace 'gradle\wrapper\gradle-wrapper.properties'
                $wrapperText = if (Test-Path -LiteralPath $wrapperProps) { Get-Content -LiteralPath $wrapperProps -Raw } else { '' }
                if ($wrapperText.Contains('distributionUrl=file\:///')) {
                    $script:passes++
                    $script:buildReady = $true
                    Write-Host '[GATE-PASS] Gradle wrapper bootstrap (verified local distribution runtime)'
                } else {
                    Add-Failure -Label 'Gradle wrapper bootstrap' -Detail 'wrapper runtime URL is not pinned to the verified local Gradle ZIP.'
                }
            } else {
                Add-Failure -Label 'Gradle wrapper bootstrap' -Detail "exit=$bootstrapExit; gradlewExists=$((Test-Path -LiteralPath $gradlew))"
            }
        } catch {
            Add-Failure -Label 'Gradle wrapper bootstrap' -Detail $_.Exception.Message
        }
    } else {
        $script:total++
        Add-Failure -Label 'Gradle wrapper bootstrap' -Detail 'isolated workspace is unavailable.'
    }

    Invoke-GradleGate -Label '[GATE 1] compileDebugKotlin' -Arguments @(':app:compileDebugKotlin','--continue')
    Invoke-GradleGate -Label '[GATE 2] compileStagingKotlin' -Arguments @(':app:compileStagingKotlin','--continue')
    Invoke-GradleGate -Label '[GATE 3] compileReleaseKotlin' -Arguments @(':app:compileReleaseKotlin','--continue')

    if ($script:compileGatesClean -and $script:buildReady) {
        Invoke-GradleGate -Label '[GATE 4] Unit tests' -Arguments @('test')
        Invoke-GradleGate -Label '[GATE 5] Lint' -Arguments @('lintDebug','lintStaging','lintRelease')
        Invoke-GradleGate -Label '[GATE 6] Assemblies' -Arguments @(':app:assembleDebug',':app:assembleStaging',':app:assembleRelease',':app:assembleDebugAndroidTest')
    } else {
        Write-Host '[GATE-SKIP] Gates 4-6 skipped because mandatory Kotlin compilation or wrapper bootstrap failed.'
    }

    if ($script:compileGatesClean -and $script:assemblyGateClean -and $script:buildReady -and $script:failures.Count -eq 0) {
        $script:total++
        Write-Host '[GATE 7] Physical-device instrumentation'
        $deviceScript = Join-Path $workspace 'scripts\VC-ANDROID-DEVICE-INSTRUMENTATION.ps1'
        if (-not (Test-Path -LiteralPath $deviceScript)) {
            Add-Failure -Label '[GATE 7] Physical-device instrumentation' -Detail 'device instrumentation script is missing.'
        } else {
            try {
                & powershell -NoProfile -ExecutionPolicy Bypass -File $deviceScript -ProjectRoot $workspace
                $deviceGate = $LASTEXITCODE
                if ($deviceGate -eq 0) {
                    $script:passes++
                    Write-Host '[GATE-PASS] Physical-device instrumentation'
                } elseif ($deviceGate -eq 2) {
                    $script:devicePending = $true
                    Add-Warning -Label '[GATE 7] Physical-device instrumentation' -Detail 'No healthy authorized API 26+ device was available. Host build gates passed, but R17 is NOT fully accepted until this gate runs.'
                } else {
                    Add-Failure -Label '[GATE 7] Physical-device instrumentation' -Detail "exit $deviceGate"
                }
            } catch {
                Add-Failure -Label '[GATE 7] Physical-device instrumentation' -Detail $_.Exception.Message
            }
        }
    } else {
        Write-Host '[GATE-SKIP] Gate 7 skipped because a mandatory build/test/lint/assembly prerequisite failed.'
    }
} catch {
    Add-Failure -Label 'R17 isolated build harness' -Detail $_.Exception.Message
} finally {
    Set-Location $sourceRoot
    if (Test-Path -LiteralPath $cachedGradleBat) {
        try {
            Write-Host '[CLEANUP] Stopping Gradle daemons with cached Gradle executable...'
            & $cachedGradleBat --stop 2>&1 | Out-Host
            Start-Sleep -Milliseconds 750
        } catch { Add-Warning -Label 'Gradle daemon stop' -Detail $_.Exception.Message }
    } elseif ((Test-Path -LiteralPath $gradlew) -and $buildReady) {
        try { & $gradlew --stop 2>&1 | Out-Host; Start-Sleep -Milliseconds 750 } catch { Add-Warning -Label 'Gradle daemon stop' -Detail $_.Exception.Message }
    }

    $workspaceRemoved = Remove-TreeRobust -Path $workspace -Label 'Isolated R17 Android workspace'
    if (-not $workspaceRemoved) { Add-Warning -Label 'R17 workspace cleanup' -Detail 'Temporary workspace remains outside delivery/Git source tree.' }

    if ($null -eq $originalGradleUserHome) { Remove-Item Env:GRADLE_USER_HOME -ErrorAction SilentlyContinue } else { $env:GRADLE_USER_HOME = $originalGradleUserHome }
    if ($null -eq $originalGradleOpts) { Remove-Item Env:GRADLE_OPTS -ErrorAction SilentlyContinue } else { $env:GRADLE_OPTS = $originalGradleOpts }
}

Write-Host '============================================================='
Write-Host "[ANDROID SUMMARY] Executable gates: $passes/$total passed; $($failures.Count) failed; $($warnings.Count) warning(s)."
foreach ($warning in $warnings) { Write-Host "[SUMMARY-WARN] $warning" }
if ($failures.Count -gt 0) {
    foreach ($failure in $failures) { Write-Host "[SUMMARY-FAIL] $failure" }
    exit 1
}
if ($devicePending) {
    Write-Host '[PENDING] Debug/Staging/Release compile, unit tests, lint and assemblies passed; physical-device instrumentation remains required.'
    exit 2
}
Write-Host '[PASS] Full R17 Android build + physical-device acceptance gates passed.'
exit 0
