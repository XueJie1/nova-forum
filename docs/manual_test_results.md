# Nova Forum 搜索功能手动测试结果

## 测试信息

- **测试日期**: 2025-11-15
- **测试人员**: 自动化测试团队
- **测试环境**: 开发环境
- **应用版本**: v1.1

## 前置条件

### 环境要求
- [x] MySQL 8.0+ 运行中
- [x] Redis 6.0+ 运行中
- [x] Elasticsearch 8.x 运行中
- [x] Spring Boot 应用已启动
- [x] 数据库中有测试数据

### 测试工具
- Postman / cURL
- 浏览器开发者工具
- Elasticsearch Head（可选）

---

## 测试用例执行记录

### 1. 索引管理测试

#### 测试用例 1.1: 创建搜索索引

**请求**:
```http
POST http://localhost:8080/api/search/index/create
Content-Type: application/json
```

**预期结果**:
- HTTP 状态码: 200
- 响应格式:
```json
{
  "code": 200,
  "message": "索引创建成功",
  "data": true
}
```

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 1.2: 重复创建索引（应该失败）

**请求**:
```http
POST http://localhost:8080/api/search/index/create
```

**预期结果**:
- HTTP 状态码: 400 或 500
- 错误消息: 索引已存在

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 1.3: 重建索引

**请求**:
```http
POST http://localhost:8080/api/search/index/rebuild
Content-Type: application/json
```

**预期结果**:
- HTTP 状态码: 200
- 所有帖子数据被重新索引

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 1.4: 删除索引

**请求**:
```http
DELETE http://localhost:8080/api/search/index
```

**预期结果**:
- HTTP 状态码: 200
- 索引被成功删除

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

### 2. 搜索功能测试

#### 测试用例 2.1: 关键词搜索（英文）

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=Java&page=1&size=10
```

**预期结果**:
- HTTP 状态码: 200
- 返回包含 "Java" 的帖子列表
- 响应格式:
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": {
    "posts": [...],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 2.2: 关键词搜索（中文）

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=学习&page=1&size=10
```

**预期结果**:
- HTTP 状态码: 200
- 返回包含 "学习" 的帖子列表

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**:
⚠️ 注意：使用 Standard Analyzer，中文分词效果可能不佳
_______________

---

#### 测试用例 2.3: 空关键词搜索

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=&page=1&size=10
```

**预期结果**:
- HTTP 状态码: 200
- 返回所有帖子（分页）

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 2.4: 不存在的关键词

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=不存在的关键词xyz123&page=1&size=10
```

**预期结果**:
- HTTP 状态码: 200
- 返回空列表
```json
{
  "code": 200,
  "data": {
    "posts": [],
    "total": 0
  }
}
```

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 2.5: 特殊字符搜索

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=C++&page=1&size=10
GET http://localhost:8080/api/search/posts?keyword=C#&page=1&size=10
```

**预期结果**:
- HTTP 状态码: 200
- 正确处理特殊字符

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 2.6: 搜索权重验证

**前置条件**: 创建以下测试数据
- 帖子A: 标题="Java编程基础", 内容="学习Spring Boot框架"
- 帖子B: 标题="学习Spring Boot", 内容="Java编程基础知识很重要"

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=Java编程基础&page=1&size=10
```

**预期结果**:
- 帖子A 排在帖子B 前面（标题权重3倍）

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

### 3. 分页功能测试

#### 测试用例 3.1: 默认分页

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=test
```

**预期结果**:
- 使用默认分页参数（page=1, size=10）

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 3.2: 自定义分页

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=test&page=2&size=5
```

**预期结果**:
- 返回第2页，每页5条记录

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 3.3: 边界值测试

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword=test&page=0&size=0
GET http://localhost:8080/api/search/posts?keyword=test&page=-1&size=-1
GET http://localhost:8080/api/search/posts?keyword=test&page=1&size=1000
```

**预期结果**:
- 正确处理无效参数（返回错误或使用默认值）

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

### 4. 搜索建议测试

#### 测试用例 4.1: 获取搜索建议

**请求**:
```http
GET http://localhost:8080/api/search/suggestions?prefix=Ja
```

**预期结果**:
- HTTP 状态码: 200
- 返回以 "Ja" 开头的建议列表

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 4.2: 空前缀

**请求**:
```http
GET http://localhost:8080/api/search/suggestions?prefix=
```

**预期结果**:
- 返回热门搜索建议或空列表

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

### 5. 数据同步测试

#### 测试用例 5.1: 创建帖子自动同步

**步骤**:
1. 创建新帖子
```http
POST http://localhost:8080/api/post/create
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "测试帖子标题123",
  "content": "测试帖子内容456"
}
```

2. 等待1-2秒（同步延迟）

3. 搜索新帖子
```http
GET http://localhost:8080/api/search/posts?keyword=测试帖子标题123
```

**预期结果**:
- 新创建的帖子能被搜索到

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 5.2: 更新帖子自动同步

**步骤**:
1. 更新现有帖子
```http
PUT http://localhost:8080/api/post/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "更新后的标题",
  "content": "更新后的内容"
}
```

2. 等待1-2秒

3. 搜索更新后的内容
```http
GET http://localhost:8080/api/search/posts?keyword=更新后的标题
```

**预期结果**:
- 搜索结果反映更新后的内容

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 5.3: 删除帖子自动同步

**步骤**:
1. 记录某个帖子的ID和标题
2. 删除该帖子
```http
DELETE http://localhost:8080/api/post/{id}
Authorization: Bearer {token}
```

3. 等待1-2秒

4. 搜索已删除的帖子
```http
GET http://localhost:8080/api/search/posts?keyword={已删除帖子的标题}
```

**预期结果**:
- 已删除的帖子不出现在搜索结果中

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

### 6. 异常场景测试

#### 测试用例 6.1: Elasticsearch 服务未启动

**步骤**:
1. 停止 Elasticsearch 服务
2. 尝试搜索
```http
GET http://localhost:8080/api/search/posts?keyword=test
```

**预期结果**:
- 返回友好的错误消息
- 应用不崩溃
- HTTP 状态码: 500 或 503

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 6.2: 索引不存在时搜索

**步骤**:
1. 删除搜索索引
```http
DELETE http://localhost:8080/api/search/index
```

2. 尝试搜索
```http
GET http://localhost:8080/api/search/posts?keyword=test
```

**预期结果**:
- 返回友好的错误消息或空结果
- 不抛出异常

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

#### 测试用例 6.3: 超长关键词

**请求**:
```http
GET http://localhost:8080/api/search/posts?keyword={1000+字符的超长字符串}
```

**预期结果**:
- 正确处理或返回参数验证错误

**实际结果**:
- [ ] 通过
- [ ] 失败

**备注**: _______________

---

## 测试总结

### 通过率统计

| 测试模块 | 总用例数 | 通过数 | 失败数 | 通过率 |
|---------|---------|--------|--------|--------|
| 索引管理 | 4 | - | - | -% |
| 搜索功能 | 6 | - | - | -% |
| 分页功能 | 3 | - | - | -% |
| 搜索建议 | 2 | - | - | -% |
| 数据同步 | 3 | - | - | -% |
| 异常场景 | 3 | - | - | -% |
| **总计** | **21** | **-** | **-** | **-%** |

### 发现的问题

#### 严重问题 (Critical)
1. _____________

#### 重要问题 (Major)
1. _____________

#### 次要问题 (Minor)
1. _____________

### 改进建议

1. **中文搜索优化**: 建议集成 IK 分词器以提升中文搜索效果
2. **搜索高亮**: 实现搜索结果高亮功能
3. **性能优化**: 测试大数据量下的搜索性能
4. **错误处理**: 完善异常场景的错误提示信息

### 后续行动

- [ ] 修复发现的所有 Critical 级别问题
- [ ] 修复发现的 Major 级别问题
- [ ] 开始自动化测试开发（单元测试 + 集成测试）
- [ ] 进行性能测试

---

## 附录

### 测试数据准备脚本

用于创建测试数据的 SQL 或 API 调用脚本：

```sql
-- 示例测试数据
INSERT INTO post (user_id, title, content, create_time, update_time) VALUES
(1, 'Java编程基础', '学习Java的基础知识，包括语法、面向对象等', NOW(), NOW()),
(1, 'Spring Boot实战', 'Spring Boot框架的实战开发指南', NOW(), NOW()),
(1, 'Elasticsearch搜索引擎', '全文搜索引擎Elasticsearch的使用教程', NOW(), NOW()),
(1, '数据库优化技巧', 'MySQL数据库性能优化的最佳实践', NOW(), NOW()),
(1, 'Redis缓存应用', 'Redis在高并发场景下的缓存应用', NOW(), NOW());
```

### 环境配置

```yaml
# application.yml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username:
    password:
```

### 常用测试命令

```bash
# 检查 Elasticsearch 状态
curl -X GET "localhost:9200/_cluster/health?pretty"

# 查看索引列表
curl -X GET "localhost:9200/_cat/indices?v"

# 查看 posts 索引的映射
curl -X GET "localhost:9200/posts/_mapping?pretty"

# 查询索引中的所有文档
curl -X GET "localhost:9200/posts/_search?pretty"
```
