# Nova Forum API 文档

## 项目概述

Nova Forum 是一个基于 Spring Boot 构建的现代化社区论坛平台，提供完整的用户管理和帖子功能。该项目使用主流技术栈构建，具备高性能和高安全性的特点。

### 技术栈

- **框架**: Spring Boot 3.5.7
- **Java版本**: 17
- **数据库**: MySQL + MyBatis Plus
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **构建工具**: Maven
- **数据验证**: Jakarta Validation

### 基础URL

```
http://localhost:8080
```

## 认证机制

本API使用JWT（JSON Web Token）进行身份认证。

### 认证流程

1. 用户注册或登录获取JWT Token
2. 在需要认证的请求头中添加 `Authorization: Bearer <token>`
3. 服务器验证Token并提取用户信息

### Token格式

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

## 统一响应格式

所有API响应都遵循统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 响应字段说明

- `code`: HTTP状态码
- `message`: 响应消息
- `data`: 响应数据（可选）

### 常见状态码

- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权（Token无效或过期）
- `403`: 禁止访问（权限不足）
- `500`: 服务器内部错误

---

## 用户管理接口

### 1. 用户注册

#### 接口信息

- **请求方法**: `POST`
- **接口路径**: `/user/register`
- **认证要求**: 无需认证

#### 请求参数

```json
{
  "username": "string",
  "password": "string", 
  "email": "string"
}
```

#### 参数说明

| 字段     | 类型   | 必填 | 约束      | 说明             |
| -------- | ------ | ---- | --------- | ---------------- |
| username | String | 是   | 3-50字符  | 用户名，唯一标识 |
| password | String | 是   | 6-100字符 | 用户密码         |
| email    | String | 是   | 邮箱格式  | 用户邮箱         |

#### 成功响应

```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

#### cURL示例

```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com"
  }'
```

---

### 2. 用户登录

#### 接口信息

- **请求方法**: `POST`
- **接口路径**: `/user/login`
- **认证要求**: 无需认证

#### 请求参数

```json
{
  "username": "string",
  "password": "string"
}
```

#### 参数说明

| 字段     | 类型   | 必填 | 说明     |
| -------- | ------ | ---- | -------- |
| username | String | 是   | 用户名   |
| password | String | 是   | 用户密码 |

#### 成功响应

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

#### 错误响应

```json
{
  "code": 400,
  "message": "用户名不存在",
  "data": null
}
```

#### cURL示例

```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

---

### 3. 获取用户信息

#### 接口信息

- **请求方法**: `GET`
- **接口路径**: `/user/profile`
- **认证要求**: 需要Bearer Token

#### 请求头

```
Authorization: Bearer <token>
```

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com"
  }
}
```

#### 错误响应

```json
{
  "code": 401,
  "message": "令牌无效或已过期",
  "data": null
}
```

#### cURL示例

```bash
curl -X GET http://localhost:8080/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 帖子管理接口

### 1. 发布帖子

#### 接口信息

- **请求方法**: `POST`
- **接口路径**: `/post/create`
- **认证要求**: 需要Bearer Token

#### 请求头

```
Authorization: Bearer <token>
Content-Type: application/json
```

#### 请求参数

```json
{
  "title": "string",
  "content": "string"
}
```

#### 参数说明

| 字段    | 类型   | 必填 | 约束       | 说明     |
| ------- | ------ | ---- | ---------- | -------- |
| title   | String | 是   | 1-200字符  | 帖子标题 |
| content | String | 是   | 1-5000字符 | 帖子内容 |

#### 成功响应

```json
{
  "code": 200,
  "message": "帖子发布成功",
  "data": "帖子ID: 123"
}
```

#### 错误响应

```json
{
  "code": 401,
  "message": "无效的授权头",
  "data": null
}
```

#### cURL示例

```bash
curl -X POST http://localhost:8080/post/create \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "我的第一个帖子",
    "content": "这是帖子的详细内容..."
  }'
```

---

### 2. 更新帖子

#### 接口信息

- **请求方法**: `PUT`
- **接口路径**: `/post/{id}`
- **认证要求**: 需要Bearer Token（仅作者可更新）

#### 接口路径参数

| 参数 | 类型 | 说明   |
| ---- | ---- | ------ |
| id   | Long | 帖子ID |

#### 请求头

```
Authorization: Bearer <token>
Content-Type: application/json
```

#### 请求参数

```json
{
  "title": "string",
  "content": "string"
}
```

#### 参数说明

| 字段    | 类型   | 必填 | 约束       | 说明     |
| ------- | ------ | ---- | ---------- | -------- |
| title   | String | 是   | 1-200字符  | 帖子标题 |
| content | String | 是   | 1-5000字符 | 帖子内容 |

#### 成功响应

```json
{
  "code": 200,
  "message": "帖子更新成功",
  "data": null
}
```

#### 错误响应

```json
{
  "code": 403,
  "message": "无权修改此帖子",
  "data": null
}
```

#### cURL示例

```bash
curl -X PUT http://localhost:8080/post/123 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "title": "更新的标题",
    "content": "更新的内容..."
  }'
```

---

### 3. 删除帖子

#### 接口信息

- **请求方法**: `DELETE`
- **接口路径**: `/post/{id}`
- **认证要求**: 需要Bearer Token（仅作者可删除）

#### 接口路径参数

| 参数 | 类型 | 说明   |
| ---- | ---- | ------ |
| id   | Long | 帖子ID |

#### 请求头

```
Authorization: Bearer <token>
```

#### 成功响应

```json
{
  "code": 200,
  "message": "帖子删除成功",
  "data": null
}
```

#### 错误响应

```json
{
  "code": 403,
  "message": "无权删除此帖子",
  "data": null
}
```

#### cURL示例

```bash
curl -X DELETE http://localhost:8080/post/123 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

### 4. 获取帖子详情

#### 接口信息

- **请求方法**: `GET`
- **接口路径**: `/post/{id}`
- **认证要求**: 无需认证

#### 接口路径参数

| 参数 | 类型 | 说明   |
| ---- | ---- | ------ |
| id   | Long | 帖子ID |

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "userId": 1,
    "username": "john_doe",
    "title": "帖子标题",
    "content": "帖子内容",
    "viewCount": 10,
    "likeCount": 5,
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  }
}
```

#### 响应字段说明

| 字段       | 类型          | 说明       |
| ---------- | ------------- | ---------- |
| id         | Long          | 帖子ID     |
| userId     | Long          | 作者ID     |
| username   | String        | 作者用户名 |
| title      | String        | 帖子标题   |
| content    | String        | 帖子内容   |
| viewCount  | Integer       | 浏览次数   |
| likeCount  | Integer       | 点赞数     |
| createTime | LocalDateTime | 创建时间   |
| updateTime | LocalDateTime | 更新时间   |

#### 错误响应

```json
{
  "code": 400,
  "message": "帖子不存在",
  "data": null
}
```

#### cURL示例

```bash
curl -X GET http://localhost:8080/post/123
```

---

### 5. 获取帖子列表（分页）

#### 接口信息

- **请求方法**: `GET`
- **接口路径**: `/post/list`
- **认证要求**: 无需认证

#### 查询参数

| 参数     | 类型    | 必填 | 默认值 | 说明                 |
| -------- | ------- | ---- | ------ | -------------------- |
| pageNum  | Integer | 否   | 1      | 页码（从1开始）      |
| pageSize | Integer | 否   | 10     | 每页数量             |
| userId   | Long    | 否   | -      | 按用户ID筛选（可选） |

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "current": 1,
    "size": 10,
    "total": 50,
    "records": [
      {
        "id": 123,
        "userId": 1,
        "username": "john_doe",
        "title": "帖子标题1",
        "content": "帖子内容1",
        "viewCount": 10,
        "likeCount": 5,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ]
  }
}
```

#### 响应字段说明

分页响应包含以下字段：

- `current`: 当前页码
- `size`: 每页数量
- `total`: 总记录数
- `records`: 当前页的记录列表

#### cURL示例

```bash
# 获取所有帖子（默认分页）
curl -X GET "http://localhost:8080/post/list"

# 获取指定用户的帖子
curl -X GET "http://localhost:8080/post/list?userId=1&pageNum=1&pageSize=5"
```

---

## 搜索接口

### 1. 搜索帖子

#### 接口信息

- **请求方法**: `GET`
- **接口路径**: `/search/posts`
- **认证要求**: 无需认证

#### 查询参数

| 参数    | 类型    | 必填 | 默认值 | 说明            |
| ------- | ------- | ---- | ------ | --------------- |
| keyword | String  | 否   | -      | 搜索关键词      |
| page    | Integer | 否   | 1      | 页码（从1开始） |
| size    | Integer | 否   | 10     | 每页数量        |

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 123,
        "title": "搜索结果标题",
        "content": "搜索结果内容...",
        "userId": 1,
        "username": "john_doe",
        "viewCount": 10,
        "likeCount": 5,
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ],
    "total": 25,
    "pages": 3,
    "current": 1,
    "size": 10,
    "hasNext": true,
    "hasPrevious": false,
    "keyword": "Spring Boot",
    "took": 45
  }
}
```

#### 响应字段说明

| 字段        | 类型               | 说明             |
| ----------- | ------------------ | ---------------- |
| records     | List<PostDocument> | 搜索结果列表     |
| total       | Long               | 总结果数         |
| pages       | Long               | 总页数           |
| current     | Long               | 当前页码         |
| size        | Long               | 每页数量         |
| hasNext     | Boolean            | 是否有下一页     |
| hasPrevious | Boolean            | 是否有上一页     |
| keyword     | String             | 搜索关键词       |
| took        | Long               | 搜索耗时（毫秒） |

#### 错误响应

```json
{
  "code": 500,
  "message": "搜索服务暂时不可用，请稍后重试",
  "data": null
}
```

#### cURL示例

```bash
# 搜索帖子
curl -X GET "http://localhost:8080/search/posts?keyword=Spring Boot&page=1&size=10"

# 获取所有帖子（无关键词）
curl -X GET "http://localhost:8080/search/posts"
```

---

### 2. 获取搜索建议

#### 接口信息

- **请求方法**: `GET`
- **接口路径**: `/search/suggestions`
- **认证要求**: 无需认证

#### 查询参数

| 参数    | 类型    | 必填 | 默认值 | 说明       |
| ------- | ------- | ---- | ------ | ---------- |
| keyword | String  | 是   | -      | 搜索关键词 |
| size    | Integer | 否   | 5      | 建议数量   |

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    "Spring Boot教程",
    "Spring Boot最佳实践",
    "Spring Boot问题解决",
    "Spring Boot经验分享",
    "Spring Boot性能优化"
  ]
}
```

#### cURL示例

```bash
curl -X GET "http://localhost:8080/search/suggestions?keyword=Spring&size=5"
```

---

### 3. 创建搜索索引

#### 接口信息

- **请求方法**: `POST`
- **接口路径**: `/search/index/create`
- **认证要求**: 无需认证

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": "索引创建成功"
}
```

#### 错误响应

```json
{
  "code": 500,
  "message": "创建索引失败",
  "data": null
}
```

#### cURL示例

```bash
curl -X POST http://localhost:8080/search/index/create
```

---

### 4. 重建搜索索引

#### 接口信息

- **请求方法**: `POST`
- **接口路径**: `/search/index/rebuild`
- **认证要求**: 无需认证

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": "索引重建完成"
}
```

#### cURL示例

```bash
curl -X POST http://localhost:8080/search/index/rebuild
```

---

### 5. 删除搜索索引

#### 接口信息

- **请求方法**: `DELETE`
- **接口路径**: `/search/index`
- **认证要求**: 无需认证

#### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": "索引删除成功"
}
```

#### 错误响应

```json
{
  "code": 500,
  "message": "删除索引失败",
  "data": null
}
```

#### cURL示例

```bash
curl -X DELETE http://localhost:8080/search/index
```

---

## 数据模型

### User（用户）

```json
{
  "id": "Long",
  "username": "String",
  "email": "String",
  "password": "String",
  "createTime": "LocalDateTime",
  "updateTime": "LocalDateTime"
}
```

### Post（帖子）

```json
{
  "id": "Long",
  "userId": "Long",
  "title": "String",
  "content": "String",
  "viewCount": "Integer",
  "likeCount": "Integer",
  "createTime": "LocalDateTime",
  "updateTime": "LocalDateTime"
}
```

---

## 错误处理

### 认证错误

#### Token无效或过期

```json
{
  "code": 401,
  "message": "令牌无效或已过期",
  "data": null
}
```

#### 权限不足

```json
{
  "code": 403,
  "message": "无权修改此帖子",
  "data": null
}
```

### 参数验证错误

#### 请求参数为空

```json
{
  "code": 400,
  "message": "用户名不能为空",
  "data": null
}
```

#### 参数格式错误

```json
{
  "code": 400,
  "message": "邮箱格式不正确",
  "data": null
}
```

---

## 最佳实践

### 1. API调用流程

1. **注册/登录**: 首先调用注册或登录接口获取Token
2. **存储Token**: 客户端安全存储Token（推荐使用安全的存储机制）
3. **添加认证**: 在需要认证的请求中添加Authorization头
4. **Token管理**: 监控Token过期时间，及时刷新

### 2. 错误处理

- 始终检查响应中的`code`字段
- 对于401错误，提示用户重新登录
- 对于400错误，显示具体的验证错误信息
- 对于500错误，显示通用的服务器错误提示

### 3. 安全建议

- 使用HTTPS传输敏感数据
- 不要在客户端硬编码Token
- 定期更新密码和Token
- 验证所有用户输入
- 实施适当的访问控制

---

## 快速开始示例

### 完整的工作流程

```bash
# 1. 注册新用户
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com"
  }'

# 2. 登录获取Token
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'

# 3. 使用Token发布帖子
curl -X POST http://localhost:8080/post/create \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "我的第一个帖子",
    "content": "这是帖子的详细内容"
  }'

# 4. 获取帖子列表
curl -X GET "http://localhost:8080/post/list?pageNum=1&pageSize=10"

# 5. 获取帖子详情
curl -X GET http://localhost:8080/post/1
```

---

## 版本信息

- **API版本**: v1.0
- **文档版本**: 1.1
- **最后更新**: 2025-11-11

## 更新日志

### v1.1 (2024-11-11)
- ✅ 新增全文搜索功能
- ✅ 新增搜索建议功能
- ✅ 新增索引管理功能
- ✅ 优化API文档格式
- ✅ 修复表格对齐问题

### v1.0 (2024-11-05)
- ✅ 初始版本发布
- ✅ 用户认证接口
- ✅ 帖子管理接口
- ✅ 基础API文档

## 支持与联系

如有问题或建议，请通过以下方式联系：

- 项目地址: https://github.com/XueJie1/nova-forum.git
- 技术栈: Spring Boot + MySQL + Redis + JWT

---

*本文档描述了Nova Forum API的完整接口规范，适用于前后端开发者、AI助手和API测试工具使用。*
