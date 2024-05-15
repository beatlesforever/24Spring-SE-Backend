package com.example.sebackend.controller;

import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Room;
import com.example.sebackend.entity.User;
import com.example.sebackend.service.ICentralUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    /**
     * 中央空调开机
     * > 根据季节设置模式和工作温度
     * > 开启定时任务---扫描调度队列长度
     */
    public ResponseEntity<Map<String, Object>> turnOn(@RequestParam String status) {
        log.info("中央空调开启");
        if (status.equals("off")) {
            CentralUnit centralUnit = centralUnitService.turnOn();
            return createResponse(HttpStatus.OK, "中央空调开启成功", centralUnit);
        } else {
            return createResponse(HttpStatus.BAD_REQUEST, "中央空调已开机", null);
        }
    }

    /**
     * 中央空调关机--不接受请求,但是仍然保留其他功能?
     * <p>
     * > 设置中央空调的状态
     * > 关闭定时任务--扫描调度队列长度
     */
    @GetMapping("/off")
    public ResponseEntity<Map<String, Object>> turnOff() {
        log.info("中央空调关闭");
        CentralUnit centralUnit = centralUnitService.turnOff();
        return createResponse(HttpStatus.OK, "中央空调已关闭", centralUnit);
    }

    /**
     * 从控机开机申请认证,加入判断(中央空调是否开启),通过后
     * 将房间的目标温度设置成缺省温度和工作模式和中央空调同步,
     * 对比目标温度和环境温度,不相同将将房间的状态设置成on,
     * 相同将房间温度设置成standby,将请求加入到请求队列中,设置房间waiting
     *
     * @return ResponseEntity<Map < String, Object>>
     */
    @PostMapping("/authen")
    public ResponseEntity<Map<String, Object>> authen() {
        log.info("从控机认证");
        CentralUnit centralUnit = centralUnitService.authen();
        if (Objects.equals(centralUnit.getStatus(), "off")) {
            return createResponse(HttpStatus.BAD_REQUEST, "中央空调未开启", null);
        }
        return createResponse(HttpStatus.OK, "认证成功", centralUnit);
    }

    /**
     * todo:
     * 主控机实时监测从控机
     * > 使用websocket实现
     * > 配置刷新频率
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("中央空调获取从控机状态");
        List<Room> rooms = centralUnitService.getStatus();
        return createResponse(HttpStatus.OK, "获取从控机状态成功", rooms);
    }
    //设置刷新频率,单位为秒
    @GetMapping("/frequency")
    public ResponseEntity<Map<String, Object>> setFrequency(@RequestParam int frequency) {
        log.info("设置刷新频率");
        CentralUnit centralUnit = centralUnitService.uodateFrequency(frequency);
        return createResponse(HttpStatus.OK, "设置刷新频率成功", centralUnit);
    }



}
