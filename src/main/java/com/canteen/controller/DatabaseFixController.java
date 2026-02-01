package com.canteen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库修复控制器
 */
@RestController
@RequestMapping("/fix")
public class DatabaseFixController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 修复操作日志表结构
     * @return 结果
     */
    @GetMapping("/operation-log")
    public Map<String, Object> fixOperationLogTable() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 删除旧表
            jdbcTemplate.execute("DROP TABLE IF EXISTS operation_log");
            
            // 创建新表
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
            jdbcTemplate.execute("CREATE INDEX idx_user_id ON operation_log(user_id)");
            jdbcTemplate.execute("CREATE INDEX idx_username ON operation_log(username)");
            jdbcTemplate.execute("CREATE INDEX idx_operation_type ON operation_log(operation_type)");
            jdbcTemplate.execute("CREATE INDEX idx_created_at ON operation_log(created_at)");
            jdbcTemplate.execute("CREATE INDEX idx_module ON operation_log(module)");
            
            result.put("success", true);
            result.put("message", "操作日志表修复成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "修复失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
