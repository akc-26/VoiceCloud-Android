@echo off
setlocal EnableExtensions EnableDelayedExpansion
cd /d "%~dp0.."
set "PYTHONDONTWRITEBYTECODE=1"
set /a VC_FAILURE_GROUPS=0
set "VC_DEVICE_PENDING=0"

echo =============================================================
echo VoiceCloud Android PH13 R15 - R14 Compiler Closure + Full Acceptance
echo Parent package: VC-ANDROID-PH13-R14
echo Scope: close actual R14 Kotlin compiler diagnostics without changing intended R14 UI/UX or functional authority
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
    powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R15-SOURCE-DIAGNOSTIC.ps1
    if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
  )
)

call scripts\VC-ANDROID-JAVA-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
call scripts\VC-ANDROID-SDK-ENV.cmd
if errorlevel 1 set /a VC_FAILURE_GROUPS+=1

if "!VC_FAILURE_GROUPS!"=="0" (
  powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-PH13-R15-ISOLATED-BUILD.ps1
  set "VC_ANDROID_RC=!ERRORLEVEL!"
  if "!VC_ANDROID_RC!"=="2" (
    echo [DEVICE-PENDING] All mandatory host build gates passed, but physical-device instrumentation did not run because no healthy authorized device was available.
    set "VC_DEVICE_PENDING=1"
  ) else if not "!VC_ANDROID_RC!"=="0" (
    echo [GATE-FAIL] R15 full Android build/device gate failed.
    set /a VC_FAILURE_GROUPS+=1
  ) else (
    echo [GATE-PASS] R15 full Android build and physical-device instrumentation passed.
  )
) else (
  echo [GATE-SKIP] Full Android build/device acceptance skipped because a prerequisite/source gate failed.
)

echo [R15 INTEGRITY] Product/delivery integrity
where python >nul 2>nul
if errorlevel 1 (
  echo [GATE-FAIL] Python unavailable; R15 delivery integrity could not run.
  set /a VC_FAILURE_GROUPS+=1
) else (
  python scripts\ph13_r15_delivery_integrity.py
  if errorlevel 1 set /a VC_FAILURE_GROUPS+=1
)

echo =============================================================
if not "!VC_FAILURE_GROUPS!"=="0" (
  echo [FAIL] VC-ANDROID-PH13-R15 acceptance completed with !VC_FAILURE_GROUPS! genuine failing group^(s^).
  exit /b 1
)
if "!VC_DEVICE_PENDING!"=="1" (
  echo [PENDING] VC-ANDROID-PH13-R15 is NOT fully accepted yet.
  echo [PASS] Source, product preservation, Debug/Staging/Release compile, unit tests, lint, assemblies and delivery integrity passed.
  echo [PENDING] Only physical-device instrumentation remains because no healthy authorized device was available.
  exit /b 2
)
echo [PASS] VC-ANDROID-PH13-R15 FULL ACCEPTANCE completed successfully.
echo [PASS] Source + Debug/Staging/Release compile + unit tests + lint + assemblies + physical-device instrumentation + delivery integrity all passed.
exit /b 0
