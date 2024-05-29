package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.ControlLog;
import com.example.sebackend.entity.Room;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/12
 * @project SE-backend
 */
public interface IControlLogService extends IService<ControlLog> {
    //通过房间号获取正在进行的温控请求
    ControlLog getUnfinishedLog(int roomId);

    //通过房间号获取在时间范围内的已经结束的温控请求
    List<ControlLog> getFinishedLogs(int roomId, LocalDateTime startTime, LocalDateTime endTime);

    //根据房间号获取要更改的最新记录的结束记录
    void setLatestLog(int roomId, LocalDateTime endTime, boolean isCompleted, Float endTemp);


    void setLatestLogDuration(int roomId);

    void addControlLog(Room room);
    // 查询房间号为roomId的最新日志记录
    ControlLog getLatestLog(int roomId);
}
