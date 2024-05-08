package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.UsageRecord;
import com.example.sebackend.mapper.UsageRecordMapper;
import com.example.sebackend.service.IUsageRecordService;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class UsageRecordServiceImpl extends ServiceImpl<UsageRecordMapper, UsageRecord> implements IUsageRecordService {
}
