@echo off
setlocal
cd /d "%~dp0.."
echo =============================================================
echo VoiceCloud Android PH09 R08 - Full Acceptance
echo Preferences + Security + Safety + CMS + Support
echo R08 closure: full PowerShell parser + device harness + warning closure
echo Parent Git baseline: VC-ANDROID-PH08-R02 @ 1739f081ac386acca9a24b37863b72c4db952089
echo =============================================================
where powershell >nul 2>nul || (echo [FAIL] Windows PowerShell is required.& exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-POWERSHELL-PARSER-CHECK.ps1 || exit /b 1
where python >nul 2>nul || (echo [FAIL] Python is required.& exit /b 1)
python scripts\ph09_r08_acceptance_wiring_regression.py || exit /b 1
python scripts\ph09_r08_powershell_surface_regression.py || exit /b 1
python scripts\ph09_r08_source_check.py || exit /b 1
python scripts\ph09_r08_product_quality_regression.py || exit /b 1
python scripts\ph09_r08_security_authority_regression.py || exit /b 1
python scripts\ph09_r08_gradle_properties_authority.py || exit /b 1
python scripts\ph09_r08_windows_preflight_regression.py || exit /b 1
python scripts\ph09_r08_preservation_regression.py || exit /b 1
python scripts\ph09_r08_compile_surface_regression.py || exit /b 1
python scripts\ph09_r08_compile_risk_regression.py || exit /b 1
python scripts\ph09_r08_warning_closure_regression.py || exit /b 1
python scripts\ph09_r08_device_closure_regression.py || exit /b 1
python scripts\ph08_r02_payment_regression.py || exit /b 1
python scripts\ph08_r02_compile_risk_regression.py || exit /b 1
python scripts\ph07_r03_adb_daemon_regression.py || exit /b 1
python scripts\ph06_r03_hosting_compile_regression.py || exit /b 1
call scripts\VC-ANDROID-JAVA-ENV.cmd || exit /b 1
call scripts\VC-ANDROID-SDK-ENV.cmd || exit /b 1
if not exist gradle\wrapper\gradle-wrapper.jar call scripts\BOOTSTRAP-GRADLE-WRAPPER.cmd || exit /b 1
if not exist gradlew.bat (echo [FAIL] gradlew.bat was not generated.& exit /b 1)
echo [GATE 1] compileDebugKotlin
call gradlew.bat :app:compileDebugKotlin --stacktrace || exit /b 1
echo [GATE 2] compileStagingKotlin
call gradlew.bat :app:compileStagingKotlin --stacktrace || exit /b 1
echo [GATE 3] compileReleaseKotlin
call gradlew.bat :app:compileReleaseKotlin --stacktrace || exit /b 1
echo [GATE 4] Unit tests
call gradlew.bat test --stacktrace || exit /b 1
echo [GATE 5] Lint
call gradlew.bat lintDebug lintStaging lintRelease --stacktrace || exit /b 1
echo [GATE 6] Assemblies
call gradlew.bat :app:assembleDebug :app:assembleStaging :app:assembleRelease :app:assembleDebugAndroidTest --stacktrace || exit /b 1
echo [GATE 7] Physical-device instrumentation
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-DEVICE-INSTRUMENTATION.ps1
set "DEVICE_GATE=%ERRORLEVEL%"
if "%DEVICE_GATE%"=="2" echo [WARN] No healthy device was available; manual device QA remains required.
if not "%DEVICE_GATE%"=="0" if not "%DEVICE_GATE%"=="2" exit /b %DEVICE_GATE%
echo [PASS] VC-ANDROID-PH09-R08 acceptance commands completed successfully.
