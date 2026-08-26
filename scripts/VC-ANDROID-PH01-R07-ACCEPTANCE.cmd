@echo off
setlocal
cd /d "%~dp0.."
echo =============================================================
echo VoiceCloud Android PH01 R07 - Full Acceptance
echo =============================================================
where powershell >nul 2>nul || (echo [FAIL] PowerShell is required for source-contract verification.& exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH01-R07-SOURCE-CHECK.ps1 || exit /b 1
where python >nul 2>nul && (python scripts\ph01_r07_source_check.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r02_windows_acceptance_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r03_agp9_builtin_kotlin_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r04_sdk_logging_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r05_module_abi_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r07_endpoint_realtime_regression.py || exit /b 1)

call scripts\VC-ANDROID-JAVA-ENV.cmd || exit /b 1
call scripts\VC-ANDROID-SDK-ENV.cmd || exit /b 1
call scripts\VC-ANDROID-LOCAL-DEVICE-BACKEND.cmd || exit /b 1

if not exist gradle\wrapper\gradle-wrapper.jar (
  echo [INFO] Standard Gradle Wrapper is not generated yet. Bootstrapping Gradle 9.5.0...
  call scripts\BOOTSTRAP-GRADLE-WRAPPER.cmd || exit /b 1
)
if not exist gradlew.bat (echo [FAIL] Standard gradlew.bat was not generated.& exit /b 1)

echo [GATE 1] compileDebugKotlin
call gradlew.bat :app:compileDebugKotlin --stacktrace || exit /b 1
echo [GATE 2] compileStagingKotlin
call gradlew.bat :app:compileStagingKotlin --stacktrace || exit /b 1
echo [GATE 3] compileReleaseKotlin
call gradlew.bat :app:compileReleaseKotlin --stacktrace || exit /b 1

echo [GATE 4] Unit tests
call gradlew.bat test --stacktrace || exit /b 1
echo [GATE 5] Android lint
call gradlew.bat lintDebug lintStaging lintRelease --stacktrace || exit /b 1
echo [GATE 6] Assemblies
call gradlew.bat :app:assembleDebug :app:assembleStaging :app:assembleRelease --stacktrace || exit /b 1

echo [INFO] Device instrumentation is the final PH01 device gate.
set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if exist "%ADB%" for /f "skip=1 tokens=1,2" %%D in ('"%ADB%" devices') do if "%%E"=="device" (
  echo [GATE 7] connectedDebugAndroidTest
  call gradlew.bat :app:connectedDebugAndroidTest --stacktrace || exit /b 1
  goto device_done
)
echo [WARN] No authorized adb device/emulator detected. Run :app:connectedDebugAndroidTest before PH01 manual approval.
:device_done
echo [PASS] VC-ANDROID-PH01-R07 acceptance commands completed successfully.
