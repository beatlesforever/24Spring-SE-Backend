package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.Room;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
public interface IRoomService extends IService<Room> {

    Room updateRoom(Room room);

    void updateRoomTemperatures(float newTemperature);

    //删除当前房间从调度队列
    void removeFromSchedulingQueue(Integer roomId);

    //设置房间的累计费用
    void setRoomCost(int roomId, LocalDateTime endTime);

}
