package com.canteen.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库修复配置类
 * 应用启动时自动修复数据库表结构
 */
@Component
public class DatabaseFixConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixDatabase() {
        try {
            // 检查操作日志表是否存在
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'operation_log'",
                Integer.class
            );
            
            if (count == null || count == 0) {
                // 表不存在，创建新表
                createOperationLogTable();
            } else {
                // 表存在，检查是否有username字段
                try {
                    jdbcTemplate.queryForObject("SELECT username FROM operation_log LIMIT 1", String.class);
                } catch (Exception e) {
                    // 字段不存在，删除旧表重建
                    jdbcTemplate.execute("DROP TABLE IF EXISTS operation_log");
                    createOperationLogTable();
                }
            }
            
            System.out.println("数据库表结构检查完成");
        } catch (Exception e) {
            System.err.println("数据库修复失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createOperationLogTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS operation_log (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id BIGINT NOT NULL COMMENT '操作用户ID', " +
                "username VARCHAR(50) NOT NULL COMMENT '操作用户名', " +
                "operation_type VARCHAR(50) NOT NULL COMMENT '操作类型', " +
                "module VARCHAR(100) NULL COMMENT '操作模块', " +
                "description VARCHAR(500) NULL COMMENT '操作描述', " +
                "request_method VARCHAR(10) NULL COMMENT '请求方式', " +
                "request_url VARCHAR(500) NULL COMMENT '请求URL', " +
                "request_params TEXT NULL COMMENT '请求参数', " +
                "old_data TEXT NULL COMMENT '变更前数据', " +
                "new_data TEXT NULL COMMENT '变更后数据', " +
                "ip_address VARCHAR(50) NULL COMMENT '操作IP地址', " +
                "user_agent VARCHAR(500) NULL COMMENT '用户代理信息', " +
                "status TINYINT NOT NULL DEFAULT 1 COMMENT '操作状态', " +
                "error_message TEXT NULL COMMENT '错误信息', " +
                "execution_time BIGINT NULL COMMENT '执行时间', " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表'";
        
        jdbcTemplate.execute(createTableSql);
        
        // 创建索引
        try {
            jdbcTemplate.execute("CREATE INDEX idx_user_id ON operation_log(user_id)");
            jdbcTemplate.execute("CREATE INDEX idx_username ON operation_log(username)");
            jdbcTemplate.execute("CREATE INDEX idx_operation_type ON operation_log(operation_type)");
            jdbcTemplate.execute("CREATE INDEX idx_created_at ON operation_log(created_at)");
            jdbcTemplate.execute("CREATE INDEX idx_module ON operation_log(module)");
        } catch (Exception e) {
            // 索引可能已存在，忽略错误
        }
        
        System.out.println("操作日志表创建成功");
    }
}
