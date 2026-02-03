# 后端逐步部署指南

## 📋 部署前准备

### 服务器信息
- **IP地址**: 111.230.115.247
- **操作系统**: Debian 12
- **部署目录**: /opt/canteen

### 必要文件
在本地电脑的 `d:\xampp\htdocs\order\` 目录下，确保有以下文件：

```
order/
├── src/                      # 后端源代码
├── pom.xml                   # Maven配置文件
├── sql/                      # 数据库SQL文件
│   └── init.sql
└── application.yml           # 应用配置文件
```

---

## 🚀 步骤1：上传文件到服务器

### 方法A：使用WinSCP（推荐）

1. **下载并安装WinSCP**: https://winscp.net/
2. **连接到服务器**：
   - 主机: `111.230.115.247`
   - 端口: `22`
   - 用户名: `root`
   - 密码: [您的服务器密码]
3. **创建部署目录**：
   - 在WinSCP中，导航到 `/opt/` 目录
   - 右键点击，选择"新建目录"
   - 输入目录名: `canteen`
4. **上传文件到 `/opt/canteen/` 目录**：
   - `src/` 文件夹（整个文件夹）
   - `pom.xml` 文件
   - `sql/` 文件夹（整个文件夹）
   - `src/main/resources/application.yml` 文件

### 方法B：使用SCP命令

在本地电脑的命令提示符中执行：

```bash
# 创建压缩包
cd d:\xampp\htdocs\order
tar -czf canteen-backend.tar.gz src/ pom.xml sql/

# 上传到服务器
scp canteen-backend.tar.gz root@111.230.115.247:/opt/

# 删除本地压缩包
del canteen-backend.tar.gz
```

然后在服务器上：

```bash
# 创建部署目录
mkdir -p /opt/canteen

# 解压文件
cd /opt
tar -xzf canteen-backend.tar.gz -C canteen/
rm canteen-backend.tar.gz
```

### 验证文件上传

```bash
cd /opt/canteen
ls -la
```

**预期输出**：
```
total XXXXX
drwxr-xr-x   3 root root  4096 Feb  2 16:30 .
drwxr-xr-x   3 root root  4096 Feb  2 16:30 ..
-rw-r--r--   1 root root XXXXX Feb  2 16:30 pom.xml
drwxr-xr-x   3 root root  4096 Feb  2 16:30 src
drwxr-xr-x   2 root root  4096 Feb  2 16:30 sql
```

---

## 🔧 步骤2：检查运行环境

### 2.1 检查Java版本

```bash
java -version
```

**预期输出**：
```
openjdk version "17.0.18" 2026-01-20
OpenJDK Runtime Environment (build 17.0.18+9)
OpenJDK 64-Bit Server VM (build 17.0.18+9, mixed mode, sharing)
```

**如果Java未安装**：
```bash
apt-get update
apt-get install -y openjdk-17-jdk
```

### 2.2 检查Maven版本

```bash
mvn -version
```

**预期输出**：
```
Apache Maven 3.8.7
Maven home: /usr/share/maven
Java version: 17.0.18, vendor: Private Build
```

**如果Maven未安装**：
```bash
apt-get install -y maven
```

### 2.3 检查MySQL服务

```bash
systemctl status mysql
```

**预期输出**：
```
● mysql.service - MySQL Community Server
     Loaded: loaded (/lib/systemd/system/mysql.service; enabled; vendor preset: enabled)
     Active: active (running) since ...
```

**如果MySQL未运行**：
```bash
systemctl start mysql
systemctl enable mysql
```

### 2.4 检查MySQL连接

```bash
mysql -u root -p
```

输入MySQL root密码后，应该能成功连接。

---

## 🗄️ 步骤3：配置数据库

### 3.1 创建数据库

```bash
mysql -u root -p
```

输入密码后，在MySQL命令行中执行：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS canteen_ordering_system
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 创建数据库用户
CREATE USER IF NOT EXISTS 'canteen'@'%' IDENTIFIED BY 'canteen123456';

-- 授予权限
GRANT ALL PRIVILEGES ON canteen_ordering_system.* TO 'canteen'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 退出MySQL
EXIT;
```

### 3.2 导入数据库结构

```bash
cd /opt/canteen
mysql -u canteen -pcanteen123456 canteen_ordering_system < sql/init.sql
```

### 3.3 验证数据库

```bash
mysql -u canteen -pcanteen123456 canteen_ordering_system -e "SHOW TABLES;"
```

**预期输出**：
```
+--------------------------------+
| Tables_in_canteen_ordering_system |
+--------------------------------+
| user                           |
| meal_type                      |
| order                          |
| operation_log                  |
+--------------------------------+
```

---

## 🔨 步骤4：编译后端应用

### 4.1 进入项目目录

```bash
cd /opt/canteen
```

### 4.2 验证pom.xml文件

```bash
cat pom.xml | head -n 20
```

应该看到Maven配置文件内容。

### 4.3 清理旧的构建文件

```bash
mvn clean
```

### 4.4 编译项目

```bash
mvn package -DskipTests
```

**注意**：这个过程可能需要5-10分钟，请耐心等待。

**预期输出**：
```
[INFO] Scanning for projects...
[INFO] Building canteen-ordering-system 1.0.0
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX s
[INFO] Finished at: 2026-02-02T16:33:54+08:00
[INFO] ------------------------------------------------------------------------
```

### 4.5 验证编译结果

```bash
ls -lh target/*.jar
```

**预期输出**：
```
-rw-r--r-- 1 root root XXM Feb  2 16:35 canteen-ordering-system-1.0.0.jar
```

---

## ⚙️ 步骤5：配置应用

### 5.1 检查配置文件

```bash
cat src/main/resources/application.yml
```

确认以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/canteen_ordering_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8
    username: canteen
    password: canteen123456
    driver-class-name: com.mysql.cj.jdbc.Driver

server:
  port: 8080
  servlet:
    context-path: /api
```

### 5.2 创建日志目录

```bash
mkdir -p /opt/canteen/logs
```

---

## 🚀 步骤6：测试运行后端

### 6.1 启动后端（前台运行）

```bash
cd /opt/canteen
java -jar target/*.jar
```

**注意**：这个命令会在前台运行，您应该看到大量的日志输出。

**预期输出**：
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.x.x)

...
Started CanteenOrderingSystemApplication in XX.XXX seconds
```

### 6.2 测试健康检查端点

打开新的终端窗口，执行：

```bash
curl http://localhost:8080/api/health
```

**预期输出**：
```json
{"status":"UP","timestamp":1738469171234}
```

### 6.3 停止测试运行

在运行后端的终端窗口中，按 `Ctrl + C` 停止。

---

## 📦 步骤7：创建systemd服务

### 7.1 创建服务文件

```bash
cat > /etc/systemd/system/canteen-backend.service <<EOF
[Unit]
Description=Canteen Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/canteen
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/canteen_ordering_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
Environment="SPRING_DATASOURCE_USERNAME=canteen"
Environment="SPRING_DATASOURCE_PASSWORD=canteen123456"
ExecStart=/usr/bin/java -jar /opt/canteen/target/*.jar
Restart=always
RestartSec=10
StandardOutput=append:/opt/canteen/logs/backend.log
StandardError=append:/opt/canteen/logs/backend-error.log

[Install]
WantedBy=multi-user.target
EOF
```

### 7.2 重载systemd配置

```bash
systemctl daemon-reload
```

### 7.3 启动服务

```bash
systemctl start canteen-backend
```

### 7.4 设置开机自启

```bash
systemctl enable canteen-backend
```

### 7.5 检查服务状态

```bash
systemctl status canteen-backend
```

**预期输出**：
```
● canteen-backend.service - Canteen Backend Service
     Loaded: loaded (/etc/systemd/system/canteen-backend.service; enabled; vendor preset: enabled)
     Active: active (running) since ...
   Main PID: XXXXX (java)
      Tasks: XX (limit: 4915)
     Memory: XXXM
        CPU: X.X%
     CGroup: /system.slice/canteen-backend.service
             └─XXXXX /usr/bin/java -jar /opt/canteen/target/*.jar
```

---

## ✅ 步骤8：验证部署

### 8.1 测试健康检查

```bash
curl http://localhost:8080/api/health
```

**预期输出**：
```json
{"status":"UP","timestamp":1738469171234}
```

### 8.2 测试API端点

```bash
curl http://localhost:8080/api/health
```

### 8.3 查看服务日志

```bash
tail -f /opt/canteen/logs/backend.log
```

### 8.4 查看错误日志

```bash
tail -f /opt/canteen/logs/backend-error.log
```

---

## 🌐 步骤9：配置Nginx反向代理（可选）

### 9.1 创建Nginx配置

```bash
cat > /etc/nginx/sites-available/canteen-backend <<EOF
server {
    listen 80;
    server_name 111.230.115.247;

    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        proxy_buffering off;
        proxy_request_buffering off;
    }

    # 健康检查端点
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF
```

### 9.2 启用配置

```bash
ln -sf /etc/nginx/sites-available/canteen-backend /etc/nginx/sites-enabled/
```

### 9.3 测试Nginx配置

```bash
nginx -t
```

**预期输出**：
```
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

### 9.4 重载Nginx

```bash
systemctl reload nginx
```

### 9.5 测试外部访问

在本地电脑的浏览器中访问：
- http://111.230.115.247/api/health

---

## 🔧 常用管理命令

### 服务管理

```bash
# 启动服务
systemctl start canteen-backend

# 停止服务
systemctl stop canteen-backend

# 重启服务
systemctl restart canteen-backend

# 查看服务状态
systemctl status canteen-backend

# 查看服务日志
journalctl -u canteen-backend -f

# 查看应用日志
tail -f /opt/canteen/logs/backend.log

# 查看错误日志
tail -f /opt/canteen/logs/backend-error.log
```

### 数据库管理

```bash
# 连接数据库
mysql -u canteen -pcanteen123456 canteen_ordering_system

# 备份数据库
mysqldump -u canteen -pcanteen123456 canteen_ordering_system > backup.sql

# 恢复数据库
mysql -u canteen -pcanteen123456 canteen_ordering_system < backup.sql
```

---

## 🔍 故障排查

### 问题1：服务无法启动

```bash
# 查看服务状态
systemctl status canteen-backend

# 查看详细日志
journalctl -u canteen-backend -n 50

# 查看应用日志
cat /opt/canteen/logs/backend-error.log
```

### 问题2：数据库连接失败

```bash
# 测试数据库连接
mysql -u canteen -pcanteen123456 canteen_ordering_system

# 检查MySQL服务
systemctl status mysql

# 检查MySQL端口
netstat -tlnp | grep 3306
```

### 问题3：端口被占用

```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 查找占用进程
lsof -i :8080

# 结束进程
kill -9 [PID]
```

### 问题4：编译失败

```bash
# 清理构建文件
cd /opt/canteen
mvn clean

# 重新编译
mvn package -DskipTests

# 查看详细错误
mvn package -X
```

---

## 📋 部署检查清单

完成部署后，请确认以下项目：

- [ ] 文件已上传到 `/opt/canteen/` 目录
- [ ] Java 17 已安装
- [ ] Maven 已安装
- [ ] MySQL 服务正在运行
- [ ] 数据库 `canteen_ordering_system` 已创建
- [ ] 数据库用户 `canteen` 已创建并授权
- [ ] 数据库结构已导入
- [ ] 后端应用已编译成功
- [ ] systemd 服务已创建
- [ ] 服务已启动并设置为开机自启
- [ ] 健康检查端点可访问
- [ ] 服务日志正常
- [ ] Nginx 反向代理已配置（可选）

---

## 📞 获取帮助

如果遇到问题，请提供以下信息：

1. 服务器IP: 111.230.115.247
2. 操作系统版本: `cat /etc/debian_version`
3. Java版本: `java -version`
4. Maven版本: `mvn -version`
5. 服务状态: `systemctl status canteen-backend`
6. 错误日志: `tail -100 /opt/canteen/logs/backend-error.log`
7. 当前执行的步骤和命令

---

## 🎯 快速参考

### 配置信息

| 配置项 | 值 |
|--------|-----|
| 部署目录 | `/opt/canteen` |
| 数据库名 | `canteen_ordering_system` |
| 数据库用户 | `canteen` |
| 数据库密码 | `canteen123456` |
| 后端端口 | `8080` |
| API路径 | `/api` |
| 日志目录 | `/opt/canteen/logs` |

### 访问地址

| 服务 | 地址 |
|------|------|
| 后端API（本地） | http://localhost:8080/api |
| 后端API（外部） | http://111.230.115.247/api |
| 健康检查 | http://111.230.115.247/api/health |
| Swagger文档 | http://111.230.115.247/api/swagger-ui.html |

---

**按照上述步骤逐步操作，每完成一个步骤后验证结果再继续下一步！**
