#!/bin/bash

# 前端构建修复脚本
# 用于修复terser依赖问题

set -e

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

FRONTEND_DIR="/opt/canteen/frontend"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  前端构建修复脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查目录
if [ ! -d "$FRONTEND_DIR" ]; then
  echo -e "${RED}前端目录不存在: $FRONTEND_DIR${NC}"
  exit 1
fi

cd "$FRONTEND_DIR"

# 步骤1：修复vite.config.js
echo -e "${YELLOW}[步骤 1/3] 修复vite.config.js...${NC}"
if [ -f "vite.config.js" ]; then
  if grep -q "minify.*terser" vite.config.js; then
    echo -e "${YELLOW}发现terser配置，正在修复...${NC}"
    sed -i "s/minify: 'terser'/minify: 'esbuild'/g" vite.config.js
    echo -e "${GREEN}✓ vite.config.js已修复${NC}"
  else
    echo -e "${GREEN}✓ vite.config.js配置正确${NC}"
  fi
else
  echo -e "${RED}vite.config.js文件不存在${NC}"
  exit 1
fi

# 步骤2：清理旧的依赖
echo -e "${YELLOW}[步骤 2/3] 清理旧的依赖...${NC}"
rm -rf node_modules package-lock.json
echo -e "${GREEN}✓ 旧的依赖已清理${NC}"

# 步骤3：重新构建
echo -e "${YELLOW}[步骤 3/3] 重新构建前端...${NC}"
npm install
npm run build

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ 前端构建成功${NC}"
else
  echo -e "${RED}✗ 前端构建失败${NC}"
  exit 1
fi

# 完成
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  修复完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}构建文件位置：${NC}"
echo -e "  $FRONTEND_DIR/dist"
echo ""
echo -e "${YELLOW}下一步：${NC}"
echo -e "  1. 创建Nginx目录："
echo -e "     mkdir -p /var/www/canteen"
echo -e "  2. 复制构建文件到Nginx目录："
echo -e "     cp -r $FRONTEND_DIR/dist/* /var/www/canteen/"
echo -e "  3. 设置权限："
echo -e "     chown -R www-data:www-data /var/www/canteen"
echo -e "  4. 重载Nginx："
echo -e "     systemctl reload nginx"
echo ""
