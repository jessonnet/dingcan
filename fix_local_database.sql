-- 修复本地数据库表结构以匹配V1.1.0版本代码

USE canteen_ordering_system;

-- 检查并添加 restaurant_id 字段到 user 表
ALTER TABLE user ADD COLUMN IF NOT EXISTS restaurant_id BIGINT NULL COMMENT '餐厅ID' AFTER department_id;

-- 检查并添加 cross_department_booking 字段到 restaurant 表
ALTER TABLE restaurant ADD COLUMN IF NOT EXISTS cross_department_booking TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许跨部门订餐（1：允许，0：不允许）' AFTER location;

-- 显示修复后的表结构
SELECT 'user 表结构:' AS info;
DESCRIBE user;

SELECT 'restaurant 表结构:' AS info;
DESCRIBE restaurant;

SELECT '修复完成！' AS result;