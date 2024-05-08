package com.example.sebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;  // 用户的唯一标识符
    private String username;  // 用户名
    private String password;  // 加密存储的用户密码
    private Integer roomId;  // 关联的房间编号
}
