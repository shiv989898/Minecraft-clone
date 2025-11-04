@echo off
echo ============================================
echo Minecraft Clone - Build Script
echo ============================================
echo.

REM Check if Java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 21 from: https://adoptium.net/temurin/releases/?version=21
    pause
    exit /b 1
)

REM Check Java version
echo Checking Java version...
java -version 2>&1 | findstr /i "version" | findstr /i "21\." >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Java 21 detected
    goto :build
)

java -version 2>&1 | findstr /i "version" | findstr /i "1.8\|11\|17" >nul
if %ERRORLEVEL% EQU 0 (
    echo [WARNING] Wrong Java version detected!
    echo.
    java -version
    echo.
    echo This project requires Java 21, but you have an older version.
    echo.
    echo Please install Java 21 from:
    echo https://adoptium.net/temurin/releases/?version=21
    echo.
    echo After installing Java 21, you have two options:
    echo   1. Set JAVA_HOME environment variable to Java 21 directory
    echo   2. Make sure Java 21 is first in your PATH
    echo.
    pause
    exit /b 1
)

:build
echo.
echo Setting JAVA_HOME...

REM Find Java executable
for /f "tokens=*" %%i in ('where java 2^>nul') do (
    set JAVA_EXE=%%i
    goto :found_java
)

:found_java
echo Found Java at: %JAVA_EXE%

REM Extract JAVA_HOME from java.exe path
REM Example: C:\Program Files\Java\jdk-21\bin\java.exe -> C:\Program Files\Java\jdk-21
for %%i in ("%JAVA_EXE%") do set JAVA_BIN_DIR=%%~dpi
set JAVA_BIN_DIR=%JAVA_BIN_DIR:~0,-1%
for %%i in ("%JAVA_BIN_DIR%") do set JAVA_HOME=%%~dpi
set JAVA_HOME=%JAVA_HOME:~0,-1%

echo JAVA_HOME set to: %JAVA_HOME%
echo.

echo Building project...
echo Running: mvnw.cmd clean compile
echo.
call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================
    echo [SUCCESS] Build completed successfully!
    echo ============================================
    echo.
    echo To run the game, execute: run.bat
    echo.
) else (
    echo.
    echo ============================================
    echo [ERROR] Build failed!
    echo ============================================
    echo.
    echo Please check the error messages above.
    echo.
)

pause
