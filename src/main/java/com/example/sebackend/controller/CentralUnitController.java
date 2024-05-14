package com.example.sebackend.controller;

import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.User;
import com.example.sebackend.service.ICentralUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@RestController
@Slf4j
@RequestMapping("/api/unit")
public class CentralUnitController {
    @Autowired
    private ICentralUnitService centralUnitService;
    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status.value() + " " + status.getReasonPhrase());
        responseBody.put("message", message);
        responseBody.put("data", data);
        return new ResponseEntity<>(responseBody, status);
    }

    //开启中央空调
    @GetMapping("/on")
    //网址: http://localhost:8080/api/unit/on
//    @ApiOperation(value = "开启中央空调")
    public ResponseEntity<Map<String, Object>> turnOn() {
//        System.out.printf("turnOn");
        System.out.print("user");
        log.info("中央空调开启");
        CentralUnit centralUnit = centralUnitService.turnOn();
        return createResponse(HttpStatus.OK, "中央空调已开启",centralUnit );
    }

    //关闭中央空调
    @GetMapping("/off")
    //网址: http://localhost:8080/api/unit/off
    public ResponseEntity<Map<String, Object>> turnOff() {
        log.info("中央空调关闭");
        CentralUnit centralUnit = centralUnitService.turnOff();
        return createResponse(HttpStatus.OK, "中央空调已关闭", null);
    }

    //获取中央空调状态
    //从控机发送认证消息,主控返回工作模式和温度
    @PostMapping("/authen ")
    //网址: http://localhost:8080/api/unit/authen
    public ResponseEntity<Map<String, Object>> authen(@RequestBody User user) {
        log.info("从控机认证");
        CentralUnit centralUnit = centralUnitService.authen(user);
        return createResponse(HttpStatus.OK, "认证成功", centralUnit);
    }
}
