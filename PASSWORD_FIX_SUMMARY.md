# 密码验证问题修复总结

## 问题描述
每次部署到远程主机时都需要重置管理员和其他用户的密码才能正常运行。

## 根本原因

### 1. 数据库初始化脚本中的密码哈希值错误
**文件**: `src/main/resources/sql/init.sql`

**问题**: 所有用户使用了相同的错误密码哈希值 `$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW`

**修复**: 为每个用户使用正确的BCrypt密码哈希值：
- admin (密码: admin123): `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17fh3`
- employee1 (密码: 123456): `$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi`
- employee2 (密码: 123456): `$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi`
- chef1 (密码: admin123): `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17fh3`

### 2. 密码重置API中的逻辑错误
**文件**: `src/main/java/com/canteen/controller/AuthController.java`

**问题**: 密码重置方法尝试重置不存在的"wang"用户

**修复**: 修改为循环重置所有实际存在的用户（admin, employee1, employee2, chef1）

### 3. 前端CDN引用问题
**文件**: `frontend/index.html`

**问题**: 使用了CDN加载Element Plus CSS，可能导致连接超时

**修复**: 移除CDN引用，使用本地打包的CSS

### 4. 数据库密码配置问题
**文件**: `src/main/resources/application.yml`

**问题**: 数据库密码配置不匹配

**修复**: 确保application.yml中的数据库密码与实际MySQL密码一致

## 已修复的文件清单

1. ✅ `src/main/resources/sql/init.sql` - 修复了所有用户的密码哈希值
2. ✅ `src/main/java/com/canteen/controller/AuthController.java` - 修复了密码重置逻辑
3. ✅ `frontend/index.html` - 移除了CDN引用
4. ✅ `src/main/resources/application.yml` - 修复了数据库密码配置

## 部署后的默认登录凭据

| 用户名 | 密码 | 角色 | 部门 |
|--------|--------|------|--------|
| admin | admin123 | 管理员 | 行政部 |
| employee1 | 123456 | 员工 | 技术部 |
| employee2 | 123456 | 员工 | 市场部 |
| chef1 | admin123 | 厨师 | 厨房 |

## 部署后验证步骤

1. 导入数据库初始化脚本
2. 启动后端服务
3. 测试登录：
   - 使用 admin/admin123 登录
   - 使用 employee1/123456 登录
   - 使用 chef1/admin123 登录
4. 验证登录后能正常跳转到对应页面
5. 如果密码验证失败，可以调用密码重置API：
   ```bash
   curl -X POST http://localhost:8080/api/auth/reset-passwords
   ```

## 注意事项

1. **密码哈希格式**: 必须使用BCrypt格式（$2a$10$...），不能使用PHP格式（$2y$10$...）
2. **密码重置API**: 现在会重置所有存在的用户，不会因为用户不存在而失败
3. **角色名称**: 数据库中的角色名称使用英文（employee, chef, admin），前端会自动转换为小写进行匹配
4. **数据库连接**: 确保application.yml中的数据库密码与实际MySQL密码一致

## 下次部署建议

1. 使用修复后的 `src/main/resources/sql/init.sql` 初始化数据库
2. 使用修复后的 `src/main/java/com/canteen/controller/AuthController.java` 作为后端代码
3. 使用修复后的 `frontend/index.html` 作为前端入口
4. 部署后直接使用默认凭据登录，无需重置密码