-- 创建数据库
CREATE DATABASE IF NOT EXISTS canteen_ordering_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE canteen_ordering_system;

-- 删除现有表（如果存在）
DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `meal_type`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `department`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `system_config`;

-- 创建部门表
CREATE TABLE IF NOT EXISTS `department` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) UNIQUE NOT NULL COMMENT '部门名称',
  `description` VARCHAR(200) NULL COMMENT '部门描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：启用，0：禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 创建角色表
CREATE TABLE IF NOT EXISTS `role` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称（员工、厨师、管理员）',
  `description` VARCHAR(200) NULL COMMENT '角色描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `department_id` BIGINT NULL COMMENT '部门ID',
  `phone` VARCHAR(20) NULL COMMENT '手机号',
  `email` VARCHAR(100) NULL COMMENT '邮箱',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：启用，0：禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`role_id`) REFERENCES `role`(`id`),
  FOREIGN KEY (`department_id`) REFERENCES `department`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建餐食类型表
CREATE TABLE IF NOT EXISTS `meal_type` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) UNIQUE NOT NULL COMMENT '餐食类型名称（早餐、午餐、晚餐）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：启用，0：禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐食类型表';

-- 创建订餐订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `meal_type_id` BIGINT NOT NULL COMMENT '餐食类型ID',
  `order_date` DATE NOT NULL COMMENT '订餐日期',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：有效，0：无效）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`meal_type_id`) REFERENCES `meal_type`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订餐订单表';

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型（订餐、修改订单、删除订单）',
  `operation_content` VARCHAR(500) NOT NULL COMMENT '操作内容',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `ip_address` VARCHAR(50) NULL COMMENT 'IP地址',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `config_key` VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键（lock_time、system_name等）',
  `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
  `description` VARCHAR(200) NULL COMMENT '配置描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化角色数据
INSERT INTO `role` (`name`, `description`) VALUES
('employee', '员工角色'),
('chef', '厨师角色'),
('admin', '管理员角色');

-- 初始化餐食类型数据
INSERT INTO `meal_type` (`name`, `price`) VALUES
('早餐', 5.00),
('午餐', 15.00),
('晚餐', 10.00);

-- 初始化系统配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('lock_time', '16:00', '锁单时间，超过此时间不能预订明天的餐食'),
('system_name', '单位内部饭堂订餐系统', '系统名称');

-- 初始化部门数据
INSERT INTO `department` (`name`, `description`) VALUES
('行政部', '负责行政管理工作'),
('技术部', '负责技术研发工作'),
('市场部', '负责市场推广工作'),
('财务部', '负责财务管理工作'),
('厨房', '负责餐食制作工作');

-- 初始化管理员用户（密码：admin123）
INSERT INTO `user` (`username`, `password`, `name`, `role_id`, `department_id`, `phone`, `email`, `balance`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '管理员', 3, 1, '13800138000', 'admin@example.com', 0.00);

-- 初始化测试员工用户（密码：123456）
INSERT INTO `user` (`username`, `password`, `name`, `role_id`, `department_id`, `phone`, `email`, `balance`) VALUES
('employee1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '张三', 1, 2, '13800138001', 'employee1@example.com', 0.00),
('employee2', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '李四', 1, 3, '13800138002', 'employee2@example.com', 0.00);

-- 初始化测试厨师用户（密码：chef123）
INSERT INTO `user` (`username`, `password`, `name`, `role_id`, `department_id`, `phone`, `email`, `balance`) VALUES
('chef1', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '王厨师', 2, 5, '13800138003', 'chef1@example.com', 0.00);
