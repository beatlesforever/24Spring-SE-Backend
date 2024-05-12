package com.example.sebackend.controller;

import com.example.sebackend.entity.Room;
import com.example.sebackend.entity.User;
import com.example.sebackend.service.IRoomService;
import com.example.sebackend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    @Autowired
    private IRoomService roomService;
    @Autowired
    private IUserService userService;
    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    /**
     * 获取指定房间的当前状态和设置
     * @param roomId 房间的唯一标识符，用于指定要查询的房间
     * @return ResponseEntity<Map<String, Object>> 响应实体，包含状态信息和房间数据。
     *         如果房间存在，则返回房间的详细信息；如果房间不存在，则返回未找到的错误信息。
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> getRoomStatus(@PathVariable int roomId) {
        // 通过房间ID查询房间信息
        Room room = roomService.getById(roomId);
        if (room == null) {
            // 如果房间不存在，创建并返回一个包含错误信息的响应实体
            return createResponse(HttpStatus.NOT_FOUND, "未找到指定房间", null);
        }
        // 如果房间存在，创建并返回一个包含房间详细信息的响应实体
        return createResponse(HttpStatus.OK, "成功获取房间详细信息", room);
    }

    /**
     * 获取所有房间的状态列表
     * 该接口不需要接受任何参数，通过调用roomService的list方法获取所有房间的状态信息，并将其封装到响应实体中返回。
     * @return ResponseEntity<Map<String, Object>> 返回一个响应实体，其中包含HTTP状态码、成功消息和所有房间的状态信息。
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllRooms() {
        // 从roomService获取所有房间的信息
        List<Room> rooms = roomService.list();
        // 创建并返回一个包含HTTP状态码、成功消息和房间状态信息的响应实体
        return createResponse(HttpStatus.OK, "成功获取所有房间信息", rooms);
    }
    /**
     * 创建一个新的房间并保存到服务中。
     *
     * @return ResponseEntity<Map<String, Object>> 包含HTTP状态码、消息和创建的房间信息。
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRoom() {
        Room room = new Room();
        // 初始化房间属性为默认值
        room.setCurrentTemperature(24.0f);  // 设置默认当前温度
        room.setTargetTemperature(24.0f);  // 设置默认目标温度
        room.setFanSpeed("medium");  // 设置默认风速
        room.setTemperatureThreshold(1.0f);  // 设置默认温度阈值
        room.setStatus("off");  // 设置默认状态为关闭
        room.setMode("cooling");  // 设置默认模式为制冷
        room.setLastUpdate(LocalDateTime.now());  // 设置最后更新时间为当前时间
        room.setServiceStatus("waiting");  // 设置默认服务状态为等待
        room.setEnergyConsumed(0.0f);  // 设置默认消耗能量为0
        room.setCostAccumulated(0.00f);  // 设置默认累计费用为0

        // 保存房间到服务
        roomService.save(room);
        // 构建并返回响应实体
        return createResponse(HttpStatus.CREATED, "房间创建成功", room);
    }

    /**
     * 获取所有没有用户的空房间
     *
     * 本方法不接受任何参数，返回所有未被用户占用的房间列表。
     *
     * @return ResponseEntity<Map<String, Object>> 包含房间列表的HTTP响应实体，
     *         其中状态码为OK（200），信息为"获取未分配房间成功"，数据部分为可用房间列表。
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableRooms() {
        // 获取所有房间列表
        List<Room> allRooms = roomService.list();

        // 获取所有已占用房间的ID列表
        List<Integer> occupiedRoomIds = userService.list().stream()
                .map(User::getRoomId)
                .collect(Collectors.toList());

        // 筛选出未被占用的房间
        List<Room> availableRooms = allRooms.stream()
                .filter(room -> !occupiedRoomIds.contains(room.getRoomId()))
                .collect(Collectors.toList());

        // 构建并返回响应实体
        return createResponse(HttpStatus.OK, "获取未分配房间成功", availableRooms);
    }

}
