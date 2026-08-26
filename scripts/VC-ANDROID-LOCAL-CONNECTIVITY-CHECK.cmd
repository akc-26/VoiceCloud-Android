@echo off
setlocal
cd /d "%~dp0.."
call scripts\VC-ANDROID-JAVA-ENV.cmd || exit /b 1
call scripts\VC-ANDROID-SDK-ENV.cmd || exit /b 1
where curl.exe >nul 2>nul || (echo [FAIL] curl.exe is required for local connectivity verification.& exit /b 1)
curl.exe -fsS --max-time 5 "http://127.0.0.1:3000/api/v1/config/mobile?platform=android" >nul 2>nul || (
  echo [FAIL] VoiceCloud REST API is not running on this PC at http://127.0.0.1:3000/api/v1/
  echo [INFO] Start the finalized VoiceCloud backend first.
  exit /b 1
)
echo [PASS] VoiceCloud REST API reachable on PC: http://127.0.0.1:3000/api/v1/
call scripts\VC-ANDROID-LOCAL-DEVICE-BACKEND.cmd || exit /b 1
set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
"%ADB%" reverse --list | findstr /C:"tcp:3000 tcp:3000" >nul || (echo [FAIL] Device reverse tunnel tcp:3000 is missing.& exit /b 1)
echo [PASS] Device tunnel maps API/Socket/Web port 3000

echo [INFO] Socket.IO authority is http://127.0.0.1:3000/realtime with path /socket.io
exit /b 0
