@echo off
echo ============================================
echo Minecraft Clone - Run Script
echo ============================================
echo.

REM Check if compiled
if not exist "target\classes\com\minecraft\Main.class" (
    echo [ERROR] Project not built yet!
    echo Please run build.bat first.
    echo.
    pause
    exit /b 1
)

REM Set JAVA_HOME
echo Setting JAVA_HOME...

REM Find Java executable
for /f "tokens=*" %%i in ('where java 2^>nul') do (
    set JAVA_EXE=%%i
    goto :found_java_run
)

:found_java_run
echo Found Java at: %JAVA_EXE%

REM Extract JAVA_HOME from java.exe path
for %%i in ("%JAVA_EXE%") do set JAVA_BIN_DIR=%%~dpi
set JAVA_BIN_DIR=%JAVA_BIN_DIR:~0,-1%
for %%i in ("%JAVA_BIN_DIR%") do set JAVA_HOME=%%~dpi
set JAVA_HOME=%JAVA_HOME:~0,-1%

echo JAVA_HOME set to: %JAVA_HOME%
echo.
echo Starting Minecraft Clone...
echo.
call mvnw.cmd exec:java -Dexec.mainClass="com.minecraft.Main"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Game failed to start!
    echo.
    pause
)
