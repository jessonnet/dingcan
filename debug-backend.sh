#!/bin/bash

# 食堂订餐系统 - 调试脚本
# 用于诊断和解决容器启动问题

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "========================================="
echo "  食堂订餐系统 - 调试脚本"
echo "========================================="
echo ""

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}请使用root用户运行此脚本${NC}"
    exit 1
fi

cd /opt/canteen

# 1. 检查Docker Compose状态
echo -e "${YELLOW}[1/8] 检查Docker Compose状态...${NC}"
docker-compose ps
echo ""

# 2. 检查backend容器日志
echo -e "${YELLOW}[2/8] 检查backend容器日志（最近50行）...${NC}"
docker logs --tail 50 canteen-backend
echo ""

# 3. 检查backend容器健康状态
echo -e "${YELLOW}[3/8] 检查backend容器健康状态...${NC}"
HEALTH_STATUS=$(docker inspect --format='{{.State.Health.Status}}' canteen-backend 2>/dev/null || echo "unknown")
echo -e "健康状态: ${BLUE}$HEALTH_STATUS${NC}"
echo ""

# 4. 测试backend健康检查端点
echo -e "${YELLOW}[4/8] 测试backend健康检查端点...${NC}"
echo "从容器内部测试:"
docker exec canteen-backend wget -q -O - http://localhost:8080/api/health 2>&1 || echo -e "${RED}健康检查失败${NC}"
echo ""
echo "从宿主机测试:"
curl -s http://localhost:8080/api/health 2>&1 || echo -e "${RED}健康检查失败${NC}"
echo ""

# 5. 检查backend容器进程
echo -e "${YELLOW}[5/8] 检查backend容器进程...${NC}"
docker exec canteen-backend ps aux
echo ""

# 6. 检查backend容器端口监听
echo -e "${YELLOW}[6/8] 检查backend容器端口监听...${NC}"
docker exec canteen-backend netstat -tlnp 2>/dev/null || docker exec canteen-backend ss -tlnp 2>/dev/null || echo "无法查看端口信息"
echo ""

# 7. 检查MySQL和Redis连接
echo -e "${YELLOW}[7/8] 检查MySQL和Redis连接...${NC}"
echo "MySQL连接测试:"
docker exec canteen-backend ping -c 2 mysql 2>&1 || echo -e "${RED}MySQL连接失败${NC}"
echo ""
echo "Redis连接测试:"
docker exec canteen-backend ping -c 2 redis 2>&1 || echo -e "${RED}Redis连接失败${NC}"
echo ""

# 8. 检查容器资源使用
echo -e "${YELLOW}[8/8] 检查容器资源使用...${NC}"
docker stats --no-stream canteen-backend canteen-mysql canteen-redis
echo ""

# 诊断建议
echo "========================================="
echo -e "${YELLOW}诊断建议${NC}"
echo "========================================="
echo ""

if [ "$HEALTH_STATUS" = "unhealthy" ]; then
    echo -e "${RED}Backend容器健康检查失败${NC}"
    echo ""
    echo "可能的原因和解决方案:"
    echo ""
    echo "1. 应用启动时间过长"
    echo "   解决方案: 等待更长时间或增加健康检查的start_period"
    echo ""
    echo "2. 应用启动失败"
    echo "   解决方案: 查看上面的日志输出，检查错误信息"
    echo ""
    echo "3. 数据库连接失败"
    echo "   解决方案: 检查MySQL容器状态和连接配置"
    echo ""
    echo "4. 端口未正确监听"
    echo "   解决方案: 检查应用配置，确保监听0.0.0.0:8080"
    echo ""
    echo "5. 健康检查端点不可用"
    echo "   解决方案: 确认/api/health端点已实现"
    echo ""
    echo -e "${YELLOW}快速修复命令:${NC}"
    echo "  1. 重启backend容器: docker-compose restart backend"
    echo "  2. 查看实时日志: docker logs -f canteen-backend"
    echo "  3. 进入容器调试: docker exec -it canteen-backend sh"
    echo "  4. 重新构建容器: docker-compose up -d --build backend"
fi

echo ""
echo "========================================="
