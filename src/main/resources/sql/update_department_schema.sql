-- 更新数据库 schema 以支持部门管理功能

-- 1. 创建部门表
CREATE TABLE IF NOT EXISTS `department` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) UNIQUE NOT NULL COMMENT '部门名称',
  `description` VARCHAR(200) NULL COMMENT '部门描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：启用，0：禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 2. 检查并添加 department_id 列到 user 表
SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = 'canteen_ordering_system'
  AND table_name = 'user'
  AND column_name = 'department_id'
);

SET @sql = IF(@column_exists = 0, 
  'ALTER TABLE user ADD COLUMN department_id BIGINT NULL COMMENT ''部门ID'' AFTER role_id',
  'SELECT ''Column department_id already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 删除旧的 department 列（如果存在）
SET @old_column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = 'canteen_ordering_system'
  AND table_name = 'user'
  AND column_name = 'department'
);

SET @sql = IF(@old_column_exists > 0, 
  'ALTER TABLE user DROP COLUMN department',
  'SELECT ''Old column department does not exist'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 添加外键约束（如果不存在）
SET @constraint_exists = (
  SELECT COUNT(*)
  FROM information_schema.table_constraints
  WHERE table_schema = 'canteen_ordering_system'
  AND table_name = 'user'
  AND constraint_name = 'fk_user_department'
);

SET @sql = IF(@constraint_exists = 0, 
  'ALTER TABLE user ADD CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES department(id)',
  'SELECT ''Constraint fk_user_department already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 初始化部门数据
INSERT IGNORE INTO department (name, description, status) VALUES
('行政部', '负责公司行政管理工作', 1),
('技术部', '负责技术研发工作', 1),
('市场部', '负责市场推广工作', 1),
('财务部', '负责财务管理工作', 1),
('厨房', '负责餐饮制作工作', 1);

-- 6. 更新现有用户的部门 ID
UPDATE user SET department_id = 5 WHERE username IN ('chef1', 'chef2', 'chef3');
UPDATE user SET department_id = 1 WHERE username = 'admin';
UPDATE user SET department_id = 2 WHERE username = 'user1';
UPDATE user SET department_id = 3 WHERE username = 'user2';
UPDATE user SET department_id = 4 WHERE username = 'user3';

-- 7. 验证更新结果
SELECT 'Database update completed successfully!' AS message;
SELECT COUNT(*) AS department_count FROM department;
SELECT COUNT(*) AS user_with_department FROM user WHERE department_id IS NOT NULL;
