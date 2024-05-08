package com.example.sebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户实体类，包含用户的身份信息和关联的房间编号。
 * @author zhouhaoran
 * @date 2024/05/08
 * @project SE-backend
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;  // 用户的唯一标识符
    private String username;  // 用户名
    private String password;  // 用户身份证号
    private Integer roomId;  // 关联的房间编号
    private String role; //用户的角色
}
