package com.canteen.controller;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/db")
public class DatabaseController {

    @PostMapping("/init")
    public String initDatabase() {
        try {
            System.out.println("开始数据库初始化...");
            DataSource initDataSource = DataSourceBuilder.create()
                    .url("jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true")
                    .username("root")
                    .password("123456")
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .build();
            
            JdbcTemplate initJdbcTemplate = new JdbcTemplate(initDataSource);
            System.out.println("JdbcTemplate创建成功");
            
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sql/init.sql");
            if (inputStream == null) {
                System.out.println("SQL文件未找到");
                return "SQL文件未找到";
            }
            System.out.println("SQL文件读取成功");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                if (!trimmedLine.startsWith("--") && !trimmedLine.isEmpty()) {
                    sqlBuilder.append(line).append("\n");
                }
            }
            reader.close();
            System.out.println("SQL内容读取完成，总长度: " + sqlBuilder.length());

            String[] statements = sqlBuilder.toString().split(";");
            System.out.println("分割得到 " + statements.length + " 条SQL语句");
            
            int successCount = 0;
            int failCount = 0;
            
            for (int i = 0; i < statements.length; i++) {
                String statement = statements[i];
                String trimmedStatement = statement.trim();
                System.out.println("SQL语句 " + (i+1) + ": 长度=" + trimmedStatement.length() + ", 内容=" + (trimmedStatement.length() > 0 ? trimmedStatement.substring(0, Math.min(50, trimmedStatement.length())) : "空"));
                if (!trimmedStatement.isEmpty() && !trimmedStatement.startsWith("--")) {
                    if (trimmedStatement.toLowerCase().startsWith("use ")) {
                        System.out.println("跳过USE语句: " + trimmedStatement);
                        continue;
                    }
                    try {
                        // 处理用户插入语句，自动加密密码
                        if (trimmedStatement.toLowerCase().startsWith("insert into `user`")) {
                            trimmedStatement = encryptUserPasswords(trimmedStatement);
                            System.out.println("处理后的用户插入语句: " + trimmedStatement.substring(0, Math.min(80, trimmedStatement.length())));
                        }
                        initJdbcTemplate.execute(trimmedStatement);
                        successCount++;
                        System.out.println("执行SQL成功(" + successCount + "/" + statements.length + "): " + trimmedStatement.substring(0, Math.min(80, trimmedStatement.length())));
                        if (trimmedStatement.toLowerCase().startsWith("create database")) {
                            DataSource appDataSource = DataSourceBuilder.create()
                                    .url("jdbc:mysql://localhost:3306/canteen_ordering_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true")
                                    .username("root")
                                    .password("123456")
                                    .driverClassName("com.mysql.cj.jdbc.Driver")
                                    .build();
                            initJdbcTemplate = new JdbcTemplate(appDataSource);
                            System.out.println("切换到新数据库连接");
                        }
                    } catch (Exception e) {
                        failCount++;
                        System.err.println("执行SQL失败(" + failCount + "): " + trimmedStatement);
                        e.printStackTrace();
                    }
                }
            }
            
            System.out.println("数据库初始化完成！成功: " + successCount + ", 失败: " + failCount);
            return "数据库初始化成功！成功执行: " + successCount + "条，失败: " + failCount + "条";
        } catch (Exception e) {
            e.printStackTrace();
            return "数据库初始化失败: " + e.getMessage();
        }
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers() {
        try {
            DataSource dataSource = DataSourceBuilder.create()
                    .url("jdbc:mysql://localhost:3306/canteen_ordering_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true")
                    .username("root")
                    .password("123456")
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .build();
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String sql = "SELECT id, username, password, status FROM user";
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String encryptUserPasswords(String sql) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 处理用户插入语句，自动加密密码
        // 匹配所有包含 INSERT INTO user 或 INSERT INTO `user` 的语句
        if (sql.toLowerCase().contains("insert into user") || sql.toLowerCase().contains("insert into `user`")) {
            System.out.println("检测到用户插入语句，开始处理密码加密");
            
            // 提取 VALUES 部分
            int valuesIndex = sql.indexOf("VALUES");
            if (valuesIndex != -1) {
                String prefix = sql.substring(0, valuesIndex + "VALUES".length());
                String valuesPart = sql.substring(valuesIndex + "VALUES".length());
                
                // 简单处理：将所有硬编码的密码哈希替换为新的加密值
                // 使用简单的字符串替换，避免正则表达式的问题
                String encodedPassword = encoder.encode("123456");
                System.out.println("生成的加密密码: " + encodedPassword);
                
                // 替换所有硬编码的密码哈希
                // 这里使用简单的字符串替换，避免正则表达式的问题
                valuesPart = valuesPart.replace("'$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW'", "'" + encodedPassword + "'");
                
                // 构建新的 SQL 语句
                String newSql = prefix + valuesPart;
                System.out.println("处理后的SQL: " + newSql.substring(0, Math.min(100, newSql.length())) + "...");
                return newSql;
            }
        }
        return sql;
    }

    @PostMapping("/update-department-schema")
    public String updateDepartmentSchema() {
        try {
            System.out.println("开始更新部门数据库schema...");
            DataSource dataSource = DataSourceBuilder.create()
                    .url("jdbc:mysql://localhost:3306/canteen_ordering_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true")
                    .username("root")
                    .password("123456")
                    .driverClassName("com.mysql.cj.jdbc.Driver")
                    .build();
            
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 1. 创建部门表
            String createDepartmentTable = """
                CREATE TABLE IF NOT EXISTS `department` (
                  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                  `name` VARCHAR(100) UNIQUE NOT NULL COMMENT '部门名称',
                  `description` VARCHAR(200) NULL COMMENT '部门描述',
                  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1：启用，0：禁用）',
                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表'
                """;
            jdbcTemplate.execute(createDepartmentTable);
            System.out.println("部门表创建成功");
            
            // 2. 检查并添加 department_id 列
            String checkColumnSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = 'canteen_ordering_system' 
                AND TABLE_NAME = 'user' 
                AND COLUMN_NAME = 'department_id'
                """;
            Integer columnCount = jdbcTemplate.queryForObject(checkColumnSql, Integer.class);
            
            if (columnCount == 0) {
                String addColumnSql = "ALTER TABLE `user` ADD COLUMN `department_id` BIGINT NULL COMMENT '部门ID' AFTER `role_id`";
                jdbcTemplate.execute(addColumnSql);
                System.out.println("添加 department_id 列成功");
            } else {
                System.out.println("department_id 列已存在");
            }
            
            // 3. 删除旧的 department 列（如果存在）
            String checkOldColumnSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = 'canteen_ordering_system' 
                AND TABLE_NAME = 'user' 
                AND COLUMN_NAME = 'department'
                """;
            Integer oldColumnCount = jdbcTemplate.queryForObject(checkOldColumnSql, Integer.class);
            
            if (oldColumnCount > 0) {
                String dropColumnSql = "ALTER TABLE `user` DROP COLUMN `department`";
                jdbcTemplate.execute(dropColumnSql);
                System.out.println("删除旧的 department 列成功");
            } else {
                System.out.println("旧的 department 列不存在");
            }
            
            // 4. 添加外键约束
            try {
                String addForeignKeySql = """
                    ALTER TABLE `user` ADD CONSTRAINT `fk_user_department` 
                    FOREIGN KEY (`department_id`) REFERENCES `department`(`id`)
                    """;
                jdbcTemplate.execute(addForeignKeySql);
                System.out.println("添加外键约束成功");
            } catch (Exception e) {
                System.out.println("外键约束可能已存在: " + e.getMessage());
            }
            
            // 5. 初始化部门数据
            String insertDepartmentSql = """
                INSERT IGNORE INTO `department` (`name`, `description`) VALUES
                ('行政部', '负责行政管理工作'),
                ('技术部', '负责技术研发工作'),
                ('市场部', '负责市场推广工作'),
                ('财务部', '负责财务管理工作'),
                ('厨房', '负责餐食制作工作')
                """;
            jdbcTemplate.execute(insertDepartmentSql);
            System.out.println("部门数据初始化成功");
            
            // 6. 更新现有用户的部门 ID
            String updateUserDepartmentSql = """
                UPDATE `user` u 
                LEFT JOIN `department` d ON d.name = (
                  CASE u.username
                    WHEN 'admin' THEN '行政部'
                    WHEN 'employee1' THEN '技术部'
                    WHEN 'employee2' THEN '市场部'
                    WHEN 'chef1' THEN '厨房'
                    ELSE NULL
                  END
                ) 
                SET u.department_id = d.id 
                WHERE u.department_id IS NULL AND d.id IS NOT NULL
                """;
            jdbcTemplate.execute(updateUserDepartmentSql);
            System.out.println("用户部门ID更新成功");
            
            return "部门数据库schema更新成功！";
        } catch (Exception e) {
            e.printStackTrace();
            return "部门数据库schema更新失败: " + e.getMessage();
        }
    }
}
