@echo off
chcp 65001 >nul
cd /d "%~dp0"
where node >nul 2>nul
if errorlevel 1 (
  echo [错误] 没有检测到 Node.js。
  echo 请先安装 Node.js 24 LTS，然后重新双击本文件。
  pause
  exit /b 1
)
if not exist node_modules (
  echo 第一次运行，正在安装依赖...
  call npm install
  if errorlevel 1 (
    echo npm install 失败，请检查网络后重试。
    pause
    exit /b 1
  )
)
echo.
echo Vue 开发服务器即将启动：
echo http://localhost:5173/
echo.
call npm run dev
pause
