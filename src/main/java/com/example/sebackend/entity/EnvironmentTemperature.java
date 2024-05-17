package com.example.sebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhouhaoran
 * @date 2024/5/16
 * @project SE-backend
 */
@Data
@TableName("environment_temperature")
public class EnvironmentTemperature {
    @TableId(type = IdType.AUTO)
    private Integer id;  // 环境温度记录的唯一标识符
    private Float temperature;  // 当前环境温度
    private LocalDateTime timestamp;  // 温度记录的时间戳
}