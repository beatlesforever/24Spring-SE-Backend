package com.example.sebackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * 配置HTTP安全设置，用于定制Spring Security的行為。
     *
     * @param http 用于配置HttpSecurity的实例，通过它可定制应用的网络安全设置。
     * @throws Exception 如果在配置过程中发生错误，则可能抛出异常。
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用跨站请求伪造（CSRF）保护
        http.csrf().disable()
                .cors()
                .and()
                // 对静态资源、登录和注册页面的访问进行许可，无需认证即可访问
                .authorizeRequests().antMatchers("/api/users/login", "/api/users/register","/api/rooms/available").permitAll()
                // 对其他所有请求进行认证，即只有经过认证的用户才能访问
                .anyRequest().authenticated()
                .and()
                // 在UsernamePasswordAuthenticationFilter之前添加自定义的JWT请求过滤器
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3555"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}