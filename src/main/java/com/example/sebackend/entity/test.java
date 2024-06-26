package com.example.sebackend.entity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author zhouhaoran
 * @date 2024/6/26
 * @project SE-backend
 */
public class test {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Encoded Password: " + encodedPassword);
    }
}
