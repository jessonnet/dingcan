-- 为订单表添加就餐食堂字段（方案二 - 完整版）
-- 执行方式：在MySQL客户端中直接执行此脚本
-- 建议使用phpMyAdmin或Navicat等MySQL客户端工具

USE canteen_ordering_system;

-- 步骤1：添加restaurant_id字段
ALTER TABLE `order` ADD COLUMN `restaurant_id` BIGINT NULL COMMENT '就餐食堂ID' AFTER `meal_type_id`;

-- 步骤2：添加外键约束
ALTER TABLE `order` ADD CONSTRAINT fk_order_restaurant FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant`(`id`);

-- 步骤3：验证字段是否添加成功
DESCRIBE `order`;

-- 步骤4：修复历史订单数据（将restaurant_id设置为餐厅表第一条记录）
UPDATE `order` 
SET restaurant_id = (SELECT id FROM restaurant LIMIT 1)
WHERE restaurant_id IS NULL;

-- 步骤5：显示修复结果
SELECT 
    COUNT(*) as total_orders,
    SUM(CASE WHEN restaurant_id IS NULL THEN 1 ELSE 0 END) as null_restaurant_count,
    SUM(CASE WHEN restaurant_id IS NOT NULL THEN 1 ELSE 0 END) as has_restaurant_count
FROM `order`;
