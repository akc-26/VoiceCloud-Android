@echo off
setlocal
cd /d "%~dp0.."
echo =============================================================
echo VoiceCloud Android PH02 R03 - Full Acceptance
echo Dual Portal Authentication and Account Lifecycle
echo Parent: PH01-R09 ce56b1e36a4b2b563a65e4699df9193751f0978d
echo Cumulative regression policy: every prior workstation fix retained
echo =============================================================
where powershell >nul 2>nul || (echo [FAIL] PowerShell is required for source-contract verification.& exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH02-R03-SOURCE-CHECK.ps1 || exit /b 1
where python >nul 2>nul && (python scripts\ph02_r01_source_check.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph02_r02_gradle_kotlin_dsl_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph02_r02_delivery_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph02_r03_compile_surface_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r09_source_check.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r02_windows_acceptance_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r03_agp9_builtin_kotlin_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r04_sdk_logging_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r05_module_abi_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r08_live_server_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph01_r09_wrapper_regression.py || exit /b 1)

call scripts\VC-ANDROID-JAVA-ENV.cmd || exit /b 1
call scripts\VC-ANDROID-SDK-ENV.cmd || exit /b 1

echo [GATE 0] Raspberry Pi live API / Socket.IO / Web connectivity
if /I "%VOICECLOUD_SKIP_LIVE_CONNECTIVITY%"=="1" (
  echo [WARN] Live connectivity explicitly skipped by VOICECLOUD_SKIP_LIVE_CONNECTIVITY=1
) else (
  call scripts\VC-ANDROID-LIVE-CONNECTIVITY-CHECK.cmd || exit /b 1
)

if not exist gradle\wrapper\gradle-wrapper.jar (
  echo [INFO] Standard Gradle Wrapper is not present in this clean package. Generating it from the cached local Gradle 9.5.0 distribution...
  call scripts\BOOTSTRAP-GRADLE-WRAPPER.cmd || exit /b 1
)
if not exist gradlew.bat (echo [FAIL] Standard gradlew.bat was not generated.& exit /b 1)

echo [GATE 1] Kotlin compile matrix - debug / staging / release
echo [INFO] --continue is intentional: one failed run should expose every reachable variant compile error.
call gradlew.bat :app:compileDebugKotlin :app:compileStagingKotlin :app:compileReleaseKotlin --continue --stacktrace || exit /b 1

echo [GATE 2] Unit tests
call gradlew.bat test --continue --stacktrace || exit /b 1
echo [GATE 3] Android lint matrix
call gradlew.bat lintDebug lintStaging lintRelease --continue --stacktrace || exit /b 1
echo [GATE 4] Assembly matrix
call gradlew.bat :app:assembleDebug :app:assembleStaging :app:assembleRelease --continue --stacktrace || exit /b 1

echo [INFO] Device instrumentation is the final PH02 automated device gate.
set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if exist "%ADB%" for /f "skip=1 tokens=1,2" %%D in ('"%ADB%" devices') do if "%%E"=="device" (
  echo [GATE 5] connectedDebugAndroidTest
  call gradlew.bat :app:connectedDebugAndroidTest --stacktrace || exit /b 1
  goto device_done
)
echo [WARN] No authorized adb device/emulator detected. Run :app:connectedDebugAndroidTest before PH02 manual approval.
:device_done
echo [PASS] VC-ANDROID-PH02-R03 acceptance commands completed successfully.
