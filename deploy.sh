#!/bin/bash

# 食堂订餐系统快速部署脚本
# 服务器: 74.48.48.132
# 操作系统: Debian x86_64

set -e

echo "========================================="
echo "  食堂订餐系统部署脚本 v1.0.2"
echo "========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}请使用root用户运行此脚本${NC}"
    exit 1
fi

# 1. 环境检查
echo -e "${YELLOW}[1/7] 检查系统环境...${NC}"
echo ""

# 检查系统
echo "系统信息:"
cat /etc/os-release | grep -E "^(NAME|VERSION)=" | head -2
echo "架构: $(uname -m)"
echo ""

# 检查已有软件
echo "已安装软件:"
command -v mysql >/dev/null 2>&1 && echo "  ✓ MySQL: $(mysql --version | head -1)" || echo "  ✗ MySQL 未安装"
command -v nginx >/dev/null 2>&1 && echo "  ✓ Nginx: $(nginx -v 2>&1)" || echo "  ✗ Nginx 未安装"
command -v php >/dev/null 2>&1 && echo "  ✓ PHP: $(php -v | head -1)" || echo "  ✗ PHP 未安装"
command -v redis-server >/dev/null 2>&1 && echo "  ✓ Redis: $(redis-server --version)" || echo "  ✗ Redis 未安装"
command -v java >/dev/null 2>&1 && echo "  ✓ Java: $(java -version 2>&1 | head -1)" || echo "  ✗ Java 未安装"
command -v node >/dev/null 2>&1 && echo "  ✓ Node.js: $(node -v)" || echo "  ✗ Node.js 未安装"
echo ""

# 2. 安装JDK
echo -e "${YELLOW}[2/7] 安装JDK 17...${NC}"
if ! command -v java >/dev/null 2>&1; then
    cd /opt
    wget -q https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.tar.gz
    tar -xzf jdk-17_linux-x64_bin.tar.gz
    ln -sf /opt/jdk-17 /opt/java
    echo 'export JAVA_HOME=/opt/java' >> /etc/profile
    echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /etc/profile
    source /etc/profile
    echo -e "${GREEN}✓ JDK 17 安装完成${NC}"
else
    echo -e "${GREEN}✓ JDK 已安装${NC}"
fi
echo ""

# 3. 安装Maven
echo -e "${YELLOW}[3/7] 安装Maven...${NC}"
if ! command -v mvn >/dev/null 2>&1; then
    cd /opt
    wget -q https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
    tar -xzf apache-maven-3.9.5-bin.tar.gz
    ln -sf /opt/apache-maven-3.9.5 /opt/maven
    echo 'export MAVEN_HOME=/opt/maven' >> /etc/profile
    echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> /etc/profile
    source /etc/profile
    echo -e "${GREEN}✓ Maven 安装完成${NC}"
else
    echo -e "${GREEN}✓ Maven 已安装${NC}"
fi
echo ""

# 4. 安装Node.js
echo -e "${YELLOW}[4/7] 安装Node.js...${NC}"
if ! command -v node >/dev/null 2>&1; then
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
    npm install -g pnpm
    echo -e "${GREEN}✓ Node.js 安装完成${NC}"
else
    echo -e "${GREEN}✓ Node.js 已安装${NC}"
fi
echo ""

# 5. 配置数据库
echo -e "${YELLOW}[5/7] 配置数据库...${NC}"
read -p "请输入MySQL root密码: " -s MYSQL_PASS

echo "创建数据库..."
mysql -u root -p$MYSQL_PASS -e "CREATE DATABASE IF NOT EXISTS canteen_ordering_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || echo "数据库可能已存在"

echo "导入数据库结构..."
if [ -f "src/main/resources/sql/init.sql" ]; then
    mysql -u root -p$MYSQL_PASS canteen_ordering_system < src/main/resources/sql/init.sql 2>/dev/null || echo "数据库结构可能已导入"
    echo -e "${GREEN}✓ 数据库配置完成${NC}"
else
    echo -e "${RED}✗ 未找到数据库SQL文件${NC}"
fi
echo ""

# 6. 部署后端
echo -e "${YELLOW}[6/7] 部署后端...${NC}"

# 创建目录
mkdir -p /var/www/canteen/{backend,logs}

# 检查是否使用git
read -p "是否使用Git克隆代码? (y/n): " -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd /var/www/canteen/backend
    if [ -d ".git" ]; then
        echo "拉取最新代码..."
        git pull origin v1.0.2
    else
        echo "克隆代码仓库..."
        git clone https://github.com/jessonnet/dingcan.git backend
        cd backend
        git checkout v1.0.2
    fi
else
    echo "请手动上传后端JAR文件到 /var/www/canteen/backend/"
    read -p "按回车继续..." -r
fi

# 构建项目
if [ -f "pom.xml" ]; then
    echo "构建后端项目..."
    mvn clean package -DskipTests -q
    echo -e "${GREEN}✓ 后端构建完成${NC}"
else
    echo -e "${YELLOW}跳过构建（假设已上传JAR文件）${NC}"
fi

# 配置application.yml
echo "配置application.yml..."
cat > src/main/resources/application.yml << EOF
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/canteen_ordering_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: $MYSQL_PASS
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
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
EOF

# 创建systemd服务
echo "创建systemd服务..."
cat > /etc/systemd/system/canteen-backend.service << EOF
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
EOF

systemctl daemon-reload
systemctl enable canteen-backend
systemctl restart canteen-backend
echo -e "${GREEN}✓ 后端服务已启动${NC}"
echo ""

# 7. 部署前端
echo -e "${YELLOW}[7/7] 部署前端...${NC}"

mkdir -p /var/www/canteen/frontend

read -p "是否使用Git克隆前端代码? (y/n): " -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd /var/www/canteen/frontend
    if [ -d ".git" ]; then
        echo "拉取最新代码..."
        git pull origin v1.0.2
    else
        echo "克隆代码仓库..."
        git clone https://github.com/jessonnet/dingcan.git frontend
        cd frontend
        git checkout v1.0.2
    fi
    
    # 构建前端
    echo "构建前端项目..."
    pnpm install
    pnpm build
    echo -e "${GREEN}✓ 前端构建完成${NC}"
else
    echo "请手动上传前端dist目录到 /var/www/canteen/frontend/"
    read -p "按回车继续..." -r
fi

# 配置nginx
echo "配置nginx..."
cat > /etc/nginx/sites-available/canteen << EOF
server {
    listen 80;
    server_name 74.48.48.132;
    
    location / {
        root /var/www/canteen/frontend/dist;
        try_files \$uri \$uri/ /index.html;
        index index.html;
        
        gzip on;
        gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
        gzip_min_length 1000;
        gzip_comp_level 6;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    access_log /var/log/nginx/canteen_access.log;
    error_log /var/log/nginx/canteen_error.log;
}
EOF

ln -sf /etc/nginx/sites-available/canteen /etc/nginx/sites-enabled/
nginx -t && nginx -s reload
echo -e "${GREEN}✓ Nginx配置完成${NC}"
echo ""

# 8. 配置防火墙
echo -e "${YELLOW}[8/7] 配置防火墙...${NC}"
if command -v ufw >/dev/null 2>&1; then
    ufw allow 80/tcp
    ufw allow 8080/tcp
    ufw reload
    echo -e "${GREEN}✓ UFW防火墙配置完成${NC}"
elif command -v iptables >/dev/null 2>&1; then
    iptables -A INPUT -p tcp --dport 80 -j ACCEPT
    iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
    service iptables save
    echo -e "${GREEN}✓ iptables防火墙配置完成${NC}"
else
    echo -e "${YELLOW}未检测到防火墙，跳过配置${NC}"
fi
echo ""

# 9. 创建监控脚本
echo -e "${YELLOW}[9/7] 创建监控脚本...${NC}"
cat > /usr/local/bin/canteen-monitor.sh << 'EOF'
#!/bin/bash
if ! systemctl is-active --quiet canteen-backend; then
    systemctl restart canteen-backend
    echo "Backend restarted at $(date)" >> /var/log/canteen-monitor.log
fi
if ! systemctl is-active --quiet nginx; then
    systemctl restart nginx
    echo "Nginx restarted at $(date)" >> /var/log/canteen-monitor.log
fi
EOF

chmod +x /usr/local/bin/canteen-monitor.sh
(crontab -l 2>/dev/null | grep -q "canteen-monitor" || (crontab -l 2>/dev/null; echo "*/5 * * * * /usr/local/bin/canteen-monitor.sh") | crontab -)
echo -e "${GREEN}✓ 监控脚本配置完成${NC}"
echo ""

# 10. 创建备份脚本
echo -e "${YELLOW}[10/7] 创建备份脚本...${NC}"
cat > /usr/local/bin/canteen-backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/var/backups/canteen"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR
mysqldump -uroot -p$MYSQL_PASS canteen_ordering_system | gzip > $BACKUP_DIR/db_$DATE.sql.gz
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +7 -delete
echo "Backup completed at $(date)" >> /var/log/canteen-backup.log
EOF

chmod +x /usr/local/bin/canteen-backup.sh
(crontab -l 2>/dev/null | grep -q "canteen-backup" || (crontab -l 2>/dev/null; echo "0 2 * * * /usr/local/bin/canteen-backup.sh") | crontab -)
echo -e "${GREEN}✓ 备份脚本配置完成${NC}"
echo ""

# 完成
echo "========================================="
echo -e "${GREEN}  部署完成！${NC}"
echo "========================================="
echo ""
echo "服务状态:"
systemctl status canteen-backend --no-pager -l | head -3
systemctl status nginx --no-pager -l | head -3
echo ""
echo "访问地址:"
echo -e "  前端: ${GREEN}http://74.48.48.132${NC}"
echo -e "  后端API: ${GREEN}http://74.48.48.132/api${NC}"
echo ""
echo "日志查看:"
echo "  后端日志: journalctl -u canteen-backend -f"
echo "  应用日志: tail -f /var/www/canteen/logs/application.log"
echo "  Nginx日志: tail -f /var/log/nginx/canteen_error.log"
echo ""
echo -e "${YELLOW}请修改数据库密码和Redis密码为实际值${NC}"
