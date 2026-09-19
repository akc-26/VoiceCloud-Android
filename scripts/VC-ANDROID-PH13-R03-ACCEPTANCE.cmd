@echo off
setlocal
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
echo =============================================================
echo VoiceCloud Android PH13 R03 - Full Acceptance
echo Isolated Gradle workspace correction
echo Product source: unchanged from PH13-R01/R02
echo Parent package: VC-ANDROID-PH13-R02
echo =============================================================
where powershell >nul 2>nul || (echo [FAIL] Windows PowerShell is required.& exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1 || exit /b 1
where python >nul 2>nul || (echo [FAIL] Python is required.& exit /b 1)
python scripts\ph13_r03_acceptance_wiring_regression.py || exit /b 1
python scripts\ph13_r03_corrective_regression.py || exit /b 1
python scripts\ph13_r03_preservation_regression.py || exit /b 1
rem PH13 product authority is unchanged; replay all functional source/contract gates.
python scripts\ph13_r01_unit_contract_regression.py || exit /b 1
python scripts\ph13_r01_source_check.py || exit /b 1
python scripts\ph13_r01_backend_contract_regression.py || exit /b 1
python scripts\ph13_r01_compile_surface_regression.py || exit /b 1
python scripts\ph13_r01_preservation_regression.py || exit /b 1
python scripts\ph13_r01_whitespace_regression.py || exit /b 1
rem Retained compatible inherited authorities.
python scripts\ph12_r01_backend_contract_regression.py || exit /b 1
python scripts\ph12_r01_unit_contract_regression.py || exit /b 1
python scripts\ph12_r01_compile_surface_regression.py || exit /b 1
python scripts\ph11_r02_backend_contract_regression.py || exit /b 1
python scripts\ph11_r02_titlecase_toast_regression.py || exit /b 1
python scripts\ph11_r02_corrections_regression.py || exit /b 1
python scripts\ph11_r02_compile_surface_regression.py || exit /b 1
python scripts\ph11_r02_compile_risk_regression.py || exit /b 1
python scripts\ph11_r02_economy_payment_regression.py || exit /b 1
python scripts\ph09_r08_gradle_properties_authority.py || exit /b 1
python scripts\ph09_r08_compile_risk_regression.py || exit /b 1
python scripts\ph09_r08_warning_closure_regression.py || exit /b 1
python scripts\ph09_r08_device_closure_regression.py || exit /b 1
python scripts\ph07_r03_adb_daemon_regression.py || exit /b 1
python scripts\ph06_r03_hosting_compile_regression.py || exit /b 1
call scripts\VC-ANDROID-JAVA-ENV.cmd || exit /b 1
call scripts\VC-ANDROID-SDK-ENV.cmd || exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R03-ISOLATED-BUILD.ps1 || exit /b 1
echo [GATE 8] Original delivery-tree integrity
python scripts\ph13_r03_delivery_hygiene.py || exit /b 1
echo [PASS] VC-ANDROID-PH13-R03 acceptance commands completed successfully.
