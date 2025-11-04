echo @echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo ============================================
echo Starting Minecraft Clone...
echo ============================================
echo.
echo Controls:
echo   WASD - Move (W auto-sprints!)
echo   Mouse - Look around
echo   Space - Jump/Fly up
echo   Shift - Sneak/Fly down
echo   F - Toggle flying
echo   Left Click - Break blocks
echo   Right Click - Place blocks
echo   1-7 - Select block type
echo   ESC - Exit
echo.
echo Starting game...
echo.

mvnw.cmd exec:java -Dexec.mainClass="com.minecraft.Main"

pause
