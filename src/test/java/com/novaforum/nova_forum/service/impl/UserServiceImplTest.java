package com.novaforum.nova_forum.service.impl;

import com.novaforum.nova_forum.entity.User;
import com.novaforum.nova_forum.mapper.UserMapper;
import com.novaforum.nova_forum.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试
 *
 * 测试覆盖：
 * - register 参数校验（用户名/密码/邮箱为空）
 * - register 用户名/邮箱重复
 * - register 成功（盐值、加密密码、创建时间设置）
 * - register 插入失败与异常
 * - findByUsername / findByEmail / findById 的空值与查不到
 * - validatePassword 校验
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("alice");
        testUser.setPassword("secret");
        testUser.setEmail("alice@example.com");
    }

    // ==================== register 参数校验 ====================

    @Test
    @DisplayName("register - 用户为空返回参数为空")
    void testRegister_NullUser_ReturnsParamEmpty() {
        assertThat(userService.register(null)).isEqualTo("参数不能为空");
        verifyNoInteractions(userMapper);
        verifyNoInteractions(passwordUtil);
    }

    @Test
    @DisplayName("register - 必填字段为空返回参数为空")
    void testRegister_MissingFields_ReturnsParamEmpty() {
        User empty = new User();
        assertThat(userService.register(empty)).isEqualTo("参数不能为空");

        User onlyUsername = new User();
        onlyUsername.setUsername("bob");
        assertThat(userService.register(onlyUsername)).isEqualTo("参数不能为空");
    }

    // ==================== register 重复校验 ====================

    @Test
    @DisplayName("register - 用户名已存在")
    void testRegister_UsernameExists() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        assertThat(userService.register(testUser)).isEqualTo("用户名已存在");
        verify(userMapper, times(1)).selectCount(any(QueryWrapper.class));
        verify(passwordUtil, never()).encodePassword(anyString(), anyString());
    }

    @Test
    @DisplayName("register - 邮箱已被注册")
    void testRegister_EmailExists() {
        // 用户名不重复，邮箱重复
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L).thenReturn(1L);

        assertThat(userService.register(testUser)).isEqualTo("邮箱已被注册");
        verify(userMapper, times(2)).selectCount(any(QueryWrapper.class));
        verify(passwordUtil, never()).encodePassword(anyString(), anyString());
    }

    // ==================== register 成功 ====================

    @Test
    @DisplayName("register - 注册成功并设置盐值与加密密码")
    void testRegister_Success_SetsSaltAndEncodedPassword() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordUtil.generateSalt()).thenReturn("salt123");
        when(passwordUtil.encodePassword(anyString(), anyString())).thenReturn("encodedpw");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        String result = userService.register(testUser);

        assertThat(result).isEqualTo("注册成功");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getSalt()).isEqualTo("salt123");
        assertThat(saved.getPassword()).isEqualTo("encodedpw");
        assertThat(saved.getCreateTime()).isNotNull();
    }

    @Test
    @DisplayName("register - 插入返回0返回注册失败")
    void testRegister_InsertFails_ReturnsRegisterFailed() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(0);

        assertThat(userService.register(testUser)).isEqualTo("注册失败");
    }

    @Test
    @DisplayName("register - 加密抛异常返回注册异常")
    void testRegister_EncodeThrows_ReturnsException() {
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(passwordUtil.generateSalt()).thenReturn("salt");
        when(passwordUtil.encodePassword(anyString(), anyString()))
                .thenThrow(new RuntimeException("加密失败"));

        String result = userService.register(testUser);

        assertThat(result).startsWith("注册异常：");
        assertThat(result).contains("加密失败");
    }

    // ==================== 查找方法 ====================

    @Test
    @DisplayName("findByUsername - 空用户名返回null")
    void testFindByUsername_Empty_ReturnsNull() {
        assertThat(userService.findByUsername("")).isNull();
        assertThat(userService.findByUsername("   ")).isNull();
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("findByUsername - 查询用户")
    void testFindByUsername_Found() {
        User found = new User();
        found.setUsername("alice");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(found);

        assertThat(userService.findByUsername("alice")).isSameAs(found);
        verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("findByUsername - 未找到返回null")
    void testFindByUsername_NotFound() {
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        assertThat(userService.findByUsername("noone")).isNull();
    }

    @Test
    @DisplayName("findByEmail - 空邮箱返回null")
    void testFindByEmail_Empty_ReturnsNull() {
        assertThat(userService.findByEmail("")).isNull();
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("findByEmail - 查询用户")
    void testFindByEmail_Found() {
        User found = new User();
        found.setEmail("alice@example.com");
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(found);

        assertThat(userService.findByEmail("alice@example.com")).isSameAs(found);
    }

    @Test
    @DisplayName("findById - 空ID返回null")
    void testFindById_Null_ReturnsNull() {
        assertThat(userService.findById(null)).isNull();
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("findById - 查询用户")
    void testFindById_Found() {
        User found = new User();
        found.setId(7L);
        when(userMapper.selectById(7L)).thenReturn(found);

        assertThat(userService.findById(7L)).isSameAs(found);
        verify(userMapper, times(1)).selectById(7L);
    }

    // ==================== validatePassword ====================

    @Test
    @DisplayName("validatePassword - 空参数返回false")
    void testValidatePassword_Empty_ReturnsFalse() {
        assertThat(userService.validatePassword(null, "pw")).isFalse();
        assertThat(userService.validatePassword(testUser, "")).isFalse();
        assertThat(userService.validatePassword(testUser, "   ")).isFalse();
        verifyNoInteractions(passwordUtil);
    }

    @Test
    @DisplayName("validatePassword - 校验成功与失败")
    void testValidatePassword_MatchesAndNot() {
        User u = new User();
        u.setPassword("encoded");
        u.setSalt("salt");

        when(passwordUtil.matches("correct", "encoded", "salt")).thenReturn(true);
        when(passwordUtil.matches("wrong", "encoded", "salt")).thenReturn(false);

        assertThat(userService.validatePassword(u, "correct")).isTrue();
        assertThat(userService.validatePassword(u, "wrong")).isFalse();
    }
}
