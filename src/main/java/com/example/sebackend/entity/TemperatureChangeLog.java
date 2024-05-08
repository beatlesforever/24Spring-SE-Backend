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

import java.time.LocalDateTime;

/**
 * Temperature_Change_Log 实体类记录房间温度变化的详细信息。
 * 这有助于分析温控系统的响应时间和效率，以及环境温度的变化模式。
 */
@Data
@TableName("temperature_change_log")
public class TemperatureChangeLog {
    @TableId(type = IdType.AUTO)
    private Integer changeId;  // 温度变化记录的唯一标识符
    private Integer roomId;  // 关联的房间编号
    private Float temperatureChange;  // 记录的温度变化
    private LocalDateTime changeTime;  // 记录的时间点
}
