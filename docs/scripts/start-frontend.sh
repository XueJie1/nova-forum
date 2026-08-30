#!/bin/bash
# Nova Forum 前端启动脚本
# 启动 Vue 3 + Vite 开发服务器

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")/.."
FRONTEND_DIR="$PROJECT_DIR/frontend"

echo "====================================="
echo "  Nova Forum - 前端启动脚本"
echo "====================================="

# 检查 Node.js 环境
if ! command -v node &> /dev/null; then
    echo "❌ 未找到 Node.js，请先安装 Node.js 18+"
    exit 1
fi

NODE_VERSION=$(node -v)
echo "📌 Node.js 版本: $NODE_VERSION"

# 检查 npm 环境
if ! command -v npm &> /dev/null; then
    echo "❌ 未找到 npm，请先安装 npm"
    exit 1
fi

NPM_VERSION=$(npm -v)
echo "📌 npm 版本: $NPM_VERSION"

# 切换到前端目录
cd "$FRONTEND_DIR"

# 检查 node_modules 是否存在
if [ ! -d "node_modules" ]; then
    echo ""
    echo "📦 首次运行，正在安装依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败"
        exit 1
    fi
    echo "✅ 依赖安装完成"
fi

# 检查后端是否运行
BACKEND_PORT=8080
if ! lsof -i :$BACKEND_PORT &> /dev/null; then
    echo ""
    echo "⚠️  警告: 后端服务 (localhost:$BACKEND_PORT) 未运行"
    echo "💡 请先运行: ./docs/scripts/start-backend.sh"
    echo ""
    read -p "是否继续启动前端? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 已取消"
        exit 0
    fi
fi

# 启动开发服务器
echo ""
echo "🚀 正在启动前端开发服务器..."
echo ""
echo "📍 访问地址: http://localhost:5173"
echo "📍 API 地址: http://localhost:8080/api"
echo ""
echo "按 Ctrl+C 停止服务"
echo "====================================="

npm run dev
