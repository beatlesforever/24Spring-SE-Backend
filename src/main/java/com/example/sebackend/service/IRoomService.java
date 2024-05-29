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

    void updateRoom(Room room);

    void updateRoomTemperatures(float newTemperature);

    Room current_userRoom();

    //设置房间的累计费用
    void setRoomCost(int roomId, LocalDateTime endTime);

}
