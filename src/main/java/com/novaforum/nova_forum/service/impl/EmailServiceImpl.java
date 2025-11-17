package com.novaforum.nova_forum.service.impl;

import com.novaforum.nova_forum.service.EmailService;
import com.novaforum.nova_forum.util.CodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 邮箱服务实现类
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Redis Key前缀
    private static final String VERIFICATION_CODE_PREFIX = "email:verification:";
    private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
    private static final String SEND_RATE_LIMIT_PREFIX = "email:rate_limit:";

    // 验证码有效期（分钟）
    private static final long CODE_EXPIRE_MINUTES = 5;
    // 发送频率限制（秒）
    private static final long SEND_RATE_LIMIT_SECONDS = 60;

    @Override
    public boolean sendVerificationCode(String email) {
        try {
            // 检查发送频率限制
            if (!checkSendRateLimit(email)) {
                log.warn("发送验证码频率过高，邮箱: {}", email);
                return false;
            }

            // 生成验证码
            String code = generateAndCacheCode(email);
            if (code == null) {
                log.error("生成验证码失败，邮箱: {}", email);
                return false;
            }

            // 发送邮件
            boolean sent = sendEmail(email, code);
            if (sent) {
                // 设置发送频率限制
                setSendRateLimit(email);
                log.info("验证码发送成功，邮箱: {}, 验证码: {}", email, code);
            } else {
                log.error("验证码发送失败，邮箱: {}", email);
            }

            return sent;
        } catch (Exception e) {
            log.error("发送验证码异常，邮箱: {}, 异常: {}", email, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        try {
            String redisKey = VERIFICATION_CODE_PREFIX + email;
            String cachedCode = (String) redisTemplate.opsForValue().get(redisKey);

            if (cachedCode == null) {
                log.warn("验证码不存在或已过期，邮箱: {}", email);
                return false;
            }

            if (!cachedCode.equals(code)) {
                log.warn("验证码不匹配，邮箱: {}, 期望: {}, 实际: {}", email, cachedCode, code);
                return false;
            }

            // 验证成功后删除验证码
            redisTemplate.delete(redisKey);

            // 设置邮箱已验证状态
            setEmailVerified(email);

            log.info("邮箱验证成功，邮箱: {}", email);
            return true;
        } catch (Exception e) {
            log.error("验证验证码异常，邮箱: {}, 验证码: {}, 异常: {}", email, code, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isEmailVerified(String email) {
        try {
            String redisKey = EMAIL_VERIFIED_PREFIX + email;
            Boolean verified = (Boolean) redisTemplate.opsForValue().get(redisKey);
            return verified != null && verified;
        } catch (Exception e) {
            log.error("检查邮箱验证状态异常，邮箱: {}, 异常: {}", email, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generateAndCacheCode(String email) {
        try {
            String code = CodeGenerator.generate6DigitCode();
            String redisKey = VERIFICATION_CODE_PREFIX + email;

            // 缓存验证码，设置5分钟过期
            redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            log.info("生成并缓存验证码，邮箱: {}, 验证码: {}", email, code);
            return code;
        } catch (Exception e) {
            log.error("生成并缓存验证码异常，邮箱: {}, 异常: {}", email, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 发送邮件
     */
    private boolean sendEmail(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Nova Forum 邮箱验证");
            message.setText("您好！\n\n" +
                    "欢迎使用Nova Forum！\n\n" +
                    "您的邮箱验证码是：" + code + "\n\n" +
                    "此验证码5分钟内有效，请勿泄露给他人。\n\n" +
                    "如非本人操作，请忽略此邮件。\n\n" +
                    "Nova Forum团队");

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("发送邮件异常，邮箱: {}, 异常: {}", email, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查发送频率限制
     */
    private boolean checkSendRateLimit(String email) {
        String redisKey = SEND_RATE_LIMIT_PREFIX + email;
        return !redisTemplate.hasKey(redisKey);
    }

    /**
     * 设置发送频率限制
     */
    private void setSendRateLimit(String email) {
        String redisKey = SEND_RATE_LIMIT_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, "1", SEND_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 设置邮箱已验证状态
     */
    private void setEmailVerified(String email) {
        String redisKey = EMAIL_VERIFIED_PREFIX + email;
        redisTemplate.opsForValue().set(redisKey, true, 30, TimeUnit.DAYS); // 验证状态缓存30天
    }
}
