#!/bin/bash

# 食堂订餐系统 - 系统优化脚本
# 解决Redis内存警告和其他系统优化问题

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "========================================="
echo "  食堂订餐系统 - 系统优化脚本"
echo "========================================="
echo ""

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}请使用root用户运行此脚本${NC}"
    exit 1
fi

# 1. 修复Redis内存过度提交警告
echo -e "${YELLOW}[1/5] 修复Redis内存过度提交警告...${NC}"
if grep -q "vm.overcommit_memory" /etc/sysctl.conf; then
    echo -e "${GREEN}✓ vm.overcommit_memory已配置${NC}"
else
    echo "vm.overcommit_memory = 1" >> /etc/sysctl.conf
    sysctl vm.overcommit_memory=1
    echo -e "${GREEN}✓ 已设置vm.overcommit_memory = 1${NC}"
fi
echo ""

# 2. 优化文件描述符限制
echo -e "${YELLOW}[2/5] 优化文件描述符限制...${NC}"
if grep -q "fs.file-max" /etc/sysctl.conf; then
    echo -e "${GREEN}✓ fs.file-max已配置${NC}"
else
    echo "fs.file-max = 100000" >> /etc/sysctl.conf
    sysctl fs.file-max=100000
    echo -e "${GREEN}✓ 已设置fs.file-max = 100000${NC}"
fi

# 3. 优化TCP参数
echo -e "${YELLOW}[3/5] 优化TCP参数...${NC}"
TCP_PARAMS=(
    "net.ipv4.tcp_max_syn_backlog=8192"
    "net.core.somaxconn=4096"
    "net.ipv4.tcp_tw_reuse=1"
    "net.ipv4.ip_local_port_range=1024 65535"
)

for param in "${TCP_PARAMS[@]}"; do
    key=$(echo $param | cut -d'=' -f1)
    if grep -q "$key" /etc/sysctl.conf; then
        echo -e "${GREEN}✓ $key已配置${NC}"
    else
        echo "$param" >> /etc/sysctl.conf
        sysctl -w "$param"
        echo -e "${GREEN}✓ 已设置$key${NC}"
    fi
done
echo ""

# 4. 增加Docker资源限制
echo -e "${YELLOW}[4/5] 配置Docker资源限制...${NC}"
DOCKER_DAEMON_JSON="/etc/docker/daemon.json"
if [ ! -f "$DOCKER_DAEMON_JSON" ]; then
    echo '{}' > "$DOCKER_DAEMON_JSON"
fi

# 检查是否需要添加配置
if ! grep -q "log-driver" "$DOCKER_DAEMON_JSON"; then
    # 备份原文件
    cp "$DOCKER_DAEMON_JSON" "${DOCKER_DAEMON_JSON}.backup"

    # 添加Docker配置
    cat > "$DOCKER_DAEMON_JSON" <<EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "storage-driver": "overlay2",
  "live-restore": true
}
EOF

    echo -e "${GREEN}✓ 已配置Docker日志和存储选项${NC}"
    echo -e "${YELLOW}请重启Docker服务: systemctl restart docker${NC}"
else
    echo -e "${GREEN}✓ Docker已配置${NC}"
fi
echo ""

# 5. 优化系统交换空间
echo -e "${YELLOW}[5/5] 优化系统交换空间...${NC}"
if grep -q "vm.swappiness" /etc/sysctl.conf; then
    echo -e "${GREEN}✓ vm.swappiness已配置${NC}"
else
    echo "vm.swappiness = 10" >> /etc/sysctl.conf
    sysctl vm.swappiness=10
    echo -e "${GREEN}✓ 已设置vm.swappiness = 10${NC}"
fi
echo ""

# 应用所有sysctl配置
echo -e "${YELLOW}应用所有系统配置...${NC}"
sysctl -p
echo -e "${GREEN}✓ 系统配置已应用${NC}"
echo ""

# 显示优化摘要
echo "========================================="
echo -e "${GREEN}系统优化完成！${NC}"
echo "========================================="
echo ""
echo "已完成的优化:"
echo "  ✓ Redis内存过度提交配置"
echo "  ✓ 文件描述符限制优化"
echo "  ✓ TCP网络参数优化"
echo "  ✓ Docker日志和存储配置"
echo "  ✓ 系统交换空间优化"
echo ""
echo "配置文件:"
echo "  系统参数: /etc/sysctl.conf"
echo "  Docker配置: /etc/docker/daemon.json"
echo ""
echo "建议操作:"
echo "  1. 重启Docker服务: systemctl restart docker"
echo "  2. 重启Redis容器: docker-compose restart redis"
echo "  3. 查看系统参数: sysctl -a | grep -E 'vm.overcommit|fs.file-max|vm.swappiness'"
echo ""
echo "========================================="
