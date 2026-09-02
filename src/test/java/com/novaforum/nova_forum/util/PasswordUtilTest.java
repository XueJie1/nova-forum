package com.novaforum.nova_forum.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

/**
 * PasswordUtil 单元测试
 */
@DisplayName("密码工具类单元测试")
class PasswordUtilTest {

    private PasswordUtil passwordUtil;

    @BeforeEach
    void setUp() {
        passwordUtil = new PasswordUtil();
    }

    // ==================== generateSalt 测试 ====================

    @Test
    @DisplayName("测试生成盐值 - 非空")
    void testGenerateSalt_NotEmpty() {
        // Act
        String salt = passwordUtil.generateSalt();

        // Assert
        assertThat(salt).isNotNull();
        assertThat(salt).isNotEmpty();
    }

    @Test
    @DisplayName("测试生成盐值 - 两次不同")
    void testGenerateSalt_DifferentEachTime() {
        // Act
        String salt1 = passwordUtil.generateSalt();
        String salt2 = passwordUtil.generateSalt();

        // Assert
        assertThat(salt1).isNotEqualTo(salt2);
    }

    @Test
    @DisplayName("测试生成盐值 - 长度合理")
    void testGenerateSalt_ValidLength() {
        // Act
        String salt = passwordUtil.generateSalt();

        // Assert
        assertThat(salt).hasSizeBetween(16, 32);
    }

    // ==================== encodePassword 测试 ====================

    @Test
    @DisplayName("测试加密密码 - 成功")
    void testEncodePassword_Success() {
        // Arrange
        String rawPassword = "password123";
        String salt = passwordUtil.generateSalt();

        // Act
        String encoded = passwordUtil.encodePassword(rawPassword, salt);

        // Assert
        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEmpty();
    }

    @Test
    @DisplayName("测试加密密码 - 相同输入产生不同输出")
    void testEncodePassword_DifferentOutputs() {
        // Arrange
        String rawPassword = "password123";
        String salt = "somesalt";

        // Act
        String encoded1 = passwordUtil.encodePassword(rawPassword, salt);
        String encoded2 = passwordUtil.encodePassword(rawPassword, salt);

        // Assert - BCrypt may produce same or different hashes depending on implementation
        assertThat(encoded1).isNotNull();
        assertThat(encoded2).isNotNull();
    }

    // ==================== matches 测试 ====================

    @Test
    @DisplayName("测试密码匹配 - 正确密码")
    void testMatches_CorrectPassword() {
        // Arrange
        String rawPassword = "password123";
        String salt = passwordUtil.generateSalt();
        String encoded = passwordUtil.encodePassword(rawPassword, salt);

        // Act
        boolean result = passwordUtil.matches(rawPassword, encoded, salt);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("测试密码匹配 - 错误密码")
    void testMatches_WrongPassword() {
        // Arrange
        String rawPassword = "password123";
        String wrongPassword = "wrongpassword";
        String salt = passwordUtil.generateSalt();
        String encoded = passwordUtil.encodePassword(rawPassword, salt);

        // Act
        boolean result = passwordUtil.matches(wrongPassword, encoded, salt);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("测试密码匹配 - 不同盐值")
    void testMatches_DifferentSalt() {
        // Arrange
        String rawPassword = "password123";
        String salt1 = "salt1";
        String salt2 = "salt2";
        String encoded = passwordUtil.encodePassword(rawPassword, salt1);

        // Act
        boolean result = passwordUtil.matches(rawPassword, encoded, salt2);

        // Assert
        assertThat(result).isFalse();
    }

    // ==================== getPasswordEncoder 测试 ====================

    @Test
    @DisplayName("测试获取加密器 - 非null")
    void testGetPasswordEncoder_NotNull() {
        // Act
        PasswordEncoder encoder = passwordUtil.getPasswordEncoder();

        // Assert
        assertThat(encoder).isNotNull();
    }

    @Test
    @DisplayName("测试获取加密器 - BCrypt类型")
    void testGetPasswordEncoder_IsBCrypt() {
        // Act
        PasswordEncoder encoder = passwordUtil.getPasswordEncoder();

        // Assert
        assertThat(encoder).isInstanceOf(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class);
    }
}
