@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
set /a VC_FAILURE_GROUPS=0
set "VC_DEVICE_PENDING=0"

echo =============================================================
echo VoiceCloud Android PH13 R12 - Workstation-Safe Final Acceptance
echo Parent package: VC-ANDROID-PH13-R11
echo Scope: acceptance-harness correction only; Android product source is byte-identical to build-proven R09/R10/R11
echo R12 fixes workstation-generated artifact classification and Windows device exit-code handling.
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
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R12-SOURCE-DIAGNOSTIC.ps1
    if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
  )
)

call scripts\VC-ANDROID-JAVA-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
call scripts\VC-ANDROID-SDK-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1

if "!VC_FAILURE_GROUPS!"=="0" (
  echo [R12 DEVICE CLOSURE] Using unchanged resilient R11 device closure after R12 source/preservation validation.
  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R11-DEVICE-CLOSURE.ps1
  set "VC_DEVICE_RC=!ERRORLEVEL!"
  if "!VC_DEVICE_RC!"=="2" (
    echo [DEVICE-PENDING] No healthy authorized Android device is currently available.
    echo [DEVICE-PENDING] This is a nonfatal environment condition; the build-proven host acceptance remains valid.
    set "VC_DEVICE_PENDING=1"
  ) else if not "!VC_DEVICE_RC!"=="0" (
    echo [GATE-FAIL] R12 physical-device closure encountered a genuine device/instrumentation failure.
    set /a VC_FAILURE_GROUPS+=1
  ) else (
    echo [GATE-PASS] R12 physical-device closure passed.
  )
) else (
  echo [GATE-SKIP] Device closure skipped because a prerequisite/source gate failed.
)

echo [R12 INTEGRITY] Product/delivery integrity
where python >nul 2>nul
if errorlevel 1 (
  echo [GATE-FAIL] Python unavailable; R12 integrity could not run.
  set /a VC_FAILURE_GROUPS+=1
) else (
  python scripts\ph13_r12_delivery_integrity.py
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo =============================================================
if not "!VC_FAILURE_GROUPS!"=="0" (
  echo [FAIL] VC-ANDROID-PH13-R12 acceptance completed with !VC_FAILURE_GROUPS! genuine failing group^(s^).
  exit /b 1
)
if "!VC_DEVICE_PENDING!"=="1" (
  echo [PASS] VC-ANDROID-PH13-R12 host acceptance completed successfully.
  echo [PASS] R09 build-proven Gates 1-6 and R12 source/integrity gates are satisfied.
  echo [PENDING] Physical-device instrumentation remains pending only because no healthy authorized device is currently available.
  echo [INFO] Re-run this same R12 acceptance when a device is connected; it preflights before any fallback build.
  exit /b 0
)
echo [PASS] VC-ANDROID-PH13-R12 acceptance completed successfully.
echo [PASS] R09 build-proven Gates 1-6 + resilient physical-device closure + R12 integrity are all satisfied.
exit /b 0
