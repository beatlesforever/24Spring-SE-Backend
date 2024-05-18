package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.Room;
import com.example.sebackend.mapper.RoomMapper;
import com.example.sebackend.service.IRoomService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {

    @Autowired
    RoomMapper roomMapper;
    private final ConcurrentHashMap<Integer, Room> roomMap;
    private final ConcurrentHashMap<Integer, Boolean> processingRooms;
    @Autowired
    public RoomServiceImpl(@Qualifier("roomQueue") ConcurrentHashMap<Integer, Room> roomQueue,
                        @Qualifier("processingRooms") ConcurrentHashMap<Integer, Boolean> processingRooms) {
        this.roomMap = roomQueue;
        this.processingRooms = processingRooms;
    }

    //判断房间的请求信息是否存在
    public boolean containsRoom(int key) {
        return roomMap.containsKey(key);
    }

    //获取优先级最高的请求
    public Room current_userRoom() {
        Room room = null;
        for (Room value : roomMap.values()) {
            // 检查房间是否已经在处理中
            if (!processingRooms.containsKey(value.getRoomId())) {
                switch (value.getFanSpeed()) {
                    case "high":
                        room = value;
                        // 标记此房间正在被处理
//                        processingRooms.put(value.getRoomId(), true);
                        break;
//                        return room;  // 一旦找到最高优先级的房间就立即退出
                    case "medium":
                        if (room == null || !room.getFanSpeed().equals("high")) { // 只有在没有找到高优先级房间的情况下才覆盖
                            room = value;
                        }
                        break;
                    case "low":
                        if (room == null || (!room.getFanSpeed().equals("high") && !room.getFanSpeed().equals("medium"))) { // 只有在没有找到高或中优先级房间的情况下才覆盖
                            room = value;
                        }
                        break;
                }
            }
        }
        if (room != null) {
            processingRooms.put(room.getRoomId(), true); // 标记选中的房间正在被处理
        }
        return room;
    }


    @Override
    public void updateRoom(Room room) {
        roomMapper.updateById(room);
    }

    /**
     * 更新所有房间的温度，基于外部环境温度和房间当前状态。
     * 如果房间当前处于关闭或待机状态，将房间温度朝着环境温度调整。
     * 每分钟向环境温度靠近0.2°C，无论温度是需要升高还是降低。
     *
     * @param environmentTemperature 外部环境的温度，用于调整房间温度的参考值。
     *                               该参数不直接影响房间温度，而是作为调整目标温度的依据。
     */
    @Override
    public void updateRoomTemperatures(float environmentTemperature) {
        // 获取所有房间列表
        List<Room> rooms = this.list();
        for (Room room : rooms) {
            // 判断房间当前状态是否为关闭或待机
            if ("off".equals(room.getStatus()) || "standby".equals(room.getStatus())) {
                float currentTemp = room.getCurrentTemperature();
                float temperatureChange = 0.2f;  // 设定温度变化的步长

                // 判断房间当前温度与环境温度的关系，并相应调整
                if (currentTemp < environmentTemperature) {
                    // 房间温度低于环境温度，增加房间温度
                    room.setCurrentTemperature(Math.min(currentTemp + temperatureChange, environmentTemperature));
                } else if (currentTemp > environmentTemperature) {
                    // 房间温度高于环境温度，降低房间温度
                    room.setCurrentTemperature(Math.max(currentTemp - temperatureChange, environmentTemperature));
                }

                // 更新房间的温度信息
                this.updateById(room);


            }
        }
    }


}
