-- 操作日志表
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

-- 创建索引以提高查询性能
CREATE INDEX idx_user_id ON operation_log(user_id);
CREATE INDEX idx_username ON operation_log(username);
CREATE INDEX idx_operation_type ON operation_log(operation_type);
CREATE INDEX idx_created_at ON operation_log(created_at);
CREATE INDEX idx_module ON operation_log(module);
