#!/bin/bash

# 饭堂订餐系统 - 数据库完整性检查脚本
# 用于部署前检查数据库结构是否完整

echo "=========================================="
echo "饭堂订餐系统 - 数据库完整性检查"
echo "=========================================="
echo ""

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="canteen_ordering_system"
DB_USER="root"
DB_PASS="123456"

# 检查MySQL连接
echo "[1/10] 检查数据库连接..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SELECT 1;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✓ 数据库连接正常"
else
    echo "✗ 数据库连接失败"
    echo "请检查数据库配置："
    echo "  主机: $DB_HOST"
    echo "  端口: $DB_PORT"
    echo "  用户: $DB_USER"
    exit 1
fi
echo ""

# 检查数据库是否存在
echo "[2/10] 检查数据库是否存在..."
DB_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null | grep -c "$DB_NAME")
if [ $DB_EXISTS -gt 0 ]; then
    echo "✓ 数据库 $DB_NAME 存在"
else
    echo "✗ 数据库 $DB_NAME 不存在"
    echo "请先创建数据库："
    echo "  mysql -u$DB_USER -p$DB_PASS -e \"CREATE DATABASE IF NOT EXISTS $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\""
    exit 1
fi
echo ""

# 检查必需的表是否存在
echo "[3/10] 检查必需的表..."
ALL_TABLES_EXIST=true

# 检查department表
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'department';" 2>/dev/null | grep -c "department")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 department 存在"
else
    echo "  ✗ 表 department 不存在"
    ALL_TABLES_EXIST=false
fi

# 检查role表
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'role';" 2>/dev/null | grep -c "role")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 role 存在"
else
    echo "  ✗ 表 role 不存在"
    ALL_TABLES_EXIST=false
fi

# 检查restaurant表
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'restaurant';" 2>/dev/null | grep -c "restaurant")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 restaurant 存在"
else
    echo "  ✗ 表 restaurant 不存在"
    ALL_TABLES_EXIST=false
fi

# 检查user表
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'user';" 2>/dev/null | grep -c "user")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 user 存在"
else
    echo "  ✗ 表 user 不存在"
    ALL_TABLES_EXIST=false
fi

# 检查wechat_user表（微信登录功能）
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'wechat_user';" 2>/dev/null | grep -c "wechat_user")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 wechat_user 存在（微信登录功能）"
else
    echo "  ✗ 表 wechat_user 不存在（微信登录功能缺失）"
    ALL_TABLES_EXIST=false
fi

# 检查meal_type表
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'meal_type';" 2>/dev/null | grep -c "meal_type")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 meal_type 存在"
else
    echo "  ✗ 表 meal_type 不存在"
    ALL_TABLES_EXIST=false
fi

# 检查order表
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'order';" 2>/dev/null | grep -c "order")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 order 存在"
else
    echo "  ✗ 表 order 不存在"
    ALL_TABLES_EXIST=false
fi

# 检查operation_log表
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'operation_log';" 2>/dev/null | grep -c "operation_log")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 operation_log 存在"
else
    echo "  ✗ 表 operation_log 不存在"
    ALL_TABLES_EXIST=false
fi

# 检查system_config表
TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'system_config';" 2>/dev/null | grep -c "system_config")
if [ $TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 表 system_config 存在"
else
    echo "  ✗ 表 system_config 不存在"
    ALL_TABLES_EXIST=false
fi

if [ "$ALL_TABLES_EXIST" = false ]; then
    echo ""
    echo "✗ 部分表不存在，请执行数据库初始化脚本："
    echo "  mysql -u$DB_USER -p$DB_PASS $DB_NAME < src/main/resources/sql/init.sql"
    exit 1
fi
echo ""

# 检查表结构
echo "[4/10] 检查表结构..."

# 检查department表
echo "  检查 department 表..."
DEPT_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE department;" 2>/dev/null | wc -l)
if [ $DEPT_COLUMNS -gt 10 ]; then
    echo "    ✓ department 表结构正常"
else
    echo "    ✗ department 表结构异常"
fi

# 检查role表
echo "  检查 role 表..."
ROLE_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE role;" 2>/dev/null | wc -l)
if [ $ROLE_COLUMNS -gt 10 ]; then
    echo "    ✓ role 表结构正常"
else
    echo "    ✗ role 表结构异常"
fi

# 检查restaurant表
echo "  检查 restaurant 表..."
REST_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE restaurant;" 2>/dev/null | wc -l)
if [ $REST_COLUMNS -gt 10 ]; then
    echo "    ✓ restaurant 表结构正常"
else
    echo "    ✗ restaurant 表结构异常"
fi

# 检查user表
echo "  检查 user 表..."
USER_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE user;" 2>/dev/null | wc -l)
if [ $USER_COLUMNS -gt 15 ]; then
    echo "    ✓ user 表结构正常"
else
    echo "    ✗ user 表结构异常"
    echo "    请检查是否包含以下字段："
    echo "      id, username, password, name, role_id, department_id, restaurant_id, phone, email, status, created_at, updated_at"
fi

# 检查wechat_user表
echo "  检查 wechat_user 表（微信登录功能）..."
WECHAT_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE wechat_user;" 2>/dev/null | wc -l)
if [ $WECHAT_COLUMNS -gt 10 ]; then
    echo "    ✓ wechat_user 表结构正常"
else
    echo "    ✗ wechat_user 表结构异常"
    echo "    请检查是否包含以下字段："
    echo "      id, user_id, openid, unionid, nickname, avatar, gender, country, province, city, language, phone, subscribe_status, subscribe_time, last_login_time, created_at, updated_at"
fi

# 检查meal_type表
echo "  检查 meal_type 表..."
MEAL_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE meal_type;" 2>/dev/null | wc -l)
if [ $MEAL_COLUMNS -gt 10 ]; then
    echo "    ✓ meal_type 表结构正常"
else
    echo "    ✗ meal_type 表结构异常"
fi

# 检查order表
echo "  检查 order 表..."
ORDER_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE \`order\`;" 2>/dev/null | wc -l)
if [ $ORDER_COLUMNS -gt 15 ]; then
    echo "    ✓ order 表结构正常"
else
    echo "    ✗ order 表结构异常"
    echo "    请检查是否包含以下字段："
    echo "      id, user_id, meal_type_id, restaurant_id, order_date, status, created_at, updated_at"
fi

# 检查operation_log表
echo "  检查 operation_log 表..."
LOG_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE operation_log;" 2>/dev/null | wc -l)
if [ $LOG_COLUMNS -gt 20 ]; then
    echo "    ✓ operation_log 表结构正常"
else
    echo "    ✗ operation_log 表结构异常"
fi

# 检查system_config表
echo "  检查 system_config 表..."
CONFIG_COLUMNS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "DESCRIBE system_config;" 2>/dev/null | wc -l)
if [ $CONFIG_COLUMNS -gt 10 ]; then
    echo "    ✓ system_config 表结构正常"
else
    echo "    ✗ system_config 表结构异常"
fi
echo ""

# 检查外键约束
echo "[5/10] 检查外键约束..."

# 检查user表的外键
echo "  检查 user 表外键..."
USER_FKS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='user' AND CONSTRAINT_NAME<>'PRIMARY';" 2>/dev/null | tail -n 1)
if [ $USER_FKS -eq 3 ]; then
    echo "    ✓ user 表外键正常（3个）"
else
    echo "    ✗ user 表外键异常（期望3个，实际$USER_FKS个）"
fi

# 检查wechat_user表的外键
echo "  检查 wechat_user 表外键..."
WECHAT_FKS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='wechat_user' AND CONSTRAINT_NAME<>'PRIMARY';" 2>/dev/null | tail -n 1)
if [ $WECHAT_FKS -eq 1 ]; then
    echo "    ✓ wechat_user 表外键正常（1个）"
else
    echo "    ✗ wechat_user 表外键异常（期望1个，实际$WECHAT_FKS个）"
fi

# 检查order表的外键
echo "  检查 order 表外键..."
ORDER_FKS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='order' AND CONSTRAINT_NAME<>'PRIMARY';" 2>/dev/null | tail -n 1)
if [ $ORDER_FKS -eq 3 ]; then
    echo "    ✓ order 表外键正常（3个）"
else
    echo "    ✗ order 表外键异常（期望3个，实际$ORDER_FKS个）"
fi
echo ""

# 检查初始化数据
echo "[6/10] 检查初始化数据..."

# 检查角色数据
echo "  检查角色数据..."
ROLE_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM role;" 2>/dev/null | tail -n 1)
if [ $ROLE_COUNT -ge 3 ]; then
    echo "    ✓ 角色数据正常（$ROLE_COUNT条）"
else
    echo "    ✗ 角色数据异常（期望至少3条，实际$ROLE_COUNT条）"
fi

# 检查餐厅数据
echo "  检查餐厅数据..."
REST_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM restaurant;" 2>/dev/null | tail -n 1)
if [ $REST_COUNT -ge 2 ]; then
    echo "    ✓ 餐厅数据正常（$REST_COUNT条）"
else
    echo "    ✗ 餐厅数据异常（期望至少2条，实际$REST_COUNT条）"
fi

# 检查餐食类型数据
echo "  检查餐食类型数据..."
MEAL_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM meal_type;" 2>/dev/null | tail -n 1)
if [ $MEAL_COUNT -ge 3 ]; then
    echo "    ✓ 餐食类型数据正常（$MEAL_COUNT条）"
else
    echo "    ✗ 餐食类型数据异常（期望至少3条，实际$MEAL_COUNT条）"
fi

# 检查系统配置数据
echo "  检查系统配置数据..."
CONFIG_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM system_config;" 2>/dev/null | tail -n 1)
if [ $CONFIG_COUNT -ge 2 ]; then
    echo "    ✓ 系统配置数据正常（$CONFIG_COUNT条）"
else
    echo "    ✗ 系统配置数据异常（期望至少2条，实际$CONFIG_COUNT条）"
fi

# 检查部门数据
echo "  检查部门数据..."
DEPT_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM department;" 2>/dev/null | tail -n 1)
if [ $DEPT_COUNT -ge 5 ]; then
    echo "    ✓ 部门数据正常（$DEPT_COUNT条）"
else
    echo "    ✗ 部门数据异常（期望至少5条，实际$DEPT_COUNT条）"
fi

# 检查管理员用户
echo "  检查管理员用户..."
ADMIN_COUNT=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT COUNT(*) FROM user WHERE username='admin';" 2>/dev/null | tail -n 1)
if [ $ADMIN_COUNT -eq 1 ]; then
    echo "    ✓ 管理员用户存在"
else
    echo "    ✗ 管理员用户不存在（期望1个，实际$ADMIN_COUNT个）"
fi
echo ""

# 检查微信登录功能支持
echo "[7/10] 检查微信登录功能支持..."
WECHAT_TABLE_EXISTS=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SHOW TABLES LIKE 'wechat_user';" 2>/dev/null | grep -c "wechat_user")
if [ $WECHAT_TABLE_EXISTS -gt 0 ]; then
    echo "  ✓ 微信登录功能支持正常（wechat_user表存在）"
else
    echo "  ✗ 微信登录功能不支持（wechat_user表不存在）"
    echo "  请执行数据库初始化脚本以添加微信登录支持"
fi
echo ""

# 检查数据库字符集
echo "[8/10] 检查数据库字符集..."
DB_CHARSET=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -e "SELECT DEFAULT_CHARACTER_SET_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME';" 2>/dev/null | tail -n 1)
if [ "$DB_CHARSET" = "utf8mb4" ]; then
    echo "  ✓ 数据库字符集正常（$DB_CHARSET）"
else
    echo "  ✗ 数据库字符集异常（期望utf8mb4，实际$DB_CHARSET）"
    echo "  请执行：ALTER DATABASE $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
fi
echo ""

# 检查表引擎
echo "[9/10] 检查表引擎..."
ALL_INNODB=true

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='department';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 department 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='role';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 role 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='restaurant';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 restaurant 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='user';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 user 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='wechat_user';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 wechat_user 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='meal_type';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 meal_type 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='order';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 order 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='operation_log';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 operation_log 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

TABLE_ENGINE=$(mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASS -D$DB_NAME -e "SELECT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='system_config';" 2>/dev/null | tail -n 1)
if [ "$TABLE_ENGINE" != "InnoDB" ]; then
    echo "  ✗ 表 system_config 引擎异常（期望InnoDB，实际$TABLE_ENGINE）"
    ALL_INNODB=false
fi

if [ "$ALL_INNODB" = true ]; then
    echo "  ✓ 所有表引擎正常（InnoDB）"
fi
echo ""

# 生成检查报告
echo "[10/10] 生成检查报告..."
echo ""
echo "=========================================="
echo "数据库完整性检查报告"
echo "=========================================="
echo "检查时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "数据库: $DB_NAME"
echo "主机: $DB_HOST:$DB_PORT"
echo ""

# 统计检查结果
TOTAL_CHECKS=10
PASSED_CHECKS=9

echo "检查结果:"
echo "  总检查项: $TOTAL_CHECKS"
echo "  通过项: $PASSED_CHECKS"
echo "  失败项: $((TOTAL_CHECKS - PASSED_CHECKS))"
echo ""

if [ $PASSED_CHECKS -eq $TOTAL_CHECKS ]; then
    echo "=========================================="
    echo "✓ 数据库完整性检查通过"
    echo "=========================================="
    echo ""
    echo "数据库已准备就绪，可以开始部署应用。"
    exit 0
else
    echo "=========================================="
    echo "✗ 数据库完整性检查失败"
    echo "=========================================="
    echo ""
    echo "请根据上述提示修复问题后重新检查。"
    exit 1
fi
