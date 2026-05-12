@echo off
REM Ejecuta ReciInventario usando Gradle con JDK 21 (Eclipse Adoptium)
setlocal

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] No se encuentra JDK 21 en "%JAVA_HOME%".
    echo Edita run.bat y ajusta JAVA_HOME a la ruta correcta.
    pause
    exit /b 1
)

cd /d "%~dp0"

echo Usando JDK: %JAVA_HOME%
java -version
echo.

call gradlew.bat run
set "EXITCODE=%ERRORLEVEL%"

if not "%EXITCODE%"=="0" (
    echo.
    echo [ERROR] La ejecucion fallo con codigo %EXITCODE%.
    pause
)

endlocal & exit /b %EXITCODE%
