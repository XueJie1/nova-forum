package com.novaforum.nova_forum.service;

import com.novaforum.nova_forum.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    String register(User user);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);

    /**
     * 验证密码
     */
    boolean validatePassword(User user, String rawPassword);
}
