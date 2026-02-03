-- 为订单表添加就餐食堂字段（完整修复脚本）
-- 执行方式：在MySQL客户端中直接执行此脚本

USE canteen_ordering_system;

-- 检查字段是否已存在
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'canteen_ordering_system'
    AND TABLE_NAME = 'order'
    AND COLUMN_NAME = 'restaurant_id'
);

-- 如果字段不存在，则添加
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `restaurant_id` BIGINT NULL COMMENT ''就餐食堂ID'' AFTER `meal_type_id`',
    'SELECT ''restaurant_id字段已存在'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加外键约束（如果不存在）
SET @constraint_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'canteen_ordering_system'
    AND TABLE_NAME = 'order'
    AND CONSTRAINT_NAME = 'fk_order_restaurant'
);

SET @sql = IF(@constraint_exists = 0,
    'ALTER TABLE `order` ADD CONSTRAINT fk_order_restaurant FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant`(`id`)',
    'SELECT ''外键约束已存在'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示订单表结构
DESCRIBE `order`;

-- 显示成功消息
SELECT 'restaurant_id字段添加完成！' AS message;
