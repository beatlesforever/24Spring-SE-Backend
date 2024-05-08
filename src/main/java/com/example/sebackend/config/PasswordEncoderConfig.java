package com.example.sebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Configuration
public class PasswordEncoderConfig {
    /**
     * 创建并返回一个BCryptPasswordEncoder实例。
     * 这个方法没有参数。
     *
     * @return BCryptPasswordEncoder - 一个新的BCryptPasswordEncoder实例，用于加密和验证密码。
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}