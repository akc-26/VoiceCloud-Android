@echo off
setlocal EnableExtensions
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
set /a VC_FAILURE_GROUPS=0

echo =============================================================
echo VoiceCloud Android PH13 R07 - Premium UI/UX Compile Correction Acceptance
echo Parent package: VC-ANDROID-PH13-R06
echo Scope: compiler correction only; functions/APIs/business rules preserved
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
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R07-SOURCE-DIAGNOSTIC.ps1
    if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
  )
)

call scripts\VC-ANDROID-JAVA-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
call scripts\VC-ANDROID-SDK-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1

where powershell >nul 2>nul
if errorlevel 1 (
  echo [BUILD-FAIL] Windows PowerShell unavailable.
  set /a VC_FAILURE_GROUPS+=1
) else (
  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R07-ISOLATED-BUILD.ps1
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo [GATE 8] Product/delivery integrity
where python >nul 2>nul
if errorlevel 1 (
  echo [GATE-FAIL] Python unavailable; Gate 8 could not run.
  set /a VC_FAILURE_GROUPS+=1
) else (
  python scripts\ph13_r07_delivery_integrity.py
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo =============================================================
if not "%VC_FAILURE_GROUPS%"=="0" (
  echo [FAIL] VC-ANDROID-PH13-R07 acceptance completed with %VC_FAILURE_GROUPS% failing group^(s^).
  exit /b 1
)
echo [PASS] VC-ANDROID-PH13-R07 acceptance commands completed successfully.
exit /b 0
