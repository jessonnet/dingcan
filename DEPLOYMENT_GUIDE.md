# Debian 12 食堂订餐系统部署指南

## 服务器信息
- IP: 111.230.115.247
- 密码: Bageba.888.
- 系统: Debian 12

## 部署步骤

### 1. 连接到服务器
```bash
ssh root@111.230.115.247
# 输入密码: Bageba.888.
```

### 2. 更新系统并安装必要软件
```bash
apt-get update
apt-get install -y openjdk-17-jdk mysql-server nginx curl wget vim git
```

### 3. 配置MySQL
```bash
# 启动MySQL服务
systemctl start mysql
systemctl enable mysql

# 设置MySQL root密码
mysql -u root
```
在MySQL命令行中执行:
```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'canteen123';
FLUSH PRIVILEGES;
EXIT;
```

### 4. 创建目录结构
```bash
mkdir -p /var/www/canteen/backend
mkdir -p /var/www/canteen/frontend
mkdir -p /var/www/canteen/logs
mkdir -p /var/www/canteen/backup
```

### 5. 创建数据库
```bash
mysql -uroot -pcanteen123 -e "CREATE DATABASE IF NOT EXISTS canteen_ordering_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 6. 导入数据库（重要：密码哈希设置）
```bash
# 从本地上传SQL文件
# 在本地Windows PowerShell中执行:
scp -o StrictHostKeyChecking=no d:\xampp\htdocs\order\database\init.sql root@111.230.115.247:/tmp/
scp -o StrictHostKeyChecking=no d:\xampp\htdocs\order\database\data.sql root@111.230.115.247:/tmp/

# 在服务器上导入
mysql -uroot -pcanteen123 canteen_ordering_system < /tmp/init.sql
mysql -uroot -pcanteen123 canteen_ordering_system < /tmp/data.sql
```

### 7. 验证并修复密码哈希
```bash
mysql -uroot -pcanteen123 canteen_ordering_system
```
在MySQL中执行:
```sql
-- 查看当前用户
SELECT id, username, password, name, role_id, status FROM user;

-- 如果密码哈希不正确，重置为admin/admin123
UPDATE user SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17fh3' WHERE username = 'admin';

-- 验证更新
SELECT id, username, password, name, role_id, status FROM user WHERE username = 'admin';
EXIT;
```

### 8. 上传后端JAR文件
在本地Windows PowerShell中执行:
```bash
scp -o StrictHostKeyChecking=no d:\xampp\htdocs\order\target\canteen-ordering-system-1.0.3.jar root@111.230.115.247:/var/www/canteen/backend/
```

### 9. 配置application.yml（如果需要）
在服务器上创建配置文件:
```bash
cat > /var/www/canteen/backend/application.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/canteen_ordering_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8
    username: root
    password: canteen123
    driver-class-name: com.mysql.cj.jdbc.Driver
server:
  port: 8080
  servlet:
    context-path: /
jwt:
  secret: canteen_ordering_system_secret_key
  expiration: 86400
  header: Authorization
logging:
  level:
    com.canteen: debug
EOF
```

### 10. 启动后端服务
```bash
cd /var/www/canteen/backend
nohup java -jar canteen-ordering-system-1.0.3.jar > /var/www/canteen/logs/application.log 2>&1 &
```

### 11. 检查后端服务状态
```bash
# 查看日志
tail -f /var/www/canteen/logs/application.log

# 检查端口
netstat -tlnp | grep 8080

# 测试健康检查
curl http://localhost:8080/api/health
```

### 12. 上传前端文件
在本地Windows PowerShell中执行:
```bash
# 如果前端已构建，上传dist目录
scp -r -o StrictHostKeyChecking=no d:\xampp\htdocs\order\frontend\dist\* root@111.230.115.247:/var/www/canteen/frontend/
```

### 13. 配置Nginx
```bash
cat > /etc/nginx/sites-available/canteen << 'EOF'
server {
    listen 80;
    server_name 111.230.115.247;

    # 前端静态文件
    location / {
        root /var/www/canteen/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 认证端点
    location /auth/ {
        proxy_pass http://localhost:8080/auth/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

# 启用站点
ln -sf /etc/nginx/sites-available/canteen /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# 测试并重启Nginx
nginx -t
systemctl restart nginx
systemctl enable nginx
```

### 14. 配置防火墙
```bash
# 如果使用ufw
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

# 或者使用iptables
iptables -A INPUT -p tcp --dport 22 -j ACCEPT
iptables -A INPUT -p tcp --dport 80 -j ACCEPT
iptables -A INPUT -p tcp --dport 443 -j ACCEPT
```

### 15. 测试登录功能
```bash
# 测试密码哈希验证
curl -X POST http://localhost:8080/api/auth/test-password \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'

# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

### 16. 创建系统服务（可选，用于自动启动）
```bash
cat > /etc/systemd/system/canteen-backend.service << 'EOF'
[Unit]
Description=Canteen Ordering System Backend
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/var/www/canteen/backend
ExecStart=/usr/bin/java -jar /var/www/canteen/backend/canteen-ordering-system-1.0.3.jar
Restart=always
RestartSec=10
StandardOutput=append:/var/www/canteen/logs/application.log
StandardError=append:/var/www/canteen/logs/application.log

[Install]
WantedBy=multi-user.target
EOF

# 启用并启动服务
systemctl daemon-reload
systemctl enable canteen-backend.service
systemctl start canteen-backend.service
systemctl status canteen-backend.service
```

## 常见问题排查

### 后端无法启动
```bash
# 查看日志
tail -100 /var/www/canteen/logs/application.log

# 检查Java版本
java -version

# 检查端口占用
netstat -tlnp | grep 8080
```

### 数据库连接失败
```bash
# 测试MySQL连接
mysql -uroot -pcanteen123 -e "SHOW DATABASES;"

# 检查MySQL服务状态
systemctl status mysql
```

### 登录失败
```bash
# 检查用户表
mysql -uroot -pcanteen123 canteen_ordering_system -e "SELECT id, username, password, name, role_id, status FROM user;"

# 重置admin密码
mysql -uroot -pcanteen123 canteen_ordering_system -e "UPDATE user SET password = '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17fh3' WHERE username = 'admin';"
```

## 访问地址
- 前端: http://111.230.115.247
- 后端API: http://111.230.115.247/api
- 登录测试: admin/admin123
