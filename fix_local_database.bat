@echo off
echo ========================================
echo 修复本地数据库表结构
echo ========================================
echo.

set MYSQL_PATH=D:\xampp\mysql\bin\mysql.exe
set DB_USER=root
set DB_PASS=123456
set DB_NAME=canteen_ordering_system
set SQL_FILE=%~dp0fix_local_database.sql

echo MySQL路径: %MYSQL_PATH%
echo 数据库名称: %DB_NAME%
echo SQL文件: %SQL_FILE%
echo.

if not exist "%MYSQL_PATH%" (
    echo 错误: 找不到MySQL命令
    echo 请检查MySQL路径是否正确
    pause
    exit /b 1
)

if not exist "%SQL_FILE%" (
    echo 错误: 找不到SQL文件
    echo SQL文件: %SQL_FILE%
    pause
    exit /b 1
)

echo 开始执行SQL脚本...
echo.

"%MYSQL_PATH%" -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%SQL_FILE%"

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo 数据库修复成功！
    echo ========================================
) else (
    echo.
    echo ========================================
    echo 数据库修复失败！
    echo ========================================
    pause
    exit /b 1
)

echo.
echo 验证表结构...
echo.

"%MYSQL_PATH%" -u %DB_USER% -p%DB_PASS% %DB_NAME% -e "DESCRIBE user;"

echo.
echo ========================================
echo 完成！请刷新浏览器重新登录
echo ========================================
echo.

pause