package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.Room;
import com.example.sebackend.mapper.RoomMapper;
import com.example.sebackend.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {
    @Autowired
    private final ConcurrentHashMap<Integer,Room> roomMap;
    @Autowired
    RoomMapper roomMapper;

    public RoomServiceImpl(ConcurrentHashMap<Integer, Room> roomMap) {
        this.roomMap = roomMap;
    }
    //添加房间的请求信息
    public void addRoom(int key, Room room) {
        roomMap.put(key, room);
    }
    //获取请求的房间号
    public Room getRoom(int key) {
        return roomMap.get(key);
    }
    //删除房间的请求信息
    public Room removeRoom(int key) {
        return roomMap.remove(key);
    }
    //判断房间的请求信息是否存在
    public boolean containsRoom(int key) {
        return roomMap.containsKey(key);
    }

    @Override
    public void updateRoom(Room room) {
        roomMapper.updateById(room);
    }
}
