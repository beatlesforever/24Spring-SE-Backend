package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.Room;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
public interface IRoomService extends IService<Room> {

    void updateRoom(Room room);

    void updateRoomTemperatures(float newTemperature);
}
