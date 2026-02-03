-- 餐厅信息表
CREATE TABLE IF NOT EXISTS `restaurant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '餐厅名称',
  `location` VARCHAR(100) NOT NULL COMMENT '地点',
  `cross_department_booking` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否可跨部门预订（0-否，1-是）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_location` (`location`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐厅信息表';
