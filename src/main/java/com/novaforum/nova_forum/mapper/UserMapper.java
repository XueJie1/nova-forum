package com.novaforum.nova_forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.novaforum.nova_forum.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     */
    int existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    int existsByEmail(@Param("email") String email);
}
