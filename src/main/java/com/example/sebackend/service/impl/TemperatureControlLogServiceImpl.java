package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.TemperatureControlLog;
import com.example.sebackend.mapper.TemperatureControlLogMapper;
import com.example.sebackend.service.ITemperatureControlLogService;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class TemperatureControlLogServiceImpl extends ServiceImpl<TemperatureControlLogMapper, TemperatureControlLog> implements ITemperatureControlLogService {
}
