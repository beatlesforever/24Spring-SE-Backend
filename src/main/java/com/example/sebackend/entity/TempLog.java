package com.example.sebackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TempLog {
    private Float actualTemp;  // 实际温度
    private Float endTemp;  // 结束温度
    private String requestedFanSpeed;  // 请求的风速 ('high', 'medium', 'low')
    private LocalDateTime requestTime;  // 请求时间
    private LocalDateTime endTime;  // 调节结束时间，系统完成调节的时间
    private Integer duration;//持续时间
    private Float energyConsumed; //能耗
    private Float cost; //所需费用
}
