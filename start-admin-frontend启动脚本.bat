@echo off
chcp 65001 >nul
echo ========================================
echo    志愿者活动服务平台 - 管理后台admin前端服务启动
echo ========================================
echo.



:: 启动开发服务器
echo [步骤2] 启动开发服务器...
echo [信息] 前端服务将在 http://localhost:3001 启动
echo [信息] 按 Ctrl+C 停止服务
echo.

call npm run dev

echo.
echo [信息] 前端服务已停止
pause
