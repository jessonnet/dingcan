package com.canteen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/init")
public class DatabaseInitController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/database")
    public String initDatabase() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sql/init.sql");
            if (inputStream == null) {
                return "SQL文件未找到";
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }
            reader.close();

            String[] statements = sqlBuilder.toString().split(";");
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty() && !trimmedStatement.startsWith("--")) {
                    try {
                        jdbcTemplate.execute(trimmedStatement);
                    } catch (Exception e) {
                        System.err.println("执行SQL失败: " + trimmedStatement);
                        e.printStackTrace();
                    }
                }
            }

            return "数据库初始化成功！";
        } catch (Exception e) {
            e.printStackTrace();
            return "数据库初始化失败: " + e.getMessage();
        }
    }

    @PostMapping("/createdb")
    public String createDatabase() {
        try {
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS canteen_ordering_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            return "数据库创建成功！";
        } catch (Exception e) {
            e.printStackTrace();
            return "数据库创建失败: " + e.getMessage();
        }
    }
}
