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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Usage_Record 实体类记录每个房间的使用情况，包括使用时间、费用和温度变化。
 */
@Data
@TableName("usage_record")
public class UsageRecord {
    @TableId(type = IdType.AUTO)
    private Integer recordId;  // 使用记录的唯一标识符
    private Integer roomId;  // 关联的房间编号
    private LocalDateTime startTime;  // 使用开始的时间
    private LocalDateTime endTime;  // 使用结束的时间
    private Float totalEnergyConsumed;  // 总能量消耗量
    private Float cost;  // 该次使用的计费金额
}
