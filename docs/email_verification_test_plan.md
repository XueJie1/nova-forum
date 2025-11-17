# 邮箱验证功能测试计划（安全版本）

## 安全改进概述

**重要更新**: 注册接口现已集成邮箱验证，消除了绕过验证的安全风险。

### 安全流程
1. 用户发送验证码到邮箱
2. 用户在注册时提供邮箱验证码
3. 系统验证验证码有效性
4. 验证成功后立即注册用户

## 测试环境准备

### 邮件服务配置
在 `application.yml` 中配置真实的邮件服务：
```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
```

**注意**：测试时需要使用真实的邮箱地址和应用程序密码。

### Redis服务
确保Redis服务正常运行。

## 功能测试用例

### 1. 发送验证码测试
```bash
curl -X POST http://localhost:8080/api/email/send-code \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

**期望结果**：
- 返回 `{"code": 200, "message": "验证码发送成功"}`
- 邮箱收到6位验证码邮件

### 2. 验证验证码测试
```bash
curl -X POST http://localhost:8080/api/email/verify \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "code": "123456"}'
```

**期望结果**：
- 验证成功：`{"code": 200, "message": "success", "data": {"isValid": true, "message": "邮箱验证成功"}}`
- 验证失败：`{"code": 200, "message": "success", "data": {"isValid": false, "message": "验证码错误或已过期"}}`

### 3. 安全注册流程测试（重点）
```bash
# 步骤1：发送验证码
curl -X POST http://localhost:8080/api/email/send-code \
  -H "Content-Type: application/json" \
  -d '{"email": "newuser@example.com"}'

# 步骤2：从邮箱获取验证码，然后进行注册
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser123",
    "password": "password123",
    "email": "newuser@example.com",
    "code": "验证码"
  }'
```

**期望结果**：
- 成功：`{"code": 200, "message": "注册成功"}`
- 失败：`{"code": 400, "message": "邮箱验证码错误或已过期"}`

### 4. 检查邮箱验证状态
```bash
curl -X POST http://localhost:8080/api/email/check-verified \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

**期望结果**：
- 已验证：`{"code": 200, "message": "success", "data": true}`
- 未验证：`{"code": 200, "message": "success", "data": false}`

## 安全测试用例

### 1. ❌ 无验证码注册尝试
```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

**期望结果**：
`{"code": 400, "message": "邮箱验证码不能为空"}`

### 2. ❌ 无效验证码注册尝试
```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "code": "000000"
  }'
```

**期望结果**：
`{"code": 400, "message": "邮箱验证码错误或已过期"}`

### 3. ❌ 过期验证码注册尝试
- 发送验证码后等待5分钟
- 尝试使用过期验证码注册

**期望结果**：
`{"code": 400, "message": "邮箱验证码错误或已过期"}`

### 4. ❌ 重复使用验证码
- 验证成功后，再次使用相同验证码

**期望结果**：
`{"code": 400, "message": "邮箱验证码错误或已过期"}`

### 5. ✅ 完整安全流程测试
```bash
# 完整流程
1. 发送验证码到邮箱
2. 立即用验证码注册
3. 检查是否能登录

curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "secureuser",
    "password": "password123",
    "email": "secure@example.com",
    "code": "有效验证码"
  }'

# 如果注册成功，尝试登录
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "secureuser",
    "password": "password123"
  }'
```

**期望结果**：
- 注册成功且能登录
- JWT令牌正常生成

## 异常情况测试

### 1. 重复用户名
```bash
# 第一次注册成功
# 第二次用相同用户名注册
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "existinguser",
    "password": "password123",
    "email": "user2@example.com",
    "code": "有效验证码"
  }'
```

**期望结果**：
`{"code": 400, "message": "用户名已存在"}`

### 2. 重复邮箱
```bash
# 第一次注册成功
# 第二次用相同邮箱注册
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2",
    "password": "password123",
    "email": "existing@example.com",
    "code": "有效验证码"
  }'
```

**期望结果**：
`{"code": 400, "message": "邮箱已被注册"}`

### 3. 频率限制测试
- 在60秒内多次发送验证码到同一邮箱

**期望结果**：
`{"code": 400, "message": "验证码发送失败，请稍后重试"}`

## 性能测试
- 并发注册请求测试
- 大量邮箱验证测试
- Redis缓存性能测试

## 监控点
- 邮件发送成功率
- 验证码生成和验证成功率
- Redis缓存命中率
- API响应时间
- 错误日志分析
- 安全事件监控

## 测试注意事项
1. 使用真实的邮件地址进行测试
2. 确保Redis服务可访问
3. 检查防火墙和网络设置
4. 监控日志输出验证功能正常
5. 验证安全性：确认无法绕过邮箱验证
6. 测试完成后清理测试数据

## 安全优势总结
✅ **强制邮箱验证**: 每次注册都必须提供有效验证码  
✅ **验证码一次性使用**: 验证后立即失效  
✅ **时间限制**: 验证码5分钟有效  
✅ **频率限制**: 60秒内同一邮箱只能发送一次验证码  
✅ **无绕过可能**: 不存在绕过验证的漏洞
