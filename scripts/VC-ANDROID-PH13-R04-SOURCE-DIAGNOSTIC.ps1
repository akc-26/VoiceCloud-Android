$ErrorActionPreference = 'Continue'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
Set-Location $root
$env:PYTHONDONTWRITEBYTECODE = '1'

$failures = New-Object System.Collections.Generic.List[string]
$passes = 0
$total = 0

function Invoke-DiagnosticGate {
    param(
        [Parameter(Mandatory=$true)][string]$Label,
        [Parameter(Mandatory=$true)][string]$FilePath,
        [Parameter(Mandatory=$true)][string[]]$Arguments
    )
    $script:total++
    Write-Host "[DIAG] $Label"
    try {
        & $FilePath @Arguments
        $rc = $LASTEXITCODE
        if ($null -eq $rc) { $rc = 0 }
        if ($rc -eq 0) {
            $script:passes++
            Write-Host "[DIAG-PASS] $Label"
        } else {
            $script:failures.Add("$Label (exit $rc)")
            Write-Host "[DIAG-FAIL] $Label (exit $rc)"
        }
    } catch {
        $script:failures.Add("$Label ($($_.Exception.Message))")
        Write-Host "[DIAG-FAIL] $Label - $($_.Exception.Message)"
    }
}

Invoke-DiagnosticGate -Label 'PowerShell parser validation' -FilePath 'powershell' -Arguments @('-NoProfile','-ExecutionPolicy','Bypass','-File','scripts\VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1')

$pythonChecks = @(
    'ph13_r04_acceptance_wiring_regression.py',
    'ph13_r04_corrective_regression.py',
    'ph13_r04_preservation_regression.py',
    'ph13_r01_unit_contract_regression.py',
    'ph13_r01_source_check.py',
    'ph13_r01_backend_contract_regression.py',
    'ph13_r01_compile_surface_regression.py',
    'ph13_r01_preservation_regression.py',
    'ph13_r01_whitespace_regression.py',
    'ph12_r01_backend_contract_regression.py',
    'ph12_r01_unit_contract_regression.py',
    'ph12_r01_compile_surface_regression.py',
    'ph11_r02_backend_contract_regression.py',
    'ph11_r02_titlecase_toast_regression.py',
    'ph11_r02_corrections_regression.py',
    'ph11_r02_compile_surface_regression.py',
    'ph11_r02_compile_risk_regression.py',
    'ph11_r02_economy_payment_regression.py',
    'ph09_r08_gradle_properties_authority.py',
    'ph09_r08_compile_risk_regression.py',
    'ph09_r08_warning_closure_regression.py',
    'ph09_r08_device_closure_regression.py',
    'ph07_r03_adb_daemon_regression.py',
    'ph06_r03_hosting_compile_regression.py'
)
foreach ($scriptName in $pythonChecks) {
    Invoke-DiagnosticGate -Label $scriptName -FilePath 'python' -Arguments @("scripts\$scriptName")
}

Write-Host '============================================================='
Write-Host "[DIAGNOSTIC SUMMARY] Source/regression gates: $passes/$total passed; $($failures.Count) failed."
if ($failures.Count -gt 0) {
    foreach ($failure in $failures) { Write-Host "[SUMMARY-FAIL] $failure" }
    exit 1
}
Write-Host '[DIAGNOSTIC SUMMARY] All source/regression gates passed.'
exit 0
