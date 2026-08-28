$ErrorActionPreference = 'Stop'

function Quote-Arg([string]$arg) {
    if ($arg -notmatch '[\s"]') { return $arg }
    return '"' + ($arg -replace '"', '\\"') + '"'
}

function Invoke-CapturedProcess {
    param(
        [Parameter(Mandatory=$true)][string]$FilePath,
        [Parameter(Mandatory=$true)][string[]]$Arguments,
        [int]$TimeoutSeconds = 15
    )

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $FilePath
    $psi.Arguments = (($Arguments | ForEach-Object { Quote-Arg $_ }) -join ' ')
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $psi
    [void]$process.Start()
    # Drain redirected streams asynchronously before waiting so verbose adb/instrumentation
    # output cannot fill an OS pipe and deadlock the harness.
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()

    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        try { $process.Kill() } catch { }
        try { $process.WaitForExit() } catch { }
        $stdout = try { $stdoutTask.GetAwaiter().GetResult() } catch { '' }
        $stderr = try { $stderrTask.GetAwaiter().GetResult() } catch { '' }
        return [pscustomobject]@{ ExitCode = 124; StdOut = $stdout; StdErr = (($stderr + "`nTimed out").Trim()); TimedOut = $true }
    }

    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    return [pscustomobject]@{ ExitCode = $process.ExitCode; StdOut = $stdout; StdErr = $stderr; TimedOut = $false }
}

function Read-ApkOutputMetadata {
    param(
        [Parameter(Mandatory=$true)][string]$MetadataPath,
        [Parameter(Mandatory=$true)][string]$Label,
        [switch]$RequireApplicationId
    )
    if (-not (Test-Path $MetadataPath)) {
        Write-Host "[FAIL] $Label output metadata not found: $MetadataPath"
        exit 1
    }
    try {
        $metadata = Get-Content -LiteralPath $MetadataPath -Raw | ConvertFrom-Json
    } catch {
        Write-Host "[FAIL] $Label output metadata could not be parsed: $MetadataPath"
        exit 1
    }
    $applicationId = [string]$metadata.applicationId
    $elements = @($metadata.elements)
    if ($elements.Count -lt 1 -or [string]::IsNullOrWhiteSpace([string]$elements[0].outputFile)) {
        Write-Host "[FAIL] $Label output metadata does not contain an APK output file."
        exit 1
    }
    if ($RequireApplicationId -and [string]::IsNullOrWhiteSpace($applicationId)) {
        Write-Host "[FAIL] $Label output metadata does not contain the target applicationId."
        exit 1
    }
    $apk = Join-Path (Split-Path -Parent $MetadataPath) ([string]$elements[0].outputFile)
    if (-not (Test-Path $apk)) {
        Write-Host "[FAIL] $Label APK referenced by output metadata was not found: $apk"
        exit 1
    }
    return [pscustomobject]@{ ApplicationId = $applicationId.Trim(); Apk = $apk }
}

function Assert-PackageEnabledForUser {
    param(
        [Parameter(Mandatory=$true)][string]$Adb,
        [Parameter(Mandatory=$true)][string]$Serial,
        [Parameter(Mandatory=$true)][int]$UserId,
        [Parameter(Mandatory=$true)][string]$PackageName,
        [Parameter(Mandatory=$true)][string]$Label
    )
    $result = Invoke-CapturedProcess -FilePath $Adb -Arguments @('-s', $Serial, 'shell', 'pm', 'list', 'packages', '-e', '--user', [string]$UserId, $PackageName) -TimeoutSeconds 15
    $expected = "package:$PackageName"
    if ($result.ExitCode -ne 0 -or $result.TimedOut -or -not (($result.StdOut -split "`r?`n") -contains $expected)) {
        Write-Host "[FAIL] $Label package is not installed and enabled for Android user ${UserId}: $PackageName"
        if ($result.StdOut) { Write-Host $result.StdOut.TrimEnd() }
        if ($result.StdErr) { Write-Host $result.StdErr.TrimEnd() }
        exit 1
    }
    Write-Host "[PASS] $Label package enabled for Android user ${UserId}: $PackageName"
}

$adb = if ($env:ANDROID_HOME) {
    Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
} elseif ($env:ANDROID_SDK_ROOT) {
    Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe'
} else {
    $null
}

if (-not $adb -or -not (Test-Path $adb)) {
    Write-Host '[FAIL] adb.exe was not found after Android SDK environment resolution.'
    exit 1
}

$deviceProbe = Invoke-CapturedProcess -FilePath $adb -Arguments @('devices') -TimeoutSeconds 20
if ($deviceProbe.StdErr) {
    foreach ($line in ($deviceProbe.StdErr -split "`r?`n")) {
        if (-not [string]::IsNullOrWhiteSpace($line)) { Write-Host "[INFO] adb: $line" }
    }
}
if ($deviceProbe.ExitCode -ne 0 -or $deviceProbe.TimedOut) {
    Write-Host '[FAIL] adb devices failed or timed out.'
    exit 1
}

$deviceOutput = $deviceProbe.StdOut -split "`r?`n"
$candidates = @()
foreach ($line in $deviceOutput) {
    if ([string]::IsNullOrWhiteSpace($line) -or $line -like 'List of devices attached*') { continue }
    $parts = $line -split "`t", 2
    if ($parts.Count -ne 2) { continue }
    $serial = $parts[0].Trim()
    $state = ($parts[1] -split '\s+')[0].Trim()
    if ($state -ne 'device') { continue }

    $sdkResult = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $serial, 'shell', 'getprop', 'ro.build.version.sdk') -TimeoutSeconds 8
    if ($sdkResult.ExitCode -ne 0 -or $sdkResult.TimedOut) {
        Write-Host "[WARN] Ignoring unhealthy adb entry: $serial (SDK query failed or timed out)"
        continue
    }

    $sdkText = $sdkResult.StdOut.Trim()
    $sdk = 0
    if (-not [int]::TryParse($sdkText, [ref]$sdk) -or $sdk -lt 26) {
        Write-Host "[WARN] Ignoring invalid/incompatible adb entry: $serial (reported API '$sdkText')"
        continue
    }

    $modelResult = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $serial, 'shell', 'getprop', 'ro.product.model') -TimeoutSeconds 8
    $model = if ($modelResult.ExitCode -eq 0 -and -not [string]::IsNullOrWhiteSpace($modelResult.StdOut)) { $modelResult.StdOut.Trim() } else { 'Android device' }
    $transportRank = if ($serial -match '_adb-tls-connect\._tcp$' -or $serial -match '^adb-') { 1 } else { 0 }
    $candidates += [pscustomobject]@{ Serial = $serial; Api = $sdk; Model = $model; Rank = $transportRank }
}

if ($candidates.Count -eq 0) {
    Write-Host '[WARN] No healthy authorized Android device with API >= 26 was found.'
    Write-Host '[WARN] Connected instrumentation is pending; host build acceptance is unaffected.'
    exit 2
}

$device = $candidates | Sort-Object Rank, Serial | Select-Object -First 1
Write-Host "[PASS] Healthy Android device selected: $($device.Model) | API $($device.Api) | $($device.Serial)"

$currentUserResult = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'shell', 'am', 'get-current-user') -TimeoutSeconds 10
$currentUser = 0
if ($currentUserResult.ExitCode -ne 0 -or $currentUserResult.TimedOut -or -not [int]::TryParse($currentUserResult.StdOut.Trim(), [ref]$currentUser) -or $currentUser -lt 0) {
    Write-Host '[FAIL] Unable to resolve the foreground Android user for isolated instrumentation.'
    if ($currentUserResult.StdOut) { Write-Host $currentUserResult.StdOut.TrimEnd() }
    if ($currentUserResult.StdErr) { Write-Host $currentUserResult.StdErr.TrimEnd() }
    exit 1
}
Write-Host "[PASS] Foreground Android user resolved: $currentUser"

$projectRoot = Split-Path -Parent $PSScriptRoot
$appMetadataPath = Join-Path $projectRoot 'app\build\outputs\apk\debug\output-metadata.json'
$testMetadataPath = Join-Path $projectRoot 'app\build\outputs\apk\androidTest\debug\output-metadata.json'
$appArtifact = Read-ApkOutputMetadata -MetadataPath $appMetadataPath -Label 'Debug app' -RequireApplicationId
$testArtifact = Read-ApkOutputMetadata -MetadataPath $testMetadataPath -Label 'Debug androidTest'
Write-Host "[PASS] Debug app identity derived from AGP metadata: $($appArtifact.ApplicationId)"
if (-not [string]::IsNullOrWhiteSpace($testArtifact.ApplicationId)) {
    Write-Host "[PASS] Debug test identity derived from AGP metadata: $($testArtifact.ApplicationId)"
} else {
    Write-Host '[INFO] Debug androidTest metadata omits applicationId; test package will be derived from the installed runner.'
}

if (-not [string]::IsNullOrWhiteSpace($testArtifact.ApplicationId) -and $appArtifact.ApplicationId -eq $testArtifact.ApplicationId) {
    Write-Host '[FAIL] Debug app and androidTest APK unexpectedly share the same applicationId.'
    exit 1
}

foreach ($artifact in @(
    [pscustomobject]@{ Label = 'Debug app'; Value = $appArtifact },
    [pscustomobject]@{ Label = 'Debug androidTest'; Value = $testArtifact }
)) {
    $install = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'install', '--user', [string]$currentUser, '-r', '-t', $artifact.Value.Apk) -TimeoutSeconds 90
    if ($install.StdOut) { Write-Host $install.StdOut.TrimEnd() }
    if ($install.StdErr) { Write-Host $install.StdErr.TrimEnd() }
    $installText = ($install.StdOut + "`n" + $install.StdErr)
    if ($install.ExitCode -ne 0 -or $installText -notmatch 'Success') {
        Write-Host "[FAIL] $($artifact.Label) APK install failed for Android user $currentUser on selected device: $($device.Serial)"
        exit 1
    }
}

Assert-PackageEnabledForUser -Adb $adb -Serial $device.Serial -UserId $currentUser -PackageName $appArtifact.ApplicationId -Label 'Debug app'

$instrumentationList = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'shell', 'pm', 'list', 'instrumentation') -TimeoutSeconds 15
if ($instrumentationList.ExitCode -ne 0 -or $instrumentationList.TimedOut) {
    Write-Host '[FAIL] Unable to query installed instrumentation runner.'
    exit 1
}

$runner = $null
$runnerTarget = $null
foreach ($line in ($instrumentationList.StdOut -split "`r?`n")) {
    if ($line -match '^instrumentation:([^\s]+)\s+\(target=([^\)]+)\)$') {
        $candidateRunner = $Matches[1]
        $candidateTarget = $Matches[2]
        if ($candidateTarget -eq $appArtifact.ApplicationId) {
            if (-not [string]::IsNullOrWhiteSpace($testArtifact.ApplicationId) -and -not $candidateRunner.StartsWith($testArtifact.ApplicationId + '/')) { continue }
            $runner = $candidateRunner
            $runnerTarget = $candidateTarget
            break
        }
    }
}
if (-not $runner) {
    Write-Host "[FAIL] Instrumentation runner targeting '$($appArtifact.ApplicationId)' was not found after installation."
    if ($instrumentationList.StdOut) { Write-Host '[INFO] Installed instrumentation:'; Write-Host $instrumentationList.StdOut.TrimEnd() }
    exit 1
}
Write-Host "[PASS] Instrumentation target matches installed debug app: $runnerTarget"
Write-Host "[PASS] Instrumentation runner resolved dynamically: $runner"
$runnerPackage = ($runner -split '/', 2)[0]
if (-not [string]::IsNullOrWhiteSpace($testArtifact.ApplicationId) -and $runnerPackage -ne $testArtifact.ApplicationId) {
    Write-Host "[FAIL] Installed runner package '$runnerPackage' does not match androidTest metadata '$($testArtifact.ApplicationId)'."
    exit 1
}
Assert-PackageEnabledForUser -Adb $adb -Serial $device.Serial -UserId $currentUser -PackageName $runnerPackage -Label 'Debug androidTest'

Write-Host "[GATE 7] Instrumentation on selected device/user only: $($device.Serial) | user $currentUser"
$testRun = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'shell', 'am', 'instrument', '--user', [string]$currentUser, '-w', $runner) -TimeoutSeconds 180
if ($testRun.StdOut) { Write-Host $testRun.StdOut.TrimEnd() }
if ($testRun.StdErr) { Write-Host $testRun.StdErr.TrimEnd() }

$testText = ($testRun.StdOut + "`n" + $testRun.StdErr)
if ($testRun.ExitCode -ne 0 -or $testText -match 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=Process crashed|Unable to find instrumentation target package') {
    Write-Host '[FAIL] Connected instrumentation failed on the selected healthy device/user.'
    $appPackages = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'shell', 'pm', 'list', 'packages', '--user', [string]$currentUser, $appArtifact.ApplicationId) -TimeoutSeconds 10
    $testPackages = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'shell', 'pm', 'list', 'packages', '--user', [string]$currentUser, $runnerPackage) -TimeoutSeconds 10
    if ($appPackages.StdOut) { Write-Host "[INFO] App package state: $($appPackages.StdOut.Trim())" }
    if ($testPackages.StdOut) { Write-Host "[INFO] Test package state: $($testPackages.StdOut.Trim())" }
    exit 1
}

$match = [regex]::Match($testText, 'OK \((\d+) tests?\)')
if (-not $match.Success -or [int]$match.Groups[1].Value -lt 1) {
    Write-Host '[FAIL] Instrumentation completed without proving that at least one test executed successfully.'
    exit 1
}

Write-Host "[PASS] Connected instrumentation passed on $($device.Model), Android user ${currentUser}: $($match.Groups[1].Value) test(s)."
exit 0
