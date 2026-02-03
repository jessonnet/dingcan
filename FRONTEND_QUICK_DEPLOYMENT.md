# 前端快速部署指南（简化版）

## 🚀 快速开始

### 步骤1：上传文件到服务器

**使用WinSCP上传以下文件到 `/opt/canteen/frontend/` 目录**：

1. 下载WinSCP: https://winscp.net/
2. 连接到服务器：`111.230.115.247`
3. 创建目录：`/opt/canteen/frontend`
4. 上传文件：
   - `frontend/src/` 文件夹（整个文件夹）
   - `frontend/index.html` 文件
   - `frontend/package.json` 文件
   - `frontend/package-lock.json` 文件
   - `frontend/vite.config.js` 文件
   - `frontend/src/main-fixed.js` 文件（修复版）

### 步骤2：修改API配置（重要！）

在服务器上执行：

```bash
cd /opt/canteen/frontend
cp src/main-fixed.js src/main.js
```

**或者手动修改** `src/main.js` 文件：

将第7行：
```javascript
axios.defaults.baseURL = 'http://localhost:8080'
```

改为：
```javascript
axios.defaults.baseURL = 'http://111.230.115.247:8080'
```

### 步骤3：安装Node.js（如果未安装）

```bash
# 检查Node.js版本
node -v

# 如果未安装，执行以下命令
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt-get install -y nodejs

# 验证安装
node -v
npm -v
```

### 步骤4：安装依赖并构建

```bash
cd /opt/canteen/frontend

# 清理旧的依赖
rm -rf node_modules package-lock.json

# 安装依赖（需要3-5分钟）
npm install

# 构建前端（需要2-3分钟）
npm run build
```

### 步骤5：部署到Nginx

```bash
# 创建Nginx目录
mkdir -p /var/www/canteen

# 复制构建文件
cp -r /opt/canteen/frontend/dist/* /var/www/canteen/

# 设置权限
chown -R www-data:www-data /var/www/canteen
chmod -R 755 /var/www/canteen
```

### 步骤6：配置Nginx

```bash
# 创建Nginx配置文件
cat > /etc/nginx/sites-available/canteen-frontend <<'EOF'
server {
    listen 80;
    server_name 111.230.115.247;

    root /var/www/canteen;
    index index.html;

    # 前端静态文件
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        proxy_buffering off;
        proxy_request_buffering off;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# 启用配置
ln -sf /etc/nginx/sites-available/canteen-frontend /etc/nginx/sites-enabled/

# 删除默认配置
rm -f /etc/nginx/sites-enabled/default

# 测试配置
nginx -t

# 重载Nginx
systemctl reload nginx
```

### 步骤7：验证部署

```bash
# 检查Nginx状态
systemctl status nginx

# 查看访问日志
tail -f /var/log/nginx/canteen-access.log

# 查看错误日志
tail -f /var/log/nginx/canteen-error.log
```

在浏览器中访问：
- **主页**: http://111.230.115.247/
- **登录页**: http://111.230.115.247/#/login

---

## 📦 使用自动部署脚本（推荐）

如果您上传了 `deploy-frontend-quick.sh` 脚本，可以一键部署：

```bash
# 上传脚本到服务器
chmod +x deploy-frontend-quick.sh

# 执行脚本
./deploy-frontend-quick.sh
```

脚本会自动完成所有部署步骤！

---

## 🔧 常用命令

### Nginx管理

```bash
# 启动Nginx
systemctl start nginx

# 停止Nginx
systemctl stop nginx

# 重启Nginx
systemctl restart nginx

# 重载配置
systemctl reload nginx

# 查看状态
systemctl status nginx

# 测试配置
nginx -t
```

### 前端重新部署

```bash
# 进入前端目录
cd /opt/canteen/frontend

# 安装依赖（如果需要）
npm install

# 构建前端
npm run build

# 复制到Nginx目录
cp -r dist/* /var/www/canteen/

# 设置权限
chown -R www-data:www-data /var/www/canteen
```

### 查看日志

```bash
# Nginx访问日志
tail -f /var/log/nginx/canteen-access.log

# Nginx错误日志
tail -f /var/log/nginx/canteen-error.log
```

---

## 🔍 故障排查

### 问题1：页面无法访问

```bash
# 检查Nginx状态
systemctl status nginx

# 检查端口占用
netstat -tlnp | grep :80

# 检查防火墙
ufw status

# 允许HTTP流量
ufw allow 80/tcp
```

### 问题2：API请求失败

```bash
# 检查后端服务
systemctl status canteen-backend

# 检查后端日志
tail -f /opt/canteen/logs/backend.log

# 测试API连接
curl http://localhost:8080/api/health
```

### 问题3：静态资源404

```bash
# 检查文件是否存在
ls -la /var/www/canteen/

# 检查文件权限
ls -la /var/www/canteen/assets/

# 修复权限
chown -R www-data:www-data /var/www/canteen
chmod -R 755 /var/www/canteen
```

### 问题4：构建失败

```bash
# 清理依赖
rm -rf node_modules package-lock.json

# 重新安装
npm install

# 查看详细错误
npm run build --verbose
```

---

## 📋 部署检查清单

- [ ] 前端文件已上传到 `/opt/canteen/frontend/` 目录
- [ ] API配置已修改为服务器IP地址
- [ ] Node.js 已安装（v18.x或更高）
- [ ] npm 已安装
- [ ] Nginx 已安装并运行
- [ ] npm依赖已安装
- [ ] 前端应用已构建成功
- [ ] 构建文件已复制到 `/var/www/canteen/`
- [ ] Nginx配置已创建并启用
- [ ] Nginx配置测试通过
- [ ] Nginx已重载配置
- [ ] 前端页面可访问
- [ ] API请求正常工作

---

## 🎯 配置信息

| 配置项 | 值 |
|--------|-----|
| 前端源码目录 | `/opt/canteen/frontend` |
| 构建输出目录 | `/opt/canteen/frontend/dist` |
| Nginx静态文件目录 | `/var/www/canteen` |
| Nginx配置文件 | `/etc/nginx/sites-available/canteen-frontend` |
| 后端API地址 | `http://111.230.115.247:8080` |

## 📝 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 员工 | employee1 | 123456 |
| 厨师 | chef1 | chef123 |

---

## 🔄 更新部署流程

当需要更新前端代码时：

1. **上传新的前端文件**到 `/opt/canteen/frontend/`
2. **修改API配置**（如果需要）：
   ```bash
   cd /opt/canteen/frontend
   cp src/main-fixed.js src/main.js
   ```
3. **安装依赖**（如果有新的依赖）：
   ```bash
   npm install
   ```
4. **构建前端**：
   ```bash
   npm run build
   ```
5. **复制到Nginx目录**：
   ```bash
   cp -r dist/* /var/www/canteen/
   ```
6. **设置权限**：
   ```bash
   chown -R www-data:www-data /var/www/canteen
   ```

---

**按照上述步骤逐步操作，每完成一个步骤后验证结果再继续下一步！**

如有问题，请查看详细的部署文档：[FRONTEND_MANUAL_DEPLOYMENT.md](file:///d:\xampp\htdocs\order\FRONTEND_MANUAL_DEPLOYMENT.md)
