package com.novaforum.nova_forum.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * EmailServiceImpl 单元测试
 *
 * 测试覆盖：
 * - sendVerificationCode 频率限制 / 发送成功 / 发送失败 / 异常
 * - verifyCode 验证码不存在 / 不匹配 / 成功
 * - isEmailVerified 已验证 / 未验证 / 异常
 * - generateAndCacheCode 生成并缓存 / 异常
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("邮箱服务单元测试")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() throws Exception {
        // @Value 注入的 fromEmail 在纯 Mockito 环境中不会填充，手动设置
        Field fromEmailField = EmailServiceImpl.class.getDeclaredField("fromEmail");
        fromEmailField.setAccessible(true);
        fromEmailField.set(emailService, "noreply@novaforum.com");

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    // ==================== sendVerificationCode ====================

    @Test
    @DisplayName("sendVerificationCode - 触发频率限制返回false")
    void testSendVerificationCode_RateLimited() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        assertThat(emailService.sendVerificationCode("user@example.com")).isFalse();
        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("sendVerificationCode - 发送成功")
    void testSendVerificationCode_Success() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        assertThat(emailService.sendVerificationCode("user@example.com")).isTrue();
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        // 成功后设置发送频率限制（redis 写操作由 generateAndCacheCode 与 setSendRateLimit 各一次）
        verify(valueOps, atLeast(2)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("sendVerificationCode - 邮件发送失败返回false")
    void testSendVerificationCode_SendFailed() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        doThrow(new RuntimeException("SMTP 错误")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThat(emailService.sendVerificationCode("user@example.com")).isFalse();
    }

    @Test
    @DisplayName("sendVerificationCode - 异常返回false")
    void testSendVerificationCode_Exception() {
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis 错误"));

        assertThat(emailService.sendVerificationCode("user@example.com")).isFalse();
    }

    // ==================== verifyCode ====================

    @Test
    @DisplayName("verifyCode - 验证码不存在返回false")
    void testVerifyCode_NotFound() {
        when(valueOps.get(anyString())).thenReturn(null);

        assertThat(emailService.verifyCode("user@example.com", "123456")).isFalse();
        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("verifyCode - 验证码不匹配返回false")
    void testVerifyCode_Mismatch() {
        when(valueOps.get(anyString())).thenReturn("654321");

        assertThat(emailService.verifyCode("user@example.com", "123456")).isFalse();
    }

    @Test
    @DisplayName("verifyCode - 验证码正确：删除验证码并标记已验证")
    void testVerifyCode_Success() {
        when(valueOps.get(anyString())).thenReturn("123456");
        doReturn(true).when(redisTemplate).delete(anyString());
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        assertThat(emailService.verifyCode("user@example.com", "123456")).isTrue();
        verify(redisTemplate, times(1)).delete(anyString());
        // 标记邮箱已验证（30天）
        verify(valueOps, times(1)).set(anyString(), eq(true), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("verifyCode - 异常返回false")
    void testVerifyCode_Exception() {
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("Redis 错误"));

        assertThat(emailService.verifyCode("user@example.com", "123456")).isFalse();
    }

    // ==================== isEmailVerified ====================

    @Test
    @DisplayName("isEmailVerified - 已验证返回true")
    void testIsEmailVerified_True() {
        when(valueOps.get(anyString())).thenReturn(true);

        assertThat(emailService.isEmailVerified("user@example.com")).isTrue();
    }

    @Test
    @DisplayName("isEmailVerified - 未验证返回false")
    void testIsEmailVerified_False() {
        when(valueOps.get(anyString())).thenReturn(false);

        assertThat(emailService.isEmailVerified("user@example.com")).isFalse();
    }

    @Test
    @DisplayName("isEmailVerified - Redis 返回null视为未验证")
    void testIsEmailVerified_Null() {
        when(valueOps.get(anyString())).thenReturn(null);

        assertThat(emailService.isEmailVerified("user@example.com")).isFalse();
    }

    @Test
    @DisplayName("isEmailVerified - 异常返回false")
    void testIsEmailVerified_Exception() {
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("Redis 错误"));

        assertThat(emailService.isEmailVerified("user@example.com")).isFalse();
    }

    // ==================== generateAndCacheCode ====================

    @Test
    @DisplayName("generateAndCacheCode - 生成6位验证码并缓存")
    void testGenerateAndCacheCode_Success() {
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        String code = emailService.generateAndCacheCode("user@example.com");

        assertThat(code).isNotNull();
        assertThat(code).matches("\\d{6}");
        verify(valueOps, times(1)).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("generateAndCacheCode - 异常返回null")
    void testGenerateAndCacheCode_Exception() {
        doThrow(new RuntimeException("Redis 错误")).when(valueOps).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        assertThat(emailService.generateAndCacheCode("user@example.com")).isNull();
    }
}
