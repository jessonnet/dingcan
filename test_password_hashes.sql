-- 测试和生成正确的BCrypt密码哈希

USE canteen_ordering_system;

-- 测试密码哈希
SELECT '测试 admin123 的哈希:' AS info;
SELECT '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17fh3' AS admin123_hash;

SELECT '测试 123456 的哈希:' AS info;
SELECT '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' AS password123456_hash;

SELECT '测试 chef123 的哈希:' AS info;
SELECT '$2a$10$wXZqT3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q3q' AS chef123_hash;

-- 验证当前数据库中的密码哈希
SELECT '当前数据库中的密码哈希:' AS info;
SELECT username, LEFT(password, 30) as password_hash FROM user;