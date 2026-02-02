#!/bin/bash

# 食堂订餐系统 - Nginx反向代理配置脚本
# 域名: dc.lwqlib.cn

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "========================================="
echo "  食堂订餐系统 Nginx配置脚本"
echo "  域名: dc.lwqlib.cn"
echo "========================================="
echo ""

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}请使用root用户运行此脚本${NC}"
    exit 1
fi

# 检查域名是否指向当前服务器
DOMAIN="dc.lwqlib.cn"
SERVER_IP=$(curl -s ifconfig.me)
DOMAIN_IP=$(dig +short $DOMAIN)

echo -e "${YELLOW}检查域名DNS解析...${NC}"
echo "域名: $DOMAIN"
echo "域名解析IP: $DOMAIN_IP"
echo "服务器IP: $SERVER_IP"

if [ "$DOMAIN_IP" != "$SERVER_IP" ]; then
    echo -e "${RED}警告: 域名解析IP与服务器IP不匹配！${NC}"
    echo "请确保域名A记录已正确指向服务器IP"
    read -p "是否继续? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo -e "${GREEN}✓ 域名解析正确${NC}"
fi

echo ""

# 检查nginx是否安装
echo -e "${YELLOW}[1/5] 检查Nginx环境...${NC}"
if ! command -v nginx &> /dev/null; then
    echo -e "${RED}Nginx未安装，正在安装...${NC}"
    apt-get update
    apt-get install -y nginx
else
    echo -e "${GREEN}✓ Nginx已安装: $(nginx -v 2>&1)${NC}"
fi
echo ""

# 检查certbot是否安装
echo -e "${YELLOW}[2/5] 检查Certbot环境...${NC}"
if ! command -v certbot &> /dev/null; then
    echo -e "${YELLOW}Certbot未安装，正在安装...${NC}"
    apt-get install -y certbot python3-certbot-nginx
else
    echo -e "${GREEN}✓ Certbot已安装: $(certbot --version)${NC}"
fi
echo ""

# 备份现有nginx配置
echo -e "${YELLOW}[3/5] 备份现有Nginx配置...${NC}"
NGINX_CONF="/etc/nginx/sites-available/canteen"
if [ -f "$NGINX_CONF" ]; then
    BACKUP_FILE="${NGINX_CONF}.backup.$(date +%Y%m%d_%H%M%S)"
    cp "$NGINX_CONF" "$BACKUP_FILE"
    echo -e "${GREEN}✓ 已备份现有配置到: $BACKUP_FILE${NC}"
else
    echo -e "${YELLOW}未找到现有配置文件${NC}"
fi
echo ""

# 复制nginx配置文件
echo -e "${YELLOW}[4/5] 部署Nginx配置文件...${NC}"
PROJECT_DIR="/opt/canteen"
CONFIG_FILE="$PROJECT_DIR/nginx/canteen-proxy.conf"

if [ ! -f "$CONFIG_FILE" ]; then
    echo -e "${RED}错误: 配置文件不存在: $CONFIG_FILE${NC}"
    exit 1
fi

cp "$CONFIG_FILE" "$NGINX_CONF"
echo -e "${GREEN}✓ 配置文件已复制到: $NGINX_CONF${NC}"

# 创建符号链接
ln -sf "$NGINX_CONF" "/etc/nginx/sites-enabled/canteen"
echo -e "${GREEN}✓ 已创建符号链接${NC}"
echo ""

# 测试nginx配置
echo -e "${YELLOW}测试Nginx配置...${NC}"
if nginx -t; then
    echo -e "${GREEN}✓ Nginx配置测试通过${NC}"
else
    echo -e "${RED}Nginx配置测试失败${NC}"
    exit 1
fi
echo ""

# 获取SSL证书
echo -e "${YELLOW}[5/5] 获取SSL证书...${NC}"
read -p "是否现在获取SSL证书? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # 先临时禁用SSL配置以获取证书
    sed -i 's/listen 443 ssl http2;/listen 443;/g' "$NGINX_CONF"
    sed -i '/ssl_certificate/d' "$NGINX_CONF"
    sed -i '/ssl_certificate_key/d' "$NGINX_CONF"

    # 重启nginx
    systemctl reload nginx

    # 获取证书
    echo -e "${YELLOW}正在获取SSL证书...${NC}"
    certbot certonly --webroot -w /var/www/html -d $DOMAIN --email admin@$DOMAIN --agree-tos --no-eff-email

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ SSL证书获取成功${NC}"

        # 恢复SSL配置
        git checkout "$CONFIG_FILE"
        cp "$CONFIG_FILE" "$NGINX_CONF"
        ln -sf "$NGINX_CONF" "/etc/nginx/sites-enabled/canteen"
    else
        echo -e "${RED}SSL证书获取失败${NC}"
        echo "请手动运行: certbot certonly --webroot -w /var/www/html -d $DOMAIN"
        exit 1
    fi
else
    echo -e "${YELLOW}跳过SSL证书获取${NC}"
    echo "请稍后手动运行: certbot certonly --webroot -w /var/www/html -d $DOMAIN"
fi
echo ""

# 重启nginx
echo -e "${YELLOW}重启Nginx...${NC}"
systemctl reload nginx
echo -e "${GREEN}✓ Nginx已重启${NC}"
echo ""

# 设置SSL证书自动续期
echo -e "${YELLOW}设置SSL证书自动续期...${NC}"
(crontab -l 2>/dev/null | grep -v "certbot renew"; echo "0 3 * * * certbot renew --quiet --post-hook 'systemctl reload nginx'") | crontab -
echo -e "${GREEN}✓ 已设置SSL证书自动续期任务${NC}"
echo ""

# 显示配置信息
echo "========================================="
echo -e "${GREEN}配置完成！${NC}"
echo "========================================="
echo ""
echo "访问地址:"
echo "  HTTP:  http://$DOMAIN"
echo "  HTTPS: https://$DOMAIN"
echo ""
echo "配置文件:"
echo "  主配置: $NGINX_CONF"
echo "  备份文件: ${NGINX_CONF}.backup.*"
echo ""
echo "SSL证书:"
echo "  证书路径: /etc/letsencrypt/live/$DOMAIN/fullchain.pem"
echo "  私钥路径: /etc/letsencrypt/live/$DOMAIN/privkey.pem"
echo ""
echo "日志文件:"
echo "  访问日志: /var/log/nginx/canteen_access.log"
echo "  错误日志: /var/log/nginx/canteen_error.log"
echo ""
echo "常用命令:"
echo "  测试配置: nginx -t"
echo "  重载配置: systemctl reload nginx"
echo "  查看状态: systemctl status nginx"
echo "  查看日志: tail -f /var/log/nginx/canteen_error.log"
echo ""
echo "========================================="
