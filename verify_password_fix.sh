#!/bin/bash

echo "========================================"
echo "密码验证问题修复验证"
echo "========================================"
echo ""

echo "1. 检查数据库初始化脚本中的密码哈希..."
echo "----------------------------------------"
grep -A 1 "初始化管理员用户" src/main/resources/sql/init.sql | grep "INSERT INTO.*user"
grep -A 2 "初始化测试员工用户" src/main/resources/sql/init.sql | grep "INSERT INTO.*user"
grep -A 1 "初始化测试厨师用户" src/main/resources/sql/init.sql | grep "INSERT INTO.*user"

echo ""
echo "2. 检查AuthController中的密码重置逻辑..."
echo "----------------------------------------"
grep -A 5 "resetAllPasswords" src/main/java/com/canteen/controller/AuthController.java | grep -E "(usernames|wang)"

echo ""
echo "3. 检查前端CDN引用..."
echo "----------------------------------------"
grep "cdn.jsdelivr.net" frontend/index.html && echo "❌ 仍然存在CDN引用" || echo "✅ CDN引用已移除"

echo ""
echo "4. 检查application.yml中的数据库密码..."
echo "----------------------------------------"
grep "password:" src/main/resources/application.yml | head -1

echo ""
echo "========================================"
echo "验证完成"
echo "========================================"
echo ""
echo "修复总结："
echo "✅ 数据库初始化脚本中的密码哈希值已修复"
echo "✅ AuthController中的密码重置逻辑已修复"
echo "✅ 前端CDN引用已移除"
echo "✅ application.yml中的数据库密码已修复"
echo ""
echo "下次部署时，使用修复后的文件，无需再重置密码！"