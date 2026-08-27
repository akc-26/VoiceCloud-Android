@echo off
setlocal
cd /d "%~dp0.."
echo =============================================================
echo VoiceCloud Android PH05 R04 - Full Acceptance
echo Live Room Listener, Access, Realtime and Engagement
echo Parent: PH04-R04 702cf4c1ae966cf826ddbc64c6cd86487f6d91e9
echo Includes: PH05 R03 scope plus centralized Compose ARGB runtime correction
echo Preservation policy: PH01-PH04 regressions run before Android build gates
echo =============================================================

where powershell >nul 2>nul || (echo [FAIL] PowerShell is required for source-contract verification.& exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH05-R01-SOURCE-CHECK.ps1 || exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH05-R02-SOURCE-CHECK.ps1 || exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH05-R03-SOURCE-CHECK.ps1 || exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH05-R04-SOURCE-CHECK.ps1 || exit /b 1
where python >nul 2>nul && (python scripts\ph05_r01_source_check.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph05_r02_agp9_branding_gradle_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph05_r03_splashscreen_compile_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph05_r04_brand_color_runtime_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph05_r01_lifecycle_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph05_r01_module_dependency_audit.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph04_r01_source_check.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph04_r02_compose_scope_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph04_r03_device_gate_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph04_r04_ui_quality_regression.py || exit /b 1)
where python >nul 2>nul && (python scripts\ph04_r01_module_dependency_audit.py || exit /b 1)
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

echo [GATE 2-5] Broad Android build sweep - compile + unit tests + lint + assemblies + androidTest APK
echo [INFO] --continue is intentional so one workstation run exposes every reachable build failure.
call gradlew.bat :app:compileDebugKotlin :app:compileStagingKotlin :app:compileReleaseKotlin test lintDebug lintStaging lintRelease :app:assembleDebug :app:assembleStaging :app:assembleRelease :app:assembleDebugAndroidTest --continue --stacktrace --warning-mode all || exit /b 1

echo [GATE 6] Healthy-device isolated connected instrumentation
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-DEVICE-INSTRUMENTATION.ps1
set "DEVICE_GATE=%ERRORLEVEL%"
if "%DEVICE_GATE%"=="0" goto device_done
if "%DEVICE_GATE%"=="2" (
  echo [WARN] No healthy device was available. Connected instrumentation remains required before manual phase approval.
  goto device_done
)
exit /b %DEVICE_GATE%

:device_done
echo [PASS] VC-ANDROID-PH05-R04 acceptance commands completed successfully.
