package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.mapper.UserMapper;
import com.example.sebackend.service.IUserService;
import org.springframework.stereotype.Service;
import com.example.sebackend.entity.User;
/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService{
    // 如果需要，可以在这里实现更多业务逻辑
}
