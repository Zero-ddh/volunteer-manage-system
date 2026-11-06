@echo off
chcp 65001 >nul
title 志愿者活动服务平台 - 后端服务

echo.
echo ========================================
echo    志愿者活动服务平台 - 后端服务启动脚本
echo ========================================
echo.


:: 启动应用
echo.
echo ========================================
echo    正在启动后端服务...
echo ========================================
echo.
echo 📡 服务地址: http://localhost:8080
echo 🗄️  数据库: MySQL (localhost:3306)
echo.
echo 💡 提示: 按 Ctrl+C 停止服务
echo.

:: 启动Spring Boot应用
call mvn spring-boot:run

:: 如果程序异常退出
echo.
echo ❌ 后端服务异常退出
pause