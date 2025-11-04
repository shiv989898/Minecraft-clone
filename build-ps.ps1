# Minecraft Clone - Build Script (PowerShell)
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Minecraft Clone - Build Script" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Clear any bad JAVA_HOME environment variable
$env:JAVA_HOME = $null
Write-Host "Cleared JAVA_HOME to let Maven detect Java automatically" -ForegroundColor Yellow
Write-Host ""

# Check Java version
Write-Host "Checking Java version..." -ForegroundColor Yellow
try {
    $javaVersionOutput = & java -version 2>&1
    Write-Host $javaVersionOutput[0] -ForegroundColor Green
    
    if ($javaVersionOutput[0] -match "21\.") {
        Write-Host "[OK] Java 21 detected!" -ForegroundColor Green
    }
    else {
        Write-Host "[WARNING] Java 21 not found!" -ForegroundColor Red
        Write-Host "Your pom.xml requires Java 21." -ForegroundColor Yellow
        Write-Host "Download from: https://adoptium.net/temurin/releases/?version=21" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Press any key to continue anyway (will likely fail)..."
        $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
    }
}
catch {
    Write-Host "[ERROR] Java not found in PATH" -ForegroundColor Red
    Write-Host "Please install Java 21" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Building project..." -ForegroundColor Cyan
Write-Host "Running: mvnw.cmd clean compile" -ForegroundColor Gray
Write-Host ""

# Run Maven build
& .\mvnw.cmd clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "[SUCCESS] Build completed!" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Run the game with: .\run-ps.ps1" -ForegroundColor Cyan
    Write-Host ""
}
else {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "[ERROR] Build failed!" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Common issues:" -ForegroundColor Yellow
    Write-Host "1. Wrong Java version (need Java 21)" -ForegroundColor White
    Write-Host "2. Java not in PATH" -ForegroundColor White
    Write-Host ""
}

Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
