package com.novaforum.nova_forum.controller;

import com.novaforum.nova_forum.dto.ApiResponse;
import com.novaforum.nova_forum.dto.EmailCodeRequest;
import com.novaforum.nova_forum.dto.EmailVerifyRequest;
import com.novaforum.nova_forum.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * EmailController 单元测试（纯单元测试）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("邮箱控制器单元测试")
class EmailControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    // ==================== 发送验证码接口测试 ====================

    @Test
    @DisplayName("测试发送验证码 - 成功")
    void testSendVerificationCode_Success() {
        when(emailService.sendVerificationCode(anyString())).thenReturn(true);

        EmailCodeRequest request = new EmailCodeRequest();
        request.setEmail("test@example.com");

        ApiResponse<String> result = emailController.sendVerificationCode(request);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试发送验证码 - 失败")
    void testSendVerificationCode_Failure() {
        when(emailService.sendVerificationCode(anyString())).thenReturn(false);

        EmailCodeRequest request = new EmailCodeRequest();
        request.setEmail("test@example.com");

        ApiResponse<String> result = emailController.sendVerificationCode(request);

        assertThat(result.getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("测试发送验证码 - 服务异常")
    void testSendVerificationCode_Exception() {
        when(emailService.sendVerificationCode(anyString())).thenThrow(new RuntimeException("SMTP异常"));

        EmailCodeRequest request = new EmailCodeRequest();
        request.setEmail("test@example.com");

        ApiResponse<String> result = emailController.sendVerificationCode(request);

        assertThat(result.getCode()).isEqualTo(500);
    }

    // ==================== 验证邮箱验证码接口测试 ====================

    @Test
    @DisplayName("测试验证邮箱验证码 - 成功")
    void testVerifyEmailCode_Success() {
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);

        EmailVerifyRequest request = new EmailVerifyRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");

        ApiResponse<com.novaforum.nova_forum.dto.EmailVerifyResponse> result = emailController.verifyEmailCode(request);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试验证邮箱验证码 - 验证码错误")
    void testVerifyEmailCode_InvalidCode() {
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(false);

        EmailVerifyRequest request = new EmailVerifyRequest();
        request.setEmail("test@example.com");
        request.setCode("654321");

        ApiResponse<com.novaforum.nova_forum.dto.EmailVerifyResponse> result = emailController.verifyEmailCode(request);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试验证邮箱验证码 - 服务异常")
    void testVerifyEmailCode_Exception() {
        when(emailService.verifyCode(anyString(), anyString())).thenThrow(new RuntimeException("Redis异常"));

        EmailVerifyRequest request = new EmailVerifyRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");

        ApiResponse<com.novaforum.nova_forum.dto.EmailVerifyResponse> result = emailController.verifyEmailCode(request);

        assertThat(result.getCode()).isEqualTo(200);
    }

    // ==================== 检查邮箱验证状态接口测试 ====================

    @Test
    @DisplayName("测试检查邮箱验证状态 - 已验证")
    void testCheckEmailVerified_Verified() {
        when(emailService.isEmailVerified(anyString())).thenReturn(true);

        EmailCodeRequest request = new EmailCodeRequest();
        request.setEmail("test@example.com");

        ApiResponse<Boolean> result = emailController.checkEmailVerified(request);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试检查邮箱验证状态 - 未验证")
    void testCheckEmailVerified_NotVerified() {
        when(emailService.isEmailVerified(anyString())).thenReturn(false);

        EmailCodeRequest request = new EmailCodeRequest();
        request.setEmail("test@example.com");

        ApiResponse<Boolean> result = emailController.checkEmailVerified(request);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试检查邮箱验证状态 - 服务异常")
    void testCheckEmailVerified_Exception() {
        when(emailService.isEmailVerified(anyString())).thenThrow(new RuntimeException("Redis异常"));

        EmailCodeRequest request = new EmailCodeRequest();
        request.setEmail("test@example.com");

        ApiResponse<Boolean> result = emailController.checkEmailVerified(request);

        assertThat(result.getCode()).isEqualTo(500);
    }
}
