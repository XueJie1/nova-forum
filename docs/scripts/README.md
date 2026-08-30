# Nova Forum 启动脚本

本目录包含 Nova Forum 项目的启动脚本。

## 脚本说明

| 脚本 | 说明 |
|------|------|
| `start-backend.sh` | 启动 Spring Boot 后端服务 |
| `start-frontend.sh` | 启动 Vue 前端开发服务器 |
| `start-all.sh` | 同时启动前后端服务 |

### 参数

| 参数 | 说明 |
|------|------|
| `--verbose` | 显示 Maven 详细构建日志（仅对后端和全栈脚本生效） |

## 前置条件

### 后端
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Elasticsearch 8.x（可选，用于搜索功能）

### 前端
- Node.js 18+
- npm 9+

## 环境变量

启动前请确保设置以下环境变量：

```bash
# 数据库配置
export DB_USERNAME=root
export DB_PASSWORD=your_password

# Redis 配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password

# JWT 配置
export JWT_SECRET=your_jwt_secret_key

# 邮件配置（用于邮箱验证）
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your_app_password

# Elasticsearch 配置（可选）
export ELASTICSEARCH_URIS=http://localhost:9200
export ELASTICSEARCH_USERNAME=
export ELASTICSEARCH_PASSWORD=
```

## 使用方法

### 仅启动后端
```bash
./docs/scripts/start-backend.sh
```

### 显示 Maven 详细日志启动后端
```bash
./docs/scripts/start-backend.sh --verbose
```

### 仅启动前端
```bash
./docs/scripts/start-frontend.sh
```

### 启动全栈（推荐开发时使用）
```bash
./docs/scripts/start-all.sh
```

### 显示 Maven 详细日志启动全栈
```bash
./docs/scripts/start-all.sh --verbose
```

## 访问地址

启动后，可以访问以下地址：

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/api/swagger-ui.html |

## 故障排除

### 端口被占用
```bash
# 查看占用 8080 端口的进程
lsof -i :8080

# 查看占用 5173 端口的进程
lsof -i :5173
```

### 后端构建失败
```bash
# 清理并重新构建
mvn clean package -DskipTests
```

### 前端依赖问题
```bash
# 重新安装依赖
cd frontend
rm -rf node_modules
npm install
```
