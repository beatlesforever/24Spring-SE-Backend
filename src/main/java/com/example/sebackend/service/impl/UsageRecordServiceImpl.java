package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.UsageRecord;
import com.example.sebackend.mapper.UsageRecordMapper;
import com.example.sebackend.service.IUsageRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
@Slf4j
public class UsageRecordServiceImpl extends ServiceImpl<UsageRecordMapper, UsageRecord> implements IUsageRecordService {
    @Override
    public int getUsageRecordCount(int roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<UsageRecord> usageRecords = baseMapper.getUsageRecords(roomId, startTime, endTime);
        if (usageRecords != null) {
            return usageRecords.size();
        } else {
            return 0;
        }
    }

    @Override
    public void saveEndRecord(int roomId, LocalDateTime now) {
        //按照开始时间排序,找到最新一条记录,如果endTime为空,则更新endTime
        log.info("room id: " + roomId + " end record.");
        List<UsageRecord> usageRecord = baseMapper.saveEndRecord(roomId);
        //找到最新的一条记录根据开始时间排序
        if (usageRecord != null && !usageRecord.isEmpty()) {
            usageRecord.sort((o1, o2) -> o2.getStartTime().compareTo(o1.getStartTime()));
        }
        //找到最新的一条记录
        if (usageRecord != null && !usageRecord.isEmpty()  ) {
            UsageRecord usageRecord1 = usageRecord.get(0);
            if (usageRecord1.getEndTime() == null) {
                usageRecord1.setEndTime(now);
                baseMapper.updateById(usageRecord1);
            }
        }
    }
    //



    @Override
    public void saveStartRecord(UsageRecord usageRecord) {
        log.info("room id: " + usageRecord.getRoomId() + " start record.");
        baseMapper.insert(usageRecord);
    }
}
