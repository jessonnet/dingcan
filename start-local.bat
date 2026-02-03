@echo off
chcp 65001
echo =========================================
echo 启动食堂订餐系统（本地开发环境）
echo =========================================
echo.

:: 检查MySQL是否运行
echo [1/4] 检查MySQL服务...
net start | findstr "MySQL" >nul
if %errorlevel% neq 0 (
    echo 警告：MySQL服务未运行，请手动启动MySQL
    pause
)
echo MySQL检查完成
echo.

:: 启动后端
echo [2/4] 启动后端服务...
start "后端服务" cmd /k "cd /d d:\xampp\htdocs\order && mvn spring-boot:run"
echo 后端服务启动中，请等待...
echo.

:: 等待后端启动
timeout /t 15 /nobreak >nul

:: 启动前端
echo [3/4] 启动前端服务...
start "前端服务" cmd /k "cd /d d:\xampp\htdocs\order\frontend && npm run dev"
echo 前端服务启动中...
echo.

:: 等待前端启动
timeout /t 5 /nobreak >nul

echo [4/4] 启动完成！
echo.
echo =========================================
echo 访问地址：
echo   前端：http://localhost:5173
echo   后端：http://localhost:8080/api/
echo =========================================
echo.
echo 默认账号：
echo   管理员：admin / 123456
echo   员工：  employee1 / 123456
echo   厨师：  chef1 / 123456
echo.
pause
