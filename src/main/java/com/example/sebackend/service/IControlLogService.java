package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.ControlLog;

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
}
