# 食堂订餐系统 - Nginx反向代理配置指南

## 端口说明

由于服务器上已有nginx容器占用80和443端口，食堂订餐系统使用以下端口：

- **后端API**: `8080`
- **前端界面**: `8081` (HTTP) / `8443` (HTTPS)

## 配置现有Nginx反向代理

为了通过标准端口(80/443)访问食堂订餐系统，需要在现有的nginx配置中添加反向代理。

### 方案1: 使用子域名（推荐）

如果可以使用子域名，例如 `canteen.yourdomain.com`，添加以下配置：

```nginx
# HTTP配置 - 重定向到HTTPS
server {
    listen 80;
    server_name canteen.yourdomain.com;
    
    return 301 https://$server_name$request_uri;
}

# HTTPS配置
server {
    listen 443 ssl http2;
    server_name canteen.yourdomain.com;

    # SSL证书配置
    ssl_certificate /path/to/your/cert.pem;
    ssl_certificate_key /path/to/your/key.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # 前端静态文件
    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 后端API
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
}
```

### 方案2: 使用路径前缀

如果无法使用子域名，可以使用路径前缀，例如 `yourdomain.com/canteen`：

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    # 前端静态文件
    location /canteen/ {
        proxy_pass http://localhost:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 重写路径
        rewrite ^/canteen/(.*)$ /$1 break;
    }

    # 后端API
    location /canteen/api {
        proxy_pass http://localhost:8080/api;
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
}
```

### 方案3: 使用不同端口（简单直接）

直接通过端口访问：

- **前端**: `http://yourdomain.com:8081` 或 `https://yourdomain.com:8443`
- **后端API**: `http://yourdomain.com:8080`

## 配置步骤

1. **备份现有nginx配置**：
   ```bash
   cp /path/to/nginx.conf /path/to/nginx.conf.backup
   ```

2. **编辑nginx配置文件**：
   ```bash
   nano /path/to/nginx.conf
   ```

3. **添加上述配置**（根据选择的方案）

4. **测试nginx配置**：
   ```bash
   nginx -t
   ```

5. **重载nginx**：
   ```bash
   nginx -s reload
   ```

## 防火墙配置

如果使用方案3（直接端口访问），需要开放相应端口：

```bash
# 开放8080端口（后端API）
ufw allow 8080/tcp

# 开放8081端口（前端HTTP）
ufw allow 8081/tcp

# 开放8443端口（前端HTTPS）
ufw allow 8443/tcp
```

## 验证配置

配置完成后，访问以下URL验证：

- 前端: `http://yourdomain.com:8081` 或配置的URL
- 后端健康检查: `http://yourdomain.com:8080/api/health`

应该返回：
```json
{
  "status": "UP",
  "timestamp": 1234567890123
}
```

## 故障排查

### 1. 无法访问前端
检查canteen-nginx容器状态：
```bash
docker ps | grep canteen-nginx
docker logs canteen-nginx
```

### 2. 无法访问后端API
检查canteen-backend容器状态：
```bash
docker ps | grep canteen-backend
docker logs canteen-backend
```

### 3. 反向代理502错误
检查后端容器是否正常运行：
```bash
curl http://localhost:8080/api/health
```

### 4. 检查端口占用
```bash
netstat -tlnp | grep -E '80|443|8080|8081|8443'
```
