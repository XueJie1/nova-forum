# Nova Forum 新星论坛

## 项目简介

Nova Forum 是一个基于 Spring Boot 构建的现代化社区交流平台，致力于提供功能完整、技术主流的社区交流体验。项目采用当前最流行的技术栈，确保高性能、高安全性和良好的可维护性。

## 🚀 核心功能

### ✅ 已实现功能

#### 用户认证系统
- **用户注册**：集成邮箱验证，发送6位验证码，确保账户安全性
- **用户登录**：JWT无状态认证机制
- **个人资料**：用户信息管理

#### 邮箱验证系统
- **验证码发送**：向邮箱发送6位数字验证码
- **验证码验证**：验证邮箱验证码有效性
- **验证状态检查**：检查邮箱是否已完成验证
- **安全防护**：60秒发送频率限制，防止邮件轰炸

#### 帖子管理
- **发布帖子**：创建新的帖子内容
- **帖子列表**：分页查询帖子列表
- **帖子详情**：查看完整帖子信息
- **编辑帖子**：修改已发布的帖子
- **删除帖子**：删除指定帖子

#### 评论系统
- **创建评论**：对帖子发表评论
- **评论列表**：查看帖子评论列表
- **评论详情**：查看评论详细信息
- **编辑评论**：修改已发表的评论
- **删除评论**：删除指定评论

#### 点赞功能
- **点赞/取消点赞**：对帖子进行点赞操作
- **点赞数统计**：实时显示点赞数量
- **点赞状态查询**：查看用户点赞状态
- **Redis缓存优化**：高性能点赞计数

#### 全文搜索
- **帖子搜索**：基于Elasticsearch的全文搜索
- **搜索建议**：智能搜索建议功能
- **权重搜索**：标题权重高于内容权重
- **实时同步**：帖子数据自动同步到搜索引擎
- **索引管理**：支持索引的创建、重建和删除

## 🛠 技术栈

### 后端技术
- **框架**：Spring Boot 3.5.7
- **Java版本**：17
- **数据库**：MySQL 8.0+
- **ORM**：MyBatis Plus 3.5.14
- **缓存**：Redis 6.0+
- **安全**：Spring Security + JWT
- **邮件**：Spring Boot Mail

### 开发工具
- **构建工具**：Maven 3.8+
- **版本控制**：Git
- **API文档**：OpenAPI 3.0 (Swagger)

## 📋 快速开始

### 环境要求
- Java 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/XueJie1/nova-forum.git
cd nova-forum
```

2. **配置数据库**
```sql
-- 创建数据库
CREATE DATABASE nova_forum CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
source src/main/resources/sql/init_database.sql
```

3. **配置环境变量**
```bash
# 数据库配置
export DB_USERNAME=root
export DB_PASSWORD=your_password

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password

# JWT配置
export JWT_SECRET=your_jwt_secret_key

# 邮件配置（用于邮箱验证）
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

4. **启动应用**
```bash
mvn spring-boot:run
```

5. **访问应用**
- API文档：http://localhost:8080/api
- Swagger UI：http://localhost:8080/api/swagger-ui.html

## 📚 API文档

### 邮箱验证流程
1. **发送验证码**：`POST /email/send-code`
2. **验证验证码**：`POST /email/verify`
3. **注册账户**：`POST /user/register`（需要验证码）

### 主要API接口

#### 用户认证
- `POST /user/register` - 用户注册（需要邮箱验证码）
- `POST /user/login` - 用户登录
- `GET /user/profile` - 获取用户信息

#### 邮箱验证
- `POST /email/send-code` - 发送验证码
- `POST /email/verify` - 验证验证码
- `POST /email/check-verified` - 检查验证状态

#### 帖子管理
- `POST /post/create` - 发布帖子
- `GET /post/list` - 获取帖子列表
- `GET /post/{id}` - 获取帖子详情
- `PUT /post/{id}` - 更新帖子
- `DELETE /post/{id}` - 删除帖子

#### 评论系统
- `POST /comment/create` - 创建评论
- `GET /comment/list/{postId}` - 获取评论列表
- `PUT /comment/{id}` - 更新评论
- `DELETE /comment/{id}` - 删除评论

#### 点赞功能
- `POST /like/{postId}` - 点赞/取消点赞
- `GET /like/count/{postId}` - 获取点赞数
- `GET /like/status/{postId}` - 获取点赞状态

#### 全文搜索
- `GET /search/posts` - 搜索帖子
- `GET /search/suggestions` - 获取搜索建议
- `POST /search/index/create` - 创建搜索索引
- `POST /search/index/rebuild` - 重建搜索索引
- `DELETE /search/index` - 删除搜索索引

## 🔐 安全特性

- **JWT认证**：无状态Token认证机制
- **密码加密**：BCrypt哈希算法
- **邮箱验证**：强制邮箱验证防止恶意注册
- **频率限制**：验证码发送频率控制
- **权限控制**：基于角色的访问控制

## 📊 项目结构

```
nova-forum/
├── src/main/java/com/novaforum/nova_forum/
│   ├── config/          # 配置类
│   │   ├── SecurityConfig.java      # Spring Security配置
│   │   ├── RedisConfig.java         # Redis配置
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── controller/     # 控制器层
│   │   ├── UserController.java      # 用户认证接口
│   │   ├── EmailController.java     # 邮箱验证接口
│   │   ├── PostController.java      # 帖子管理接口
│   │   ├── CommentController.java   # 评论系统接口
│   │   └── LikeController.java      # 点赞功能接口
│   ├── service/        # 服务层
│   │   ├── impl/       # 服务实现
│   │   └── EmailService.java        # 邮箱服务接口
│   ├── mapper/         # 数据访问层
│   ├── entity/         # 实体类
│   ├── dto/            # 数据传输对象
│   └── util/           # 工具类
├── src/main/resources/
│   ├── mapper/         # MyBatis映射文件
│   ├── sql/            # 数据库初始化脚本
│   └── application.yml # 应用配置
└── pom.xml             # Maven配置
```

## 🧪 测试

### 邮箱验证测试
```bash
# 1. 发送验证码
curl -X POST http://localhost:8080/api/email/send-code \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'

# 2. 验证验证码
curl -X POST http://localhost:8080/api/email/verify \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "code": "123456"}'

# 3. 注册用户
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "code": "123456"
  }'
```

## 📈 性能优化

- **Redis缓存**：验证码和点赞数据缓存
- **数据库索引**：关键字段索引优化
- **连接池**：HikariCP数据库连接池
- **分页查询**：高效的分页查询实现

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
