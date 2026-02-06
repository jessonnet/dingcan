-- 检查并添加微信登录相关配置项
USE canteen_ordering_system;

-- 检查配置项是否存在
SELECT '检查 wechat_login_enabled 配置项:' AS message;
SELECT * FROM system_config WHERE config_key = 'wechat_login_enabled';

SELECT '检查 wechat_login_mode 配置项:' AS message;
SELECT * FROM system_config WHERE config_key = 'wechat_login_mode';

-- 如果不存在，则添加
INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
SELECT 'wechat_login_enabled', '1', '微信登录功能是否启用（1：启用，0：禁用）'
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE `config_key` = 'wechat_login_enabled');

INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
SELECT 'wechat_login_mode', 'auto', '微信登录模式（auto：自动检测，force：强制微信，manual：手动选择）'
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE `config_key` = 'wechat_login_mode');

-- 再次检查
SELECT '添加后检查 wechat_login_enabled 配置项:' AS message;
SELECT * FROM system_config WHERE config_key = 'wechat_login_enabled';

SELECT '添加后检查 wechat_login_mode 配置项:' AS message;
SELECT * FROM system_config WHERE config_key = 'wechat_login_mode';

-- 显示所有系统配置
SELECT '所有系统配置:' AS message;
SELECT * FROM system_config;
