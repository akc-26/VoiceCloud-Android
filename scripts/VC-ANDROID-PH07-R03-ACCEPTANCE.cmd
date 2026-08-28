@echo off
setlocal
cd /d "%~dp0.."
echo =============================================================
echo VoiceCloud Android PH07 R03 - Full Acceptance
echo Consumer Economy and Progression
echo Parent package: VC-ANDROID-PH06-R03
echo =============================================================
where python >nul 2>nul || (echo [FAIL] Python is required.& exit /b 1)
python scripts\ph07_r02_source_check.py || exit /b 1
python scripts\ph07_r02_preservation_regression.py || exit /b 1
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
echo [PASS] VC-ANDROID-PH07-R03 acceptance commands completed successfully.
