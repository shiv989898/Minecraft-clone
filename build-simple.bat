@echo off
echo ============================================
echo Minecraft Clone - Build Script (Simple)
echo ============================================
echo.

REM Clear any bad JAVA_HOME
set JAVA_HOME=

REM Check Java version
echo Checking Java version...
java -version 2>&1 | findstr /i "version"
echo.

REM Check if Java 21
java -version 2>&1 | findstr /i "21\." >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Java 21 detected!
) else (
    echo [WARNING] Java 21 not detected!
    echo This project requires Java 21.
    echo Download from: https://adoptium.net/temurin/releases/?version=21
    echo.
    pause
    exit /b 1
)

echo.
echo Building project...
call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo [SUCCESS] Build completed!
    echo ============================================
    echo.
    echo Run the game with: .\run-simple.bat
    echo.
) else (
    echo.
    echo ============================================
    echo [ERROR] Build failed!
    echo ============================================
    echo.
)

pause
