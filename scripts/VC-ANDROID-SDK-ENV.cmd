@echo off
rem Resolves Android SDK for command-line acceptance without committing machine-specific local.properties.
if defined ANDROID_HOME if exist "%ANDROID_HOME%" goto sdk_ready_home
if defined ANDROID_SDK_ROOT if exist "%ANDROID_SDK_ROOT%" (
  set "ANDROID_HOME=%ANDROID_SDK_ROOT%"
  goto sdk_ready_home
)
if defined LOCALAPPDATA if exist "%LOCALAPPDATA%\Android\Sdk" (
  set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
  set "ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
  goto sdk_ready_home
)
if exist "%USERPROFILE%\AppData\Local\Android\Sdk" (
  set "ANDROID_HOME=%USERPROFILE%\AppData\Local\Android\Sdk"
  set "ANDROID_SDK_ROOT=%USERPROFILE%\AppData\Local\Android\Sdk"
  goto sdk_ready_home
)
echo [FAIL] Android SDK location was not found.
echo [INFO] Open Android Studio ^> Settings ^> Languages ^& Frameworks ^> Android SDK and confirm the SDK path.
echo [INFO] Then set ANDROID_HOME to that folder, or create local.properties with sdk.dir pointing to it.
exit /b 1

:sdk_ready_home
set "ANDROID_SDK_ROOT=%ANDROID_HOME%"
echo [PASS] Android SDK detected: %ANDROID_HOME%
exit /b 0
