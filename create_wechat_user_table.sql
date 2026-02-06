USE canteen_ordering_system;

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
