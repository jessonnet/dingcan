-- 餐厅信息表
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

-- 初始化餐厅数据
INSERT INTO `restaurant` (`name`, `location`, `cross_department_booking`, `description`) VALUES
('一号食堂', '一号楼一楼', 1, '主要提供中餐'),
('二号食堂', '二号楼二楼', 1, '主要提供西餐和小吃');