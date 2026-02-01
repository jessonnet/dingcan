package com.canteen.controller;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/check-department-table")
    public Map<String, Object> checkDepartmentTable() {
        try {
            DataSource dataSource = DataSourceBuilder.create()
                    .url("jdbc:mysql://localhost:3306/canteen_ordering_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true")
                    .username("root")
                    .password("123456")
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .build();
            
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 检查 department 表是否存在
            String checkTableSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_SCHEMA = 'canteen_ordering_system' 
                AND TABLE_NAME = 'department'
                """;
            Integer tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class);
            
            // 检查 user 表是否有 department_id 列
            String checkColumnSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = 'canteen_ordering_system' 
                AND TABLE_NAME = 'user' 
                AND COLUMN_NAME = 'department_id'
                """;
            Integer columnCount = jdbcTemplate.queryForObject(checkColumnSql, Integer.class);
            
            // 获取部门列表
            List<Map<String, Object>> departments = null;
            if (tableCount > 0) {
                String selectDepartmentsSql = "SELECT * FROM department";
                departments = jdbcTemplate.queryForList(selectDepartmentsSql);
            }
            
            return Map.of(
                "success", true,
                "departmentTableExists", tableCount > 0,
                "departmentIdColumnExists", columnCount > 0,
                "departments", departments
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
}
