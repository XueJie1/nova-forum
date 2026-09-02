package com.novaforum.nova_forum.controller;

import com.novaforum.nova_forum.dto.ApiResponse;
import com.novaforum.nova_forum.dto.LoginRequest;
import com.novaforum.nova_forum.dto.RegisterRequest;
import com.novaforum.nova_forum.entity.User;
import com.novaforum.nova_forum.service.EmailService;
import com.novaforum.nova_forum.service.UserService;
import com.novaforum.nova_forum.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserController 单元测试（纯单元测试）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户控制器单元测试")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    // ==================== 注册接口测试 ====================

    @Test
    @DisplayName("测试注册 - 成功")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setCode("123456");

        when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userService.findByUsername(anyString())).thenReturn(null);
        when(userService.findByEmail(anyString())).thenReturn(null);
        when(userService.register(any(User.class))).thenReturn("注册成功");

        ApiResponse<String> result = userController.register(request);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试注册 - 验证码错误")
    void testRegister_InvalidCode() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setCode("123456");
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(false);

        ApiResponse<String> result = userController.register(request);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试注册 - 用户名已存在")
    void testRegister_UserExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setCode("123456");
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        when(userService.findByUsername(anyString())).thenReturn(existingUser);

        ApiResponse<String> result = userController.register(request);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试注册 - 邮箱已被注册")
    void testRegister_EmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setCode("123456");
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userService.findByUsername(anyString())).thenReturn(null);
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        when(userService.findByEmail(anyString())).thenReturn(existingUser);

        ApiResponse<String> result = userController.register(request);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试注册 - 服务异常")
    void testRegister_ServiceException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setCode("123456");
        when(emailService.verifyCode(anyString(), anyString())).thenThrow(new RuntimeException("服务异常"));

        ApiResponse<String> result = userController.register(request);

        assertThat(result.getCode()).isEqualTo(500);
    }

    // ==================== 登录接口测试 ====================

    @Test
    @DisplayName("测试登录 - 成功")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userService.findByUsername(anyString())).thenReturn(user);
        when(userService.validatePassword(eq(user), anyString())).thenReturn(true);
        JwtUtil.UserInfo jwtUserInfo = new JwtUtil.UserInfo(1L, "testuser", "test@example.com");
        when(jwtUtil.generateToken(any(JwtUtil.UserInfo.class))).thenReturn("test-token");

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.LoginResponse> result = userController.login(request);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试登录 - 用户不存在")
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");
        when(userService.findByUsername(anyString())).thenReturn(null);

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.LoginResponse> result = userController.login(request);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试登录 - 密码错误")
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpass");
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userService.findByUsername(anyString())).thenReturn(user);
        when(userService.validatePassword(eq(user), anyString())).thenReturn(false);

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.LoginResponse> result = userController.login(request);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试登录 - 服务异常")
    void testLogin_ServiceException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        when(userService.findByUsername(anyString())).thenThrow(new RuntimeException("数据库异常"));

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.LoginResponse> result = userController.login(request);

        assertThat(result.getCode()).isEqualTo(500);
    }

    // ==================== 获取用户信息接口测试 ====================

    @Test
    @DisplayName("测试获取用户信息 - 成功")
    void testGetProfile_Success() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        when(userService.findById(1L)).thenReturn(user);

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.UserProfileResponse> result = userController.getProfile("Bearer test-token");

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取用户信息 - 无Authorization头")
    void testGetProfile_NoAuthHeader() {
        @SuppressWarnings("unchecked")
        ApiResponse<UserController.UserProfileResponse> result = userController.getProfile("");

        assertThat(result.getCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试获取用户信息 - 无效Token")
    void testGetProfile_InvalidToken() {
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.UserProfileResponse> result = userController.getProfile("Bearer invalid-token");

        assertThat(result.getCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试获取用户信息 - Token无法提取用户ID")
    void testGetProfile_CannotExtractUserId() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(null);

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.UserProfileResponse> result = userController.getProfile("Bearer test-token");

        assertThat(result.getCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试获取用户信息 - 用户不存在")
    void testGetProfile_UserNotFound() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(null);

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.UserProfileResponse> result = userController.getProfile("Bearer test-token");

        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("测试获取用户信息 - 服务异常")
    void testGetProfile_ServiceException() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(userService.findById(1L)).thenThrow(new RuntimeException("数据库异常"));

        @SuppressWarnings("unchecked")
        ApiResponse<UserController.UserProfileResponse> result = userController.getProfile("Bearer test-token");

        assertThat(result.getCode()).isEqualTo(500);
    }
}
