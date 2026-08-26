@echo off
setlocal
cd /d "%~dp0.."
where powershell >nul 2>nul || (echo [FAIL] PowerShell is required.& exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\VC-ANDROID-LIVE-CONNECTIVITY-CHECK.ps1
exit /b %ERRORLEVEL%
