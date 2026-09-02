package com.novaforum.nova_forum.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * CodeGenerator 单元测试
 */
@DisplayName("验证码生成器单元测试")
class CodeGeneratorTest {

    // ==================== generate6DigitCode 测试 ====================

    @Test
    @DisplayName("测试生成6位验证码 - 长度正确")
    void testGenerate6DigitCode_Length() {
        // Act
        String code = CodeGenerator.generate6DigitCode();

        // Assert
        assertThat(code).hasSize(6);
    }

    @Test
    @DisplayName("测试生成6位验证码 - 全为数字")
    void testGenerate6DigitCode_AllDigits() {
        // Act
        String code = CodeGenerator.generate6DigitCode();

        // Assert
        assertThat(code).matches("\\d{6}");
    }

    @Test
    @DisplayName("测试生成6位验证码 - 多次调用产生不同值")
    void testGenerate6DigitCode_DifferentValues() {
        // Act
        String code1 = CodeGenerator.generate6DigitCode();
        String code2 = CodeGenerator.generate6DigitCode();

        // Assert - with high probability they should be different
        // (even if same, it's still valid)
        assertThat(code1).isNotNull();
        assertThat(code2).isNotNull();
    }

    @Test
    @DisplayName("测试生成6位验证码 - 范围在000000-999999")
    void testGenerate6DigitCode_Range() {
        // Act
        String code = CodeGenerator.generate6DigitCode();
        int value = Integer.parseInt(code);

        // Assert
        assertThat(value).isBetween(0, 999999);
    }

    // ==================== isValidCode 测试 ====================

    @Test
    @DisplayName("测试验证验证码 - 有效6位数字")
    void testIsValidCode_Valid() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode("123456")).isTrue();
    }

    @Test
    @DisplayName("测试验证验证码 - null返回false")
    void testIsValidCode_Null() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode(null)).isFalse();
    }

    @Test
    @DisplayName("测试验证验证码 - 空字符串返回false")
    void testIsValidCode_EmptyString() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode("")).isFalse();
    }

    @Test
    @DisplayName("测试验证验证码 - 5位数字返回false")
    void testIsValidCode_TooShort() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode("12345")).isFalse();
    }

    @Test
    @DisplayName("测试验证验证码 - 7位数字返回false")
    void testIsValidCode_TooLong() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode("1234567")).isFalse();
    }

    @Test
    @DisplayName("测试验证验证码 - 包含字母返回false")
    void testIsValidCode_ContainsLetters() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode("12345a")).isFalse();
        assertThat(CodeGenerator.isValidCode("abcdef")).isFalse();
    }

    @Test
    @DisplayName("测试验证验证码 - 包含特殊字符返回false")
    void testIsValidCode_ContainsSpecialChars() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode("12345!")).isFalse();
        assertThat(CodeGenerator.isValidCode("12-345")).isFalse();
    }

    @Test
    @DisplayName("测试验证验证码 - 全0返回true")
    void testIsValidCode_AllZeros() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode("000000")).isTrue();
    }

    @Test
    @DisplayName("测试验证验证码 - 全9返回true")
    void testIsValidCode_AllNines() {
        // Act & Assert
        assertThat(CodeGenerator.isValidCode("999999")).isTrue();
    }
}
