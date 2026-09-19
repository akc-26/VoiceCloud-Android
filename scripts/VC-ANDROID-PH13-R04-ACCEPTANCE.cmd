@echo off
setlocal EnableExtensions
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
set /a VC_FAILURE_GROUPS=0

echo =============================================================
echo VoiceCloud Android PH13 R04 - Full Diagnostic Acceptance
echo Complete-gate discovery + isolated Windows build cleanup correction
echo Product source: unchanged from PH13-R01/R02/R03
echo Parent package: VC-ANDROID-PH13-R03
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
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R04-SOURCE-DIAGNOSTIC.ps1
    if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
  )
)

echo [PRECHECK] Java environment
call scripts\VC-ANDROID-JAVA-ENV.cmd
if errorlevel 1 (
  echo [PRECHECK-FAIL] Java environment resolution failed. Android build diagnostics will still be attempted.
  set /a VC_FAILURE_GROUPS+=1
)

echo [PRECHECK] Android SDK environment
call scripts\VC-ANDROID-SDK-ENV.cmd
if errorlevel 1 (
  echo [PRECHECK-FAIL] Android SDK environment resolution failed. Android build diagnostics will still be attempted.
  set /a VC_FAILURE_GROUPS+=1
)

where powershell >nul 2>nul
if errorlevel 1 (
  echo [BUILD-FAIL] Windows PowerShell unavailable; isolated Android diagnostic build cannot run.
  set /a VC_FAILURE_GROUPS+=1
) else (
  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R04-ISOLATED-BUILD.ps1
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo [GATE 8] Original delivery-tree integrity
where python >nul 2>nul
if errorlevel 1 (
  echo [GATE-FAIL] Python unavailable; Gate 8 delivery-tree integrity could not run.
  set /a VC_FAILURE_GROUPS+=1
) else (
  python scripts\ph13_r04_delivery_hygiene.py
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo =============================================================
if not "%VC_FAILURE_GROUPS%"=="0" (
  echo [FAIL] VC-ANDROID-PH13-R04 full diagnostic acceptance completed with %VC_FAILURE_GROUPS% failing group^(s^).
  echo [INFO] All independent applicable gates were attempted; review every [SUMMARY-FAIL], [GATE-FAIL], [DIAG-FAIL], and [PRECHECK-FAIL] above.
  exit /b 1
)
echo [PASS] VC-ANDROID-PH13-R04 acceptance commands completed successfully.
exit /b 0
