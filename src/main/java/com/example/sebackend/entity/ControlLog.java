package com.example.sebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
    private Float endTemp;  // 结束温度
    private String requestedFanSpeed;  // 请求的风速 ('high', 'medium', 'low')
    private String mode;  // 当前工作模式 ('heating', 'cooling')
    private LocalDateTime requestTime;  // 请求时间
    private LocalDateTime endTime;  // 调节结束时间，系统完成调节的时间
    private Integer duration;  // 调节持续的时间（秒），可用于后续的能耗计算
    private boolean isCompleted;  // 是否完成

    public ControlLog(Integer roomId, Float requestedTemp, Float actualTemp, String requestedFanSpeed, String mode, LocalDateTime requestTime,  boolean isCompleted) {
        this.roomId = roomId;
        this.requestedTemp = requestedTemp;
        this.actualTemp = actualTemp;
        this.requestedFanSpeed = requestedFanSpeed;
        this.mode = mode;
        this.requestTime = requestTime;
        this.isCompleted = isCompleted;
    }
    //无参构造函数
    public ControlLog() {
    }


    public float getCost(){
        //计算费用
        //一个单位能耗的费用为5元
        float cost = getEnergyConsumed() * 5;
        return cost;
    }

    public float getEnergyConsumed(){
        //计算能耗
        //风速为high时，能耗为1.2倍，medium时为1倍，low时为0.8倍
        //把持续时间转化为分钟
        Float durationMinute = (float) (duration / 60);
        float energyConsumed = 0;
        if (requestedFanSpeed.equals("high")){
            energyConsumed = (float) (durationMinute * 1.2);
        }else if (requestedFanSpeed.equals("medium")){
            energyConsumed = durationMinute;
        }else if (requestedFanSpeed.equals("low")){
            energyConsumed = (float) (durationMinute * 0.8);
        }
        return energyConsumed;
    }
}
