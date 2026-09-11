@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist node_modules call npm install
call npm run build
if errorlevel 1 (
  echo 构建失败。
  pause
  exit /b 1
)
echo.
echo 构建完成，生产文件位于 dist 文件夹。
pause
