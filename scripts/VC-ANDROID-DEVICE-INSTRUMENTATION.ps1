$ErrorActionPreference = 'Stop'

function Quote-Arg([string]$arg) {
    if ($arg -notmatch '[\s"]') { return $arg }
    # The VoiceCloud acceptance arguments are paths/serials without embedded quotes.
    # Wrap whitespace-bearing values so Windows ProcessStartInfo preserves each argument.
    return '"' + ($arg -replace '"', '\"') + '"'
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

    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        try { $process.Kill() } catch { }
        return [pscustomobject]@{ ExitCode = 124; StdOut = ''; StdErr = 'Timed out'; TimedOut = $true }
    }

    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    return [pscustomobject]@{ ExitCode = $process.ExitCode; StdOut = $stdout; StdErr = $stderr; TimedOut = $false }
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

$deviceOutput = & $adb devices 2>&1
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

$projectRoot = Split-Path -Parent $PSScriptRoot
$appApk = Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk'
$testApk = Join-Path $projectRoot 'app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk'

if (-not (Test-Path $appApk)) {
    Write-Host "[FAIL] Debug APK not found: $appApk"
    exit 1
}
if (-not (Test-Path $testApk)) {
    Write-Host "[FAIL] Debug androidTest APK not found: $testApk"
    exit 1
}

foreach ($apk in @($appApk, $testApk)) {
    $install = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'install', '-r', '-t', $apk) -TimeoutSeconds 90
    if ($install.StdOut) { Write-Host $install.StdOut.TrimEnd() }
    if ($install.StdErr) { Write-Host $install.StdErr.TrimEnd() }
    $installText = ($install.StdOut + "`n" + $install.StdErr)
    if ($install.ExitCode -ne 0 -or $installText -notmatch 'Success') {
        Write-Host "[FAIL] APK install failed on selected device: $($device.Serial)"
        exit 1
    }
}

$instrumentationList = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'shell', 'pm', 'list', 'instrumentation') -TimeoutSeconds 15
if ($instrumentationList.ExitCode -ne 0) {
    Write-Host '[FAIL] Unable to query installed instrumentation runner.'
    exit 1
}

$runner = $null
foreach ($line in ($instrumentationList.StdOut -split "`r?`n")) {
    if ($line -match '^instrumentation:([^\s]+)\s+\(target=app\.voicecloud\.android\.debug\)$') {
        $runner = $Matches[1]
        break
    }
}
if (-not $runner) {
    Write-Host '[FAIL] VoiceCloud debug instrumentation runner was not found after APK installation.'
    exit 1
}

Write-Host "[GATE 6] Instrumentation on selected device only: $($device.Serial)"
$testRun = Invoke-CapturedProcess -FilePath $adb -Arguments @('-s', $device.Serial, 'shell', 'am', 'instrument', '-w', $runner) -TimeoutSeconds 120
if ($testRun.StdOut) { Write-Host $testRun.StdOut.TrimEnd() }
if ($testRun.StdErr) { Write-Host $testRun.StdErr.TrimEnd() }

$testText = ($testRun.StdOut + "`n" + $testRun.StdErr)
if ($testRun.ExitCode -ne 0 -or $testText -match 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=Process crashed') {
    Write-Host '[FAIL] Connected instrumentation failed on the selected healthy device.'
    exit 1
}

$match = [regex]::Match($testText, 'OK \((\d+) tests?\)')
if (-not $match.Success -or [int]$match.Groups[1].Value -lt 1) {
    Write-Host '[FAIL] Instrumentation completed without proving that at least one test executed successfully.'
    exit 1
}

Write-Host "[PASS] Connected instrumentation passed on $($device.Model): $($match.Groups[1].Value) test(s)."
exit 0
