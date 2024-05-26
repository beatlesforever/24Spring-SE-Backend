package com.example.sebackend.controller;

import com.example.sebackend.entity.ControlLog;
import com.example.sebackend.entity.Room;
import com.example.sebackend.service.IControlLogService;
import com.example.sebackend.service.IRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@RestController
@Slf4j
@RequestMapping("/api/costs")
public class EnergyConsumptionController {

    @Autowired
    private IControlLogService controlLogService;
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
     * 查询指定房间的累积费用
     *
     * 本方法接受一个roomId和一个查询时间queryTime作为参数，通过roomId查询指定房间的累积费用、累积能耗以及当前风速，
     * 查询正在进行的温控请求的请求时间，计算当前累积能耗和费用
     * 规定在一次温控请求内，中控机供风风速不发生改变
     *
     * @param roomId 房间ID
     * //@param queryTime 查询时间
     * @return ResponseEntity<Map<String, Object>> 包含更新后的房间信息的HTTP响应实体，
     *         其中状态码为OK（200），信息为"实时房间能耗费用查询成功"，数据部分为累积能耗和累积费用。
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> getRoomCost(@PathVariable int roomId) {
        List<Float> data = new ArrayList<>();
        // 通过roomId查询房间信息
        Room room = roomService.getById(roomId);

        // 获取房间的累积能耗和累积费用
        float energyConsumed = room.getEnergyConsumed();
        float costAccumulated = room.getCostAccumulated();

        // 获取房间的当前风速
        String fanSpeed = room.getFanSpeed();

        // 获取房间正在进行的温控请求
        ControlLog controlLog = controlLogService.getUnfinishedLog(roomId);

        // 获取当前时间
        LocalDateTime queryTime = LocalDateTime.now();
        // 如果没有正在进行的温控请求，直接返回当前累积能耗和费用
        if (controlLog == null) {
            // 构建data
            data.add(energyConsumed);
            data.add(costAccumulated);
        }
        // 如果有正在进行的温控请求，计算当前累积能耗和费用
        else {
            // 获取房间的当前请求时间
            LocalDateTime requestTime = controlLog.getRequestTime();
            // 通过查询时间和请求时间计算时间差，单位为分钟，如果不足一分钟则折算为小数
            Duration durationH = Duration.between(requestTime, queryTime);
            long seconds = durationH.getSeconds();
            float duration = seconds / 60.0f; // 转换为分钟
            // 计算当前累积能耗
            // 风速为high时，能耗为1.2倍，medium时为1倍，low时为0.8倍
            if (fanSpeed.equals("high")) {
                duration *= 1.2f;
            } else if (fanSpeed.equals("low")) {
                duration *= 0.8f;
            }
            float currentEnergyConsumed = (float) energyConsumed + duration;
            // 计算当前累积费用，一个单位能耗对应5元
            float currentCostAccumulated = costAccumulated + duration * 5f;

            // 构建data
            data.add(currentEnergyConsumed);
            data.add(currentCostAccumulated);
            // 构建并返回响应实体
        }
        return createResponse(HttpStatus.OK, "实时房间能耗费用查询成功", data);

    }

}
