package com.novaforum.nova_forum.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.novaforum.nova_forum.dto.*;
import com.novaforum.nova_forum.entity.User;
import com.novaforum.nova_forum.service.UserService;
import com.novaforum.nova_forum.util.JwtUtil;

import jakarta.validation.Valid;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 转换DTO为实体
            User user = new User();
            BeanUtils.copyProperties(request, user);

            // 调用注册服务
            String result = userService.register(user);

            if ("注册成功".equals(result)) {
                return ApiResponse.success("注册成功");
            } else {
                return ApiResponse.error(400, result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("注册异常：" + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 查找用户
            User user = userService.findByUsername(request.getUsername());

            if (user == null) {
                return ApiResponse.error(400, "用户名不存在");
            }

            // 验证密码
            if (!userService.validatePassword(user, request.getPassword())) {
                return ApiResponse.error(400, "密码错误");
            }

            // 生成JWT令牌
            JwtUtil.UserInfo userInfo = new JwtUtil.UserInfo(user.getId(), user.getUsername(), user.getEmail());
            String token = jwtUtil.generateToken(userInfo);

            // 返回响应
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setToken(token);

            return ApiResponse.success("登录成功", response);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("登录异常：" + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            // 解析JWT令牌
            if (!authHeader.startsWith("Bearer ")) {
                return ApiResponse.error(401, "无效的授权头");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ApiResponse.error(401, "令牌无效或已过期");
            }

            // 获取用户ID
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) {
                return ApiResponse.error(401, "无法从令牌中获取用户信息");
            }

            // 这里应该根据ID查询用户信息，暂时简化处理
            String username = jwtUtil.extractUsername(token);

            UserProfileResponse response = new UserProfileResponse();
            response.setUserId(userId);
            response.setUsername(username);
            // response.setEmail(email); // 需要从数据库查询

            return ApiResponse.success(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取用户信息异常：" + e.getMessage());
        }
    }

    /**
     * 登录响应类
     */
    public static class LoginResponse {
        private Long userId;
        private String username;
        private String email;
        private String token;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * 用户资料响应类
     */
    public static class UserProfileResponse {
        private Long userId;
        private String username;
        private String email;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
