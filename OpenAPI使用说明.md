# Nova Forum OpenAPI 文档使用说明

## 📋 文档概述

Nova Forum API 完整接口文档，基于 OpenAPI 3.0.3 标准编写，包含项目的所有20个API接口，可直接导入到 Postman、APIFox、Insomnia 等API测试工具中使用。

## 📁 文件信息

- **文件名**: `nova-forum-openapi.yaml`
- **格式**: OpenAPI 3.0.3 YAML
- **API版本**: v1.0.0
- **文档版本**: 1.0.0
- **生成时间**: 2025-11-06 09:45

## 🔧 导入方法

### 1. Postman 导入

1. 打开 Postman
2. 点击 "Import" 按钮
3. 选择 "Upload Files" 选项
4. 上传 `nova-forum-openapi.yaml` 文件
5. 点击 "Import" 完成导入

**配置 JWT 认证**:
- 导入后会自动创建名为 "Nova Forum API" 的 Collection
- 在 Collection 的 "Authorization" 标签中设置：
  - Type: Bearer Token
  - Token: {{jwt_token}}

### 2. APIFox 导入

1. 打开 APIFox
2. 点击 "导入项目" 
3. 选择 "OpenAPI/Swagger" 选项
4. 上传 `nova-forum-openapi.yaml` 文件
5. 设置项目名称：Nova Forum API
6. 点击 "确定" 完成导入

### 3. Insomnia 导入

1. 打开 Insomnia
2. 点击 "Application" > "Preferences" (Windows/Linux) 或 "Insomnia" > "Preferences" (Mac)
3. 点击 "Import/Export" 标签
4. 选择 "Import Data" > "From File"
5. 选择 `nova-forum-openapi.yaml` 文件
6. 点击 "Import"

### 4. Swagger UI 在线查看

将 `nova-forum-openapi.yaml` 文件上传到 Swagger Editor (https://editor.swagger.io/) 可以在线查看和测试API文档。

## 🔐 认证配置

### JWT Bearer Token 认证

除用户注册和登录接口外，所有API都需要JWT认证：

1. **获取Token**: 先调用 `/user/login` 接口获取JWT Token
2. **添加认证头**: 在请求头中添加 `Authorization: Bearer {token}`
3. **Token格式**: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`

### 测试用户账户

```json
{
  "username": "testuser123",
  "password": "password123",
  "email": "test@example.com"
}
```

## 📚 API 模块详解

### 1. 用户认证模块 (3个接口)

| 接口     | 方法 | 路径             | 描述              | 认证 |
| -------- | ---- | ---------------- | ----------------- | ---- |
| 注册     | POST | `/user/register` | 用户注册新账号    | ❌    |
| 登录     | POST | `/user/login`    | 用户登录获取Token | ❌    |
| 获取信息 | GET  | `/user/profile`  | 获取当前用户信息  | ✅    |

### 2. 帖子管理模块 (5个接口)

| 接口     | 方法   | 路径           | 描述           | 认证 |
| -------- | ------ | -------------- | -------------- | ---- |
| 发布帖子 | POST   | `/post/create` | 创建新帖子     | ✅    |
| 获取列表 | GET    | `/post/list`   | 帖子列表(分页) | ❌    |
| 获取详情 | GET    | `/post/{id}`   | 帖子详细信息   | ❌    |
| 更新帖子 | PUT    | `/post/{id}`   | 更新帖子内容   | ✅    |
| 删除帖子 | DELETE | `/post/{id}`   | 删除帖子       | ✅    |

### 3. 评论系统模块 (8个接口)

| 接口     | 方法   | 路径                          | 描述          | 认证 |
| -------- | ------ | ----------------------------- | ------------- | ---- |
| 创建评论 | POST   | `/comment/create`             | 创建评论/回复 | ✅    |
| 评论列表 | GET    | `/comment/list/{postId}`      | 帖子评论列表  | ❌    |
| 评论详情 | GET    | `/comment/{id}`               | 获取评论详情  | ❌    |
| 更新评论 | PUT    | `/comment/{id}`               | 更新评论内容  | ✅    |
| 删除评论 | DELETE | `/comment/{id}`               | 删除评论      | ✅    |
| 用户评论 | GET    | `/comment/user/{userId}`      | 用户评论列表  | ❌    |
| 回复列表 | GET    | `/comment/replies/{parentId}` | 评论回复列表  | ❌    |
| 评论数量 | GET    | `/comment/count/{postId}`     | 帖子评论数    | ❌    |

### 4. 点赞功能模块 (4个接口)

| 接口       | 方法 | 路径                    | 描述           | 认证 |
| ---------- | ---- | ----------------------- | -------------- | ---- |
| 点赞/取消  | POST | `/like/{postId}`        | 点赞或取消点赞 | ✅    |
| 获取点赞数 | GET  | `/like/count/{postId}`  | 帖子点赞总数   | ❌    |
| 点赞状态   | GET  | `/like/status/{postId}` | 用户点赞状态   | ✅    |
| 同步数据   | POST | `/like/sync`            | 同步点赞数到DB | ✅    |

## 🧪 API 测试流程

### 完整测试步骤

1. **用户注册**
   ```
   POST /user/register
   Body: {
     "username": "testuser123",
     "password": "password123", 
     "email": "test@example.com"
   }
   ```

2. **用户登录**
   ```
   POST /user/login
   Body: {
     "username": "testuser123",
     "password": "password123"
   }
   ```
   **注意**: 记录返回的 `token` 值

3. **发布帖子**
   ```
   POST /post/create
   Header: Authorization: Bearer {token}
   Body: {
     "title": "测试帖子",
     "content": "这是一个测试帖子的内容"
   }
   ```
   **注意**: 记录返回的 `postId`

4. **创建评论**
   ```
   POST /comment/create
   Header: Authorization: Bearer {token}
   Body: {
     "postId": {postId},
     "content": "这是一条测试评论"
   }
   ```

5. **点赞操作**
   ```
   POST /like/{postId}
   Header: Authorization: Bearer {token}
   ```

6. **获取帖子详情**
   ```
   GET /post/{postId}
   ```

### 环境变量设置

在 Postman/APIFox 中设置环境变量：

```json
{
  "base_url": "http://localhost:8080/api",
  "jwt_token": "{{登录接口返回的token}}"
}
```

## 📝 请求/响应格式

### 统一请求格式

**Headers**:
```
Content-Type: application/json
Authorization: Bearer {token}  # 需要认证的接口
```

**请求体** (JSON):
```json
{
  "参数1": "值1",
  "参数2": "值2"
}
```

### 统一响应格式

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    // 具体数据
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

## 🔍 错误码说明

| 状态码 | 说明       | 场景                         |
| ------ | ---------- | ---------------------------- |
| 200    | 成功       | 操作成功                     |
| 400    | 请求错误   | 参数验证失败、用户名已存在等 |
| 401    | 未授权     | Token无效或已过期            |
| 403    | 权限不足   | 非作者尝试编辑/删除帖子      |
| 404    | 资源不存在 | 帖子/评论不存在              |
| 500    | 服务器错误 | 系统内部异常                 |

## 🛠️ 开发提示

### 1. 接口开发顺序

建议按以下顺序开发和测试：
1. 用户认证 → 2. 帖子管理 → 3. 评论系统 → 4. 点赞功能

### 2. 测试数据

- 可以使用统一的测试账户：`testuser123` / `password123`
- 创建多个测试用户验证权限控制
- 创建不同类型的帖子和评论测试完整流程

### 3. 认证流程

1. 所有需要认证的接口都需要先调用登录获取Token
2. Token过期时间：7天
3. 前端需要处理Token过期的情况

## 📊 文档统计

- **总API接口**: 20个
- **认证接口**: 3个 (注册、登录无需认证)
- **需要认证**: 17个接口
- **HTTP方法分布**:
  - GET: 10个接口
  - POST: 7个接口  
  - PUT: 2个接口
  - DELETE: 1个接口
- **数据模型**: 12个Schema定义

## 🔄 维护更新

### 更新文档

当API接口发生变化时：
1. 更新源代码中的控制器和方法
2. 重新生成或手动更新 `nova-forum-openapi.yaml`
3. 测试新文档的导入和API调用

### 版本控制

建议采用语义化版本管理：
- `v1.0.0`: 初始版本
- `v1.1.0`: 新增功能
- `v1.0.1`: 修复bug

## 📞 技术支持

如遇到问题，请检查：
1. 服务器是否正常运行 (http://localhost:8080)
2. 数据库连接是否正常
3. JWT Token是否有效
4. 请求参数是否符合规范

## 📄 许可证

MIT License - 请查看项目根目录的 LICENSE 文件

---

**最后更新**: 2025-11-06 09:45  
**文档作者**: Nova Forum Team  
**版本**: v1.0.0
