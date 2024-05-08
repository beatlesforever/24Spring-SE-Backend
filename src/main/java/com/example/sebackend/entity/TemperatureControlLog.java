package com.example.sebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Data
@TableName("temperature_control_log")
public class TemperatureControlLog {
    @TableId(type = IdType.AUTO)
    private Integer logId;  // 温度控制日志的唯一标识符
    private Integer roomId;  // 关联的房间编号
    private Float requestedTemp;  // 请求的温度
    private Float actualTemp;  // 实际达到的温度
    private LocalDateTime requestTime;  // 请求时间
    private LocalDateTime responseTime;  // 响应时间
    private String actionTaken;  // 采取的行动，如 'cooling on', 'heating on', 'standby'
}
