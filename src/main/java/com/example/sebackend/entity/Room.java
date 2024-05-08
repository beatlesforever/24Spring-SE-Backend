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
/**
 * Room 实体类表示每个房间的详细信息。
 * 包括房间的温度控制数据以及其与中央空调的连接状态。
 */
@Data
@TableName("room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Integer roomId;  // 房间的唯一标识符
    private Float currentTemperature;  // 房间当前的实际温度
    private Float targetTemperature;  // 房间设定的目标温度
    private Float minTemperature;  // 温度下限，用于自动控制重新启动温控机制
    private Float maxTemperature;  // 温度上限，用于自动控制重新启动温控机制
    private Float temperatureThreshold;  // 温度变化阈值，用于判断是否触发重启或调整
    private String status;  // 房间的当前状态（'on', 'off', 'standby'）
    private String mode;  // 当前的工作模式（'heating' or 'cooling'）
    private LocalDateTime lastUpdate;  // 最后一次状态更新时间
    private Boolean connected;  // 表示房间是否与中央空调系统连接
}
