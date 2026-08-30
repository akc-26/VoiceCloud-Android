$ErrorActionPreference = 'Continue'
$sourceRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).ProviderPath
Set-Location $sourceRoot
$failures = New-Object System.Collections.Generic.List[string]
$passes = 0
function Invoke-DiagnosticGate {
    param([string]$Label,[string]$FilePath,[string[]]$Arguments)
    Write-Host "[SOURCE] $Label"
    try { & $FilePath @Arguments; if ($LASTEXITCODE -eq 0) { $script:passes++; Write-Host "[DIAG-PASS] $Label" } else { $script:failures.Add("$Label :: exit $LASTEXITCODE"); Write-Host "[DIAG-FAIL] $Label :: exit $LASTEXITCODE" } }
    catch { $script:failures.Add("$Label :: $($_.Exception.Message)"); Write-Host "[DIAG-FAIL] $Label :: $($_.Exception.Message)" }
}
$pythonChecks = @(
 'ph13_r11_acceptance_wiring_regression.py',
 'ph13_r11_product_preservation_regression.py',
 'ph13_r11_device_closure_regression.py',
 'ph13_r09_full_ui_compile_risk_regression.py',
 'ph13_r08_full_ui_compile_risk_regression.py',
 'ph13_r07_compile_correction_regression.py',
 'ph13_r06_harness_correction_regression.py',
 'ph13_r01_unit_contract_regression.py','ph13_r01_source_check.py','ph13_r01_backend_contract_regression.py','ph13_r01_compile_surface_regression.py','ph13_r01_whitespace_regression.py',
 'ph12_r01_backend_contract_regression.py','ph12_r01_unit_contract_regression.py','ph12_r01_compile_surface_regression.py',
 'ph11_r02_backend_contract_regression.py','ph11_r02_titlecase_toast_regression.py','ph11_r02_corrections_regression.py','ph11_r02_compile_surface_regression.py','ph11_r02_compile_risk_regression.py','ph11_r02_economy_payment_regression.py',
 'ph09_r08_gradle_properties_authority.py','ph09_r08_compile_risk_regression.py','ph09_r08_warning_closure_regression.py','ph09_r08_device_closure_regression.py','ph07_r03_adb_daemon_regression.py','ph06_r03_hosting_compile_regression.py'
)
foreach ($scriptName in $pythonChecks) { Invoke-DiagnosticGate -Label $scriptName -FilePath 'python' -Arguments @("scripts\$scriptName") }
Write-Host '============================================================='
Write-Host "[SOURCE SUMMARY] $passes/$($pythonChecks.Count) source/regression gates passed."
if ($failures.Count -gt 0) { foreach ($failure in $failures) { Write-Host "[SUMMARY-FAIL] $failure" }; exit 1 }
Write-Host '[SOURCE SUMMARY] R11 final device-gate correction and all applicable R09/R08 functional, UI, security, payment and RTC authority checks passed.'
exit 0
