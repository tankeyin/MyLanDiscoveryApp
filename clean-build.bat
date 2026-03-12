@echo off
chcp 65001 >nul
title 清理并重新构建
echo.
echo ========================================
echo    清理并重新构建项目
echo ========================================
echo.

if not exist "gradlew.bat" (
    echo [错误] 请在项目根目录运行此脚本！
    pause
    exit /b 1
)

echo [1/3] 清理旧构建...
call .\gradlew.bat clean

echo.
echo [2/3] 刷新依赖...
call .\gradlew.bat --refresh-dependencies

echo.
echo [3/3] 重新构建 Debug APK...
call .\gradlew.bat assembleDebug

if %errorLevel% equ 0 (
    echo.
    echo ========================================
    echo    构建成功！
    echo ========================================
    echo.
    echo APK: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo [错误] 构建失败
)

echo.
pause
