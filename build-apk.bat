@echo off
chcp 65001 >nul
title 构建 MyLanDiscoveryApp APK
echo.
echo ========================================
echo    MyLanDiscoveryApp APK 构建工具
echo ========================================
echo.

:: 检查是否在项目目录
if not exist "gradlew.bat" (
    echo [错误] 请在项目根目录运行此脚本！
    echo [信息] 当前目录: %CD%
    pause
    exit /b 1
)

echo [信息] 开始构建 Debug APK...
echo.

:: 执行构建
call .\gradlew.bat assembleDebug

if %errorLevel% neq 0 (
    echo.
    echo [错误] 构建失败！
    echo.
    echo 常见解决方法：
    echo 1. 检查是否已安装 Android Studio 和 SDK
echo 2. 检查 JAVA_HOME 环境变量
echo 3. 运行: .\gradlew.bat --refresh-dependencies
echo 4. 删除 .gradle 文件夹后重试
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo    构建成功！
echo ========================================
echo.
echo APK 文件位置:
echo   %CD%\app\build\outputs\apk\debug\app-debug.apk
echo.

:: 检查文件是否存在
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo [信息] 文件大小:
    for %%I in ("app\build\outputs\apk\debug\app-debug.apk") do echo   %%~zI bytes
    echo.
    echo 安装到手机:
    echo   adb install app\build\outputs\apk\debug\app-debug.apk
) else (
    echo [警告] 未找到 APK 文件，请检查构建日志
)

echo.
pause
