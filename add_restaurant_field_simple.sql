-- 为订单表添加就餐食堂字段（简化版）
-- 执行方式：在MySQL客户端中直接执行此脚本

USE canteen_ordering_system;

-- 添加restaurant_id字段到订单表
ALTER TABLE `order` ADD COLUMN `restaurant_id` BIGINT NULL COMMENT '就餐食堂ID' AFTER `meal_type_id`;

-- 显示订单表结构
DESCRIBE `order`;
