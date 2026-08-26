@echo off
setlocal
cd /d "%~dp0.."
where powershell >nul 2>nul || (echo [FAIL] PowerShell is required to bootstrap Gradle.& exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0BOOTSTRAP-GRADLE-WRAPPER.ps1"
exit /b %ERRORLEVEL%
