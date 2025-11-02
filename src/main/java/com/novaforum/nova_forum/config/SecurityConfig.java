package com.novaforum.nova_forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF，因为我们现在使用JWT
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用会话管理，因为使用JWT无状态认证
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权
                .authorizeHttpRequests(authz -> authz
                        // 允许用户注册接口不需要认证
                        .requestMatchers("/user/register", "/user/login").permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated());

        return http.build();
    }
}
