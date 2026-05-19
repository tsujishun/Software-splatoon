@echo off
setlocal
cd /d "%~dp0"
call .\gradlew.bat lwjgl3:run --args="3d"
