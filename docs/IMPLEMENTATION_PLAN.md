# Nova Forum 详细实现计划

## 1. 项目目标

Nova Forum 计划建设为一个基于 Spring Boot 3.5 和 Java 17 的高响应社区交流平台，实现完整的用户认证、内容发布、互动社交和全文检索功能。

目标技术栈：

- Spring Boot 3.5.x
- Java 17
- Spring MVC
- Spring Security + JWT
- MySQL + HikariCP
- MyBatis Plus
- Redis
- Elasticsearch
- OpenAPI 3.0
- Maven

### 关于“响应式”的技术边界

本项目中的“响应式”指响应迅速、适合高并发访问的社区后端，而不是端到端的 Reactive Programming。

MyBatis Plus、JDBC 和 HikariCP 均采用阻塞式数据库访问模型，因此项目继续使用 Spring MVC。如果未来明确要求使用 Spring WebFlux，则需要将数据访问层迁移至 R2DBC，同时重新评估 MyBatis Plus 和 HikariCP，不能只把 Web 层替换为 WebFlux。

## 2. 当前状态

本计划以远程 `main` 的 `3fa7da1` 提交为当前实现基线。仓库已经从最初的项目骨架发展为具备主要业务模块的后端原型：

- 已实现用户注册、登录、个人资料查询、BCrypt 密码哈希和无状态 JWT 请求认证。
- 已实现基于 Redis 的邮箱验证码、5 分钟有效期和原子 60 秒发送限流。
- 已实现帖子与评论的基础 CRUD 和分页查询。
- 已实现 Redis Set 点赞缓存、点赞计数以及手动批量同步 MySQL。
- 已实现 Elasticsearch 帖子索引、全文搜索、搜索建议和基础权重排序。
- 已将 Elasticsearch 索引管理接口限制为 `ADMIN`；现有 JWT 尚未携带角色权限，因此当前按默认拒绝策略关闭这些接口。
- 已提供 OpenAPI YAML、API 使用说明，以及搜索与索引同步相关单元测试。
- 已配置 MyBatis Plus、MySQL 和 HikariCP。

与本文档的目标架构相比，仍有以下主要差距：

- JWT 目前只有单一长效 Access Token，尚无短期 Access Token、Refresh Token Rotation、退出撤销和密码重置闭环。
- 邮箱验证码仍以明文写入 Redis；尚缺少 IP/每日限流、失败次数限制、验证码摘要存储和场景隔离。
- 关注、粉丝、标签、关注 Feed、管理员权限等社交和治理能力尚未实现。
- 点赞流程尚未使用 Lua 保证原子性，也没有 Redis Stream 异步事件、失败重试和可靠的缓存重建流程。
- 帖子数据直接同步 Elasticsearch，尚无 Outbox、版本化索引别名和可恢复的最终一致性机制。
- 数据库使用手工初始化 SQL，尚未引入 Flyway；普通 MyBatis Starter 与 MyBatis Plus Starter 仍重复存在。
- OpenAPI 当前以静态 YAML 为主，尚未集成 Springdoc 自动生成和可执行 Swagger UI。
- 测试主要覆盖搜索相关代码，认证、帖子、评论、点赞和并发场景仍缺少系统化测试。
- Maven Wrapper 仍缺少 `.mvn/wrapper` 文件；Docker Compose、Actuator 指标和 GitHub Actions CI 尚未提供。
- 配置与日志仍需进行敏感信息审计，并轮换任何历史上已经提交或输出过的真实凭据。

## 3. 目标架构

```text
客户端
  │
  ▼
Spring Security Filter Chain
JWT 校验、权限控制、访问限制
  │
  ▼
REST Controller
  │
  ▼
Application Service
  ├── MyBatis Plus ── MySQL
  ├── Redis ───────── 验证码、限流、点赞、热点缓存
  └── Outbox Worker ─ Elasticsearch
                         │
                         └── 全文检索、搜索建议、权重排序

OpenAPI / Swagger UI 提供交互式接口文档
Actuator / Micrometer 提供健康检查与运行指标
```

MySQL 是用户、帖子、评论、关注和点赞关系的最终事实数据源；Redis 承担高频、短生命周期和可恢复的数据；Elasticsearch 是可重建的检索索引，不作为业务主数据库。

## 4. 里程碑总览

下表中的耗时是从初始骨架完整建设的原始估算，总计约 7 至 9 周。当前仓库已经覆盖多个里程碑的基础能力，但所有里程碑仍有未达到的验收项；“部分完成”不代表已经满足生产交付标准。

| 里程碑 | 主题 | 原始估算 | 基线状态 | 主要结果 |
|---|---|---:|---|---|
| M0 | 工程基础与本地环境 | 2–3 天 | 部分完成 | 可重复启动、构建和配置 |
| M1 | MySQL 数据模型与数据层 | 4–5 天 | 部分完成 | 完整表结构、迁移和 CRUD 基础 |
| M2 | Security、JWT 与邮箱验证 | 5–7 天 | 部分完成 | 完整注册登录闭环 |
| M3 | 用户资料与关注社交 | 3–4 天 | 部分完成 | 用户主页、关注关系 |
| M4 | 帖子、标签与评论 | 6–8 天 | 基础功能完成 | 可用社区 MVP |
| M5 | Redis 高性能点赞 | 4–6 天 | 部分完成 | 实时点赞与异步持久化 |
| M6 | Elasticsearch 智能搜索 | 5–7 天 | 部分完成 | 全文检索、建议和权重排序 |
| M7 | OpenAPI、连接池与可观测性 | 4–5 天 | 部分完成 | 交互文档和性能调优能力 |
| M8 | 测试、安全加固与交付 | 5–7 天 | 部分完成 | 可验证、可部署版本 |

依赖顺序：

```text
M0 → M1 → M2 → M3/M4 → M5/M6 → M7 → M8
```

M3 和 M4 的部分工作可以并行；M5 依赖帖子模型，M6 依赖帖子发布流程和 Outbox 机制。

## 5. M0：工程基础与开发环境

预计耗时：2–3 天。

### 实现内容

1. 固定基础版本：
   - Spring Boot 3.5.x。
   - Java 17。
   - Maven Wrapper 使用与项目兼容的 Maven 版本。
2. 整理依赖：
   - 保留 Spring Web、Security、Validation、Redis、MySQL 和 MyBatis Plus。
   - 避免重复引入普通 MyBatis Starter 和 MyBatis Plus Starter。
   - 增加 Spring Data Elasticsearch。
   - 增加 Spring Security OAuth2 Resource Server，以 Nimbus JOSE/JWT 完成 JWT 编解码。
   - 增加 Spring Mail、Flyway、Springdoc OpenAPI、Actuator。
   - 测试范围增加 Testcontainers。
3. 修复 Maven Wrapper，补齐 `.mvn/wrapper` 文件。
4. 建立 `dev`、`test`、`prod` 配置：
   - 数据库密码、Redis 密码、JWT 密钥和 SMTP 凭据全部从环境变量读取。
   - 不在 Git 中保存生产凭据。
   - 检查并轮换历史上已经提交过的真实凭据。
5. 增加 Docker Compose，提供 MySQL、Redis 和 Elasticsearch 本地服务。
6. 建立清晰的包结构：
   - `config`
   - `security`
   - `controller`
   - `service`
   - `mapper`
   - `entity`
   - `dto`
   - `exception`
   - `search`
   - `job`
7. 统一 REST 约定：
   - API 前缀使用 `/api/v1`。
   - 使用 JSON 请求体，不用 GET 执行注册等状态变更。
   - 使用准确的 HTTP 状态码。
   - 采用 RFC Problem Details 或统一错误响应结构。
   - 增加全局异常处理、Bean Validation 和请求追踪 ID。
   - 数据库时间统一按 UTC 保存，对外返回 ISO 8601 时间。

### 交付物

- 可运行的 Maven Wrapper。
- 本地 Docker Compose 环境。
- 分环境配置模板。
- 统一响应、异常和分页模型。
- 基础健康检查。

### 验收标准

- `docker compose up` 可以启动全部依赖。
- `./mvnw verify` 可以执行。
- 应用可以使用开发配置启动。
- Git 跟踪文件中不存在明文密码、验证码或 JWT 私钥。

## 6. M1：MySQL 数据模型与 MyBatis Plus 基础

预计耗时：4–5 天。

### 数据模型

至少建立以下表：

- `users`
- `roles`
- `user_roles`
- `posts`
- `tags`
- `post_tags`
- `comments`
- `user_follows`
- `post_likes`
- `search_outbox`

主要设计：

- `users` 对用户名和邮箱建立唯一约束。
- 密码保存为 `password_hash`。BCrypt 已经在哈希中包含随机盐，不再单独保存当前实体中的 `salt` 字段。
- `posts` 支持草稿、已发布、隐藏和已删除等状态。
- `comments` 使用 `parent_id` 和 `root_id` 支持回复结构，并限制最大嵌套深度。
- `user_follows` 和 `post_likes` 使用联合唯一约束保证幂等。
- 内容表包含 `created_at`、`updated_at`、逻辑删除标记和乐观锁版本。
- `search_outbox` 记录待同步搜索事件，避免业务事务直接双写 MySQL 与 Elasticsearch。

建议索引：

- `posts(status, created_at, id)`
- `posts(author_id, status, created_at)`
- `comments(post_id, root_id, created_at)`
- `user_follows(follower_id, followee_id)` 唯一索引
- `user_follows(followee_id, follower_id)` 查询索引
- `post_likes(post_id, user_id)` 唯一索引
- `post_likes(post_id, created_at)` 查询索引
- `search_outbox(status, next_retry_at)`

### MyBatis Plus

- 基础 Mapper 继承 `BaseMapper<T>`。
- 启用分页、乐观锁和逻辑删除插件。
- 简单 CRUD 使用 MyBatis Plus。
- Feed、评论列表、用户主页等关联查询使用 DTO 投影和定制 SQL，避免 N+1。
- 普通后台列表使用 `Page<T>`；时间流 Feed 优先使用游标分页。
- Service 层负责事务边界，Controller 不直接调用 Mapper。

### 数据库迁移

- 使用 Flyway 管理所有 DDL。
- 禁止依赖 Hibernate 或手工操作自动修改生产表结构。
- 每次迁移提供向前兼容策略和必要的数据回填脚本。

### 验收标准

- 可以从空数据库运行全部 Flyway 迁移。
- Mapper 集成测试通过。
- 唯一约束、分页、逻辑删除和乐观锁均有自动化测试。
- 常用列表查询通过执行计划确认命中索引。

## 7. M2：Spring Security、JWT 与邮箱验证码

预计耗时：5–7 天。

### 无 HttpSession 的认证体系

- 使用 `SecurityFilterChain` 配置安全规则。
- 设置 `SessionCreationPolicy.STATELESS`，不创建服务端登录 Session。
- 使用 `AuthenticationManager`、`UserDetailsService` 和 `BCryptPasswordEncoder`。
- 使用 Spring Security OAuth2 Resource Server 验证 Access Token，减少自定义过滤器代码。
- 启用方法级权限控制，至少包含 `USER` 和 `ADMIN` 角色。
- Access Token 使用 RSA 签名 JWT：
  - 包含 `sub`、`roles`、`jti`、`iat`、`exp`。
  - 建议有效期 15 分钟。
- Refresh Token 建议有效期为 7 至 30 天：
  - Redis 保存有效 Refresh Token 的 `jti` 或不可逆摘要。
  - 每次刷新执行 Token Rotation，旧 Refresh Token 立即失效。
  - 重放旧 Refresh Token 时撤销对应令牌族。
- 明确区分 `401 Unauthorized` 与 `403 Forbidden`。
- 配置明确的 CORS 白名单。
- Bearer Token API 不使用浏览器认证 Cookie，因此关闭 CSRF；如果后续改用 Cookie，需要重新启用 CSRF 防护。
- 日志禁止记录密码、验证码、完整 JWT 和 Authorization Header。

### Redis 邮箱验证码

建议键设计：

```text
verify:email:code:{scene}:{email}       TTL 5 分钟
verify:email:cooldown:{scene}:{email}   TTL 60 秒
verify:email:attempts:{scene}:{email}   TTL 5 分钟
verify:ip:minute:{ip}                   TTL 1 分钟
verify:email:daily:{email}:{date}       TTL 至次日
```

实现规则：

- 通过 `SET NX EX 60` 保证同一邮箱、同一场景 60 秒内只能发送一次。
- 使用 Lua 脚本原子完成频率判断、计数和验证码状态写入。
- 增加 IP 每分钟上限和邮箱每日上限。
- 验证码使用安全随机数生成，只在 Redis 中保存 HMAC 摘要。
- 验证失败最多允许 5 次，超过次数后在原 TTL 内锁定。
- 校验成功后原子删除，确保验证码只能使用一次。
- 注册和重置密码使用不同 `scene`，验证码不能跨场景使用。
- SMTP 发送失败时记录失败原因，并安全释放或缩短冷却状态。
- 对外响应避免泄漏邮箱是否已经注册，降低账号枚举风险。

### 认证流程

需要覆盖：

- 发送邮箱验证码。
- 验证邮箱并注册。
- 用户名或邮箱登录。
- Access Token 刷新。
- Refresh Token Rotation。
- 用户退出并撤销 Refresh Token。
- 邮箱验证码重置密码。
- 管理员禁用用户后阻止其继续刷新令牌。

### 验收标准

- 注册、登录、刷新、退出和重置密码形成完整闭环。
- 60 秒内重复发送验证码被拒绝。
- 过期、错误、跨场景和重复使用的验证码均失败。
- 篡改、过期或错误签名 JWT 均返回 `401`。
- 普通用户无法访问管理员接口。
- 并发刷新同一个 Refresh Token 时最多有一个请求成功。

## 8. M3：用户资料与关注社交

预计耗时：3–4 天。

### 实现内容

- 查看和修改当前用户资料。
- 查看其他用户公开资料。
- 关注和取消关注。
- 分页查询粉丝列表和关注列表。
- 禁止关注自己。
- 使用数据库唯一索引保证关注操作幂等。
- 用户状态支持正常、禁用和注销。
- 头像第一阶段保存对象存储或静态资源 URL，文件上传可独立扩展。
- 提供粉丝数、关注数和帖子数统计。
- 管理员可以禁用或恢复账号。

### 权限规则

- 用户只能修改自己的个人资料。
- 公开资料不返回邮箱、密码哈希等敏感字段。
- 禁用用户不能登录、发帖、评论、点赞或关注。
- 注销用户的历史公开内容按照产品规则匿名化或隐藏。

### 验收标准

- 并发重复关注不会产生重复记录。
- 重复取消关注保持幂等。
- 私有字段不会出现在公开用户 DTO 中。
- 粉丝数和关注数与关系表一致。

## 9. M4：帖子、标签与评论

预计耗时：6–8 天。这一阶段完成后形成社区 MVP。

### 帖子模块

- 创建帖子。
- 保存草稿和发布帖子。
- 查询帖子详情。
- 修改和删除帖子。
- 最新、热门和关注用户 Feed。
- 标签添加、移除和筛选。
- 作者或管理员才能修改、删除帖子。
- 正文可保存 Markdown；如果后端输出 HTML，必须经过白名单清洗以防止 XSS。
- 帖子详情包含浏览量、点赞数和评论数。
- 发布、修改、隐藏和删除事务中写入 `search_outbox`。

### 评论模块

- 发布一级评论和回复。
- 分页加载评论与回复。
- 用户修改和删除自己的评论。
- 管理员隐藏违规评论。
- 删除使用逻辑删除，保留仍有回复的上下文结构。
- 限制评论长度、发布频率和嵌套深度。
- 在事务中维护帖子评论数，定时任务负责校正异常计数。

### 查询策略

- Feed 使用游标分页，游标至少包含排序时间和主键。
- 后台管理列表可以使用 MyBatis Plus `Page<T>`。
- 批量查询作者和互动状态，避免逐条查询。
- 热门排序使用限定时间窗口，避免历史累计值永久占据榜首。

### 验收标准

- 帖子、标签和评论完整 CRUD 可用。
- 草稿、已隐藏和已删除内容不会被公开查询。
- 权限越权测试全部通过。
- 核心列表不存在明显的 N+1 查询。
- 创建、更新和删除帖子都会可靠地产生搜索同步事件。

## 10. M5：Redis 高性能点赞模块

预计耗时：4–6 天。

采用“Redis 实时响应 + Redis Stream 异步落库”方案，减少高频点赞对 MySQL 的同步写压力。

建议键设计：

```text
like:post:{postId}:users     Set，保存实时点赞用户 ID
like:post:{postId}:count     String，保存实时点赞数
like:post:{postId}:ready     String，标记缓存已经初始化
like:events                  Redis Stream，保存待落库事件
```

### 点赞流程

1. 使用 Lua 脚本原子执行 `SADD`、计数增加和 `XADD`。
2. 只有 `SADD` 真正新增成员时才增加计数并写入事件。
3. 重复点赞直接返回当前状态，不生成重复事件。
4. 取消点赞使用 Lua 原子执行 `SREM`、计数减少和事件写入。
5. Redis Stream 消费者组批量将事件写入 `post_likes`。
6. 数据库联合唯一索引承担最终幂等保护。
7. Feed 使用 Pipeline 批量读取点赞数和当前用户的点赞状态。
8. 缓存缺失时从 MySQL 回填，并使用短期锁防止缓存击穿。
9. 定时任务校正 Redis 计数、MySQL 明细和帖子聚合计数。

### 可靠性要求

- Redis 开启 AOF，并在生产环境使用主从或高可用部署。
- 消费成功后才确认 Stream 消息。
- Pending 消息定时认领并重试。
- 应用重启后继续消费未确认事件。
- 如果业务要求点赞绝对零丢失，必须保证 Redis 持久化与复制能力，或改为数据库/事务消息作为耐久事件源。
- 点赞写入数据库采用幂等插入和幂等删除，不能通过简单的计数累加重放事件。

### 验收标准

- 并发重复点赞始终只计算一次。
- 点赞接口无需同步等待 MySQL 写入。
- Redis 和 MySQL 在约定时间内最终一致，建议目标为 5 秒内。
- 消费者重启和消息重放不会导致重复累计。
- Redis 缓存丢失后可以从 MySQL 恢复。

## 11. M6：Elasticsearch 全文检索与搜索建议

预计耗时：5–7 天。

### 索引设计

建立带版本号的索引，并通过别名隔离应用与物理索引：

```text
posts_read  -> posts_v1
posts_write -> posts_v1
```

主要字段：

- `title`：全文字段，权重最高。
- `content`：全文字段。
- `tags`：`keyword` 与全文双字段。
- `authorId`、`authorName`。
- `status`。
- `createdAt`、`updatedAt`。
- `likeCount`、`commentCount`。
- `suggest` 或 `search_as_you_type` 字段。

中文搜索可以部署与 Elasticsearch 版本严格一致的 IK 分词插件。如果暂时不维护额外插件，第一阶段使用内置分析器和 `search_as_you_type`，同时为后续切换分析器预留索引重建能力。

### 数据同步

- MySQL 始终是事实数据源。
- 帖子业务事务只写 MySQL 和 `search_outbox`。
- 后台 Worker 在事务提交后读取 Outbox 并同步 Elasticsearch。
- 同步失败采用指数退避重试，并记录最后一次错误。
- 监控积压量、重试次数和最长等待时间。
- 提供全量重建任务：
  1. 创建新版本索引。
  2. 从 MySQL 游标分页读取并批量导入。
  3. 校验文档数量和抽样内容。
  4. 原子切换读写别名。
  5. 保留上一版本用于短期回滚。

### 搜索与排序

基础文本权重建议：

```text
title^4
tags^3
content^1
authorName^0.5
```

在基础相关性得分上增加：

- 标题完整短语匹配加权。
- 发布时间衰减。
- `log1p(likeCount)` 热度加权。
- 评论数加权。
- 标签、作者、状态和时间范围过滤。
- 只允许搜索已发布内容。

搜索能力：

- 关键词全文搜索。
- 高亮匹配片段。
- 标题和标签搜索建议。
- 作者、标签和时间过滤。
- 最相关、最新和最热门排序。
- 普通分页限制最大窗口，深分页使用 `search_after`。

### 验收标准

- 发布、修改、隐藏和删除帖子后，搜索结果最终一致。
- 标题匹配默认优先于正文匹配。
- 删除、草稿和隐藏内容不会出现在搜索结果中。
- 搜索建议可以返回前缀相关的标题或标签。
- 全量重建和别名切换期间搜索服务不中断。

## 12. M7：OpenAPI、HikariCP 与可观测性

预计耗时：4–5 天。

### OpenAPI 3.0

使用 Springdoc：

- 生成 `/v3/api-docs`。
- 提供 Swagger UI 交互页面。
- 定义 Bearer JWT Security Scheme。
- 为请求、成功响应、分页响应和错误响应提供示例。
- 标记公开接口、认证接口、用户接口和管理员接口。
- 描述字段约束、枚举、排序方式和游标规则。
- 通过自动化测试检查关键 Controller 是否进入 OpenAPI 文档。

### HikariCP

通过 `spring.datasource.hikari` 配置并压测调优：

- `maximum-pool-size` 根据数据库连接上限、保留连接数和应用实例数量计算。
- 设置合理的 `minimum-idle`，避免流量突增时频繁建立连接。
- 连接获取超时建议从 2 至 3 秒开始调优。
- `max-lifetime` 比 MySQL 或网络设备的空闲断开时间短 30 至 60 秒。
- 开发和测试环境可以启用连接泄漏检测，生产环境谨慎开启。
- 不盲目扩大连接池；过大的池会增加 MySQL 锁、CPU 和上下文切换竞争。

需要观测：

- 活跃连接数。
- 空闲连接数。
- 等待连接的线程数。
- 获取连接耗时。
- SQL 执行时间和慢查询。

### 可观测性

- 健康检查覆盖 MySQL、Redis 和 Elasticsearch。
- 结构化日志携带 `requestId`。
- 记录接口耗时，但不记录敏感请求字段。
- 监控 Elasticsearch Outbox 积压数量。
- 监控 Redis Stream Pending 数量。
- 监控邮件发送失败率、登录失败率和验证码限流次数。

### 验收标准

- Swagger UI 可以完成登录并调用受保护接口。
- OpenAPI 文档包含不少于 20 个核心接口。
- 压力测试期间连接池没有持续耗尽。
- 基础服务异常时健康检查能够准确反映依赖状态。
- HikariCP 调优结果有指标或压测数据支持。

## 13. M8：测试、安全加固与交付

预计耗时：5–7 天。

### 自动化测试

- 单元测试：Service、JWT、验证码、权限判断和排序参数。
- MVC 测试：参数校验、状态码、错误响应和权限。
- Testcontainers 集成测试：MySQL、Redis 和 Elasticsearch。
- 端到端流程：
  - 验证码 → 注册 → 登录 → 刷新 → 退出。
  - 发帖 → 评论 → 点赞 → 搜索。
  - 关注用户 → 查询关注 Feed。
- 并发测试：
  - 验证码 60 秒限流。
  - 重复点赞和取消点赞。
  - 重复关注和取消关注。
  - Refresh Token 并发刷新与重放。
- 搜索一致性测试：Outbox 重试、全量重建和索引别名切换。

### 安全加固

- SQL 注入测试。
- XSS 和危险 Markdown/HTML 测试。
- IDOR/越权访问测试。
- JWT 篡改、过期和错误受众测试。
- 暴力登录与验证码滥用测试。
- 敏感信息日志扫描。
- 依赖漏洞扫描。

### 性能测试

需要覆盖：

- 帖子列表和 Feed。
- 帖子详情。
- 点赞与取消点赞。
- 搜索和搜索建议。
- 登录和刷新 Token。

建议在固定测试环境中使用以下初始目标，最终数值以实际硬件和业务容量为准：

- 200 个并发用户。
- 核心读取接口 P95 小于 250ms。
- 核心写接口 P95 小于 400ms。
- 错误率低于 1%。
- 点赞持久化延迟小于 5 秒。
- 搜索索引同步延迟小于 10 秒。

### 最终交付物

- Docker Compose 开发环境。
- Flyway 数据库迁移。
- 完整 OpenAPI 文档和 Swagger UI。
- README 启动、配置、架构和故障排查说明。
- CI 执行 `./mvnw verify`。
- 自动化测试和覆盖率报告。
- 性能测试报告。
- 不包含高危依赖和明文凭据的发布包。

## 14. 核心 REST API 清单

计划提供 33 个核心接口，满足“20+ 核心 API”的目标。

| 编号 | 模块 | 方法与路径 | 认证要求 | 用途 |
|---:|---|---|---|---|
| 1 | 认证 | `POST /api/v1/auth/email-codes` | 公开、受限流保护 | 发送注册或重置密码验证码 |
| 2 | 认证 | `POST /api/v1/auth/register` | 邮箱验证码 | 注册用户 |
| 3 | 认证 | `POST /api/v1/auth/login` | 公开、受限流保护 | 登录并签发 Token |
| 4 | 认证 | `POST /api/v1/auth/token/refresh` | Refresh Token | 轮换 Token |
| 5 | 认证 | `POST /api/v1/auth/logout` | 用户 | 撤销 Refresh Token |
| 6 | 认证 | `POST /api/v1/auth/password/reset` | 邮箱验证码 | 重置密码 |
| 7 | 用户 | `GET /api/v1/users/me` | 用户 | 当前用户资料 |
| 8 | 用户 | `PATCH /api/v1/users/me` | 用户 | 修改当前用户资料 |
| 9 | 用户 | `GET /api/v1/users/{userId}` | 公开 | 查询公开资料 |
| 10 | 用户 | `GET /api/v1/users/{userId}/posts` | 公开 | 查询用户帖子 |
| 11 | 关注 | `PUT /api/v1/users/{userId}/following` | 用户 | 关注用户 |
| 12 | 关注 | `DELETE /api/v1/users/{userId}/following` | 用户 | 取消关注 |
| 13 | 关注 | `GET /api/v1/users/{userId}/followers` | 公开 | 查询粉丝 |
| 14 | 关注 | `GET /api/v1/users/{userId}/following` | 公开 | 查询关注列表 |
| 15 | 帖子 | `POST /api/v1/posts` | 用户 | 创建帖子或草稿 |
| 16 | 帖子 | `GET /api/v1/posts` | 公开 | 分页查询帖子/Feed |
| 17 | 帖子 | `GET /api/v1/posts/{postId}` | 公开 | 查询帖子详情 |
| 18 | 帖子 | `PATCH /api/v1/posts/{postId}` | 作者/管理员 | 修改帖子 |
| 19 | 帖子 | `DELETE /api/v1/posts/{postId}` | 作者/管理员 | 删除帖子 |
| 20 | 标签 | `GET /api/v1/tags` | 公开 | 查询标签 |
| 21 | 标签 | `GET /api/v1/tags/{tag}/posts` | 公开 | 按标签查询帖子 |
| 22 | 评论 | `POST /api/v1/posts/{postId}/comments` | 用户 | 评论或回复 |
| 23 | 评论 | `GET /api/v1/posts/{postId}/comments` | 公开 | 查询评论树 |
| 24 | 评论 | `PATCH /api/v1/comments/{commentId}` | 作者/管理员 | 修改评论 |
| 25 | 评论 | `DELETE /api/v1/comments/{commentId}` | 作者/管理员 | 删除评论 |
| 26 | 点赞 | `PUT /api/v1/posts/{postId}/like` | 用户 | 点赞帖子 |
| 27 | 点赞 | `DELETE /api/v1/posts/{postId}/like` | 用户 | 取消点赞 |
| 28 | 点赞 | `GET /api/v1/posts/{postId}/likes` | 公开 | 查询点赞用户 |
| 29 | 搜索 | `GET /api/v1/search/posts` | 公开 | 全文搜索帖子 |
| 30 | 搜索 | `GET /api/v1/search/suggestions` | 公开 | 获取搜索建议 |
| 31 | 管理 | `PATCH /api/v1/admin/users/{userId}/status` | 管理员 | 管理用户状态 |
| 32 | 管理 | `PATCH /api/v1/admin/posts/{postId}/status` | 管理员 | 审核或隐藏帖子 |
| 33 | 管理 | `POST /api/v1/admin/search/reindex` | 管理员 | 触发搜索索引重建 |

## 15. 需求与里程碑对应关系

| 项目要求 | 对应实现 |
|---|---|
| Spring Boot 3.5、Java 17 | M0 固定运行和构建基线 |
| MySQL、MyBatis Plus | M1 数据模型、迁移、分页和关联查询 |
| Spring Security + JWT 无 Session 认证 | M2 Security Filter Chain、Access/Refresh Token |
| Redis 邮箱验证码 | M2 验证码 TTL、60 秒冷却、IP/每日限制、原子校验 |
| 完整用户认证 | M2 注册、登录、刷新、退出和重置密码 |
| 内容发布 | M4 帖子、草稿、标签和评论 |
| 互动社交 | M3 关注、M4 评论、M5 点赞 |
| Redis 高频点赞缓存 | M5 Lua、Set、计数和 Redis Stream 异步落库 |
| MyBatis Plus 通用 CRUD 与分页 | M1、M3、M4 |
| Elasticsearch 全文检索 | M6 Outbox 同步和版本化索引 |
| 搜索建议 | M6 `search_as_you_type` 或 Suggest 字段 |
| 多维度权重排序 | M6 文本相关性、时间、点赞和评论综合评分 |
| 20+ RESTful API | 第 14 节定义 33 个核心接口 |
| OpenAPI 3.0 交互文档 | M7 Springdoc 和 Swagger UI |
| HikariCP 优化 | M7 指标驱动的连接池配置和压测 |
| 可交付质量 | M8 自动化测试、安全测试和性能测试 |

## 16. 全局完成标准

一个里程碑只有在满足以下条件后才能标记完成：

- 功能代码已经实现，而不是只增加接口占位符。
- 数据库结构通过 Flyway 管理。
- 输入校验、鉴权和错误响应完整。
- 单元测试及相关集成测试通过。
- OpenAPI 文档与实际行为一致。
- 不记录或提交密码、验证码、Token 和私钥。
- `./mvnw verify` 通过。
- 代码审查中不存在未处理的高风险问题。

项目只有在 M8 的端到端、安全和性能验收全部通过后，才可以使用“实现了完整的用户认证、内容发布、互动社交及全文检索功能”作为完成状态描述。
