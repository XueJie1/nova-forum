# Elasticsearch 搜索功能测试指南

## 概述

本文档详细介绍了Nova Forum项目中Elasticsearch全文搜索功能的使用方法和测试步骤。搜索功能基于Elasticsearch 8.x构建，提供高性能的全文搜索、搜索建议和索引管理功能。

## 环境要求

### 必要环境
- **Elasticsearch**: 8.0+ (默认运行在 http://localhost:9200)
- **Spring Boot应用**: 已集成搜索功能
- **MySQL数据库**: 包含帖子数据
- **Redis缓存**: 点赞数据缓存

### 启动检查
确保Elasticsearch服务正在运行：
```bash
curl http://localhost:9200
```

## 功能概览

### 核心特性
- ✅ **全文搜索**: 基于标题和内容的全文检索
- ✅ **权重搜索**: 标题权重高于内容权重
- ✅ **搜索建议**: 智能搜索建议功能
- ✅ **分页查询**: 支持分页的搜索结果
- ✅ **实时同步**: 帖子CRUD操作自动同步到搜索引擎
- ✅ **索引管理**: 支持索引的创建、重建和删除

## 快速测试

### 1. 基础搜索测试

#### 搜索帖子
```bash
curl -X GET "http://localhost:8080/api/search/posts?keyword=Spring Boot"
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 6,
        "title": "测试Elasticsearch搜索功能",
        "content": "这是一个测试帖子，用于验证Elasticsearch全文搜索是否正常工作。包含关键词：Spring Boot、Elasticsearch、搜索功能。",
        "userId": 7,
        "username": "用户7",
        "viewCount": 1,
        "likeCount": 0,
        "createTime": "2024-11-09T17:02:21",
        "updateTime": "2024-11-09T17:02:21"
      }
    ],
    "total": 1,
    "pages": 1,
    "current": 1,
    "size": 10,
    "hasNext": false,
    "hasPrevious": false,
    "keyword": "Spring Boot",
    "took": 45
  }
}
```

### 2. 搜索建议测试

#### 获取搜索建议
```bash
curl -X GET "http://localhost:8080/api/search/suggestions?keyword=Spring&size=5"
```

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": [
    "Spring教程",
    "Spring经验分享",
    "Spring问题解决",
    "Spring最佳实践"
  ]
}
```

## 详细测试步骤

### 步骤1: 索引管理测试

#### 1.1 创建搜索索引
```bash
curl -X POST http://localhost:8080/api/search/index/create
```

**期望响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "索引创建成功"
}
```

#### 1.2 检查索引状态
```bash
curl -X GET http://localhost:9200/posts
```

**期望响应** (Elasticsearch原生响应):
```json
{
  "posts": {
    "aliases": {},
    "mappings": {
      "properties": {
        "id": { "type": "long" },
        "title": {
          "type": "text",
          "analyzer": "standard"
        },
        "content": {
          "type": "text", 
          "analyzer": "standard"
        },
        "userId": { "type": "long" },
        "username": { "type": "keyword" },
        "viewCount": { "type": "integer" },
        "likeCount": { "type": "integer" },
        "createTime": { "type": "date" },
        "updateTime": { "type": "date" }
      }
    }
  }
}
```

#### 1.3 重建索引
```bash
curl -X POST http://localhost:8080/api/search/index/rebuild
```

**期望响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "索引重建完成"
}
```

#### 1.4 删除索引
```bash
curl -X DELETE http://localhost:8080/api/search/index
```

**期望响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "索引删除成功"
}
```

### 步骤2: 搜索功能测试

#### 2.1 关键词搜索
```bash
curl -X GET "http://localhost:8080/api/search/posts?keyword=Java&page=1&size=5"
```

#### 2.2 空搜索（获取所有帖子）
```bash
curl -X GET "http://localhost:8080/api/search/posts"
```

#### 2.3 分页搜索
```bash
curl -X GET "http://localhost:8080/api/search/posts?keyword=测试&page=2&size=3"
```

### 步骤3: 数据同步测试

#### 3.1 发布新帖子（应该自动同步到搜索索引）
```bash
# 先登录获取token
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username": "searchtest", "password": "password123"}'

# 发布新帖子
curl -X POST http://localhost:8080/api/post/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Elasticsearch搜索测试帖子",
    "content": "这是一个专门用于测试Elasticsearch搜索功能的帖子，包含多个关键词用于验证搜索的准确性。"
  }'
```

#### 3.2 验证新帖子可搜索
```bash
curl -X GET "http://localhost:8080/api/search/posts?keyword=Elasticsearch"
```

应该能在搜索结果中看到刚发布的帖子。

### 步骤4: 高级搜索测试

#### 4.1 多关键词搜索
```bash
curl -X GET "http://localhost:8080/api/search/posts?keyword=Spring Boot Elasticsearch"
```

#### 4.2 搜索结果验证
确保搜索结果包含：
- 匹配的帖子标题和内容
- 正确的作者信息
- 准确的浏览数和点赞数
- 正确的时间戳

## 性能测试

### 响应时间测试
使用`time`命令测量搜索响应时间：
```bash
time curl -X GET "http://localhost:8080/api/search/posts?keyword=测试"
```

**期望性能指标**:
- 搜索响应时间 < 100ms
- 索引创建时间 < 1s
- 重建索引时间 < 5s

### 并发测试
使用多线程同时进行搜索请求：
```bash
# 使用简单的并发测试
for i in {1..10}; do
  curl -X GET "http://localhost:8080/api/search/posts?keyword=测试" &
done
wait
```

## 错误场景测试

### 1. Elasticsearch服务不可用
停止Elasticsearch服务，然后测试搜索接口：
```bash
# 停止Elasticsearch (根据你的安装方式)
# systemctl stop elasticsearch
# 或
# docker stop elasticsearch

curl -X GET "http://localhost:8080/api/search/posts?keyword=test"
```

**期望响应**:
```json
{
  "code": 500,
  "message": "搜索服务暂时不可用，请稍后重试",
  "data": null
}
```

### 2. 索引不存在
删除索引后尝试搜索：
```bash
curl -X DELETE http://localhost:8080/api/search/index
curl -X GET "http://localhost:8080/api/search/posts?keyword=test"
```

**期望行为**: 应该自动创建索引或返回适当的错误信息。

### 3. 无效参数测试
```bash
# 测试空关键词
curl -X GET "http://localhost:8080/api/search/posts?keyword="

# 测试无效分页参数
curl -X GET "http://localhost:8080/api/search/posts?keyword=test&page=0&size=1000"
```

## 数据验证测试

### 1. 搜索结果准确性
创建包含特定关键词的测试帖子：
```bash
# 发布测试帖子
curl -X POST http://localhost:8080/api/post/create \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "搜索测试：唯一关键词XYZ123",
    "content": "这是内容中的搜索测试关键词ABC789"
  }'

# 验证搜索结果
curl -X GET "http://localhost:8080/api/search/posts?keyword=XYZ123"
curl -X GET "http://localhost:8080/api/search/posts?keyword=ABC789"
```

### 2. 权重搜索测试
创建标题和内容都包含相同关键词的帖子：
```bash
# 验证标题匹配优先级更高
curl -X GET "http://localhost:8080/api/search/posts?keyword=权重测试"
```

## 监控和日志

### 1. 应用日志监控
查看Spring Boot应用日志中的搜索相关记录：
```bash
# 查看最近的搜索相关日志
tail -f application.log | grep -i search
```

### 2. Elasticsearch日志监控
```bash
# 查看Elasticsearch日志 (位置可能不同)
tail -f /var/log/elasticsearch/elasticsearch.log
```

## 故障排除

### 常见问题

#### 1. 搜索返回空结果
**可能原因**:
- 索引不存在
- 数据库中没有匹配的帖子
- 搜索关键词拼写错误

**解决方案**:
```bash
# 检查索引状态
curl -X GET http://localhost:9200/posts/_count

# 重建索引
curl -X POST http://localhost:8080/api/search/index/rebuild
```

#### 2. 搜索服务异常
**可能原因**:
- Elasticsearch服务未启动
- 连接配置错误
- 端口被占用

**解决方案**:
```bash
# 检查Elasticsearch服务状态
curl http://localhost:9200

# 检查应用配置
grep -r "elasticsearch" src/main/resources/application.yml
```

#### 3. 搜索性能问题
**可能原因**:
- 索引过大
- 查询复杂度高
- 硬件资源不足

**解决方案**:
- 优化索引映射
- 使用更具体的搜索关键词
- 考虑分片和副本配置

## 最佳实践

### 1. 搜索优化
- 使用具体的关键词而不是通用词汇
- 利用搜索建议功能改善用户体验
- 定期重建索引保持最佳性能

### 2. 监控建议
- 监控搜索响应时间
- 跟踪搜索查询频率
- 设置告警机制

### 3. 数据同步
- 确保新帖子及时同步到搜索索引
- 定期验证数据一致性
- 监控同步失败情况

## 总结

通过以上测试步骤，可以全面验证Elasticsearch搜索功能的正确性和性能。搜索系统应该能够：

- ✅ 准确搜索帖子内容
- ✅ 提供有用的搜索建议
- ✅ 支持分页查询
- ✅ 维护数据一致性
- ✅ 处理各种错误场景
- ✅ 提供良好的性能表现

如遇到问题，请检查应用日志和Elasticsearch日志获取详细错误信息。

---

**最后更新**: 2024-11-11  
**适用版本**: Nova Forum v1.1+  
**测试环境**: Spring Boot 3.5.7 + Elasticsearch 8.x
