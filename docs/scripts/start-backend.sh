#!/bin/bash
# Nova Forum 后端启动脚本
# 启动 Spring Boot 应用
# 用法: ./start-backend.sh [--verbose]
#   --verbose  显示 Maven 详细构建日志

set -e

VERBOSE=false
if [[ "${1:-}" == "--verbose" ]]; then
    VERBOSE=true
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")/.."

echo "====================================="
echo "  Nova Forum - 后端启动脚本"
echo "====================================="

# 检查 Java 环境
if ! command -v java &> /dev/null; then
    echo "❌ 未找到 Java，请先安装 Java 17+"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1)
echo "📌 Java 版本: $JAVA_VERSION"

# 检查 Maven 环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 未找到 Maven，请先安装 Maven 3.8+"
    exit 1
fi

echo "📌 Maven 版本: $(mvn -v | head -1)"

# 检查必要的环境变量
if [ -z "$DB_PASSWORD" ]; then
    echo "⚠️  警告: 未设置 DB_PASSWORD 环境变量，将使用默认值"
fi

# 构建项目（跳过测试）
echo ""
echo "🔨 正在构建项目..."
cd "$PROJECT_DIR"
if [ "$VERBOSE" = true ]; then
    echo ""
    echo "🔨 正在构建项目（verbose 模式）..."
    mvn clean package -DskipTests
else
    echo ""
    echo "🔨 正在构建项目..."
    mvn clean package -DskipTests -q
fi

if [ $? -eq 0 ]; then
    echo "✅ 构建成功"
else
    echo "❌ 构建失败"
    exit 1
fi

# 启动应用
echo ""
echo "🚀 正在启动 Spring Boot 应用..."
echo "📍 API 地址: http://localhost:8080/api"
echo "📍 Swagger UI: http://localhost:8080/api/swagger-ui.html"
echo ""

# 检查 8080 端口是否被占用
if lsof -i :8080 &> /dev/null; then
    echo "⚠️  端口 8080 已被占用，请先关闭占用该端口的进程"
    echo "💡 提示: 可以使用 'lsof -i :8080' 查看占用进程"
    exit 1
fi

# 启动 Spring Boot
java -jar target/nova-forum-*.jar --server.port=8080
