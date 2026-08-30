# Nightly Report

本项目质量改进的每日记录。每次循环记录一次修改。

---

## 记录 2 — 2026-08-31

- **优先级**：测试缺失
- **问题**：`PostServiceImpl` 没有单元测试。
- **修改方案**：新增 `PostServiceImplTest`，用 Mockito 模拟 `PostMapper`/`UserService`/`PostSyncService`，覆盖 `createPost`（空参数 / 成功 / 插入失败 / ES 同步失败不影响）、`updatePost`（校验 / 帖子不存在 / 非作者 / 成功并保持原浏览点赞数）、`deletePost`（校验 / 帖子不存在 / 非作者 / 成功）、`getPostDetail`（空ID / 不存在 / 成功并自增浏览数）、`getPostList`（分页默认值 / 越界回退）、计数自增方法（空ID / 异常被吞）。
- **修改文件**：`src/test/java/com/novaforum/nova_forum/service/impl/PostServiceImplTest.java`（新增）
- **测试结果**：22 个测试全部通过；PostServiceImpl 行覆盖约 88%。
- **备注**：
  - 发现既有 bug（未修复，遵守"不改变现有功能"约束）：`PostServiceImpl.updatePost/deletePost/getPostDetail` 把 `IllegalArgumentException`/`SecurityException` 在 `try` 块内抛出并被外层 `catch (Exception)` 包装成通用 `RuntimeException`，导致 `PostController` 中对应的 `catch (IllegalArgumentException)`(400) 与 `catch (SecurityException)`(403) 成为死代码——非法请求返回 500 而非预期的 400/403。已在测试中以"拒绝行为"断言记录该现状，供后续迭代修复。
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
