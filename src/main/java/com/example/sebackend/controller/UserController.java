package com.example.sebackend.controller;

import com.example.sebackend.config.JwtUtil;
import com.example.sebackend.entity.User;
import com.example.sebackend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private IUserService userService;

    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User loginuser) {
        // 尝试通过提供的用户名、密码进行用户登录
        User user = userService.login(loginuser.getUsername(), loginuser.getPassword());
        if (user != null) {
            // 验证房间号是否匹配
            if (user.getRoomId().equals(loginuser.getRoomId())) {
                // 登录成功，生成并返回JWT令牌
                String token = JwtUtil.generateToken(user.getUsername());
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getUserId());
                data.put("username", user.getUsername());
                data.put("roomId", user.getRoomId());
                data.put("role", user.getRole());
                data.put("token", token);

                return createResponse(HttpStatus.OK, "登录成功", data);
            } else {
                // 房间号不匹配，返回未授权
                return createResponse(HttpStatus.UNAUTHORIZED, "房间号不匹配", null);
            }
        } else {
            // 登录失败，返回未授权
            return createResponse(HttpStatus.UNAUTHORIZED, "用户名或密码错误", null);
        }
    }

    /**
     * 处理用户注册请求。
     *
     * @param user 包含用户注册信息的实体，通过请求体传入。
     * @return 根据注册结果返回不同的响应实体，包括状态码、消息和数据部分。
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        // 检查房间是否已经被占用
        User existingUser = userService.getUserByRoomId(user.getRoomId());
        if (existingUser != null) {
            // 房间已被占用，返回错误状态码和相应的消息
            return createResponse(HttpStatus.BAD_REQUEST, "注册失败，该房间已被占用", null);
        }

        // 尝试注册用户
        boolean success = userService.register(user);
        if (success) {
            // 注册成功，返回创建状态码和相应的消息
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId()); // 假设ID是自动生成的
            data.put("username", user.getUsername());
            data.put("roomId",user.getRoomId());
            data.put("role", user.getRole());
            return createResponse(HttpStatus.OK, "用户注册成功", data);
        } else {
            // 注册失败，返回错误状态码和相应的消息
            return createResponse(HttpStatus.BAD_REQUEST, "注册失败，用户名可能已存在", null);
        }
    }

}
