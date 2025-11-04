@echo off
echo ============================================
echo Minecraft Clone - Run Script (Simple)
echo ============================================
echo.

REM Check if compiled
if not exist "target\classes\com\minecraft\Main.class" (
    echo [ERROR] Project not built yet!
    echo Please run build-simple.bat first.
    echo.
    pause
    exit /b 1
)

REM Clear any bad JAVA_HOME
set JAVA_HOME=

echo Starting Minecraft Clone...
echo.
call mvnw.cmd exec:java -Dexec.mainClass="com.minecraft.Main"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Game failed to start!
    echo.
    pause
)
