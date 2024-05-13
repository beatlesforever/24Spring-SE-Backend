package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.ControlLog;
import com.example.sebackend.mapper.CentralUnitMapper;
import com.example.sebackend.mapper.ControlLogMapper;
import com.example.sebackend.service.ICentralUnitService;
import com.example.sebackend.service.IControlLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/12
 * @project SE-backend
 */
@Service
public class ControlLogServiceImpl  extends ServiceImpl<ControlLogMapper, ControlLog> implements IControlLogService {

    @Override
    public ControlLog getUnfinishedLog(int roomId) {
        return baseMapper.getUnfinishedLog(roomId);
    }

    @Override
    public List<ControlLog> getFinishedLogs(int roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return  baseMapper.getFinishedLogs(roomId, startTime, endTime);
    }
}
