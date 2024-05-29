package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.ControlLog;
import com.example.sebackend.entity.Room;
import com.example.sebackend.mapper.ControlLogMapper;
import com.example.sebackend.service.IControlLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/12
 * @project SE-backend
 */
@Service
@Slf4j
public class ControlLogServiceImpl  extends ServiceImpl<ControlLogMapper, ControlLog> implements IControlLogService {

    @Override
    public ControlLog getUnfinishedLog(int roomId) {
        return baseMapper.getUnfinishedLog(roomId);
    }

    /**
     * 获取指定房间在指定时间范围内的完成的日志列表。
     *
     * @param roomId 房间ID，指定要查询日志的房间。
     * @param startTime 开始时间，查询范围的起始时间。
     * @param endTime 结束时间，查询范围的结束时间。
     * @return 返回一个ControlLog对象的列表，这些对象代表了在指定房间内在指定时间内完成的日志。
     */
    @Override
    public List<ControlLog> getFinishedLogs(int roomId, LocalDateTime startTime, LocalDateTime endTime) {
        // 调用底层Mapper接口，查询指定房间、指定时间范围内的完成日志
        return baseMapper.getFinishedLogs(roomId, startTime, endTime);
    }

    /**
     * 更新房间的最新日志记录。
     * 该方法首先查询房间号为roomId的最新日志记录，如果该记录的结束时间或结束温度为空，
     * 则使用提供的结束时间、完成状态和结束温度更新该记录，并计算并更新记录的持续时间。
     *
     * @param roomId 房间ID，用于指定要更新日志记录的房间。
     * @param endTime 操作的结束时间，使用LocalDateTime表示。
     * @param isCompleted 操作是否已完成，布尔值表示。
     * @param endTemp 操作结束时的温度，以浮点数表示。
     */
    @Override
    public void setLatestLog(int roomId, LocalDateTime endTime, boolean isCompleted, Float endTemp) {
        // 查询房间号为roomId的最新日志记录
        ControlLog latestLog = baseMapper.getLatestLog(roomId);
        // 如果最新记录不为空，并且结束时间或结束温度为空，则进行更新
        if (latestLog!=null) {
            if (latestLog.getEndTime() == null || latestLog.getEndTemp() == null) {
                log.info("房间{}设置最新日志的结束时间", roomId);
                // 更新结束时间、完成状态、结束温度和持续时间
                latestLog.setEndTime(endTime);
//                latestLog.setCompleted(isCompleted);
                latestLog.setCompleted(true);
                latestLog.setEndTemp(endTemp);
                // 计算持续时间（单位：秒）
//                int duration = (int) (endTime.toEpochSecond(ZoneOffset.UTC) - (latestLog.getRequestTime()).toEpochSecond(ZoneOffset.UTC));
//                latestLog.setDuration(duration);
                // 根据ID更新日志记录
                baseMapper.updateById(latestLog);
            }
        }
    }

    @Override
    public void setLatestLogDuration(int roomId) {
        // 查询房间号为roomId的最新日志记录
        ControlLog latestLog = baseMapper.getLatestLog(roomId);
        //System.out.println(latestLog);
        // 如果最新记录不为空，并且结束时间或结束温度为空，则进行更新
        if (latestLog != null) {
            if (latestLog.getEndTime() == null || latestLog.getEndTemp() == null) {
                log.info("房间{}设置最新日志的结束时间", roomId);
                // 计算持续时间（单位：秒）
                Integer duration = latestLog.getDuration();
                latestLog.setDuration(duration + 10);
                // 根据ID更新日志记录
                baseMapper.updateById(latestLog);
            }
        }
    }

    /**
     * 向系统中添加一个控制日志记录。
     * 该方法用于记录房间的控制信息，如目标温度、实际温度、风扇速度等，作为系统控制操作的日志。
     *
     * @param room 房间对象，包含房间的相关控制信息。
     * 不返回任何值，操作完成后控制日志将被持久化存储。
     */
    @Override
    public void addControlLog(Room room) {
        // 创建一个新的控制日志实例，填充房间的相关控制信息及当前时间，并设置完成状态为false
        ControlLog controlLog = new ControlLog(room.getRoomId(),
                room.getTargetTemperature(), room.getCurrentTemperature(),
                room.getFanSpeed(), room.getMode(),LocalDateTime.now(), false);

        // 将控制日志插入数据库
        baseMapper.insert(controlLog);
    }

    @Override
    public ControlLog getLatestLog(int roomId) {
        return baseMapper.getLatestLog(roomId);
    }


}
