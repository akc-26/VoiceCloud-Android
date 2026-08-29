@echo off
setlocal EnableExtensions
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
set /a VC_FAILURE_GROUPS=0

echo =============================================================
echo VoiceCloud Android PH13 R10 - R09 Build-Evidence Preserving Device Closure
echo Parent package: VC-ANDROID-PH13-R09
echo Scope: acceptance/device-harness correction only; Android product source is byte-identical to R09
echo IMPORTANT: supplied R09 workstation log already proved Debug/Staging/Release compile, unit tests, lint and assemblies.
echo R10 therefore verifies identical product hashes and retests only the previously failing physical-device closure.
echo =============================================================

where powershell >nul 2>nul
if errorlevel 1 (
  echo [PRECHECK-FAIL] Windows PowerShell is required.
  set /a VC_FAILURE_GROUPS+=1
) else (
  where python >nul 2>nul
  if errorlevel 1 (
    echo [PRECHECK-FAIL] Python is required for source/regression diagnostics.
    set /a VC_FAILURE_GROUPS+=1
  ) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R10-SOURCE-DIAGNOSTIC.ps1
    if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
  )
)

call scripts\VC-ANDROID-JAVA-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
call scripts\VC-ANDROID-SDK-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1

echo [R10 DEVICE CLOSURE] Reusing verified R09 build artifacts when available; otherwise building Debug + androidTest prerequisites only.
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R10-DEVICE-CLOSURE.ps1
set "VC_DEVICE_RC=%ERRORLEVEL%"
if "%VC_DEVICE_RC%"=="2" (
  echo [WARN] No healthy authorized Android device was available; physical-device closure remains pending.
  set /a VC_FAILURE_GROUPS+=1
) else if not "%VC_DEVICE_RC%"=="0" (
  set /a VC_FAILURE_GROUPS+=1
)

echo [R10 INTEGRITY] Product/delivery integrity
where python >nul 2>nul
if errorlevel 1 (
  echo [GATE-FAIL] Python unavailable; R10 integrity could not run.
  set /a VC_FAILURE_GROUPS+=1
) else (
  python scripts\ph13_r10_delivery_integrity.py
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo =============================================================
if not "%VC_FAILURE_GROUPS%"=="0" (
  echo [FAIL] VC-ANDROID-PH13-R10 corrective acceptance completed with %VC_FAILURE_GROUPS% failing group^(s^).
  exit /b 1
)
echo [PASS] VC-ANDROID-PH13-R10 corrective acceptance completed successfully.
echo [PASS] R09 workstation Gates 1-6 + R10 resilient physical-device closure + R10 integrity are all satisfied.
exit /b 0
