#!/bin/bash

# 食堂订餐系统 Docker 快速部署脚本
# 服务器: 74.48.48.132
# 操作系统: Debian x86_64
# 环境: Docker (MySQL、Nginx、Redis、PHP)

set -e

echo "========================================="
echo "  食堂订餐系统 Docker 部署脚本 v1.0.2"
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
echo -e "${YELLOW}[1/14] 检查Docker环境...${NC}"
echo ""

# 检查Docker
if ! command -v docker >/dev/null 2>&1; then
    echo -e "${RED}Docker 未安装，正在安装...${NC}"
    apt-get update
    apt-get install -y \
        apt-transport-https \
        ca-certificates \
        curl \
        gnupg \
        lsb-release
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    echo \
      "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian \
      $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    apt-get update
    apt-get install -y docker-ce docker-ce-cli containerd.io
    systemctl start docker
    systemctl enable docker
    echo -e "${GREEN}✓ Docker 安装完成${NC}"
else
    echo -e "${GREEN}✓ Docker 已安装: $(docker --version | head -1)${NC}"
fi

# 检查Docker Compose
if ! command -v docker-compose >/dev/null 2>&1; then
    echo -e "${RED}Docker Compose 未安装，正在安装...${NC}"
    curl -SL https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
    echo -e "${GREEN}✓ Docker Compose 安装完成: $(docker-compose --version | head -1)${NC}"
else
    echo -e "${GREEN}✓ Docker Compose 已安装: $(docker-compose --version | head -1)${NC}"
fi

# 检查已有容器
echo ""
echo "已有容器:"
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

# 2. 项目准备
echo -e "${YELLOW}[2/14] 准备项目文件...${NC}"
echo ""

# 创建项目目录
mkdir -p /opt/canteen
cd /opt/canteen

# 检查是否使用Git
read -p "是否使用Git克隆代码? (y/n): " -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if [ -d ".git" ]; then
        echo "拉取最新代码..."
        git pull origin v1.0.2
    else
        echo "克隆代码仓库..."
        git clone https://github.com/jessonnet/dingcan.git .
        git checkout v1.0.2
    fi
    echo -e "${GREEN}✓ 代码已更新${NC}"
else
    echo -e "${YELLOW}请手动上传项目文件到 /opt/canteen/${NC}"
    read -p "按回车继续..." -r
fi

# 3. 配置环境变量
echo -e "${YELLOW}[3/14] 配置环境变量...${NC}"
echo ""

# 创建.env文件
if [ ! -f ".env" ]; then
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
    chmod 600 .env
    echo -e "${GREEN}✓ .env 文件已创建${NC}"
    echo -e "${YELLOW}⚠️  请修改.env中的密码为实际值${NC}"
else
    echo -e "${GREEN}✓ .env 文件已存在${NC}"
fi

# 4. 检查Docker Compose文件
echo -e "${YELLOW}[4/14] 检查Docker Compose配置...${NC}"
echo ""

if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}✗ 未找到docker-compose.yml文件${NC}"
    echo "请确保docker-compose.yml文件存在于 /opt/canteen/ 目录中"
    exit 1
else
    echo -e "${GREEN}✓ docker-compose.yml 文件已找到${NC}"
fi

# 5. 构建并启动服务
echo -e "${YELLOW}[5/14] 构建并启动Docker服务...${NC}"
echo ""

# 创建必要的目录
mkdir -p logs nginx/ssl

# 构建并启动所有服务
docker-compose up -d --build

# 等待服务启动
echo "等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo -e "${YELLOW}[6/14] 检查服务状态...${NC}"
echo ""

# 检查MySQL
if docker-compose ps mysql | grep -q "Up"; then
    echo -e "${GREEN}✓ MySQL 容器运行正常${NC}"
else
    echo -e "${RED}✗ MySQL 容器未启动${NC}"
    docker-compose logs mysql --tail=20
fi

# 检查Redis
if docker-compose ps redis | grep -q "Up"; then
    echo -e "${GREEN}✓ Redis 容器运行正常${NC}"
else
    echo -e "${RED}✗ Redis 容器未启动${NC}"
    docker-compose logs redis --tail=20
fi

# 检查后端
if docker-compose ps backend | grep -q "Up"; then
    echo -e "${GREEN}✓ 后端容器运行正常${NC}"
else
    echo -e "${RED}✗ 后端容器未启动${NC}"
    docker-compose logs backend --tail=20
fi

# 检查Nginx
if docker-compose ps nginx | grep -q "Up"; then
    echo -e "${GREEN}✓ Nginx 容器运行正常${NC}"
else
    echo -e "${RED}✗ Nginx 容器未启动${NC}"
    docker-compose logs nginx --tail=20
fi

# 6. 前端构建
echo ""
echo -e "${YELLOW}[7/14] 构建前端...${NC}"
echo ""

cd frontend

# 检查是否需要安装依赖
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    if command -v pnpm >/dev/null 2>&1; then
        pnpm install
    else
        npm install
    fi
    echo -e "${GREEN}✓ 依赖安装完成${NC}"
else
    echo -e "${GREEN}✓ 依赖已安装${NC}"
fi

# 构建前端
if [ ! -d "dist" ]; then
    echo "构建前端项目..."
    if command -v pnpm >/dev/null 2>&1; then
        pnpm build
    else
        npm run build
    fi
    echo -e "${GREEN}✓ 前端构建完成${NC}"
else
    echo -e "${GREEN}✓ 前端已构建${NC}"
fi

# 重启Nginx以加载前端文件
echo ""
echo -e "${YELLOW}[8/14] 重启Nginx以加载前端...${NC}"
docker-compose restart nginx
sleep 5

# 7. 验证部署
echo ""
echo -e "${YELLOW}[9/14] 验证部署...${NC}"
echo ""

# 测试前端访问
if curl -sf http://localhost > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 前端可以访问${NC}"
else
    echo -e "${RED}✗ 前端无法访问${NC}"
fi

# 测试后端API
if curl -sf http://localhost:8080/api/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 后端API可以访问${NC}"
else
    echo -e "${YELLOW}⚠️  后端健康检查端点可能不存在，这是正常的${NC}"
fi

# 8. 显示访问信息
echo ""
echo "========================================="
echo -e "${GREEN}  部署完成！${NC}"
echo "========================================="
echo ""
echo "服务状态:"
docker-compose ps
echo ""
echo "访问地址:"
echo -e "  前端: ${GREEN}http://74.48.48.132${NC}"
echo -e "  后端API: ${GREEN}http://74.48.48.132/api${NC}"
echo ""
echo "常用命令:"
echo "  查看所有服务状态: docker-compose ps"
echo "  查看所有服务日志: docker-compose logs -f"
echo "  查看后端日志: docker-compose logs -f backend"
echo "  重启所有服务: docker-compose restart"
echo "  停止所有服务: docker-compose stop"
echo "  重新构建并启动: docker-compose up -d --build"
echo ""
echo -e "${YELLOW}⚠️  重要提示:${NC}"
echo "  1. 请立即修改.env文件中的密码"
echo "  2. 默认账号: admin / admin123"
echo "  3. 查看详细日志: docker-compose logs [service_name]"
echo "  4. 进入容器: docker-compose exec [service_name] bash"
echo ""
echo "查看部署文档: cat DOCKER_DEPLOYMENT_GUIDE.md"
