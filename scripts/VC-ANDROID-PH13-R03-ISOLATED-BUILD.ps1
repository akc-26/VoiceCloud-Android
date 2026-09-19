$ErrorActionPreference = 'Stop'
$sourceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
$workspace = Join-Path $env:TEMP ("VoiceCloud-PH13-R03-" + [Guid]::NewGuid().ToString('N'))

function Invoke-GradleGate {
    param(
        [Parameter(Mandatory=$true)][string]$Label,
        [Parameter(Mandatory=$true)][string[]]$Arguments
    )
    Write-Host $Label
    & .\gradlew.bat @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Label failed with exit code $LASTEXITCODE"
    }
}

$exitCode = 0
try {
    New-Item -ItemType Directory -Force -Path $workspace | Out-Null
    Write-Host "[INFO] Creating isolated Android acceptance workspace: $workspace"

    $excludeDirs = @('.git','.gradle','.idea','.kotlin','build','captures','.externalNativeBuild','.cxx','__pycache__')
    $excludeFiles = @('local.properties','secrets.properties','gradlew','gradlew.bat','gradle-wrapper.jar','gradle-daemon-jvm.properties','*.apk','*.aab','*.pyc','*.pyo','*.iml','*.hprof')
    $roboArgs = @($sourceRoot, $workspace, '/E', '/COPY:DAT', '/DCOPY:DAT', '/R:1', '/W:1', '/NFL', '/NDL', '/NP', '/XD') + $excludeDirs + @('/XF') + $excludeFiles
    & robocopy @roboArgs
    $robocopyExit = $LASTEXITCODE
    if ($robocopyExit -ge 8) { throw "robocopy failed with exit code $robocopyExit" }

    Set-Location $workspace
    Write-Host '[PASS] Isolated source copy created; original delivery tree will not be used as a Gradle workspace.'

    & powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\BOOTSTRAP-GRADLE-WRAPPER.ps1
    if ($LASTEXITCODE -ne 0) { throw "Gradle wrapper bootstrap failed with exit code $LASTEXITCODE" }
    if (-not (Test-Path '.\gradlew.bat')) { throw 'gradlew.bat was not generated in isolated workspace.' }

    Invoke-GradleGate -Label '[GATE 1] compileDebugKotlin' -Arguments @(':app:compileDebugKotlin','--stacktrace')
    Invoke-GradleGate -Label '[GATE 2] compileStagingKotlin' -Arguments @(':app:compileStagingKotlin','--stacktrace')
    Invoke-GradleGate -Label '[GATE 3] compileReleaseKotlin' -Arguments @(':app:compileReleaseKotlin','--stacktrace')
    Invoke-GradleGate -Label '[GATE 4] Unit tests' -Arguments @('test','--stacktrace')
    Invoke-GradleGate -Label '[GATE 5] Lint' -Arguments @('lintDebug','lintStaging','lintRelease','--stacktrace')
    Invoke-GradleGate -Label '[GATE 6] Assemblies' -Arguments @(':app:assembleDebug',':app:assembleStaging',':app:assembleRelease',':app:assembleDebugAndroidTest','--stacktrace')

    Write-Host '[GATE 7] Physical-device instrumentation'
    & powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\VC-ANDROID-DEVICE-INSTRUMENTATION.ps1
    $deviceGate = $LASTEXITCODE
    if ($deviceGate -eq 2) {
        Write-Host '[WARN] No healthy device was available; manual device QA remains required.'
    } elseif ($deviceGate -ne 0) {
        throw "Physical-device instrumentation failed with exit code $deviceGate"
    }

    Write-Host '[PASS] PH13-R03 isolated Android build/device gates completed.'
} catch {
    Write-Host "[FAIL] $($_.Exception.Message)"
    $exitCode = 1
} finally {
    Set-Location $sourceRoot
    if (Test-Path $workspace) {
        Remove-Item -LiteralPath $workspace -Recurse -Force -ErrorAction SilentlyContinue
    }
    if (Test-Path $workspace) {
        Write-Host "[FAIL] Isolated workspace cleanup could not fully remove: $workspace"
        $exitCode = 1
    } else {
        Write-Host '[PASS] Isolated Android acceptance workspace removed.'
    }
}
exit $exitCode
