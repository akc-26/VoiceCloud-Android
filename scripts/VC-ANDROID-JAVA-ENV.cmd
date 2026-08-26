@echo off
rem Resolves a valid JDK for command-line acceptance. Invalid JAVA_HOME values are cleared.
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto java_ready_home
if defined JAVA_HOME (
  echo [WARN] JAVA_HOME is invalid and will be ignored.
  set "JAVA_HOME="
)

if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" (
  set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
  set "PATH=%ProgramFiles%\Android\Android Studio\jbr\bin;%PATH%"
  goto java_ready_home
)
if defined LOCALAPPDATA if exist "%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\java.exe" (
  set "JAVA_HOME=%LOCALAPPDATA%\Programs\Android Studio\jbr"
  set "PATH=%LOCALAPPDATA%\Programs\Android Studio\jbr\bin;%PATH%"
  goto java_ready_home
)
if exist "%ProgramFiles%\Android\Android Studio Preview\jbr\bin\java.exe" (
  set "JAVA_HOME=%ProgramFiles%\Android\Android Studio Preview\jbr"
  set "PATH=%ProgramFiles%\Android\Android Studio Preview\jbr\bin;%PATH%"
  goto java_ready_home
)
where java >nul 2>nul && goto java_ready_path

echo [FAIL] A Java 17+ runtime was not found.
echo [INFO] Android Studio includes a compatible JBR at C:\Program Files\Android\Android Studio\jbr on standard installations.
exit /b 1

:java_ready_home
"%JAVA_HOME%\bin\java.exe" -version >nul 2>nul || (echo [FAIL] JAVA_HOME Java could not start.& exit /b 1)
echo [PASS] Java detected: %JAVA_HOME%
exit /b 0

:java_ready_path
java -version >nul 2>nul || (echo [FAIL] Java on PATH could not start.& exit /b 1)
echo [PASS] Java detected on PATH.
exit /b 0
