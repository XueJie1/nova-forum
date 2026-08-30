# Nightly Report

本项目质量改进的每日记录。每次循环记录一次修改。

---

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
    - `@WebMvcTest` 切片无法加载上下文，因为 `SecurityAutoConfiguration` 会创建 `JwtAuthenticationFilter`，需要 `JwtUtil`（非切片扫描的 `@Component`），导致 `SearchControllerTest` 等 20 个用例返回 404。
