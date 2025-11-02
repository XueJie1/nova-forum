package com.novaforum.nova_forum.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类
 */
@Component
public class PasswordUtil {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 生成随机盐值
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 加密密码
     */
    public String encodePassword(String rawPassword, String salt) {
        // 将盐值和密码组合进行加密
        String saltedPassword = salt + rawPassword;
        return passwordEncoder.encode(saltedPassword);
    }

    /**
     * 验证密码
     */
    public boolean matches(String rawPassword, String encodedPassword, String salt) {
        String saltedPassword = salt + rawPassword;
        return passwordEncoder.matches(saltedPassword, encodedPassword);
    }

    /**
     * 获取加密器
     */
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
