# Minecraft Clone - Run Script (PowerShell)
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "Minecraft Clone - Run Script" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Check if compiled
if (-not (Test-Path ".\target\classes\com\minecraft\Main.class")) {
    Write-Host "[ERROR] Project not built yet!" -ForegroundColor Red
    Write-Host "Please run build-ps.ps1 first." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
    exit 1
}

# Clear any bad JAVA_HOME
$env:JAVA_HOME = $null
Write-Host "Cleared JAVA_HOME" -ForegroundColor Yellow
Write-Host ""

Write-Host "Starting Minecraft Clone..." -ForegroundColor Green
Write-Host ""

# Run the game
& .\mvnw.cmd exec:java -Dexec.mainClass="com.minecraft.Main"

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Game failed to start!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
}
