# Minecraft Clone - Setup Helper
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Minecraft Clone - Setup Helper" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check Java installation
Write-Host "Checking Java installation..." -ForegroundColor Yellow

try {
    $javaVersion = & java -version 2>&1
    $versionLine = $javaVersion[0]
    Write-Host "Found: $versionLine" -ForegroundColor Green
    
    if ($versionLine -match '"21\.') {
        Write-Host "[OK] Java 21 detected - Perfect!" -ForegroundColor Green
        Write-Host ""
        Write-Host "You're all set! Run these commands:" -ForegroundColor Cyan
        Write-Host "  .\build.bat   - To compile the project" -ForegroundColor White
        Write-Host "  .\run.bat     - To run the game" -ForegroundColor White
        Write-Host ""
    }
    elseif ($versionLine -match '"1\.8') {
        Write-Host "[ERROR] Wrong Java version!" -ForegroundColor Red
        Write-Host ""
        Write-Host "Current version: $versionLine" -ForegroundColor Yellow
        Write-Host "Required version: Java 21" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "SOLUTION:" -ForegroundColor Cyan
        Write-Host "1. Download Java 21 from:" -ForegroundColor White
        Write-Host "   https://adoptium.net/temurin/releases/?version=21" -ForegroundColor Blue
        Write-Host ""
        Write-Host "2. During installation:" -ForegroundColor White
        Write-Host "   [X] Check 'Set JAVA_HOME'" -ForegroundColor Green
        Write-Host "   [X] Check 'Add to PATH'" -ForegroundColor Green
        Write-Host ""
        Write-Host "3. Restart PowerShell and run this script again" -ForegroundColor White
        Write-Host ""
    }
    elseif ($versionLine -match '"11\.|"17\.') {
        Write-Host "[ERROR] Wrong Java version!" -ForegroundColor Red
        Write-Host "You have Java 11 or 17, but need Java 21" -ForegroundColor Yellow
    }
    else {
        Write-Host "[ERROR] Unknown Java version: $versionLine" -ForegroundColor Red
        Write-Host "Please install Java 21" -ForegroundColor Yellow
    }
}
catch {
    Write-Host "[ERROR] Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host ""
    Write-Host "SOLUTION:" -ForegroundColor Cyan
    Write-Host "Download and install Java 21 from:" -ForegroundColor White
    Write-Host "https://adoptium.net/temurin/releases/?version=21" -ForegroundColor Blue
    Write-Host ""
    Write-Host "Make sure to:" -ForegroundColor White
    Write-Host "[X] Check 'Set JAVA_HOME' during installation" -ForegroundColor Green
    Write-Host "[X] Check 'Add to PATH' during installation" -ForegroundColor Green
    Write-Host ""
}

# Check Maven wrapper
Write-Host "Checking Maven wrapper..." -ForegroundColor Yellow
if (Test-Path ".\mvnw.cmd") {
    Write-Host "[OK] Maven wrapper found" -ForegroundColor Green
}
else {
    Write-Host "[ERROR] Maven wrapper missing" -ForegroundColor Red
    Write-Host "This shouldn't happen - please check your project files" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Setup check complete!" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check if already built
if (Test-Path ".\target\classes\com\minecraft\Main.class") {
    Write-Host "[OK] Project appears to be already built" -ForegroundColor Green
    Write-Host "  You can run: .\run.bat" -ForegroundColor White
    Write-Host ""
}
else {
    Write-Host "Project not built yet" -ForegroundColor Yellow
    Write-Host "After installing Java 21, run: .\build.bat" -ForegroundColor White
    Write-Host ""
}

Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
