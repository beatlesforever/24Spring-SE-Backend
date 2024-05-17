package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
public interface IUserService extends IService<User> {
    User login(String username, String password);

    boolean register(User user);

    UserDetails loadUserByUsername(String username);

    User getUserByRoomId(Integer roomId);
}
