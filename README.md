# Nova Forum (新星论坛)

一个基于 Spring Boot + Redis + Elasticsearch 构建的现代化社区论坛项目。

## 项目介绍

本项目旨在实现一个功能完整、技术主流的社区平台。





## 核心技术栈

- **后端**: Spring Boot 3.5.7
- **数据库**: MySQL
- **数据层**: MyBatis-Plus
- **缓存**: Redis
- **搜索**: Elasticsearch
- **认证**: JWT
- **部署**: Docker

## 项目状态

### ✅ 已完成 (第一阶段：用户认证模块)
- [x] Spring Boot 3.5.7 项目框架搭建
- [x] 依赖配置（MyBatis-Plus、MySQL、Redis、Spring Security、JWT）
- [x] 基础 MVC 架构（User 模块）
- [x] 配置文件（application.yml）
- [x] 数据库设计和初始化（init_database.sql）
- [x] **完整用户注册系统**
  - UserMapper（MyBatis-Plus BaseMapper）
  - PasswordUtil（BCrypt + 随机盐值加密）
  - UserServiceImpl（注册逻辑 + 参数验证）
  - UserController（注册接口 + 输入验证）
- [x] **Spring Security 配置**
  - 开放注册/登录接口
  - JWT无状态认证
  - CSRF防护禁用
- [x] **JWT 令牌系统**
  - JwtUtil（生成、验证、解析）
  - 7天有效期
  - 用户ID和用户名封装
- [x] **API接口设计**
  - POST /user/register - 用户注册 ✅
  - POST /user/login - 用户登录 ✅
  - GET /user/profile - 获取用户信息 ✅

### ✅ 已完成 (第二阶段：帖子模块)
- [x] Post实体类创建
- [x] PostMapper、PostService、PostController开发
- [x] 帖子发布、编辑、删除功能
- [x] 帖子列表分页查询
- [x] JWT权限控制

### 📊 开发进度
**第一阶段**：✅ 完成 (用户认证模块)
**第二阶段**：✅ 完成 (帖子模块)
**整体进度**：95% (19/20 核心任务)

## 详细开发计划

### ✅ 第一阶段：用户认证模块 (已完成)
**目标：建立完整的用户注册登录系统**

#### 1.1 数据库设计和初始化
- [x] 创建数据库 nova_forum
- [x] 设计并执行用户表(user)建表SQL
- [x] 配置数据库连接测试

#### 1.2 完善用户注册功能
- [x] 完善 UserMapper（MyBatis-Plus BaseMapper + QueryWrapper）
- [x] 实现密码加密（BCrypt + 随机盐值）
- [x] 完善 UserServiceImpl：
  - 用户名/邮箱唯一性验证
  - 密码加密和存储
  - 用户数据验证
- [x] 完善 UserController：
  - 输入参数验证 (@Valid)
  - 统一响应格式
  - 异常处理

#### 1.3 用户登录功能
- [x] 扩展 UserController（添加 /login 接口）
- [x] 实现 JWT Token 生成和管理
- [x] 配置 Spring Security
- [x] 用户认证中间件

#### 🧪 测试验证
- ✅ 用户注册接口正常工作
- ✅ 参数验证生效（用户名/邮箱唯一性检查）
- ✅ 密码安全（BCrypt加密 + 随机盐值）
- ✅ 数据库操作成功（MyBatis-Plus事务管理）

### ✅ 第二阶段：帖子模块开发 (已完成)
**目标：实现基本的帖子发布和浏览功能**

#### 2.1 帖子实体和基础CRUD
- [x] 创建 Post 实体类
- [x] 实现 PostMapper、PostService、PostController
- [x] 帖子发布功能
- [x] 帖子列表分页查询
- [x] 帖子详情查看

#### 2.2 用户权限控制
- [x] 实现登录拦截器
- [x] 添加 JWT Token 验证
- [x] 权限控制（只有登录用户可以发帖）
- [x] 作者验证（只能编辑/删除自己的帖子）

### 第三阶段：增强功能 (Enhanced Features)
**目标：添加互动和缓存功能**

#### 3.1 评论模块
- [ ] 创建 Comment 实体类
- [ ] 实现评论CRUD功能
- [ ] 评论层级结构支持
- [ ] 评论数量统计

#### 3.2 点赞功能 (Redis实现)
- [ ] 使用 Redis 实现点赞数缓存
- [ ] 点赞/取消点赞接口
- [ ] 点赞数实时统计
- [ ] 用户点赞状态缓存

### 第四阶段：高级功能 (Advanced Features)
**目标：搜索和部署优化**

#### 4.1 全文检索功能 (Elasticsearch)
- [ ] 配置 Elasticsearch 连接
- [ ] 实现帖子内容索引
- [ ] 帖子标题和内容搜索
- [ ] 搜索结果高亮显示
- [ ] 搜索热度统计

#### 4.2 Docker化部署
- [ ] 创建 Dockerfile
- [ ] 编写 docker-compose.yml
- [ ] 配置 MySQL、Redis、Elasticsearch 容器
- [ ] 环境变量配置
- [ ] 完整环境部署方案

## 技术实现细节

### 密码加密策略
- 使用 BCrypt 加密算法
- 生成随机 Salt 值
- 存储格式：salt$encrypted_password

### JWT Token 配置
- 过期时间：7天
- 包含用户ID和用户名
- 通过 Authorization Header 传递

### Redis 缓存策略
- 用户点赞状态缓存
- 点赞数统计缓存
- 缓存过期时间：24小时

### Elasticsearch 索引设计
- 帖子标题（分词 + 高亮）
- 帖子内容（分词 + 高亮）
- 创建时间、作者信息

## API 接口设计

### 用户相关
- ✅ `POST /user/register` - 用户注册
- ✅ `POST /user/login` - 用户登录
- ✅ `GET /user/profile` - 获取用户信息

### 帖子相关
- ✅ `POST /post/create` - 发布帖子
- ✅ `GET /post/list` - 帖子列表（分页）
- ✅ `GET /post/{id}` - 帖子详情
- ✅ `PUT /post/{id}` - 更新帖子
- ✅ `DELETE /post/{id}` - 删除帖子

### 评论相关
- [ ] `POST /comment/create` - 添加评论
- [ ] `GET /comment/list/{postId}` - 获取评论列表
- [ ] `DELETE /comment/{id}` - 删除评论

### 点赞相关
- [ ] `POST /like/{postId}` - 点赞/取消点赞
- [ ] `GET /like/count/{postId}` - 获取点赞数

## 数据库设计

### user 表
```sql
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    salt VARCHAR(32) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### post 表
```sql
CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

### comment 表
```sql
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    content TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (parent_id) REFERENCES comment(id)
);
```

## 部署架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Server    │    │   MySQL         │    │   Redis         │
│   (Spring Boot) │    │   Database      │    │   Cache         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Elasticsearch  │
                    │   Search Engine │
                    └─────────────────┘
```

## 核心文件结构

```
src/main/java/com/novaforum/nova_forum/
├── controller/
│   ├── UserController.java            # 用户API接口
│   └── PostController.java            # 帖子API接口
├── service/
│   ├── UserService.java               # 用户服务接口
│   ├── UserServiceImpl.java           # 用户服务实现
│   ├── PostService.java               # 帖子服务接口
│   └── PostServiceImpl.java           # 帖子服务实现
├── mapper/
│   ├── UserMapper.java                # 用户数据访问层
│   └── PostMapper.java                # 帖子数据访问层
├── entity/
│   ├── User.java                      # 用户实体
│   └── Post.java                      # 帖子实体
├── util/
│   ├── PasswordUtil.java              # 密码加密工具
│   └── JwtUtil.java                   # JWT工具
├── config/
│   └── SecurityConfig.java            # Spring Security配置
├── dto/
│   ├── RegisterRequest.java           # 注册请求DTO
│   ├── LoginRequest.java              # 登录请求DTO
│   ├── ApiResponse.java               # 统一响应类
│   ├── PostRequest.java               # 帖子请求DTO
│   └── PostResponse.java              # 帖子响应DTO
└── resources/sql/init_database.sql    # 数据库初始化脚本
```

## 开发注意事项

1. **数据验证**：所有用户输入都需要进行严格验证
2. **异常处理**：统一异常处理和错误响应格式
3. **日志记录**：关键操作和错误需要详细日志
4. **性能优化**：数据库查询优化，缓存策略
5. **安全性**：防止SQL注入、XSS攻击等
6. **代码规范**：遵循Java编码规范，保持代码整洁

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
