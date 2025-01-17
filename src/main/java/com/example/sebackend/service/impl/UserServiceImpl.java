package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.mapper.UserMapper;
import com.example.sebackend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.sebackend.entity.User;
/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService{
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(BCryptPasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;

    }
    private User findByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
    /**
     * 注册一个新的用户。
     *
     * @param user 用户对象，包含用户名和密码等信息。
     * @return boolean 如果用户成功注册返回true，如果用户已存在则返回false。
     */
    public User register(User user) {
        // 根据用户名查找已存在的用户
        User existingUser = findByUsername(user.getUsername());
        if (existingUser == null) {
            // 如果不存在相同用户名的用户，则对密码进行编码并保存新用户
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setRole("user");
            userMapper.insert(user);
            return user; // 返回已注册的用户对象

        }
        // 如果已存在相同用户名的用户，则不进行注册，返回null
        return null;
    }

    /**
     * 用户登录验证。
     *
     * @param username 用户名。
     * @param password 密码。
     * @return 如果用户名和密码匹配，则返回对应的User对象；否则返回null。
     */
    public User login(String username, String password) {
        // 通过用户名查询用户
        User user = findByUsername(username);

        // 验证密码是否匹配
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        // 密码不匹配或用户不存在，返回null
        return null;
    }
    /**
     * 根据用户名加载用户详情。
     *
     * @param username 用户名。
     * @return UserDetails 用户详情对象，包含了用户的认证信息。
     * @throws UsernameNotFoundException 如果根据给定的用户名找不到用户，则抛出此异常。
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 通过用户名查找用户
        User user = findByUsername(username);
        // 如果用户不存在，则抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("用户名为 " + username + " 的用户不存在");
        }
        // 构建并返回一个Spring Security的UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole()) // 基于应用需求可能需要调整角色信息
                .build();
    }

    /**
     * 根据房间ID获取用户信息。
     *
     * @param roomId 房间ID，用于查询与之关联的用户信息。
     * @return 返回查询到的用户对象。如果没有找到匹配的用户，则返回null。
     */
    @Override
    public User getUserByRoomId(Integer roomId) {
        // 创建查询包装器并设置查询条件为房间ID等于传入的roomId
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        // 根据查询条件从数据库中查询一个用户并返回
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 修改用户密码的方法。
     *
     * @param username 用户名。
     * @param newPassword 新密码。
     * @return 如果密码成功修改则返回true，否则返回false。
     */
    public boolean changePassword(String username, String newPassword) {
        User user = findByUsername(username);
        if (user != null) {
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
            userMapper.updateById(user);
            return true;
        }
        return false;
    }
}
