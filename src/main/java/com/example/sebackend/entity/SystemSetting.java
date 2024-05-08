package com.example.sebackend.entity;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * System_Setting 实体类用于存储系统的配置参数。
 * 这些设置可以影响系统的全局行为，例如温度范围、定时任务等。
 */
@Data
@TableName("system_setting")
public class SystemSetting {
    @TableId(type = IdType.AUTO)
    private Integer settingId;  // 设置的唯一标识符
    private String name;  // 设置项的名称
    private String value;  // 设置项的值
    private String description;  // 设置项的详细描述
}
