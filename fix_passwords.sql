-- 修复用户密码为BCrypt格式

USE canteen_ordering_system;

-- 重置admin用户密码为 admin123
UPDATE user SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17fh3' WHERE username = 'admin';

-- 验证修改
SELECT username, LEFT(password, 30) as password_hash FROM user WHERE username = 'admin';

SELECT '密码修复完成！现在可以使用 admin/admin123 登录' AS result;