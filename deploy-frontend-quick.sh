#!/bin/bash

# 前端快速部署脚本
# 用于在Debian 12服务器上部署前端应用

set -e

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
FRONTEND_DIR="/opt/canteen/frontend"
NGINX_DIR="/var/www/canteen"
SERVER_IP="111.230.115.247"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  前端快速部署脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
  echo -e "${RED}请使用root用户运行此脚本${NC}"
  exit 1
fi

# 步骤1：检查Node.js
echo -e "${YELLOW}[步骤 1/8] 检查Node.js环境...${NC}"
if ! command -v node &> /dev/null; then
  echo -e "${RED}Node.js未安装，正在安装...${NC}"
  curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
  apt-get install -y nodejs
  echo -e "${GREEN}✓ Node.js已安装: $(node -v)${NC}"
else
  echo -e "${GREEN}✓ Node.js已安装: $(node -v)${NC}"
fi

# 步骤2：检查npm
echo -e "${YELLOW}[步骤 2/8] 检查npm环境...${NC}"
if ! command -v npm &> /dev/null; then
  echo -e "${RED}npm未安装，正在安装...${NC}"
  apt-get install -y npm
  echo -e "${GREEN}✓ npm已安装: $(npm -v)${NC}"
else
  echo -e "${GREEN}✓ npm已安装: $(npm -v)${NC}"
fi

# 步骤3：检查Nginx
echo -e "${YELLOW}[步骤 3/8] 检查Nginx环境...${NC}"
if ! command -v nginx &> /dev/null; then
  echo -e "${RED}Nginx未安装，正在安装...${NC}"
  apt-get install -y nginx
  systemctl start nginx
  systemctl enable nginx
  echo -e "${GREEN}✓ Nginx已安装并启动${NC}"
else
  echo -e "${GREEN}✓ Nginx已安装${NC}"
fi

# 步骤4：检查前端目录
echo -e "${YELLOW}[步骤 4/8] 检查前端目录...${NC}"
if [ ! -d "$FRONTEND_DIR" ]; then
  echo -e "${RED}前端目录不存在: $FRONTEND_DIR${NC}"
  echo -e "${YELLOW}请先上传前端文件到 $FRONTEND_DIR${NC}"
  exit 1
fi

# 检查必要文件
if [ ! -f "$FRONTEND_DIR/package.json" ]; then
  echo -e "${RED}package.json文件不存在${NC}"
  exit 1
fi

echo -e "${GREEN}✓ 前端目录检查通过${NC}"

# 步骤5：安装npm依赖
echo -e "${YELLOW}[步骤 5/8] 安装npm依赖...${NC}"
cd "$FRONTEND_DIR"

# 检查是否需要使用修复版的main.js
if [ -f "src/main-fixed.js" ]; then
  echo -e "${YELLOW}发现修复版main.js，正在替换...${NC}"
  cp src/main-fixed.js src/main.js
  echo -e "${GREEN}✓ 已使用修复版main.js${NC}"
fi

# 修复vite.config.js（将terser改为esbuild）
if [ -f "vite.config.js" ]; then
  echo -e "${YELLOW}检查vite.config.js配置...${NC}"
  if grep -q "minify.*terser" vite.config.js; then
    echo -e "${YELLOW}修复vite.config.js，将terser改为esbuild...${NC}"
    sed -i "s/minify: 'terser'/minify: 'esbuild'/g" vite.config.js
    echo -e "${GREEN}✓ vite.config.js已修复${NC}"
  else
    echo -e "${GREEN}✓ vite.config.js配置正确${NC}"
  fi
fi

# 清理旧的依赖
echo -e "${YELLOW}清理旧的依赖...${NC}"
rm -rf node_modules package-lock.json

# 安装依赖
echo -e "${YELLOW}正在安装依赖（这可能需要几分钟）...${NC}"
npm install

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ 依赖安装成功${NC}"
else
  echo -e "${RED}✗ 依赖安装失败${NC}"
  exit 1
fi

# 步骤6：构建前端
echo -e "${YELLOW}[步骤 6/8] 构建前端应用...${NC}"
npm run build

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ 前端构建成功${NC}"
else
  echo -e "${RED}✗ 前端构建失败${NC}"
  exit 1
fi

# 步骤7：部署到Nginx
echo -e "${YELLOW}[步骤 7/8] 部署到Nginx目录...${NC}"

# 创建Nginx目录
mkdir -p "$NGINX_DIR"

# 复制构建文件
cp -r dist/* "$NGINX_DIR/"

# 设置权限
chown -R www-data:www-data "$NGINX_DIR"
chmod -R 755 "$NGINX_DIR"

echo -e "${GREEN}✓ 文件已部署到 $NGINX_DIR${NC}"

# 步骤8：配置Nginx
echo -e "${YELLOW}[步骤 8/8] 配置Nginx...${NC}"

# 创建Nginx配置文件
cat > /etc/nginx/sites-available/canteen-frontend <<EOF
server {
    listen 80;
    server_name $SERVER_IP;

    root $NGINX_DIR;
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

# 启用配置
ln -sf /etc/nginx/sites-available/canteen-frontend /etc/nginx/sites-enabled/

# 删除默认配置
rm -f /etc/nginx/sites-enabled/default

# 测试配置
nginx -t

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ Nginx配置测试通过${NC}"
else
  echo -e "${RED}✗ Nginx配置测试失败${NC}"
  exit 1
fi

# 重载Nginx
systemctl reload nginx

echo -e "${GREEN}✓ Nginx已重载配置${NC}"

# 完成
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  部署完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}访问地址：${NC}"
echo -e "  前端主页: http://$SERVER_IP/"
echo -e "  登录页面: http://$SERVER_IP/#/login"
echo ""
echo -e "${YELLOW}默认账号：${NC}"
echo -e "  管理员: admin / admin123"
echo -e "  员工: employee1 / 123456"
echo -e "  厨师: chef1 / chef123"
echo ""
echo -e "${YELLOW}常用命令：${NC}"
echo -e "  查看Nginx状态: systemctl status nginx"
echo -e "  查看访问日志: tail -f /var/log/nginx/canteen-access.log"
echo -e "  查看错误日志: tail -f /var/log/nginx/canteen-error.log"
echo ""
