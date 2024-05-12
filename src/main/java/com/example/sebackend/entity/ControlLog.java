package com.example.sebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zhouhaoran
 * @date 2024/5/12
 * @project SE-backend
 */
@Data
@TableName("control_log")
public class ControlLog {
    @TableId(type = IdType.AUTO)
    private Integer logId;  // 日志的唯一标识符
    private Integer roomId;  // 关联的房间编号
    private Float requestedTemp;  // 请求的温度
    private Float actualTemp;  // 实际温度
    private String requestedFanSpeed;  // 请求的风速 ('high', 'medium', 'low')
    private String mode;  // 当前工作模式 ('heating', 'cooling')
    private LocalDateTime requestTime;  // 请求时间
    private LocalDateTime responseTime;  // 响应时间，系统处理请求并开始调节的时间
    private LocalDateTime endTime;  // 调节结束时间，系统完成调节的时间
    private Integer duration;  // 调节持续的时间（秒），可用于后续的能耗计算
}
