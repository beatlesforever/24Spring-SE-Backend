package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.ControlLog;
import com.example.sebackend.entity.Room;
import com.example.sebackend.mapper.RoomMapper;
import com.example.sebackend.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {

    @Autowired
    RoomMapper roomMapper;
    @Autowired
    ControlLogServiceImpl controlLogService;

    @Autowired
    private SchedulingQueueService schedulingQueueService;

    @Override
    public void removeFromSchedulingQueue(Integer roomId) {
        schedulingQueueService.removeRoomFromQueue(roomId);
    }


    /**
     * 获取当前用户房间的方法。
     * 该方法依据房间的风扇速度和先来先服务的原则，选择一个房间返回。
     * 首先查找风扇速度为高的房间，如果不存在，则选择风扇速度为中的房间，
     * 如果还不存在，则选择风扇速度为低的房间。找到房间后，标记该房间为正在处理中。
     *
     * @return 返回选中的房间，如果没有可用房间则返回null。
     */
//    public Room current_userRoom() {
//        Room room = null;
//        for (Room value : roomMap.values()) {
//            // 检查房间是否已经在处理中
//            if (!processingRooms.containsKey(value.getRoomId())) {
//                switch (value.getFanSpeed()) {
//                    case "high":
//                        room = value;
//                        // 标记此房间正在被处理
////                        processingRooms.put(value.getRoomId(), true);
//                        break;
////                        return room;  // 一旦找到最高优先级的房间就立即退出
//                    case "medium":
//                        if (room == null || !room.getFanSpeed().equals("high")) { // 只有在没有找到高优先级房间的情况下才覆盖
//                            room = value;
//                        }
//                        break;
//                    case "low":
//                        if (room == null || (!room.getFanSpeed().equals("high") && !room.getFanSpeed().equals("medium"))) { // 只有在没有找到高或中优先级房间的情况下才覆盖
//                            room = value;
//                        }
//                        break;
//                }
//            }
//        }
//        if (room != null) {
//            processingRooms.put(room.getRoomId(), true); // 标记选中的房间正在被处理
//        }
//        return room;
//    }

    @Override
    public void setRoomCost(int roomId, LocalDateTime endTime) {
        LocalDateTime startTime = LocalDateTime.of(1999, 1, 1, 0, 0, 0);
        // 获取当前时间
        LocalDateTime queryTime = LocalDateTime.now();
        // 查询结束时间为当前请求时间

        // 查询时间范围内的该房间内的已经完成的温控请求
        List<ControlLog> controlLogs = controlLogService.getFinishedLogs(roomId, startTime, endTime);
        // 累加报告期间总能量消耗和总费用
        float totalEnergyConsumed = 0.0f;
        float totalCost = 0.0f;
        for (ControlLog controlLog : controlLogs) {
            totalEnergyConsumed += controlLog.getEnergyConsumed();
            totalCost += controlLog.getCost();
        }
        // 更新房间的累计费用
        Room room = roomMapper.selectById(roomId);
        room.setEnergyConsumed(totalEnergyConsumed);
        room.setCostAccumulated(totalCost);
        updateRoom(room);
    }

    /**
     * 更新房间信息。
     *
     * @param room 房间对象，包含需要更新的房间信息。
     *             该方法通过调用roomMapper的updateById方法，根据房间对象的ID更新数据库中的房间信息。
     *             无返回值，更新操作的结果直接由roomMapper处理。
     */
    @Override
    public Room updateRoom(Room room) {
        roomMapper.updateById(room);
        return roomMapper.getId(room.getRoomId());
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
    public void updateRoomTemperatures(float environmentTemperature,float temperatureChange) {
        // 获取所有房间列表
        List<Room> rooms = this.list();
        for (Room room : rooms) {
            // 判断房间当前状态是否为关闭或待机
            if ("off".equals(room.getStatus()) || "standby".equals(room.getStatus())) {
                float currentTemp = room.getCurrentTemperature();

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
