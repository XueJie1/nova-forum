#!/bin/bash
# Nova Forum 全栈启动脚本
# 同时启动后端和前端服务
# 用法: ./start-all.sh [--verbose]
#   --verbose  显示 Maven 详细构建日志

VERBOSE=false
if [[ "${1:-}" == "--verbose" ]]; then
    VERBOSE=true
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")/.."

echo "====================================="
echo "  Nova Forum - 全栈启动脚本"
echo "====================================="

# 检查 Docker 是否可用（用于启动 Elasticsearch 等依赖）
if command -v docker &> /dev/null; then
    echo "📌 Docker 环境已就绪"
else
    echo "⚠️  警告: Docker 未安装，Elasticsearch 等服务可能无法启动"
fi

# 启动后端（后台运行）
echo ""
echo "🚀 正在启动后端服务..."
echo ""

# 后台启动后端
if [ "$VERBOSE" = true ]; then
    bash "$SCRIPT_DIR/start-backend.sh" --verbose &
else
    bash "$SCRIPT_DIR/start-backend.sh" &
fi
BACKEND_PID=$!

# 等待后端启动（最多 30 秒）
echo "⏳ 等待后端服务启动..."
for i in {1..30}; do
    if lsof -i :8080 &> /dev/null; then
        echo "✅ 后端服务已启动 (PID: $BACKEND_PID)"
        break
    fi
    sleep 1
done

if ! lsof -i :8080 &> /dev/null; then
    echo "❌ 后端服务启动超时"
    exit 1
fi

# 启动前端（前台运行）
echo ""
echo "🚀 正在启动前端服务..."
echo ""

bash "$SCRIPT_DIR/start-frontend.sh"

# 清理
echo ""
echo "👋 正在停止后端服务..."
kill $BACKEND_PID 2>/dev/null || true
echo "✅ 服务已停止"
