package com.novaforum.nova_forum.controller;

import com.novaforum.nova_forum.dto.ApiResponse;
import com.novaforum.nova_forum.dto.EmailCodeRequest;
import com.novaforum.nova_forum.dto.EmailVerifyRequest;
import com.novaforum.nova_forum.dto.EmailVerifyResponse;
import com.novaforum.nova_forum.service.EmailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱验证控制器
 */
@Slf4j
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public ApiResponse<String> sendVerificationCode(@Valid @RequestBody EmailCodeRequest request) {
        try {
            log.info("收到发送验证码请求，邮箱: {}", request.getEmail());

            boolean success = emailService.sendVerificationCode(request.getEmail());

            if (success) {
                return ApiResponse.success("验证码发送成功");
            } else {
                return ApiResponse.error("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("发送验证码异常，邮箱: {}, 异常: {}", request.getEmail(), e.getMessage(), e);
            return ApiResponse.error("发送验证码失败");
        }
    }

    /**
     * 验证邮箱验证码
     */
    @PostMapping("/verify")
    public ApiResponse<EmailVerifyResponse> verifyEmailCode(@Valid @RequestBody EmailVerifyRequest request) {
        try {
            log.info("收到邮箱验证请求，邮箱: {}", request.getEmail());

            boolean isValid = emailService.verifyCode(request.getEmail(), request.getCode());

            if (isValid) {
                return ApiResponse.success(EmailVerifyResponse.success());
            } else {
                return ApiResponse.success(EmailVerifyResponse.failed("验证码错误或已过期"));
            }
        } catch (Exception e) {
            log.error("验证邮箱验证码异常，邮箱: {}, 验证码: {}, 异常: {}",
                    request.getEmail(), request.getCode(), e.getMessage(), e);
            return ApiResponse.success(EmailVerifyResponse.failed("验证过程出现异常"));
        }
    }

    /**
     * 检查邮箱是否已验证
     */
    @PostMapping("/check-verified")
    public ApiResponse<Boolean> checkEmailVerified(@Valid @RequestBody EmailCodeRequest request) {
        try {
            log.info("收到检查邮箱验证状态请求，邮箱: {}", request.getEmail());

            boolean isVerified = emailService.isEmailVerified(request.getEmail());

            return ApiResponse.success(isVerified);
        } catch (Exception e) {
            log.error("检查邮箱验证状态异常，邮箱: {}, 异常: {}", request.getEmail(), e.getMessage(), e);
            return ApiResponse.error("检查验证状态失败");
        }
    }
}
