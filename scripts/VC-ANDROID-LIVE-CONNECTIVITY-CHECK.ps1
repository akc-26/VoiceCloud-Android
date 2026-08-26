$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$defaultLive = 'https://voicecloud.tailfca77b.ts.net'

function Read-GradleProperties([string]$path) {
    $values = @{}
    if (-not (Test-Path $path)) { return $values }
    foreach ($line in Get-Content $path) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#') -or -not $trimmed.Contains('=')) { continue }
        $parts = $trimmed.Split('=', 2)
        $values[$parts[0].Trim()] = $parts[1].Trim()
    }
    return $values
}

$project = Read-GradleProperties (Join-Path $root 'gradle.properties')
$user = Read-GradleProperties (Join-Path $env:USERPROFILE '.gradle\gradle.properties')
function Resolve-Property([string]$name) {
    if ($user.ContainsKey($name) -and -not [string]::IsNullOrWhiteSpace($user[$name])) { return $user[$name] }
    if ($project.ContainsKey($name) -and -not [string]::IsNullOrWhiteSpace($project[$name])) { return $project[$name] }
    return $defaultLive
}
function Normalize-Api([string]$value) {
    $base = $value.Trim().TrimEnd('/')
    if ($base.EndsWith('/api/v1', [System.StringComparison]::OrdinalIgnoreCase)) { return "$base/" }
    return "$base/api/v1/"
}
function Normalize-Socket([string]$value) { return $value.Trim().TrimEnd('/') }
function Normalize-Web([string]$value) { return $value.Trim().TrimEnd('/') + '/' }

$api = Normalize-Api (Resolve-Property 'VOICECLOUD_DEBUG_API_BASE_URL')
$socket = Normalize-Socket (Resolve-Property 'VOICECLOUD_DEBUG_SOCKET_BASE_URL')
$web = Normalize-Web (Resolve-Property 'VOICECLOUD_DEBUG_WEB_BASE_URL')

Write-Host "[INFO] VoiceCloud API authority:    $api"
Write-Host "[INFO] VoiceCloud Socket authority: $socket/realtime (path /socket.io)"
Write-Host "[INFO] VoiceCloud Web authority:    $web"

$curl = Get-Command curl.exe -ErrorAction SilentlyContinue
if (-not $curl) { Write-Host '[FAIL] curl.exe is required for live connectivity verification.'; exit 1 }

function Invoke-CurlProbe([string]$label, [string]$url, [string]$outputFile = 'NUL', [int]$attempts = 3) {
    for ($attempt = 1; $attempt -le $attempts; $attempt++) {
        & curl.exe -fsS --connect-timeout 10 --max-time 20 -o $outputFile $url
        if ($LASTEXITCODE -eq 0) { return $true }
        if ($attempt -lt $attempts) {
            Write-Host "[WARN] $label probe attempt $attempt/$attempts failed; retrying in 3 seconds..."
            Start-Sleep -Seconds 3
        }
    }
    return $false
}

$apiProbe = "${api}config/mobile?platform=android"
if (-not (Invoke-CurlProbe 'REST API' $apiProbe)) { Write-Host "[FAIL] VoiceCloud REST API is not reachable: $apiProbe"; exit 1 }
Write-Host "[PASS] VoiceCloud REST API reachable: $apiProbe"

$tempSocket = Join-Path $env:TEMP 'voicecloud-socket-handshake.txt'
Remove-Item $tempSocket -Force -ErrorAction SilentlyContinue
$socketProbe = "$socket/socket.io/?EIO=4&transport=polling"
if (-not (Invoke-CurlProbe 'Socket.IO' $socketProbe $tempSocket)) { Write-Host "[FAIL] VoiceCloud Socket.IO transport is not reachable: $socketProbe"; exit 1 }
if (-not (Test-Path $tempSocket)) { Write-Host "[FAIL] Socket.IO response file was not created."; exit 1 }
$socketBody = [IO.File]::ReadAllText($tempSocket)
Remove-Item $tempSocket -Force -ErrorAction SilentlyContinue
if (-not $socketBody.Contains('"sid"')) { Write-Host '[FAIL] Socket.IO endpoint responded but did not return a valid Engine.IO handshake.'; exit 1 }
Write-Host "[PASS] VoiceCloud Socket.IO transport reachable: $socketProbe"

if (-not (Invoke-CurlProbe 'Web' $web)) { Write-Host "[FAIL] VoiceCloud Web authority is not reachable: $web"; exit 1 }
Write-Host "[PASS] VoiceCloud Web authority reachable: $web"
Write-Host '[PASS] VoiceCloud Raspberry Pi API/Socket/Web connectivity verified.'
