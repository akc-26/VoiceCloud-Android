@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
set "PYTHONUTF8=1"
set "PYTHONIOENCODING=utf-8"
set /a VC_FAILURE_GROUPS=0
set "VC_DEVICE_PENDING=0"

echo =============================================================
echo VoiceCloud Android PH13 R19 - Video-Driven Product Architecture + Design Fidelity Acceptance
echo Functional/build baseline: VC-ANDROID-PH13-R18 / build-proven R15 core authority
echo Scope: consolidate latest End User + Creator physical-device findings without log-by-log patching.
echo Full Debug/Staging/Release compile, tests, lint, assemblies and physical-device instrumentation remain mandatory.
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
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R19-SOURCE-DIAGNOSTIC.ps1
    if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
  )
)
call scripts\VC-ANDROID-JAVA-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
call scripts\VC-ANDROID-SDK-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
if "!VC_FAILURE_GROUPS!"=="0" (
  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R19-ISOLATED-BUILD.ps1
  set "VC_ANDROID_RC=!ERRORLEVEL!"
  if "!VC_ANDROID_RC!"=="2" (
    echo [DEVICE-PENDING] Host build gates passed, but physical-device instrumentation did not run because no healthy authorized device was available.
    set "VC_DEVICE_PENDING=1"
  ) else if not "!VC_ANDROID_RC!"=="0" (
    echo [GATE-FAIL] R19 full Android build/device gate failed.
    set /a VC_FAILURE_GROUPS+=1
  ) else (
    echo [GATE-PASS] R19 full Android build and physical-device instrumentation passed.
  )
) else (
  echo [GATE-SKIP] Full Android build/device acceptance skipped because a prerequisite/source gate failed.
)
echo [R19 INTEGRITY] Product/delivery integrity
python scripts\ph13_r19_delivery_integrity.py
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
echo =============================================================
if not "!VC_FAILURE_GROUPS!"=="0" (
  echo [FAIL] VC-ANDROID-PH13-R19 acceptance completed with !VC_FAILURE_GROUPS! genuine failing group^(s^).
  exit /b 1
)
if "!VC_DEVICE_PENDING!"=="1" (
  echo [PENDING] VC-ANDROID-PH13-R19 is NOT fully accepted yet.
  echo [PASS] Source/design/navigation/Host-authority, Debug/Staging/Release compile, unit tests, lint, assemblies and delivery integrity passed.
  echo [PENDING] Only physical-device instrumentation remains because no healthy authorized device was available.
  exit /b 2
)
echo [PASS] VC-ANDROID-PH13-R19 FULL ACCEPTANCE completed successfully.
echo [PASS] Video-driven End User + Creator product architecture, approved UI/UX authority and full host/device acceptance all passed.
exit /b 0
