package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.TemperatureChangeLog;
import com.example.sebackend.mapper.TemperatureChangeLogMapper;
import com.example.sebackend.service.ITemperatureChangeLogService;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class TemperatureChangeLogServiceImpl extends ServiceImpl<TemperatureChangeLogMapper, TemperatureChangeLog> implements ITemperatureChangeLogService {
}
