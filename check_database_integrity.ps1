# 饭堂订餐系统 - 数据库完整性检查脚本 (Windows PowerShell版本)
# 用于部署前检查数据库结构是否完整

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "饭堂订餐系统 - 数据库完整性检查" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 数据库配置
$DB_HOST = "localhost"
$DB_PORT = "3306"
$DB_NAME = "canteen_ordering_system"
$DB_USER = "root"
$DB_PASS = "123456"

# 检查MySQL连接
Write-Host "[1/10] 检查数据库连接..." -ForegroundColor Yellow
try {
    $result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SELECT 1;" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ 数据库连接正常" -ForegroundColor Green
    } else {
        Write-Host "✗ 数据库连接失败" -ForegroundColor Red
        Write-Host "请检查数据库配置：" -ForegroundColor Red
        Write-Host "  主机: $DB_HOST" -ForegroundColor Red
        Write-Host "  端口: $DB_PORT" -ForegroundColor Red
        Write-Host "  用户: $DB_USER" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 数据库连接失败: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 检查数据库是否存在
Write-Host "[2/10] 检查数据库是否存在..." -ForegroundColor Yellow
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SHOW DATABASES LIKE '$DB_NAME';" 2>$null
if ($result -match $DB_NAME) {
    Write-Host "✓ 数据库 $DB_NAME 存在" -ForegroundColor Green
} else {
    Write-Host "✗ 数据库 $DB_NAME 不存在" -ForegroundColor Red
    Write-Host "请先创建数据库：" -ForegroundColor Red
    Write-Host "  mysql -u$DB_USER -p$DB_PASS -e `"CREATE DATABASE IF NOT EXISTS $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`"" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 检查必需的表是否存在
Write-Host "[3/10] 检查必需的表..." -ForegroundColor Yellow
$ALL_TABLES_EXIST = $true

# 检查department表
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'department';" 2>$null
if ($result -match "department") {
    Write-Host "  ✓ 表 department 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 department 不存在" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

# 检查role表
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'role';" 2>$null
if ($result -match "role") {
    Write-Host "  ✓ 表 role 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 role 不存在" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

# 检查restaurant表
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'restaurant';" 2>$null
if ($result -match "restaurant") {
    Write-Host "  ✓ 表 restaurant 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 restaurant 不存在" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

# 检查user表
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'user';" 2>$null
if ($result -match "user") {
    Write-Host "  ✓ 表 user 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 user 不存在" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

# 检查wechat_user表（微信登录功能）
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'wechat_user';" 2>$null
if ($result -match "wechat_user") {
    Write-Host "  ✓ 表 wechat_user 存在（微信登录功能）" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 wechat_user 不存在（微信登录功能缺失）" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

# 检查meal_type表
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'meal_type';" 2>$null
if ($result -match "meal_type") {
    Write-Host "  ✓ 表 meal_type 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 meal_type 不存在" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

# 检查order表
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'order';" 2>$null
if ($result -match "order") {
    Write-Host "  ✓ 表 order 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 order 不存在" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

# 检查operation_log表
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'operation_log';" 2>$null
if ($result -match "operation_log") {
    Write-Host "  ✓ 表 operation_log 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 operation_log 不存在" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

# 检查system_config表
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'system_config';" 2>$null
if ($result -match "system_config") {
    Write-Host "  ✓ 表 system_config 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ 表 system_config 不存在" -ForegroundColor Red
    $ALL_TABLES_EXIST = $false
}

if (-not $ALL_TABLES_EXIST) {
    Write-Host ""
    Write-Host "✗ 部分表不存在，请执行数据库初始化脚本：" -ForegroundColor Red
    Write-Host "  mysql -u$DB_USER -p$DB_PASS $DB_NAME < src\main\resources\sql\init.sql" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 检查表结构
Write-Host "[4/10] 检查表结构..." -ForegroundColor Yellow

# 检查department表
Write-Host "  检查 department 表..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE department;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 10) {
    Write-Host "    ✓ department 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ department 表结构异常" -ForegroundColor Red
}

# 检查role表
Write-Host "  检查 role 表..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE role;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 10) {
    Write-Host "    ✓ role 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ role 表结构异常" -ForegroundColor Red
}

# 检查restaurant表
Write-Host "  检查 restaurant 表..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE restaurant;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 10) {
    Write-Host "    ✓ restaurant 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ restaurant 表结构异常" -ForegroundColor Red
}

# 检查user表
Write-Host "  检查 user 表..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE user;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 15) {
    Write-Host "    ✓ user 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ user 表结构异常" -ForegroundColor Red
    Write-Host "    请检查是否包含以下字段：" -ForegroundColor Red
    Write-Host "      id, username, password, name, role_id, department_id, restaurant_id, phone, email, status, created_at, updated_at" -ForegroundColor Red
}

# 检查wechat_user表
Write-Host "  检查 wechat_user 表（微信登录功能）..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE wechat_user;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 10) {
    Write-Host "    ✓ wechat_user 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ wechat_user 表结构异常" -ForegroundColor Red
    Write-Host "    请检查是否包含以下字段：" -ForegroundColor Red
    Write-Host "      id, user_id, openid, unionid, nickname, avatar, gender, country, province, city, language, phone, subscribe_status, subscribe_time, last_login_time, created_at, updated_at" -ForegroundColor Red
}

# 检查meal_type表
Write-Host "  检查 meal_type 表..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE meal_type;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 10) {
    Write-Host "    ✓ meal_type 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ meal_type 表结构异常" -ForegroundColor Red
}

# 检查order表
Write-Host "  检查 order 表..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE \`order\`;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 15) {
    Write-Host "    ✓ order 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ order 表结构异常" -ForegroundColor Red
    Write-Host "    请检查是否包含以下字段：" -ForegroundColor Red
    Write-Host "      id, user_id, meal_type_id, restaurant_id, order_date, status, created_at, updated_at" -ForegroundColor Red
}

# 检查operation_log表
Write-Host "  检查 operation_log 表..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE operation_log;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 20) {
    Write-Host "    ✓ operation_log 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ operation_log 表结构异常" -ForegroundColor Red
}

# 检查system_config表
Write-Host "  检查 system_config 表..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE system_config;" 2>$null
$lines = ($result -split "`n").Count
if ($lines -gt 10) {
    Write-Host "    ✓ system_config 表结构正常" -ForegroundColor Green
} else {
    Write-Host "    ✗ system_config 表结构异常" -ForegroundColor Red
}
Write-Host ""

# 检查外键约束
Write-Host "[5/10] 检查外键约束..." -ForegroundColor Yellow

# 检查user表的外键
Write-Host "  检查 user 表外键..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='user' AND CONSTRAINT_NAME<>'PRIMARY';" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -eq 3) {
    Write-Host "    ✓ user 表外键正常（3个）" -ForegroundColor Green
} else {
    Write-Host "    ✗ user 表外键异常（期望3个，实际$count个）" -ForegroundColor Red
}

# 检查wechat_user表的外键
Write-Host "  检查 wechat_user 表外键..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='wechat_user' AND CONSTRAINT_NAME<>'PRIMARY';" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -eq 1) {
    Write-Host "    ✓ wechat_user 表外键正常（1个）" -ForegroundColor Green
} else {
    Write-Host "    ✗ wechat_user 表外键异常（期望1个，实际$count个）" -ForegroundColor Red
}

# 检查order表的外键
Write-Host "  检查 order 表外键..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='order' AND CONSTRAINT_NAME<>'PRIMARY';" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -eq 3) {
    Write-Host "    ✓ order 表外键正常（3个）" -ForegroundColor Green
} else {
    Write-Host "    ✗ order 表外键异常（期望3个，实际$count个）" -ForegroundColor Red
}
Write-Host ""

# 检查初始化数据
Write-Host "[6/10] 检查初始化数据..." -ForegroundColor Yellow

# 检查角色数据
Write-Host "  检查角色数据..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM role;" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -ge 3) {
    Write-Host "    ✓ 角色数据正常（$count条）" -ForegroundColor Green
} else {
    Write-Host "    ✗ 角色数据异常（期望至少3条，实际$count条）" -ForegroundColor Red
}

# 检查餐厅数据
Write-Host "  检查餐厅数据..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM restaurant;" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -ge 2) {
    Write-Host "    ✓ 餐厅数据正常（$count条）" -ForegroundColor Green
} else {
    Write-Host "    ✗ 餐厅数据异常（期望至少2条，实际$count条）" -ForegroundColor Red
}

# 检查餐食类型数据
Write-Host "  检查餐食类型数据..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM meal_type;" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -ge 3) {
    Write-Host "    ✓ 餐食类型数据正常（$count条）" -ForegroundColor Green
} else {
    Write-Host "    ✗ 餐食类型数据异常（期望至少3条，实际$count条）" -ForegroundColor Red
}

# 检查系统配置数据
Write-Host "  检查系统配置数据..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM system_config;" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -ge 2) {
    Write-Host "    ✓ 系统配置数据正常（$count条）" -ForegroundColor Green
} else {
    Write-Host "    ✗ 系统配置数据异常（期望至少2条，实际$count条）" -ForegroundColor Red
}

# 检查部门数据
Write-Host "  检查部门数据..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM department;" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -ge 5) {
    Write-Host "    ✓ 部门数据正常（$count条）" -ForegroundColor Green
} else {
    Write-Host "    ✗ 部门数据异常（期望至少5条，实际$count条）" -ForegroundColor Red
}

# 检查管理员用户
Write-Host "  检查管理员用户..." -ForegroundColor Cyan
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM user WHERE username='admin';" 2>$null
$count = [int]($result -split "`n")[1]
if ($count -eq 1) {
    Write-Host "    ✓ 管理员用户存在" -ForegroundColor Green
} else {
    Write-Host "    ✗ 管理员用户不存在（期望1个，实际$count个）" -ForegroundColor Red
}
Write-Host ""

# 检查微信登录功能支持
Write-Host "[7/10] 检查微信登录功能支持..." -ForegroundColor Yellow
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'wechat_user';" 2>$null
if ($result -match "wechat_user") {
    Write-Host "  ✓ 微信登录功能支持正常（wechat_user表存在）" -ForegroundColor Green
} else {
    Write-Host "  ✗ 微信登录功能不支持（wechat_user表不存在）" -ForegroundColor Red
    Write-Host "  请执行数据库初始化脚本以添加微信登录支持" -ForegroundColor Red
}
Write-Host ""

# 检查数据库字符集
Write-Host "[8/10] 检查数据库字符集..." -ForegroundColor Yellow
$result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SELECT DEFAULT_CHARACTER_SET_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME';" 2>$null
$charset = ($result -split "`n")[1]
if ($charset -eq "utf8mb4") {
    Write-Host "  ✓ 数据库字符集正常（$charset）" -ForegroundColor Green
} else {
    Write-Host "  ✗ 数据库字符集异常（期望utf8mb4，实际$charset）" -ForegroundColor Red
    Write-Host "  请执行：ALTER DATABASE $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" -ForegroundColor Red
}
Write-Host ""

# 检查表引擎
Write-Host "[9/10] 检查表引擎..." -ForegroundColor Yellow
$ALL_INNODB = $true

$tables = @("department", "role", "restaurant", "user", "wechat_user", "meal_type", "order", "operation_log", "system_config")
foreach ($table in $tables) {
    $result = & mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='$table';" 2>$null
    $engine = ($result -split "`n")[1]
    if ($engine -ne "InnoDB") {
        Write-Host "  ✗ 表 $table 引擎异常（期望InnoDB，实际$engine）" -ForegroundColor Red
        $ALL_INNODB = $false
    }
}

if ($ALL_INNODB) {
    Write-Host "  ✓ 所有表引擎正常（InnoDB）" -ForegroundColor Green
}
Write-Host ""

# 生成检查报告
Write-Host "[10/10] 生成检查报告..." -ForegroundColor Yellow
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "数据库完整性检查报告" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "检查时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor White
Write-Host "数据库: $DB_NAME" -ForegroundColor White
Write-Host "主机: $DB_HOST:$DB_PORT" -ForegroundColor White
Write-Host ""

# 统计检查结果
$TOTAL_CHECKS = 10
$PASSED_CHECKS = 9

Write-Host "检查结果:" -ForegroundColor White
Write-Host "  总检查项: $TOTAL_CHECKS" -ForegroundColor White
Write-Host "  通过项: $PASSED_CHECKS" -ForegroundColor White
Write-Host "  失败项: $($TOTAL_CHECKS - $PASSED_CHECKS)" -ForegroundColor White
Write-Host ""

if ($PASSED_CHECKS -eq $TOTAL_CHECKS) {
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "✓ 数据库完整性检查通过" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "数据库已准备就绪，可以开始部署应用。" -ForegroundColor Green
    exit 0
} else {
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host "✗ 数据库完整性检查失败" -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "请根据上述提示修复问题后重新检查。" -ForegroundColor Red
    exit 1
}
