-- 添加余额字段到用户表
ALTER TABLE `user` ADD COLUMN `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额' AFTER `email`;

-- 更新现有记录的余额字段
UPDATE `user` SET `balance` = 0.00 WHERE `balance` IS NULL;