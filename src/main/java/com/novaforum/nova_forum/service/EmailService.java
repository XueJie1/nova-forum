package com.novaforum.nova_forum.service;

/**
 * 邮箱服务接口
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     * 
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String email);

    /**
     * 验证邮箱验证码
     * 
     * @param email 邮箱地址
     * @param code  验证码
     * @return 验证结果
     */
    boolean verifyCode(String email, String code);

    /**
     * 检查邮箱是否已验证
     * 
     * @param email 邮箱地址
     * @return 是否已验证
     */
    boolean isEmailVerified(String email);

    /**
     * 生成并缓存验证码
     * 
     * @param email 邮箱地址
     * @return 生成的验证码
     */
    String generateAndCacheCode(String email);
}
