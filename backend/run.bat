@echo off
setlocal
echo ══════════════════════════════════════════════
echo   INICIANDO SHINE BACKEND (Pure Java)
echo ══════════════════════════════════════════════

:: Cambiar al directorio del script
cd /d %~dp0

:: Usar Java 17
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.7.7-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

:: Matar procesos Java anteriores para liberar el JAR bloqueado
echo Deteniendo instancias anteriores de Java...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 2 /nobreak >nul

:: Verificar si existe mvnw
if not exist "mvnw.cmd" (
    echo ❌ Error: No se encuentra mvnw.cmd en %CD%
    pause
    exit /b 1
)

echo 🛠️  Compilando y empaquetando proyecto...
call mvnw.cmd clean package
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: La compilacion ha fallado. Revisa el codigo arriba.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ✅ Compilacion exitosa. Arrancando servidor...
echo.

:: Ejecutar el servidor
java -jar target\shine-server.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ El servidor se ha detenido con errores.
    pause
)

endlocal
