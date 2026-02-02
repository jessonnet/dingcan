# 食堂订餐系统部署指导

## 服务器信息
- **IP地址**: 74.48.48.132
- **操作系统**: Debian x86_64
- **已有环境**: MySQL、nginx、PHP、Redis

---

## 一、环境准备

### 1.1 检查系统信息
```bash
# 查看系统版本
cat /etc/debian_version
cat /etc/os-release

# 查看系统架构
uname -m

# 查看已安装的软件版本
mysql --version
nginx -v
php -v
redis-cli --version
```

### 1.2 安装必要软件

#### 安装JDK 17
```bash
# 下载JDK 17
cd /opt
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.tar.gz

# 解压
tar -xzf jdk-17_linux-x64_bin.tar.gz

# 创建软链接
ln -s /opt/jdk-17 /opt/java

# 配置环境变量
echo 'export JAVA_HOME=/opt/java' >> /etc/profile
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile
source /etc/profile

# 验证安装
java -version
```

#### 安装Maven
```bash
# 下载Maven
cd /opt
wget https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz

# 解压
tar -xzf apache-maven-3.9.5-bin.tar.gz

# 创建软链接
ln -s /opt/apache-maven-3.9.5 /opt/maven

# 配置环境变量
echo 'export MAVEN_HOME=/opt/maven' >> /etc/profile
echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> /etc/profile
source /etc/profile

# 验证安装
mvn -version
```

#### 安装Node.js（用于前端构建）
```bash
# 安装Node.js 18.x
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt-get install -y nodejs

# 验证安装
node -v
npm -v

# 安装pnpm（推荐使用pnpm加速构建）
npm install -g pnpm
```

---

## 二、数据库配置

### 2.1 创建数据库
```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS canteen_ordering_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户（如果需要）
CREATE USER IF NOT EXISTS 'canteen'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON canteen_ordering_system.* TO 'canteen'@'localhost';
FLUSH PRIVILEGES;

# 退出MySQL
EXIT;
```

### 2.2 导入数据库结构
```bash
# 从本地导出数据库
mysqldump -u root -p canteen_ordering_system > canteen_ordering_system.sql

# 上传到服务器后导入
mysql -u root -p canteen_ordering_system < canteen_ordering_system.sql
```

---

## 三、后端部署

### 3.1 创建项目目录
```bash
# 创建应用目录
mkdir -p /var/www/canteen
cd /var/www/canteen

# 创建子目录
mkdir -p backend logs
```

### 3.2 上传后端代码
```bash
# 方式1：使用git克隆
git clone https://github.com/jessonnet/dingcan.git backend
cd backend
git checkout v1.0.2

# 方式2：使用scp上传（从本地）
scp -r target/canteen-ordering-system-1.0.2.jar root@74.48.48.132:/var/www/canteen/backend/
```

### 3.3 修改配置文件
```bash
cd /var/www/canteen/backend

# 编辑application.yml
vim src/main/resources/application.yml
```

配置内容：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/canteen_ordering_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_mysql_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0

server:
  port: 8080
  servlet:
    context-path: /api

logging:
  level:
    com.canteen: INFO
  file:
    name: /var/www/canteen/logs/application.log
```

### 3.4 构建后端项目
```bash
cd /var/www/canteen/backend

# 如果上传的是源码，需要先构建
mvn clean package -DskipTests

# 构建完成后，jar文件在target目录下
ls -lh target/*.jar
```

### 3.5 创建systemd服务
```bash
# 创建服务文件
vim /etc/systemd/system/canteen-backend.service
```

服务文件内容：
```ini
[Unit]
Description=Canteen Ordering System Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/var/www/canteen/backend
ExecStart=/opt/java/bin/java -jar /var/www/canteen/backend/target/canteen-ordering-system-1.0.2.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=canteen-backend

[Install]
WantedBy=multi-user.target
```

### 3.6 启动后端服务
```bash
# 重载systemd配置
systemctl daemon-reload

# 启动服务
systemctl start canteen-backend

# 设置开机自启
systemctl enable canteen-backend

# 查看服务状态
systemctl status canteen-backend

# 查看日志
journalctl -u canteen-backend -f
```

---

## 四、前端部署

### 4.1 创建前端目录
```bash
# 创建前端目录
mkdir -p /var/www/canteen/frontend
cd /var/www/canteen/frontend
```

### 4.2 上传前端代码
```bash
# 方式1：使用git克隆
git clone https://github.com/jessonnet/dingcan.git frontend
cd frontend
git checkout v1.0.2

# 方式2：使用scp上传（从本地）
scp -r dist/* root@74.48.48.132:/var/www/canteen/frontend/
```

### 4.3 构建前端项目
```bash
cd /var/www/canteen/frontend

# 安装依赖
pnpm install

# 构建生产版本
pnpm build

# 或使用npm
npm install
npm run build
```

### 4.4 配置nginx
```bash
# 创建nginx配置文件
vim /etc/nginx/sites-available/canteen
```

nginx配置内容：
```nginx
server {
    listen 80;
    server_name 74.48.48.132;
    
    # 前端静态文件
    location / {
        root /var/www/canteen/frontend/dist;
        try_files $uri $uri/ /index.html;
        index index.html;
        
        # 启用gzip压缩
        gzip on;
        gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
        gzip_min_length 1000;
        gzip_comp_level 6;
    }
    
    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 日志配置
    access_log /var/log/nginx/canteen_access.log;
    error_log /var/log/nginx/canteen_error.log;
}
```

### 4.5 启用nginx配置
```bash
# 创建软链接
ln -s /etc/nginx/sites-available/canteen /etc/nginx/sites-enabled/

# 测试配置
nginx -t

# 重载nginx
nginx -s reload

# 重启nginx
systemctl restart nginx
```

---

## 五、防火墙配置

### 5.1 开放必要端口
```bash
# 使用ufw防火墙
ufw allow 80/tcp
ufw allow 443/tcp
ufw allow 8080/tcp
ufw reload

# 或使用iptables
iptables -A INPUT -p tcp --dport 80 -j ACCEPT
iptables -A INPUT -p tcp --dport 443 -j ACCEPT
iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
service iptables save
```

---

## 六、SSL证书配置（可选）

### 6.1 使用Let's Encrypt免费证书
```bash
# 安装certbot
apt-get install -y certbot python3-certbot-nginx

# 获取证书
certbot --nginx -d 74.48.48.132

# 自动续期
certbot renew --dry-run
```

### 6.2 更新nginx配置支持HTTPS
```nginx
server {
    listen 443 ssl http2;
    server_name 74.48.48.132;
    
    ssl_certificate /etc/letsencrypt/live/74.48.48.132/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/74.48.48.132/privkey.pem;
    
    # SSL配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # 其他配置同上...
}
```

---

## 七、监控和维护

### 7.1 服务监控脚本
```bash
# 创建监控脚本
vim /usr/local/bin/canteen-monitor.sh
```

监控脚本内容：
```bash
#!/bin/bash

# 检查后端服务
if ! systemctl is-active --quiet canteen-backend; then
    echo "Backend service is down, restarting..."
    systemctl restart canteen-backend
    echo "Backend service restarted at $(date)" >> /var/log/canteen-monitor.log
fi

# 检查nginx服务
if ! systemctl is-active --quiet nginx; then
    echo "Nginx service is down, restarting..."
    systemctl restart nginx
    echo "Nginx service restarted at $(date)" >> /var/log/canteen-monitor.log
fi

# 检查MySQL服务
if ! systemctl is-active --quiet mysql; then
    echo "MySQL service is down, restarting..."
    systemctl restart mysql
    echo "MySQL service restarted at $(date)" >> /var/log/canteen-monitor.log
fi
```

### 7.2 设置定时任务
```bash
# 添加到crontab
crontab -e

# 每5分钟检查一次
*/5 * * * * * /usr/local/bin/canteen-monitor.sh
```

---

## 八、备份策略

### 8.1 数据库备份
```bash
# 创建备份脚本
vim /usr/local/bin/canteen-backup.sh
```

备份脚本内容：
```bash
#!/bin/bash

BACKUP_DIR="/var/backups/canteen"
DATE=$(date +%Y%m%d_%H%M%S)
MYSQL_USER="root"
MYSQL_PASS="your_password"
DB_NAME="canteen_ordering_system"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u$MYSQL_USER -p$MYSQL_PASS $DB_NAME | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# 删除7天前的备份
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +7 -delete

echo "Backup completed at $(date)" >> /var/log/canteen-backup.log
```

### 8.2 设置定时备份
```bash
# 添加到crontab
crontab -e

# 每天凌晨2点备份
0 2 * * * /usr/local/bin/canteen-backup.sh
```

---

## 九、常见问题排查

### 9.1 后端无法启动
```bash
# 查看服务状态
systemctl status canteen-backend

# 查看详细日志
journalctl -u canteen-backend -n 100

# 查看应用日志
tail -f /var/www/canteen/logs/application.log

# 检查端口占用
netstat -tlnp | grep 8080
```

### 9.2 前端无法访问
```bash
# 检查nginx状态
systemctl status nginx

# 查看nginx错误日志
tail -f /var/log/nginx/canteen_error.log

# 检查配置文件
nginx -t

# 检查文件权限
ls -la /var/www/canteen/frontend/dist/
```

### 9.3 数据库连接失败
```bash
# 检查MySQL状态
systemctl status mysql

# 测试连接
mysql -u root -p -e "SELECT 1"

# 检查防火墙
ufw status
iptables -L -n
```

---

## 十、部署检查清单

- [ ] JDK 17已安装并配置环境变量
- [ ] Maven已安装并配置环境变量
- [ ] Node.js已安装
- [ ] 数据库已创建并导入数据
- [ ] 后端配置文件已修改
- [ ] 后端服务已启动并设置开机自启
- [ ] 前端已构建
- [ ] nginx配置已完成
- [ ] 防火墙端口已开放
- [ ] 服务监控脚本已配置
- [ ] 数据库备份脚本已配置
- [ ] SSL证书已配置（如需要）
- [ ] 所有服务正常运行

---

## 十一、快速部署命令汇总

```bash
# 一键部署后端
cd /var/www/canteen/backend
git pull origin v1.0.2
mvn clean package -DskipTests
systemctl restart canteen-backend

# 一键部署前端
cd /var/www/canteen/frontend
git pull origin v1.0.2
pnpm install
pnpm build
nginx -s reload

# 查看所有服务状态
systemctl status canteen-backend nginx mysql redis
```

---

## 联系方式

如有问题，请联系技术支持或查看项目文档：
- GitHub: https://github.com/jessonnet/dingcan
- 版本: v1.0.2
