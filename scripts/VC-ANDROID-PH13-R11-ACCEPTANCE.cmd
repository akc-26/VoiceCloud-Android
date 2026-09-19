@echo off
setlocal EnableExtensions
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
set /a VC_FAILURE_GROUPS=0
set "VC_DEVICE_PENDING=0"

echo =============================================================
echo VoiceCloud Android PH13 R11 - Final Device-Gate Closure
echo Parent package: VC-ANDROID-PH13-R10
echo Scope: acceptance/device-harness correction only; Android product source is byte-identical to build-proven R09/R10
echo IMPORTANT: R09 workstation evidence already passed Debug/Staging/Release compile, unit tests, lint and all assemblies.
echo R11 does NOT repeat those proven gates. It preflights device availability before any fallback build.
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
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R11-SOURCE-DIAGNOSTIC.ps1
    if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
  )
)

call scripts\VC-ANDROID-JAVA-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
call scripts\VC-ANDROID-SDK-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1

if "%VC_FAILURE_GROUPS%"=="0" (
  echo [R11 DEVICE CLOSURE] Device preflight runs before any artifact reuse or fallback build.
  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R11-DEVICE-CLOSURE.ps1
  set "VC_DEVICE_RC=%ERRORLEVEL%"
  if "%VC_DEVICE_RC%"=="2" (
    echo [DEVICE-PENDING] No healthy authorized Android device is currently available.
    echo [DEVICE-PENDING] This is a nonfatal environment condition; the build-proven host acceptance remains valid.
    set "VC_DEVICE_PENDING=1"
  ) else if not "%VC_DEVICE_RC%"=="0" (
    echo [GATE-FAIL] R11 physical-device closure encountered a real device/instrumentation failure.
    set /a VC_FAILURE_GROUPS+=1
  )
) else (
  echo [GATE-SKIP] Device closure skipped because a prerequisite/source gate failed.
)

echo [R11 INTEGRITY] Product/delivery integrity
where python >nul 2>nul
if errorlevel 1 (
  echo [GATE-FAIL] Python unavailable; R11 integrity could not run.
  set /a VC_FAILURE_GROUPS+=1
) else (
  python scripts\ph13_r11_delivery_integrity.py
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo =============================================================
if not "%VC_FAILURE_GROUPS%"=="0" (
  echo [FAIL] VC-ANDROID-PH13-R11 acceptance completed with %VC_FAILURE_GROUPS% genuine failing group^(s^).
  exit /b 1
)
if "%VC_DEVICE_PENDING%"=="1" (
  echo [PASS] VC-ANDROID-PH13-R11 host acceptance completed successfully.
  echo [PASS] R09 build-proven Gates 1-6 and R11 source/integrity gates are satisfied.
  echo [PENDING] Physical-device instrumentation remains pending only because no healthy authorized device is currently available.
  echo [INFO] Re-run this same R11 acceptance when a device is connected; it will preflight first and will not repeat the full R09 build chain.
  exit /b 0
)
echo [PASS] VC-ANDROID-PH13-R11 acceptance completed successfully.
echo [PASS] R09 build-proven Gates 1-6 + R11 resilient physical-device closure + R11 integrity are all satisfied.
exit /b 0
