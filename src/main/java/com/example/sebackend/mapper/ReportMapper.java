package com.example.sebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sebackend.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}