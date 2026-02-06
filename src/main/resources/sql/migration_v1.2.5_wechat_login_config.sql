-- 微信登录功能配置迁移脚本
-- 版本: V1.2.5
-- 说明: 添加微信登录功能开关和登录模式配置

USE canteen_ordering_system;

-- 检查并添加微信登录功能启用配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
VALUES ('wechat_login_enabled', '1', '微信登录功能是否启用（1：启用，0：禁用）')
ON DUPLICATE KEY UPDATE `config_value` = '1';

-- 检查并添加微信登录模式配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`)
VALUES ('wechat_login_mode', 'auto', '微信登录模式（auto：自动检测，force：强制微信，manual：手动选择）')
ON DUPLICATE KEY UPDATE `config_value` = 'auto';

-- 验证配置是否添加成功
SELECT * FROM `system_config` WHERE `config_key` IN ('wechat_login_enabled', 'wechat_login_mode');