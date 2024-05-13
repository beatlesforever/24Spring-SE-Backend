package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.UsageRecord;

import java.time.LocalDateTime;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
public interface IUsageRecordService extends IService<UsageRecord> {
    // 通过房间号获取在时间范围内的使用记录次数
    int getUsageRecordCount(int roomId, LocalDateTime startTime, LocalDateTime endTime);
}
