# 前端手动部署指南

## 📋 部署前准备

### 服务器信息
- **IP地址**: 111.230.115.247
- **操作系统**: Debian 12
- **前端部署目录**: /opt/canteen/frontend
- **Nginx静态文件目录**: /var/www/canteen

### 本地文件准备
在本地电脑的 `d:\xampp\htdocs\order\` 目录下，确保有以下文件：

```
order/
├── frontend/
│   ├── src/                      # 前端源代码
│   │   ├── App.vue
│   │   ├── main.js
│   │   ├── router/
│   │   ├── store/
│   │   └── views/
│   ├── index.html                # HTML入口文件
│   ├── package.json              # npm配置文件
│   ├── package-lock.json         # npm依赖锁定文件
│   └── vite.config.js            # Vite配置文件
```

---

## 🚀 步骤1：上传前端文件到服务器

### 方法A：使用WinSCP（推荐）

1. **连接到服务器**：
   - 主机: `111.230.115.247`
   - 端口: `22`
   - 用户名: `root`
   - 密码: [您的服务器密码]

2. **创建前端目录**：
   - 在WinSCP中，导航到 `/opt/canteen/` 目录
   - 右键点击，选择"新建目录"
   - 输入目录名: `frontend`

3. **上传文件到 `/opt/canteen/frontend/` 目录**：
   - `src/` 文件夹（整个文件夹）
   - `index.html` 文件
   - `package.json` 文件
   - `package-lock.json` 文件
   - `vite.config.js` 文件

### 方法B：使用SCP命令

在本地电脑的命令提示符中执行：

```bash
# 创建压缩包
cd d:\xampp\htdocs\order
tar -czf frontend.tar.gz frontend/

# 上传到服务器
scp frontend.tar.gz root@111.230.115.247:/opt/canteen/

# 删除本地压缩包
del frontend.tar.gz
```

然后在服务器上：

```bash
# 解压文件
cd /opt/canteen
tar -xzf frontend.tar.gz
rm frontend.tar.gz
```

### 验证文件上传

```bash
cd /opt/canteen/frontend
ls -la
```

**预期输出**：
```
total XXXXX
drwxr-xr-x   3 root root  4096 Feb  2 17:00 .
drwxr-xr-x   4 root root  4096 Feb  2 17:00 ..
-rw-r--r--   1 root root XXXXX Feb  2 17:00 index.html
-rw-r--r--   1 root root XXXXX Feb  2 17:00 package.json
-rw-r--r--   1 root root XXXXX Feb  2 17:00 package-lock.json
drwxr-xr-x   3 root root  4096 Feb  2 17:00 src
-rw-r--r--   1 root root XXXXX Feb  2 17:00 vite.config.js
```

---

## 🔧 步骤2：检查运行环境

### 2.1 检查Node.js版本

```bash
node -v
```

**预期输出**：
```
v18.x.x 或 v20.x.x
```

**如果Node.js未安装**：

```bash
# 更新包管理器
apt-get update

# 安装Node.js 18.x（推荐）
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt-get install -y nodejs

# 验证安装
node -v
npm -v
```

### 2.2 检查npm版本

```bash
npm -v
```

**预期输出**：
```
9.x.x 或 10.x.x
```

### 2.3 检查Nginx服务

```bash
systemctl status nginx
```

**预期输出**：
```
● nginx.service - A high performance web server and a reverse proxy server
     Loaded: loaded (/lib/systemd/system/nginx.service; enabled; vendor preset: enabled)
     Active: active (running) since ...
```

**如果Nginx未运行**：

```bash
# 安装Nginx
apt-get install -y nginx

# 启动Nginx
systemctl start nginx

# 设置开机自启
systemctl enable nginx
```

---

## ⚙️ 步骤3：修改API配置

### 3.1 查看当前API配置

```bash
cd /opt/canteen/frontend
cat src/main.js
```

### 3.2 修改API基础URL

需要将API地址从 `localhost` 改为服务器IP地址。

**创建新的main.js文件**：

```bash
cat > src/main.js <<EOF
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
EOF
```

### 3.3 检查router配置

```bash
cat src/router/index.js
```

确保路由配置正确。

### 3.4 修改axios配置（如果存在）

查找并修改API基础URL：

```bash
# 查找包含axios的文件
grep -r "axios" src/ --include="*.js" --include="*.vue"

# 查找包含localhost或API的文件
grep -r "localhost\|baseURL\|BASE_URL" src/ --include="*.js" --include="*.vue"
```

如果找到API配置文件，需要将 `http://localhost:8080` 改为 `http://111.230.115.247:8080`

---

## 📦 步骤4：安装npm依赖

### 4.1 进入前端目录

```bash
cd /opt/canteen/frontend
```

### 4.2 清理旧的依赖（如果有）

```bash
# 删除node_modules目录（如果存在）
rm -rf node_modules

# 删除package-lock.json（如果需要重新安装）
rm -f package-lock.json
```

### 4.3 安装依赖

```bash
npm install
```

**注意**：这个过程可能需要3-5分钟，请耐心等待。

**预期输出**：
```
added XXX packages in XXs
```

### 4.4 验证依赖安装

```bash
ls -la node_modules/ | head -n 20
```

应该看到大量的依赖包目录。

---

## 🔨 步骤5：构建前端应用

### 5.1 清理旧的构建文件

```bash
npm run clean 2>/dev/null || rm -rf dist
```

### 5.2 构建生产版本

```bash
npm run build
```

**注意**：这个过程需要2-3分钟。

**预期输出**：
```
vite v4.4.5 building for production...
✓ XXX modules transformed.
dist/index.html                   0.45 kB │ gzip:  0.30 kB
dist/assets/index-XXXXXX.js       XXX kB │ gzip:  XX kB
dist/assets/index-XXXXXX.css      XX kB │ gzip:  X kB
✓ built in XX.XXs
```

### 5.3 验证构建结果

```bash
ls -lh dist/
```

**预期输出**：
```
total XXXXX
-rw-r--r-- 1 root root XXXXX Feb  2 17:10 index.html
drwxr-xr-x 2 root root 4096 Feb  2 17:10 assets
```

```bash
ls -lh dist/assets/
```

应该看到构建后的JS和CSS文件。

---

## 🌐 步骤6：配置Nginx

### 6.1 创建Nginx静态文件目录

```bash
mkdir -p /var/www/canteen
```

### 6.2 复制构建文件到Nginx目录

```bash
cp -r /opt/canteen/frontend/dist/* /var/www/canteen/
```

### 6.3 验证文件复制

```bash
ls -la /var/www/canteen/
```

**预期输出**：
```
total XXXXX
drwxr-xr-x  2 root root  4096 Feb  2 17:15 .
drwxr-xr-x  3 root root  4096 Feb  2 17:15 ..
-rw-r--r--  1 root root XXXXX Feb  2 17:15 index.html
drwxr-xr-x  2 root root  4096 Feb  2 17:15 assets
```

### 6.4 设置文件权限

```bash
chown -R www-data:www-data /var/www/canteen
chmod -R 755 /var/www/canteen
```

### 6.5 创建Nginx配置文件

```bash
cat > /etc/nginx/sites-available/canteen-frontend <<EOF
server {
    listen 80;
    server_name 111.230.115.247;

    root /var/www/canteen;
    index index.html;

    # 访问日志
    access_log /var/log/nginx/canteen-access.log;
    error_log /var/log/nginx/canteen-error.log;

    # 前端静态文件
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;

        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        # 禁用缓冲
        proxy_buffering off;
        proxy_request_buffering off;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 禁止访问隐藏文件
    location ~ /\. {
        deny all;
        access_log off;
        log_not_found off;
    }
}
EOF
```

### 6.6 启用配置

```bash
ln -sf /etc/nginx/sites-available/canteen-frontend /etc/nginx/sites-enabled/
```

### 6.7 删除默认配置（可选）

```bash
rm -f /etc/nginx/sites-enabled/default
```

### 6.8 测试Nginx配置

```bash
nginx -t
```

**预期输出**：
```
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

### 6.9 重载Nginx

```bash
systemctl reload nginx
```

---

## ✅ 步骤7：验证部署

### 7.1 检查Nginx状态

```bash
systemctl status nginx
```

**预期输出**：
```
● nginx.service - A high performance web server and a reverse proxy server
     Loaded: loaded (/lib/systemd/system/nginx.service; enabled; vendor preset: enabled)
     Active: active (running) since ...
```

### 7.2 测试前端访问

在本地电脑的浏览器中访问：

- **主页**: http://111.230.115.247/
- **登录页**: http://111.230.115.247/#/login

**预期结果**：
- 应该看到登录页面
- 页面样式正常加载
- 没有控制台错误

### 7.3 测试API连接

在浏览器中打开开发者工具（F12），查看Network标签：

1. 尝试登录
2. 查看API请求是否成功

**预期结果**：
- API请求返回200状态码
- 登录成功
- 能够正常使用系统功能

### 7.4 检查Nginx日志

```bash
# 查看访问日志
tail -f /var/log/nginx/canteen-access.log

# 查看错误日志
tail -f /var/log/nginx/canteen-error.log
```

---

## 🔧 步骤8：配置HTTPS（可选）

如果需要配置HTTPS，可以使用Let's Encrypt免费证书：

### 8.1 安装Certbot

```bash
apt-get install -y certbot python3-certbot-nginx
```

### 8.2 获取SSL证书

```bash
certbot --nginx -d dc.lwqlib.cn -d www.dc.lwqlib.cn
```

按照提示操作，Certbot会自动配置SSL。

### 8.3 自动续期

Certbot会自动设置续期任务，可以手动测试：

```bash
certbot renew --dry-run
```

---

## 📝 常用管理命令

### Nginx管理

```bash
# 启动Nginx
systemctl start nginx

# 停止Nginx
systemctl stop nginx

# 重启Nginx
systemctl restart nginx

# 重载配置（不中断服务）
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

# Nginx主日志
tail -f /var/log/nginx/error.log
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
```

如果防火墙阻止，允许HTTP流量：

```bash
ufw allow 80/tcp
ufw allow 443/tcp
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

### 问题5：Nginx配置错误

```bash
# 测试配置
nginx -t

# 查看错误日志
tail -50 /var/log/nginx/error.log

# 恢复默认配置
cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak
```

---

## 📋 部署检查清单

完成部署后，请确认以下项目：

- [ ] 前端文件已上传到 `/opt/canteen/frontend/` 目录
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
- [ ] 静态资源正常加载

---

## 🎯 快速参考

### 配置信息

| 配置项 | 值 |
|--------|-----|
| 前端源码目录 | `/opt/canteen/frontend` |
| 构建输出目录 | `/opt/canteen/frontend/dist` |
| Nginx静态文件目录 | `/var/www/canteen` |
| Nginx配置文件 | `/etc/nginx/sites-available/canteen-frontend` |
| 后端API地址 | `http://localhost:8080/api` |

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端主页 | http://111.230.115.247/ |
| 登录页面 | http://111.230.115.247/#/login |
| 后端API | http://111.230.115.247/api/health |

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 员工 | employee1 | 123456 |
| 厨师 | chef1 | chef123 |

---

## 📞 获取帮助

如果遇到问题，请提供以下信息：

1. 服务器IP: 111.230.115.247
2. Node.js版本: `node -v`
3. npm版本: `npm -v`
4. Nginx状态: `systemctl status nginx`
5. Nginx错误日志: `tail -50 /var/log/nginx/canteen-error.log`
6. 浏览器控制台错误（截图）
7. 当前执行的步骤和命令

---

## 🔄 更新部署流程

当需要更新前端代码时：

1. **上传新的前端文件**到 `/opt/canteen/frontend/`
2. **安装依赖**（如果有新的依赖）：
   ```bash
   cd /opt/canteen/frontend
   npm install
   ```
3. **构建前端**：
   ```bash
   npm run build
   ```
4. **复制到Nginx目录**：
   ```bash
   cp -r dist/* /var/www/canteen/
   ```
5. **设置权限**：
   ```bash
   chown -R www-data:www-data /var/www/canteen
   ```

---

**按照上述步骤逐步操作，每完成一个步骤后验证结果再继续下一步！**
