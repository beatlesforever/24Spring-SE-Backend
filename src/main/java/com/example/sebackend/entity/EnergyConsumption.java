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
 * Energy_Consumption 实体类记录房间的能耗数据。
 * 这些数据包括消耗的能量量及其对应的费用，有助于计费和能源管理。
 */
@Data
@TableName("energy_consumption")
public class EnergyConsumption {
    @TableId(type = IdType.AUTO)
    private Integer consumptionId;  // 能耗记录的唯一标识符
    private Integer roomId;  // 关联的房间编号
    private LocalDateTime datetime;  // 记录时间
    private BigDecimal energyUsed;  // 使用的能量量
    private BigDecimal cost;  // 对应的费用
}