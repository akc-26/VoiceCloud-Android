$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
$checks = @(
  "scripts\ph08_r02_source_check.py",
  "scripts\ph08_r02_product_quality_regression.py",
  "scripts\ph08_r02_payment_regression.py",
  "scripts\ph08_r02_preservation_regression.py",
  "scripts\ph08_r02_compile_risk_regression.py",
  "scripts\ph08_r01_compile_risk_regression.py",
  "scripts\ph07_r03_adb_daemon_regression.py",
  "scripts\ph06_r03_hosting_compile_regression.py"
)
foreach ($check in $checks) {
  python $check
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
Write-Host "[PASS] VC-ANDROID-PH08-R02 source/regression authority complete"
exit 0
