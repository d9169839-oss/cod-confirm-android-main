@echo off
setlocal
set GRADLE_VERSION=8.13
if not defined GRADLE_USER_HOME set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set GRADLE_INSTALL_ROOT=%GRADLE_USER_HOME%\cod-confirm-wrapper
set GRADLE_HOME=%GRADLE_INSTALL_ROOT%\gradle-%GRADLE_VERSION%
set GRADLE_EXEC=%GRADLE_HOME%\bin\gradle.bat

if not exist "%GRADLE_EXEC%" (
    if not exist "%GRADLE_INSTALL_ROOT%" mkdir "%GRADLE_INSTALL_ROOT%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_INSTALL_ROOT%\gradle.zip'; Expand-Archive -Path '%GRADLE_INSTALL_ROOT%\gradle.zip' -DestinationPath '%GRADLE_INSTALL_ROOT%' -Force; Remove-Item '%GRADLE_INSTALL_ROOT%\gradle.zip'"
    if errorlevel 1 exit /b 1
)
call "%GRADLE_EXEC%" %*
exit /b %ERRORLEVEL%
