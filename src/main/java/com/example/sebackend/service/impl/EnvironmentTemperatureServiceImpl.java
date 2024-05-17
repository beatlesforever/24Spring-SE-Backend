package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.EnvironmentTemperature;
import com.example.sebackend.mapper.EnvironmentTemperatureMapper;
import com.example.sebackend.service.IEnvironmentTemperatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author zhouhaoran
 * @date 2024/5/16
 * @project SE-backend
 */
@Service
public class EnvironmentTemperatureServiceImpl extends ServiceImpl<EnvironmentTemperatureMapper, EnvironmentTemperature> implements IEnvironmentTemperatureService {

}
