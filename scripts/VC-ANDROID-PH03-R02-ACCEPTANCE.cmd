@echo off
setlocal
cd /d "%~dp0.."
echo =============================================================
echo VoiceCloud Android PH03 R02 - Full Acceptance
echo Consumer Home, Discovery, Search, Profile and Social
echo Parent: PH02-R05 ae84dc7ea408c5d0948f2064e0955e088cbad957
echo Preservation policy: PH01/PH02 regressions run before Android build gates
echo =============================================================

where powershell >nul 2>nul || (echo [FAIL] PowerShell is required for source-contract verification.& exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH03-R02-SOURCE-CHECK.ps1 || exit /b 1
where python >nul 2>nul && (python scripts\ph03_r02_lazy_list_key_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph03_r01_source_check.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph03_r01_module_dependency_audit.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph02_r01_source_check.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph02_r02_gradle_kotlin_dsl_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph02_r02_delivery_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph02_r03_compile_surface_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph02_r05_bootstrap_signature_regression.py || exit /b 1)
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
  echo [INFO] Standard Gradle Wrapper is not present in this clean package. Generating it from verified cached Gradle 9.5.0...
  call scripts\BOOTSTRAP-GRADLE-WRAPPER.cmd || exit /b 1
)
if not exist gradlew.bat (echo [FAIL] Standard gradlew.bat was not generated.& exit /b 1)

echo [GATE 1] Clean generated outputs
call gradlew.bat clean --stacktrace || exit /b 1

echo [GATE 2-5] Broad Android build sweep - compile + unit tests + lint + assemblies
 echo [INFO] --continue is intentional so one workstation run exposes every reachable build failure.
call gradlew.bat :app:compileDebugKotlin :app:compileStagingKotlin :app:compileReleaseKotlin test lintDebug lintStaging lintRelease :app:assembleDebug :app:assembleStaging :app:assembleRelease --continue --stacktrace --warning-mode all || exit /b 1

echo [INFO] Device instrumentation is the final automated device gate.
set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if exist "%ADB%" for /f "skip=1 tokens=1,2" %%D in ('"%ADB%" devices') do if "%%E"=="device" (
  echo [GATE 6] connectedDebugAndroidTest
  call gradlew.bat :app:connectedDebugAndroidTest --stacktrace || exit /b 1
  goto device_done
)
echo [WARN] No authorized adb device/emulator detected. Run :app:connectedDebugAndroidTest before PH03 manual approval.
:device_done
echo [PASS] VC-ANDROID-PH03-R02 acceptance commands completed successfully.
