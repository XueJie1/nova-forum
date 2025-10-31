package com.novaforum.nova_forum.service.impl;

import org.springframework.stereotype.Service;

import com.novaforum.nova_forum.entity.User;
import com.novaforum.nova_forum.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public String register(User user) {
        return "register";
    }
}
