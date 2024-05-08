package com.example.sebackend.controller;

import com.example.sebackend.entity.Room;
import com.example.sebackend.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


}
