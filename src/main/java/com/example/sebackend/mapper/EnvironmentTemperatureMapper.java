package com.example.sebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sebackend.entity.EnvironmentTemperature;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhouhaoran
 * @date 2024/5/16
 * @project SE-backend
 */
@Mapper
public interface EnvironmentTemperatureMapper extends BaseMapper<EnvironmentTemperature> {
}
