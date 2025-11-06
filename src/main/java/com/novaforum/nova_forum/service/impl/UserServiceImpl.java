package com.novaforum.nova_forum.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.novaforum.nova_forum.entity.User;
import com.novaforum.nova_forum.mapper.UserMapper;
import com.novaforum.nova_forum.service.UserService;
import com.novaforum.nova_forum.util.PasswordUtil;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordUtil passwordUtil;

    @Override
    @Transactional
    public String register(User user) {
        // 参数验证
        if (user == null || !StringUtils.hasText(user.getUsername()) ||
                !StringUtils.hasText(user.getPassword()) || !StringUtils.hasText(user.getEmail())) {
            return "参数不能为空";
        }

        // 检查用户名是否已存在
        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", user.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            return "用户名已存在";
        }

        // 检查邮箱是否已存在
        QueryWrapper<User> emailWrapper = new QueryWrapper<>();
        emailWrapper.eq("email", user.getEmail());
        if (userMapper.selectCount(emailWrapper) > 0) {
            return "邮箱已被注册";
        }

        try {
            // 生成盐值
            String salt = passwordUtil.generateSalt();

            // 加密密码
            String encryptedPassword = passwordUtil.encodePassword(user.getPassword(), salt);

            // 设置用户信息
            user.setSalt(salt);
            user.setPassword(encryptedPassword);
            user.setCreateTime(LocalDateTime.now());

            // 保存用户
            int result = userMapper.insert(user);

            if (result > 0) {
                return "注册成功";
            } else {
                return "注册失败";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "注册异常：" + e.getMessage();
        }
    }

    @Override
    public User findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User findById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectById(id);
    }

    @Override
    public boolean validatePassword(User user, String rawPassword) {
        if (user == null || !StringUtils.hasText(rawPassword)) {
            return false;
        }
        return passwordUtil.matches(rawPassword, user.getPassword(), user.getSalt());
    }
}
