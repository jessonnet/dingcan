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
DROP TABLE IF EXISTS `restaurant`;
DROP TABLE IF EXISTS `system_config`;
DROP TABLE IF EXISTS `wechat_user`;

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

-- 创建餐厅表
CREATE TABLE IF NOT EXISTS `restaurant` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '餐厅名称',
  `location` VARCHAR(200) NOT NULL COMMENT '餐厅位置',
  `cross_department_booking` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许跨部门订餐（1：允许，0：不允许）',
  `description` VARCHAR(500) NULL COMMENT '餐厅描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：启用，0：禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐厅表';

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `department_id` BIGINT NULL COMMENT '部门ID',
  `restaurant_id` BIGINT NULL COMMENT '餐厅ID',
  `phone` VARCHAR(20) NULL COMMENT '手机号',
  `email` VARCHAR(100) NULL COMMENT '邮箱',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：启用，0：禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`role_id`) REFERENCES `role`(`id`),
  FOREIGN KEY (`department_id`) REFERENCES `department`(`id`),
  FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建微信用户信息表
CREATE TABLE IF NOT EXISTS `wechat_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NULL COMMENT '关联的用户ID（如果已绑定）',
  `openid` VARCHAR(100) UNIQUE NOT NULL COMMENT '微信OpenID',
  `unionid` VARCHAR(100) NULL COMMENT '微信UnionID（如果有）',
  `nickname` VARCHAR(100) NULL COMMENT '微信昵称',
  `avatar` VARCHAR(500) NULL COMMENT '微信头像URL',
  `gender` TINYINT NULL COMMENT '性别（0：未知，1：男，2：女）',
  `country` VARCHAR(50) NULL COMMENT '国家',
  `province` VARCHAR(50) NULL COMMENT '省份',
  `city` VARCHAR(50) NULL COMMENT '城市',
  `language` VARCHAR(20) NULL COMMENT '语言',
  `phone` VARCHAR(20) NULL COMMENT '手机号',
  `subscribe_status` TINYINT NOT NULL DEFAULT 0 COMMENT '关注状态（0：未关注，1：已关注）',
  `subscribe_time` DATETIME NULL COMMENT '关注时间',
  `last_login_time` DATETIME NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户信息表';

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
  `restaurant_id` BIGINT NULL COMMENT '餐厅ID',
  `order_date` DATE NOT NULL COMMENT '订餐日期',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：有效，0：无效）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`meal_type_id`) REFERENCES `meal_type`(`id`),
  FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订餐订单表';

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '操作用户名',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型（LOGIN/LOGOUT/CREATE/UPDATE/DELETE/QUERY等）',
  `module` VARCHAR(100) NULL COMMENT '操作模块（用户管理/部门管理/餐食管理等）',
  `description` VARCHAR(500) NULL COMMENT '操作描述',
  `request_method` VARCHAR(10) NULL COMMENT '请求方式（GET/POST/PUT/DELETE）',
  `request_url` VARCHAR(500) NULL COMMENT '请求URL',
  `request_params` TEXT NULL COMMENT '请求参数',
  `old_data` TEXT NULL COMMENT '变更前数据（JSON格式）',
  `new_data` TEXT NULL COMMENT '变更后数据（JSON格式）',
  `ip_address` VARCHAR(50) NULL COMMENT '操作IP地址',
  `user_agent` VARCHAR(500) NULL COMMENT '用户代理信息',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态（1：成功，0：失败）',
  `error_message` TEXT NULL COMMENT '错误信息',
  `execution_time` BIGINT NULL COMMENT '执行时间（毫秒）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
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

-- 初始化餐厅数据
INSERT INTO `restaurant` (`name`, `location`, `cross_department_booking`, `description`) VALUES
('一号食堂', '一号楼一楼', 1, '主要提供中餐'),
('二号食堂', '二号楼二楼', 1, '主要提供西餐和小吃');

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
INSERT INTO `user` (`username`, `password`, `name`, `role_id`, `department_id`, `restaurant_id`, `phone`, `email`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17fh3', '管理员', 3, 1, 1, '13800138000', 'admin@example.com');

-- 初始化测试员工用户（密码：123456）
INSERT INTO `user` (`username`, `password`, `name`, `role_id`, `department_id`, `restaurant_id`, `phone`, `email`) VALUES
('employee1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '张三', 1, 2, 1, '13800138001', 'employee1@example.com'),
('employee2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '李四', 1, 3, 1, '13800138002', 'employee2@example.com');

-- 初始化测试厨师用户（密码：admin123）
INSERT INTO `user` (`username`, `password`, `name`, `role_id`, `department_id`, `restaurant_id`, `phone`, `email`) VALUES
('chef1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17fh3', '王厨师', 2, 5, 1, '13800138003', 'chef1@example.com');
