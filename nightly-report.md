# Nightly Report

本项目质量改进的每日记录。每次循环记录一次修改。

---

## 记录 6 — 2026-08-31

- **优先级**：修复已知缺陷
- **问题**：`PostServiceImpl.updatePost/deletePost/getPostDetail` 中 `IllegalArgumentException`/`SecurityException` 被外层 `catch (Exception e)` 吞掉并包装成 `RuntimeException`，导致 `PostController` 中对应的 `catch (IllegalArgumentException)`(400) 与 `catch (SecurityException)`(403) 成为死代码——非法请求返回 500 而非预期的 400/403。
- **修改方案**：在 `createPost`/`updatePost`/`deletePost`/`getPostDetail` 四个方法的 `catch` 链中增加 `catch (IllegalArgumentException | SecurityException e) { throw e; }`，让特定异常直接传播到 Controller，仅对真正的意外异常做兜底包装。同步更新 `PostServiceImplTest` 中 5 个测试用例的断言：`帖子不存在` 从断言 `RuntimeException` 改为断言 `IllegalArgumentException`；`非作者` 从断言 `RuntimeException` 改为断言 `SecurityException`。
- **修改文件**：
  - `src/main/java/com/novaforum/nova_forum/service/impl/PostServiceImpl.java`（修复）
  - `src/test/java/com/novaforum/nova_forum/service/impl/PostServiceImplTest.java`（更新断言）
- **备注**：`getPostList` 方法本身不抛出 `IllegalArgumentException`/`SecurityException`，无需改动。

---

## 记录 5 — 2026-08-31

- **优先级**：测试缺失
- **问题**：`LikeServiceImpl` 没有单元测试。
- **修改方案**：新增 `LikeServiceImplTest`，用 Mockito 模拟 `RedisTemplate`/`PostMapper`/`PostLikeMapper`，覆盖 `toggleLike`（帖子不存在 / 点赞 / 取消点赞 / 计数为 null 降级）、`getLikeCount`（缓存命中 / 缓存未命中回写 / 全 null）、`isLiked`、`getLikeCountFromDatabase`（存在 / 不存在）、`syncLikeCountsToDatabase`（跳过不存在 / 同步记录与点赞数 / 无用户不插入）。
- **修改文件**：`src/test/java/com/novaforum/nova_forum/service/impl/LikeServiceImplTest.java`（新增）
- **测试结果**：15 个测试全部通过。
- **备注**：
  - `RedisTemplate.opsForSet().add/remove` 返回 `Long`（用 `doReturn(1L)`），`isMember` 返回 `Boolean`，`members` 返回 `Set`（断言需用 `HashSet`/`emptySet`，不能是 `List`）。
  - `ValueOperations.set(key, value, timeout, unit)` 返回 `void`（用 `doNothing()`）。
  - `SetOperations.isMember(K, Object)` 第二个参数用 `any(Object.class)`，裸 `any()` 会引发泛型推断错误。
  - 类上加 `@MockitoSettings(strictness = Strictness.LENIENT)`，避免 setUp 中固定的 `opsForSet()/opsForValue()` mock 在个别用例中未被调用而触发严格 stub 校验。

## 记录 4 — 2026-08-31

- **优先级**：测试缺失
- **问题**：`EmailServiceImpl` 没有单元测试。
- **修改方案**：新增 `EmailServiceImplTest`，用 Mockito 模拟 `RedisTemplate`/`JavaMailSender`，覆盖 `sendVerificationCode`（频率限制 / 发送成功 / 发送失败 / 异常）、`verifyCode`（验证码不存在 / 不匹配 / 成功——删除验证码并标记已验证）、`isEmailVerified`（已验证 / 未验证 / null / 异常）、`generateAndCacheCode`（生成 6 位验证码并缓存 / 异常）。
- **修改文件**：`src/test/java/com/novaforum/nova_forum/service/impl/EmailServiceImplTest.java`（新增）
- **测试结果**：14 个测试全部通过。
- **备注**：
  - `@Value("${spring.mail.username}")` 注入的 `fromEmail` 在纯 Mockito 环境中不会填充，测试通过反射手动设置。
  - `RedisTemplate.opsForValue().set(key, value, timeout, unit)` 返回 `void`（用 `doNothing()`），`RedisTemplate.delete(key)` 返回 `Boolean`（用 `doReturn(true)`）。
  - `JavaMailSender.send()` 有两个重载，用 `any(SimpleMailMessage.class)` 消除歧义。
  - 类上加 `@MockitoSettings(strictness = Strictness.LENIENT)`，避免因 setUp 中固定的 `opsForValue()` mock 在个别用例中未被调用而触发严格 stub 校验。
  - `Strictness` 位于 `org.mockito.quality` 包。

## 记录 3 — 2026-08-31

- **优先级**：测试缺失
- **问题**：`CommentServiceImpl` 没有单元测试。
- **修改方案**：新增 `CommentServiceImplTest`，用 Mockito 模拟 `CommentMapper`/`PostMapper`/`UserMapper`，覆盖 `createComment`（帖子/父评论校验、成功回填主键）、`canEditComment`（所有权判断）、`updateComment`/`deleteComment`（权限校验与连同子评论批量删除）、`buildCommentTree`（空值/单层/多层级/时间升序）、`getCommentsByPostId`（树构建与分页）。
- **修改文件**：`src/test/java/com/novaforum/nova_forum/service/impl/CommentServiceImplTest.java`（新增）
- **测试结果**：16 个测试全部通过。
- **备注**：
  - Mockito 5.x 移除了 `verifyZeroInteractions`，改用 `verifyNoInteractions`。
  - `buildCommentTree` 对无子评论的顶级评论 `replies` 为 `null`（未初始化），测试据此断言。
  - `getAllSubComments` 递归查询子评论，mock 必须按调用次序返回（首次返回子评论列表、后续返回空），否则无限递归导致 `StackOverflowError`。

## 记录 2 — 2026-08-31

- **优先级**：测试缺失
- **问题**：`PostServiceImpl` 没有单元测试。
- **修改方案**：新增 `PostServiceImplTest`，用 Mockito 模拟 `PostMapper`/`UserService`/`PostSyncService`，覆盖 `createPost`（空参数 / 成功 / 插入失败 / ES 同步失败不影响）、`updatePost`（校验 / 帖子不存在 / 非作者 / 成功并保持原浏览点赞数）、`deletePost`（校验 / 帖子不存在 / 非作者 / 成功）、`getPostDetail`（空ID / 不存在 / 成功并自增浏览数）、`getPostList`（分页默认值 / 越界回退）、计数自增方法（空ID / 异常被吞）。
- **修改文件**：`src/test/java/com/novaforum/nova_forum/service/impl/PostServiceImplTest.java`（新增）
- **测试结果**：22 个测试全部通过；PostServiceImpl 行覆盖约 88%。
- **备注**：
  - 已修复（见记录 6）：`PostServiceImpl.updatePost/deletePost/getPostDetail` 的异常传播问题已修正，测试断言同步更新为验证 `IllegalArgumentException`/`SecurityException` 正确传播。
  - AssertJ 无 `assertThatSecurityException()`，改用 `assertThatThrownBy(...).isInstanceOf(SecurityException.class)`。
  - Mockito 5.x 移除了 `verifyZeroInteractions`，改用 `verifyNoInteractions`。

## 记录 1 — 2026-08-30

- **优先级**：测试缺失（最高优先级）
- **问题**：`service.impl` 包下 `UserServiceImpl` 没有单元测试，且该包 JaCoCo 覆盖率检查要求 ≥70%。
- **修改方案**：新增 `UserServiceImplTest`，用 Mockito 模拟 `UserMapper` 与 `PasswordUtil`，覆盖 `register` 全部分支（参数为空 / 用户名重复 / 邮箱重复 / 成功 / 插入失败 / 加密异常）、`findByUsername`/`findByEmail`/`findById` 的空值与查不到场景、`validatePassword` 校验。
- **修改文件**：`src/test/java/com/novaforum/nova_forum/service/impl/UserServiceImplTest.java`（新增）
- **测试结果**：16 个测试全部通过；JaCoCo 覆盖率检查通过。
- **备注**：
  - Mockito 5.x 移除了 `verifyZeroInteractions`，改用 `verifyNoInteractions`。
  - 已知的既有环境性问题（非本次引入，非代码缺陷）：
    - `NovaForumApplicationTests.contextLoads` 失败，因为 `application.yml` 依赖 MySQL/Redis/ES/SMTP，本地无这些服务。
    - `SearchControllerTest` 20 个用例返回 404：`@WebMvcTest(controllers = SearchController.class)` 未把 `SearchController` 注册为 bean（上下文能加载，但 `RequestMappingHandlerMapping` 中没有该控制器的映射）。这是既有问题，已恢复原始测试文件未做任何修改。
