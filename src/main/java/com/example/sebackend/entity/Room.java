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
    private Float currentTemperature;  // 房间当前的温度
    private Float targetTemperature;  // 房间设定的目标温度
    private String fanSpeed;  // 当前风速设置 ('high', 'medium', 'low')
    private Float temperatureThreshold;  // 温度变化阈值，用于自动重启
    private String status;  // 房间的当前状态 ('on', 'off', 'standby')
    private String mode;  // 当前工作模式 ('heating', 'cooling')
    private LocalDateTime lastUpdate;  // 最后一次状态更新时间
    private String serviceStatus;  // 服务状态 ('waiting', 'serving')
    private Float energyConsumed;  // 房间消耗的能量（能量单位）
    private Float costAccumulated;  // 房间累计的费用
    private Integer unitId;  // 关联的中央空调单元ID

}
