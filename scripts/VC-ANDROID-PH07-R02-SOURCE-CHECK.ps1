$ErrorActionPreference='Stop'
Set-Location (Join-Path $PSScriptRoot '..')
python scripts\ph07_r02_source_check.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host '[PASS] VC-ANDROID-PH07-R02 source checks completed.'
