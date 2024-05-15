package com.example.sebackend.config;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
@Slf4j
public class JwtUtil {
    private static final String SECRET_KEY = "can_you_guess_key";

    /**
     * 生成用于认证的JWT Token。
     *
     * @param username 用户名，作为Token的主题。
     * @return 生成的JWT Token字符串。
     */
    public static String generateToken(String username) {
        // 创建JWT Token构建器
        return Jwts.builder()
                // 设置Token的主题为用户名
                .setSubject(username)
                // 设置Token的签发时间
                .setIssuedAt(new Date())
                // 设置Token的过期时间，2小时后过期
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 2))
                // 使用HS256算法和预定义的SECRET_KEY对Token进行签名
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                // 将Token构建为紧凑格式的字符串
                .compact();
    }

    /**
     * 从给定的JWT令牌中提取声明（Claims）。
     *
     * @param token 待解析的JWT令牌字符串。
     * @return 解析后的声明信息。
     */
    public static Claims extractClaims(String token) {
        // 使用JWT解析器并设置签名密钥，然后解析令牌为Claims
//        log.info("token: " + token);
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中提取用户名。
     *
     * @param token 用于提取用户名的令牌，预期为一个经过认证的安全令牌。
     * @return 从令牌中解析出的用户名。
     */
    public static String extractUsername(String token) {
        // 从令牌中提取声明，并返回主题（用户名）
        return extractClaims(token).getSubject();
    }
    /**
     * 判断令牌（token）是否已过期。
     *
     * @param token 待检查的令牌字符串。
     * @return 如果令牌已过期，则返回true；否则返回false。
     */
    public static boolean isTokenExpired(String token) {
        // 从令牌中提取声明，并检查其过期时间是否早于当前时间
        return extractClaims(token).getExpiration().before(new Date());
    }
}

