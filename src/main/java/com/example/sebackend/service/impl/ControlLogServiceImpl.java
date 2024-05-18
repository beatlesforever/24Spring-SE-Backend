package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.ControlLog;
import com.example.sebackend.entity.Room;
import com.example.sebackend.mapper.ControlLogMapper;
import com.example.sebackend.service.IControlLogService;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    @Override
    public void setLatestLog(int roomId, LocalDateTime endTime, boolean isCompleted, Float endTemp) {
        //将所有的房间号相同的记录按照时间排序，取最新的一条
        ControlLog latestLog = baseMapper.getLatestLog(roomId);
        //如果最新记录的结束时间,结束温度,为空则更新数据库
        if (latestLog!=null) {
            if (latestLog.getEndTime() == null || latestLog.getEndTemp() == null) {
                latestLog.setEndTime(endTime);
                latestLog.setCompleted(isCompleted);
                latestLog.setEndTemp(endTemp);
                int duration = (int) (endTime.toEpochSecond(ZoneOffset.UTC) - (latestLog.getRequestTime()).toEpochSecond(ZoneOffset.UTC));
                latestLog.setDuration(duration);
                baseMapper.updateById(latestLog);
            }
        }
    }

    @Override
    public void addControlLog(Room room) {
        //将房间信息转化为控制日志
        //Integer roomId, Float requestedTemp, Float actualTemp,
        // String requestedFanSpeed, String mode,
        // LocalDateTime requestTime,  boolean isCompleted
        ControlLog controlLog = new ControlLog(room.getRoomId(),
                room.getTargetTemperature(), room.getCurrentTemperature(),
                room.getFanSpeed(), room.getMode(),LocalDateTime.now(), false);
        baseMapper.insert(controlLog);
    }
}
