@echo off
setlocal
cd /d "%~dp0.."
call scripts\VC-ANDROID-SDK-ENV.cmd || exit /b 1
set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if not exist "%ADB%" (echo [FAIL] adb.exe not found at %ADB%& exit /b 1)

where curl.exe >nul 2>nul
if not errorlevel 1 (
  curl.exe -fsS --max-time 4 "http://127.0.0.1:3000/api/v1/config/mobile?platform=android" >nul 2>nul
  if errorlevel 1 (
    echo [WARN] VoiceCloud backend is not currently reachable on this PC at http://127.0.0.1:3000
    echo [INFO] The tunnel can still be configured; start the finalized VoiceCloud backend before launching runtime validation.
  ) else (
    echo [PASS] VoiceCloud REST API reachable on PC port 3000
  )
)

set "FOUND_DEVICE="
for /f "skip=1 tokens=1,2" %%D in ('"%ADB%" devices') do if "%%E"=="device" (
  set "FOUND_DEVICE=1"
  echo [INFO] Mapping VoiceCloud API/Socket/Web for device %%D: device tcp:3000 ^> PC tcp:3000
  "%ADB%" -s %%D reverse tcp:3000 tcp:3000 || exit /b 1
  "%ADB%" -s %%D reverse --list | findstr /C:"tcp:3000 tcp:3000" >nul || (echo [FAIL] adb reverse verification failed for %%D& exit /b 1)
  echo [PASS] VoiceCloud API/Socket/Web tunnel ready for device %%D
)
if not defined FOUND_DEVICE (
  echo [WARN] No authorized physical device/emulator detected by adb. Connect the device and run this script again before launching the debug app.
  exit /b 0
)
exit /b 0
