# Nova Forum 技术背景

## 技术栈概览

Nova Forum 采用现代化的 Java 技术栈，确保系统的高性能、高可用性和良好的可维护性。以下是详细的技术选型和配置信息。

## 核心技术栈

### 1. 后端框架
- **Spring Boot**: 3.5.7 (最新稳定版本)
  - 提供自动配置、起步依赖、内置容器等特性
  - 支持微服务架构和分布式部署
  - 内置 Actuator 监控和管理功能
  - 支持多种配置方式（properties、yaml、环境变量）

- **Java版本**: 17
  - 使用最新的语言特性（记录类、模式匹配、密封类等）
  - 更好的垃圾回收器（G1GC、ZGC）
  - 改进的并发编程支持
  - 增强的安全性和性能

### 2. 数据存储层
- **主数据库**: MySQL 8.0+
  - 关系型数据库，支持事务完整性
  - 优秀的读写性能和并发处理能力
  - 完善的备份和恢复机制
  - 丰富的索引类型和查询优化

- **ORM框架**: MyBatis Plus 3.5.14
  - 增强的 MyBatis，简化 CRUD 操作
  - 强大的代码生成器
  - 内置分页插件和乐观锁
  - 支持 Lambda 表达式查询
  - 动态 SQL 支持

### 3. 缓存层
- **Redis**: 6.0+
  - 高性能键值对存储
  - 支持多种数据结构（字符串、哈希、列表、集合、有序集合）
  - 持久化支持（RDB、AOF）
  - 集群和高可用支持
  - 丰富的功能（发布订阅、事务、Lua脚本）

### 4. 搜索引擎
- **Elasticsearch**: 8.0+
  - 分布式全文搜索引擎
  - 实时搜索和分析
  - 支持复杂的查询 DSL
  - 聚合分析和可视化
  - 水平扩展能力

### 5. 安全框架
- **Spring Security**: 6.x
  - 全面的安全认证和授权框架
  - 支持多种认证方式（表单、OAuth、JWT）
  - 细粒度的权限控制
  - CSRF 防护和会话管理
  - 与 Spring Boot 完美集成

- **JWT**: 0.11.5
  - 无状态认证机制
  - 自包含的令牌信息
  - 支持分布式部署
  - 良好的性能和安全性

### 6. 开发工具和依赖
- **构建工具**: Maven 3.8+
  - 依赖管理和项目构建
  - 插件生态丰富
  - 支持多模块项目
  - 持续集成友好

- **开发语言**: Java 17
  - 强类型、面向对象编程
  - 丰富的生态系统和库
  - 优秀的性能和并发支持
  - 长期支持和维护

## 项目配置详情

### 1. Maven 依赖管理

#### 核心依赖
```xml
<!-- Spring Boot Starter Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Spring Data Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- MyBatis Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.14</version>
</dependency>

<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- JWT Dependencies -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

#### 测试依赖
```xml
<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Security Test -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- MyBatis Plus Test -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter-test</artifactId>
    <version>3.0.5</version>
    <scope>test</scope>
</dependency>
```

### 2. 数据库设计

#### 用户表 (user)
```sql
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码哈希',
    salt VARCHAR(32) NOT NULL COMMENT '密码盐值',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

#### 帖子表 (post)
```sql
CREATE TABLE post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '作者ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';
```

#### 评论表 (规划中)
```sql
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '作者ID',
    parent_id BIGINT NULL COMMENT '父评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (post_id) REFERENCES post(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (parent_id) REFERENCES comment(id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
```

### 3. 应用配置

#### application.yml 基本配置
```yaml
spring:
  application:
    name: nova-forum
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/nova_forum?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
  
  data:
    elasticsearch:
      uri: ${ELASTICSEARCH_URI:http://localhost:9200}
      username: ${ELASTICSEARCH_USERNAME:}
      password: ${ELASTICSEARCH_PASSWORD:}

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    call-setters-on-nulls: true
    jdbc-type-for-null: 'null'
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
    banner: false
  mapper-locations: classpath*:/mapper/**/*.xml

logging:
  level:
    com.novaforum.nova_forum: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# JWT 配置
jwt:
  secret: ${JWT_SECRET:mySecretKey123456789012345678901234567890}
  expiration: 604800 # 7天，单位：秒

# 应用配置
app:
  name: Nova Forum
  version: 1.0.0
  description: 现代化社区论坛平台
```

## 开发环境设置

### 1. 开发工具推荐
- **IDE**: IntelliJ IDEA (最新版本)
- **数据库管理**: MySQL Workbench、Navicat
- **Redis客户端**: Redis Desktop Manager
- **API测试**: Postman、Insomnia
- **版本控制**: Git + GitHub/GitLab

### 2. 环境要求
- **Java**: JDK 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Elasticsearch**: 8.0+ (可选)

### 3. 本地开发启动
```bash
# 克隆项目
git clone https://github.com/XueJie1/nova-forum.git

# 进入项目目录
cd nova-forum

# 启动MySQL和Redis服务
# 或者使用docker-compose启动
docker-compose up -d

# 初始化数据库
mysql -u root -p < src/main/resources/sql/init_database.sql

# 运行应用
mvn spring-boot:run
```

## 生产环境部署

### 1. Docker 部署 (规划中)
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY target/nova-forum-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
```

### 2. Docker Compose 配置 (规划中)
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: nova_forum
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:6.0-alpine

  elasticsearch:
    image: elasticsearch:8.0.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    volumes:
      - es_data:/usr/share/elasticsearch/data

volumes:
  mysql_data:
  es_data:
```

## 性能优化策略

### 1. 数据库优化
- **连接池配置**: HikariCP 优化参数调优
- **索引策略**: 合理的单列索引和复合索引
- **查询优化**: 使用 Explain 分析查询执行计划
- **分页优化**: 使用游标分页或子查询分页

### 2. 缓存策略
- **多级缓存**: 本地缓存 + Redis 分布式缓存
- **缓存预热**: 应用启动时预加载热点数据
- **缓存更新**: 写穿模式和写回模式结合
- **缓存穿透防护**: 布隆过滤器或空值缓存

### 3. 应用优化
- **异步处理**: 使用 @Async 异步处理非关键业务
- **批处理**: 批量数据库操作减少网络开销
- **连接复用**: HTTP/2 和数据库连接复用
- **资源压缩**: Gzip 压缩和静态资源优化

## 安全考虑

### 1. 认证安全
- **JWT安全**: 密钥强度、过期时间控制
- **密码安全**: BCrypt 加盐哈希、强度验证
- **会话管理**: 无状态认证、令牌刷新机制

### 2. 数据安全
- **输入验证**: 严格的参数验证和过滤
- **SQL注入防护**: 预编译语句和参数绑定
- **XSS防护**: 输出转义和内容安全策略
- **CSRF防护**: 令牌验证和 SameSite 属性

### 3. 传输安全
- **HTTPS**: 全站 HTTPS 加密传输
- **安全头**: HSTS、CSP、X-Frame-Options 等
- **敏感信息**: 密码、Token 等不记录日志

## 监控和运维

### 1. 应用监控 (规划中)
- **性能监控**: 响应时间、吞吐量、错误率
- **资源监控**: CPU、内存、磁盘、网络使用率
- **业务监控**: 用户活跃度、内容质量指标
- **告警机制**: 异常阈值告警和自动通知

### 2. 日志管理 (规划中)
- **结构化日志**: JSON 格式日志输出
- **日志聚合**: ELK Stack 或类似方案
- **日志轮转**: 按大小和时间自动轮转
- **敏感信息过滤**: 自动过滤敏感数据

### 3. 错误处理
- **全局异常**: @ControllerAdvice 统一异常处理
- **错误码体系**: 统一的错误码和错误信息
- **降级策略**: 关键服务的熔断和降级
- **重试机制**: 网络异常和临时故障重试

*Nova Forum 的技术架构注重性能、安全性和可维护性，为用户提供稳定可靠的服务体验。*
