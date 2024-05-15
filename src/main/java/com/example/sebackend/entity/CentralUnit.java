package com.example.sebackend.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */


/**
 * Central_Unit 实体类代表中央空调单元的配置和状态。
 */
@Data
@TableName("central_unit")
public class CentralUnit {
    @TableId(type = IdType.AUTO)
    private Integer unitId;  // 中央空调单元的唯一标识符
    private String mode;  // 工作模式 ('heating', 'cooling')
    private Float defaultTemperature;  // 缺省的工作温度设置
    private Float currentTemperature;  // 当前工作温度
    private Float minTemperature;  // 最低工作温度限制
    private Float maxTemperature;  // 最高工作温度限制
    private String status;  // 中央空调的状态 ('on', 'off', 'standby')
    private Integer capacity;  // 同时处理的最大从控机数量
    private Integer activeUnits;  // 正在服务的从控机数量
    private Integer frequency;  //刷新频率

}

