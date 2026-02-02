# 食堂订餐系统 Docker 部署指南

## 服务器信息
- **IP地址**: 74.48.48.132
- **操作系统**: Debian x86_64
- **已有环境**: Docker、MySQL、Nginx、PHP、Redis

---

## 一、环境准备

### 1.1 检查Docker环境
```bash
# 检查Docker是否安装
docker --version

# 检查Docker Compose是否安装
docker-compose --version

# 查看运行中的容器
docker ps -a

# 查看所有容器
docker ps -a
```

### 1.2 安装Docker（如果未安装）
```bash
# 更新软件包索引
apt-get update

# 安装依赖包
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# 添加Docker官方GPG密钥
curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 添加Docker APT源
echo \
  "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装Docker
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io

# 启动Docker服务
systemctl start docker
systemctl enable docker

# 验证安装
docker --version
```

### 1.3 安装Docker Compose（如果未安装）
```bash
# 下载Docker Compose
curl -SL https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose

# 添加执行权限
chmod +x /usr/local/bin/docker-compose

# 创建软链接
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# 验证安装
docker-compose --version
```

### 1.4 配置Docker用户（可选，推荐）
```bash
# 创建docker用户组（如果不存在）
groupadd docker 2>/dev/null || true

# 将当前用户添加到docker组
usermod -aG docker $USER

# 重新登录以使组权限生效
newgrp docker
```

---

## 二、项目准备

### 2.1 上传项目文件到服务器
```bash
# 方式1：使用Git克隆
cd /opt
git clone https://github.com/jessonnet/dingcan.git canteen
cd canteen
git checkout v1.0.2

# 方式2：使用SCP上传（从本地）
scp -r /path/to/local/canteen root@74.48.48.132:/opt/canteen/
```

### 2.2 准备配置文件
```bash
cd /opt/canteen

# 创建.env文件
cat > .env << 'EOF'
# MySQL配置
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=canteen_ordering_system
MYSQL_USER=canteen
MYSQL_PASSWORD=canteen123456

# Redis配置（如果需要密码）
# REDIS_PASSWORD=redis123456

# 后端配置
SPRING_PROFILES_ACTIVE=prod
EOF

# 设置.env文件权限（重要！）
chmod 600 .env
```

### 2.3 准备数据库初始化文件
```bash
# 检查init.sql是否存在
ls -lh src/main/resources/sql/init.sql

# 如果使用Git克隆，文件应该已存在
# 如果使用SCP上传，确保init.sql已上传
```

---

## 三、部署步骤

### 3.1 构建并启动所有服务
```bash
cd /opt/canteen

# 构建并启动所有服务（首次部署）
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f
```

### 3.2 查看服务启动状态
```bash
# 查看所有容器状态
docker-compose ps

# 预期输出：
# NAME                IMAGE                    STATUS
# canteen-mysql       mysql:8.0               Up (healthy)
# canteen-redis       redis:7-alpine           Up (healthy)
# canteen-backend     canteen-backend           Up (healthy)
# canteen-nginx       nginx:alpine             Up (healthy)
```

### 3.3 查看各服务日志
```bash
# 查看MySQL日志
docker-compose logs mysql

# 查看Redis日志
docker-compose logs redis

# 查看后端日志
docker-compose logs backend

# 查看Nginx日志
docker-compose logs nginx

# 查看所有服务日志
docker-compose logs -f
```

---

## 四、数据库初始化

### 4.1 连接到MySQL容器
```bash
# 进入MySQL容器
docker-compose exec mysql mysql -uroot -proot123456

# 或从外部连接
mysql -h 127.0.0.1 -P 3306 -uroot -proot123456
```

### 4.2 导入数据库结构
```bash
# 方式1：使用init.sql自动初始化
# Docker Compose配置中已挂载init.sql，容器启动时会自动执行

# 方式2：手动导入
docker-compose exec -T mysql mysql -uroot -proot123456 < src/main/resources/sql/init.sql
```

### 4.3 验证数据库
```bash
# 连接数据库
docker-compose exec mysql mysql -uroot -proot123456

# 查看数据库
SHOW DATABASES;

# 使用数据库
USE canteen_ordering_system;

# 查看表
SHOW TABLES;

# 退出
EXIT;
```

---

## 五、前端部署

### 5.1 构建前端
```bash
# 进入前端目录
cd /opt/canteen/frontend

# 安装依赖
npm install

# 或使用pnpm（推荐，更快）
pnpm install

# 构建生产版本
npm run build

# 或
pnpm build

# 构建完成后，dist目录包含所有静态文件
ls -lh dist/
```

### 5.2 配置Nginx
```bash
# Nginx已在Docker Compose中配置
# 前端dist目录已挂载到Nginx容器

# 重新加载Nginx配置
docker-compose restart nginx
```

### 5.3 验证前端访问
```bash
# 访问前端
curl -I http://74.48.48.132

# 预期输出：
# HTTP/1.1 200 OK
# Content-Type: text/html
```

---

## 六、常用操作命令

### 6.1 服务管理
```bash
cd /opt/canteen

# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose stop

# 重启所有服务
docker-compose restart

# 停止并删除所有容器
docker-compose down

# 停止并删除所有容器和卷
docker-compose down -v

# 重新构建并启动
docker-compose up -d --build

# 查看服务状态
docker-compose ps
```

### 6.2 日志查看
```bash
cd /opt/canteen

# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend

# 实时查看日志
docker-compose logs -f backend

# 查看最近100行日志
docker-compose logs --tail=100 backend
```

### 6.3 容器操作
```bash
# 进入后端容器
docker-compose exec backend bash

# 进入MySQL容器
docker-compose exec mysql bash

# 进入Redis容器
docker-compose exec redis sh

# 在容器中执行命令
docker-compose exec backend java -version

# 查看容器资源使用
docker stats
```

### 6.4 镜像管理
```bash
# 查看所有镜像
docker images

# 删除未使用的镜像
docker image prune -f

# 删除所有未使用的镜像
docker image prune -a -f

# 查看镜像大小
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
```

---

## 七、更新部署

### 7.1 更新后端代码
```bash
cd /opt/canteen

# 拉取最新代码
git pull origin v1.0.2

# 重新构建并启动后端
docker-compose up -d --build backend

# 查看后端日志
docker-compose logs -f backend
```

### 7.2 更新前端代码
```bash
cd /opt/canteen/frontend

# 拉取最新代码
git pull origin v1.0.2

# 重新构建前端
npm run build
# 或
pnpm build

# 重启Nginx以加载新文件
docker-compose restart nginx
```

### 7.3 更新数据库结构
```bash
# 如果有数据库结构变更
docker-compose exec -T mysql mysql -uroot -proot123456 < src/main/resources/sql/init.sql
```

---

## 八、数据备份

### 8.1 备份数据库
```bash
# 创建备份目录
mkdir -p /opt/canteen/backups

# 备份MySQL数据库
docker-compose exec -T mysql mysqldump -uroot -proot123456 \
  canteen_ordering_system | gzip > /opt/canteen/backups/db_$(date +%Y%m%d_%H%M%S).sql.gz

# 查看备份文件
ls -lh /opt/canteen/backups/
```

### 8.2 恢复数据库
```bash
# 恢复数据库
gunzip < /opt/canteen/backups/db_20240101_120000.sql.gz | \
  docker-compose exec -T mysql mysql -uroot -proot123456 canteen_ordering_system
```

### 8.3 自动备份脚本
```bash
# 创建备份脚本
cat > /opt/canteen/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/canteen/backups"
mkdir -p $BACKUP_DIR

docker-compose exec -T mysql mysqldump -uroot -proot123456 \
  canteen_ordering_system | gzip > $BACKUP_DIR/db_$(date +%Y%m%d_%H%M%S).sql.gz

# 删除7天前的备份
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +7 -delete

echo "Backup completed at $(date)" >> /opt/canteen/backup.log
EOF

# 添加执行权限
chmod +x /opt/canteen/backup.sh

# 添加到crontab（每天凌晨2点备份）
(crontab -l 2>/dev/null | grep -q "canteen/backup.sh" || \
  (crontab -l 2>/dev/null; echo "0 2 * * * /opt/canteen/backup.sh") | crontab -)
```

---

## 九、SSL证书配置

### 9.1 使用Let's Encrypt免费证书
```bash
# 安装certbot
apt-get install -y certbot

# 停止Nginx容器
docker-compose stop nginx

# 获取证书
certbot certonly --standalone -d 74.48.48.132

# 证书位置
# /etc/letsencrypt/live/74.48.48.132/fullchain.pem
# /etc/letsencrypt/live/74.48.48.132/privkey.pem

# 复制证书到nginx目录
mkdir -p nginx/ssl
cp /etc/letsencrypt/live/74.48.48.132/fullchain.pem nginx/ssl/cert.pem
cp /etc/letsencrypt/live/74.48.48.132/privkey.pem nginx/ssl/key.pem

# 重启Nginx
docker-compose start nginx
```

### 9.2 配置自动续期
```bash
# 添加到crontab（每周一凌晨3点检查续期）
(crontab -l 2>/dev/null | grep -q "certbot renew" || \
  (crontab -l 2>/dev/null; echo "0 3 * * 1 /usr/bin/certbot renew --quiet && docker-compose restart nginx") | crontab -)
```

---

## 十、监控和维护

### 10.1 健康检查
```bash
# 检查所有服务健康状态
docker-compose ps

# 查看容器健康状态
docker inspect --format='{{.State.Health.Status}}' canteen-mysql
docker inspect --format='{{.State.Health.Status}}' canteen-redis
docker inspect --format='{{.State.Health.Status}}' canteen-backend
docker inspect --format='{{.State.Health.Status}}' canteen-nginx

# 预期输出：healthy
```

### 10.2 资源监控
```bash
# 查看容器资源使用
docker stats --no-stream

# 查看磁盘使用
df -h

# 查看内存使用
free -h

# 查看Docker系统信息
docker system df
```

### 10.3 日志管理
```bash
# 查看Docker日志大小
docker system df

# 清理未使用的日志
docker system prune -f

# 清理所有未使用的数据
docker system prune -a -f --volumes
```

---

## 十一、常见问题排查

### 11.1 容器无法启动
```bash
# 查看容器日志
docker-compose logs backend

# 查看容器详细状态
docker-compose ps -a

# 查看容器详细信息
docker inspect canteen-backend

# 重新构建容器
docker-compose up -d --build backend
```

### 11.2 端口冲突
```bash
# 检查端口占用
netstat -tlnp | grep -E ':(80|443|8080|3306|6379)'

# 修改docker-compose.yml中的端口映射
vim docker-compose.yml

# 重启服务
docker-compose up -d
```

### 11.3 数据库连接失败
```bash
# 检查MySQL容器状态
docker-compose ps mysql

# 进入MySQL容器测试
docker-compose exec mysql mysql -uroot -proot123456 -e "SELECT 1"

# 检查网络连接
docker-compose exec backend ping -c 3 mysql

# 查看后端日志
docker-compose logs backend | grep -i "error"
```

### 11.4 前端无法访问
```bash
# 检查Nginx容器状态
docker-compose ps nginx

# 查看Nginx日志
docker-compose logs nginx

# 检查前端文件
docker-compose exec nginx ls -la /usr/share/nginx/html

# 测试Nginx配置
docker-compose exec nginx nginx -t

# 重启Nginx
docker-compose restart nginx
```

### 11.5 权限问题
```bash
# 检查.env文件权限
ls -la .env

# 修复权限
chmod 600 .env

# 检查日志目录权限
ls -la logs/

# 修复日志目录权限
chmod -R 755 logs/
```

---

## 十二、性能优化

### 12.1 限制容器资源
```bash
# 编辑docker-compose.yml
vim docker-compose.yml

# 添加资源限制
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### 12.2 使用Docker网络优化
```bash
# 查看Docker网络
docker network ls

# 创建自定义网络（已配置）
# networks:
#   canteen-network:
#     driver: bridge

# 使用host网络（如果需要更高性能）
# networks:
#   canteen-network:
#     driver: host
```

---

## 十三、安全加固

### 13.1 更新默认密码
```bash
# 修改.env文件中的密码
vim .env

# 修改MySQL密码
MYSQL_ROOT_PASSWORD=your_strong_password
MYSQL_PASSWORD=your_strong_password

# 重启服务
docker-compose up -d mysql backend
```

### 13.2 配置防火墙
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

### 13.3 启用Docker内容信任
```bash
# 编辑Docker配置
vim /etc/docker/daemon.json

# 添加内容
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}

# 重启Docker
systemctl restart docker
```

---

## 十四、部署检查清单

### 14.1 部署前检查
- [ ] Docker已安装
- [ ] Docker Compose已安装
- [ ] 项目文件已上传到服务器
- [ ] .env配置文件已创建
- [ ] init.sql文件存在
- [ ] 防火墙端口已开放（80, 443, 8080）

### 14.2 部署后检查
- [ ] MySQL容器运行正常
- [ ] Redis容器运行正常
- [ ] 后端容器运行正常
- [ ] Nginx容器运行正常
- [ ] 数据库已初始化
- [ ] 前端已构建
- [ ] 前端可以访问（http://74.48.48.132）
- [ ] 后端API可以访问（http://74.48.48.132/api）
- [ ] SSL证书已配置（如需要）
- [ ] 备份脚本已配置
- [ ] 监控脚本已配置

---

## 十五、快速部署命令汇总

```bash
# 一键部署（首次）
cd /opt/canteen
docker-compose up -d --build

# 一键更新
cd /opt/canteen
git pull origin v1.0.2
docker-compose up -d --build

# 查看所有服务状态
docker-compose ps

# 查看所有服务日志
docker-compose logs -f

# 重启所有服务
docker-compose restart

# 停止所有服务
docker-compose stop

# 清理并重启
docker-compose down
docker-compose up -d --build
```

---

## 十六、联系与支持

- **GitHub仓库**: https://github.com/jessonnet/dingcan
- **当前版本**: v1.0.2
- **部署文档**: 本文档

### 常用命令速查表

| 命令 | 说明 |
|------|------|
| `docker-compose ps` | 查看所有容器状态 |
| `docker-compose logs` | 查看所有服务日志 |
| `docker-compose logs -f backend` | 实时查看后端日志 |
| `docker-compose restart` | 重启所有服务 |
| `docker-compose up -d --build` | 重新构建并启动 |
| `docker-compose down` | 停止并删除所有容器 |
| `docker stats` | 查看容器资源使用 |
| `docker exec backend bash` | 进入后端容器 |

---

## 附录：Docker Compose文件说明

### docker-compose.yml结构
- **mysql**: MySQL 8.0数据库服务
- **redis**: Redis 7缓存服务
- **backend**: Spring Boot后端服务
- **nginx**: Nginx反向代理服务

### 环境变量
- `MYSQL_ROOT_PASSWORD`: MySQL root密码
- `MYSQL_DATABASE`: 数据库名称
- `MYSQL_USER`: 数据库用户
- `MYSQL_PASSWORD`: 数据库密码
- `SPRING_DATASOURCE_URL`: 数据库连接URL
- `SPRING_REDIS_HOST`: Redis主机地址
- `SPRING_REDIS_PORT`: Redis端口

### 端口映射
- `3306`: MySQL
- `6379`: Redis
- `8080`: 后端API
- `80`: HTTP前端
- `443`: HTTPS前端

### 数据卷
- `mysql-data`: MySQL数据持久化
- `redis-data`: Redis数据持久化
- `./logs`: 应用日志
- `./frontend/dist`: 前端静态文件
- `./nginx/nginx.conf`: Nginx配置文件
- `./nginx/ssl`: SSL证书

---

**部署完成后访问地址**:
- 前端: http://74.48.48.132
- 后端API: http://74.48.48.132/api
- 默认账号: admin / admin123（请立即修改密码）
