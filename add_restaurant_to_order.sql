-- 为订单表添加就餐食堂字段
USE canteen_ordering_system;

-- 添加restaurant_id字段到订单表
ALTER TABLE `order` ADD COLUMN `restaurant_id` BIGINT NULL COMMENT '就餐食堂ID' AFTER `meal_type_id`;

-- 添加外键约束
ALTER TABLE `order` ADD FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant`(`id`);

-- 验证字段是否添加成功
DESCRIBE `order`;
