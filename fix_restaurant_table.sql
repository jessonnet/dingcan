USE canteen_ordering_system;

-- 修复 restaurant 表结构异常
-- 1. 修改 name 字段长度
ALTER TABLE restaurant MODIFY COLUMN name VARCHAR(100) NOT NULL COMMENT '餐厅名称';

-- 2. 添加 description 字段
ALTER TABLE restaurant ADD COLUMN description VARCHAR(500) NULL COMMENT '餐厅描述' AFTER location;

-- 3. 修改 cross_department_booking 默认值
ALTER TABLE restaurant MODIFY COLUMN cross_department_booking TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许跨部门订餐（1：允许，0：不允许）';

-- 4. 更新现有餐厅数据的描述信息
UPDATE restaurant SET description = '提供早餐、午餐、晚餐服务' WHERE id = 1;
UPDATE restaurant SET description = '提供午餐、晚餐服务' WHERE id = 2;
UPDATE restaurant SET description = '提供午餐服务' WHERE id = 3;

-- 5. 验证修复结果
SELECT '修复完成，当前餐厅表结构：' AS message;
DESCRIBE restaurant;

SELECT '当前餐厅数据：' AS message;
SELECT * FROM restaurant;
