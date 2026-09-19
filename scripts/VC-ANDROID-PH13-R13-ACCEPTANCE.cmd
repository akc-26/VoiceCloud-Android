@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
set /a VC_FAILURE_GROUPS=0
set "VC_DEVICE_PENDING=0"

echo =============================================================
echo VoiceCloud Android PH13 R13 - Instrumentation Palette Closure
echo Parent package: VC-ANDROID-PH13-R12
echo Scope: androidTest authority correction only; production app/UI/API/runtime source unchanged
echo R13 corrects stale pre-redesign palette assertions discovered by real R12 device instrumentation.
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
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R13-SOURCE-DIAGNOSTIC.ps1
    if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
  )
)

call scripts\VC-ANDROID-JAVA-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
call scripts\VC-ANDROID-SDK-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1

if "!VC_FAILURE_GROUPS!"=="0" (
  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R13-DEVICE-CLOSURE.ps1
  set "VC_DEVICE_RC=!ERRORLEVEL!"
  if "!VC_DEVICE_RC!"=="2" (
    echo [DEVICE-PENDING] No healthy authorized Android device is currently available.
    set "VC_DEVICE_PENDING=1"
  ) else if not "!VC_DEVICE_RC!"=="0" (
    echo [GATE-FAIL] R13 corrected instrumentation/device closure failed.
    set /a VC_FAILURE_GROUPS+=1
  ) else (
    echo [GATE-PASS] R13 corrected physical-device instrumentation passed.
  )
) else (
  echo [GATE-SKIP] Corrected device instrumentation skipped because a prerequisite/source gate failed.
)

echo [R13 INTEGRITY] Product/delivery integrity
where python >nul 2>nul
if errorlevel 1 (
  echo [GATE-FAIL] Python unavailable; R13 integrity could not run.
  set /a VC_FAILURE_GROUPS+=1
) else (
  python scripts\ph13_r13_delivery_integrity.py
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo =============================================================
if not "!VC_FAILURE_GROUPS!"=="0" (
  echo [FAIL] VC-ANDROID-PH13-R13 acceptance completed with !VC_FAILURE_GROUPS! genuine failing group^(s^).
  exit /b 1
)
if "!VC_DEVICE_PENDING!"=="1" (
  echo [PASS] VC-ANDROID-PH13-R13 host/source acceptance completed successfully.
  echo [PENDING] Corrected physical-device instrumentation is pending only because no healthy authorized device is available.
  exit /b 0
)
echo [PASS] VC-ANDROID-PH13-R13 acceptance completed successfully.
echo [PASS] Build-proven production app preserved and corrected premium-palette instrumentation passed on device.
exit /b 0
