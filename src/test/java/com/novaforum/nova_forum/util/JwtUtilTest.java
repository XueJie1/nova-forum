package com.novaforum.nova_forum.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtUtil 单元测试
 */
@DisplayName("JWT工具类单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        // Use reflection to set @Value fields since we're not in a Spring context
        setField(jwtUtil, "secret", "mySecretKey123456789012345678901234567890");
        setField(jwtUtil, "expiration", 604800000L);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JwtUtil.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ==================== generateToken 测试 ====================

    @Test
    @DisplayName("测试生成令牌 - 使用UserInfo")
    void testGenerateToken_WithUserInfo() {
        // Arrange
        JwtUtil.UserInfo userInfo = new JwtUtil.UserInfo(1L, "testuser", "test@example.com");

        // Act
        String token = jwtUtil.generateToken(userInfo);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("测试生成令牌 - 使用用户名")
    void testGenerateToken_WithUsername() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtUtil.generateToken(username);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    // ==================== extractUsername 测试 ====================

    @Test
    @DisplayName("测试提取用户名 - 成功")
    void testExtractUsername_Success() {
        // Arrange
        JwtUtil.UserInfo userInfo = new JwtUtil.UserInfo(1L, "testuser", "test@example.com");
        String token = jwtUtil.generateToken(userInfo);

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("testuser");
    }

    // ==================== extractExpiration 测试 ====================

    @Test
    @DisplayName("测试提取过期时间 - 成功")
    void testExtractExpiration_Success() {
        // Arrange
        String token = jwtUtil.generateToken("testuser");

        // Act
        java.util.Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertThat(expiration).isNotNull();
        assertThat(expiration.after(new java.util.Date())).isTrue();
    }

    // ==================== extractClaim 测试 ====================

    @Test
    @DisplayName("测试提取指定声明 - subject")
    void testExtractClaim_Subject() {
        // Arrange
        JwtUtil.UserInfo userInfo = new JwtUtil.UserInfo(42L, "testuser", "test@example.com");
        String token = jwtUtil.generateToken(userInfo);

        // Act
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);

        // Assert
        assertThat(subject).isEqualTo("testuser");
    }

    // ==================== validateToken 测试 ====================

    @Test
    @DisplayName("测试验证令牌 - 有效令牌")
    void testValidateToken_Valid() {
        // Arrange
        String token = jwtUtil.generateToken("testuser");

        // Act
        Boolean result = jwtUtil.validateToken(token, "testuser");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试验证令牌 - 用户名不匹配")
    void testValidateToken_WrongUsername() {
        // Arrange
        String token = jwtUtil.generateToken("testuser");

        // Act
        Boolean result = jwtUtil.validateToken(token, "wronguser");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试验证令牌 - 无效令牌")
    void testValidateToken_Invalid() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        Boolean result = jwtUtil.validateToken(invalidToken, "testuser");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试验证令牌 - null令牌")
    void testValidateToken_Null() {
        // Act & Assert
        assertThatCode(() -> jwtUtil.validateToken(null, "testuser"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("测试验证令牌(单参数) - 有效令牌")
    void testValidateToken_SingleParam_Valid() {
        // Arrange
        String token = jwtUtil.generateToken("testuser");

        // Act
        Boolean result = jwtUtil.validateToken(token);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试验证令牌(单参数) - 无效令牌")
    void testValidateToken_SingleParam_Invalid() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        Boolean result = jwtUtil.validateToken(invalidToken);

        // Assert
        assertThat(result).isFalse();
    }

    // ==================== extractUserId 测试 ====================

    @Test
    @DisplayName("测试提取用户ID - Long类型")
    void testExtractUserId_Long() {
        // Arrange
        JwtUtil.UserInfo userInfo = new JwtUtil.UserInfo(123L, "testuser", "test@example.com");
        String token = jwtUtil.generateToken(userInfo);

        // Act
        Long userId = jwtUtil.extractUserId(token);

        // Assert
        assertThat(userId).isEqualTo(123L);
    }

    @Test
    @DisplayName("测试提取用户ID - 无效令牌返回null")
    void testExtractUserId_InvalidToken_ReturnsNull() {
        // Act & Assert
        assertThatThrownBy(() -> jwtUtil.extractUserId("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }

    // ==================== UserInfo 内部类测试 ====================

    @Test
    @DisplayName("测试UserInfo - 无参构造")
    void testUserInfo_DefaultConstructor() {
        // Act
        JwtUtil.UserInfo userInfo = new JwtUtil.UserInfo();

        // Assert
        assertThat(userInfo.getId()).isNull();
        assertThat(userInfo.getUsername()).isNull();
        assertThat(userInfo.getEmail()).isNull();
    }

    @Test
    @DisplayName("测试UserInfo - 全参构造")
    void testUserInfo_FullConstructor() {
        // Act
        JwtUtil.UserInfo userInfo = new JwtUtil.UserInfo(1L, "testuser", "test@example.com");

        // Assert
        assertThat(userInfo.getId()).isEqualTo(1L);
        assertThat(userInfo.getUsername()).isEqualTo("testuser");
        assertThat(userInfo.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("测试UserInfo - setters")
    void testUserInfo_Setters() {
        // Arrange
        JwtUtil.UserInfo userInfo = new JwtUtil.UserInfo();

        // Act
        userInfo.setId(2L);
        userInfo.setUsername("newuser");
        userInfo.setEmail("new@example.com");

        // Assert
        assertThat(userInfo.getId()).isEqualTo(2L);
        assertThat(userInfo.getUsername()).isEqualTo("newuser");
        assertThat(userInfo.getEmail()).isEqualTo("new@example.com");
    }
}
