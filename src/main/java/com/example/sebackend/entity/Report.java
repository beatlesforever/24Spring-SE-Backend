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
 * Report 实体类用于生成不同类型的报告，如日报、周报、月报。
 * 报告可以包括温度控制、能耗、成本等多方面的数据。
 */
@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Integer reportId;  // 报告的唯一标识符
    private String type;  // 报告类型 ('daily', 'weekly', 'monthly')
    private LocalDateTime generationDate;  // 报告生成日期
    private Float totalEnergyConsumed;  // 报告期间总能量消耗
    private Float totalCost;  // 报告期间总费用
    private String details;  // 报告的详细内容
    private String creator;  // 报告生成者
}
