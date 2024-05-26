package com.example.sebackend.controller;

import com.example.sebackend.context.BaseContext;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Response;
import com.example.sebackend.entity.Room;
import com.example.sebackend.entity.User;
import com.example.sebackend.service.ICentralUnitService;
import com.example.sebackend.service.IRoomService;
import com.example.sebackend.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.naming.Context;
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
     * 获取中央空调状态
     * @return ResponseEntity<Map < String, Object>>
     */
    @GetMapping("/CentralUnit")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> getCentralUnit() {
        log.info("中央空调获取状态");
        CentralUnit centralUnit = centralUnitService.getById(1);
        return createResponse(HttpStatus.OK, "获取中央空调状态成功", centralUnit);
    }

    /**
     * 获取中央空调的刷新频率
     * @return ResponseEntity<Map < String, Object>>
     */
//    @GetMapping("/frequency")
//    public ResponseEntity<Map<String, Object>> getFrequency() {
//        log.info("获取中央空调的刷新频率");
//        CentralUnit centralUnit = centralUnitService.getById(1);
//        return createResponse(HttpStatus.OK, "获取中央空调的刷新频率成功", centralUnit.getFrequency());
//    }

    /**
     * 开启中央空调
     * > 根据季节设置模式和工作温度
     */
    @PostMapping("/on")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> turnOn() {
        log.info("中央空调开启");
        //获取当前中央空调的状态
        String status = centralUnitService.getById(1).getStatus();
        if (status.equals("off")) {
            CentralUnit centralUnit = centralUnitService.turnOn();
            return createResponse(HttpStatus.OK, "中央空调开启成功", centralUnit);
        } else {
            return createResponse(HttpStatus.BAD_REQUEST, "中央空调已开机", null);
        }
    }

    /**
     * 关闭中央空调
     * 不接受温度调节请求,但是仍然保留其他功能?
     * <p>
     * > 设置中央空调的状态
     * > 关闭定时任务--扫描调度队列长度
     */
    @PostMapping("/off")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Map<String, Object>> turnOff() {
        log.info("中央空调关闭");
        CentralUnit centralUnit = centralUnitService.getById(1);
        //如果中央空调已经关闭,返回已经关闭
        if (Objects.equals(centralUnit.getStatus(), "off")) {
            return createResponse(HttpStatus.BAD_REQUEST, "中央空调已关闭", centralUnit);
        }
        centralUnit = centralUnitService.turnOff();
        return createResponse(HttpStatus.OK, "中央空调关闭成功", centralUnit);
    }

    /**
     * 从控机开机认证
     * ,加入判断(中央空调是否开启),通过后
     * 将房间的目标温度设置成缺省温度和工作模式和中央空调同步,
     * 对比目标温度和环境温度,不相同将将房间的状态设置成on,
     * 相同将房间温度设置成standby,将请求加入到请求队列中,设置房间waiting
     *
     * @return ResponseEntity<Map < String, Object>>
     */
    @PostMapping("/{roomId}/authen")
    public ResponseEntity<Map<String, Object>> authen(@PathVariable int roomId, @RequestBody User loginuser) {
        log.info("从控机认证");
        // 根据房间ID获取房间信息
        Room room = roomService.getById(roomId);
        // 房间不存在时的处理
        if (room == null) {
            return createResponse(HttpStatus.NOT_FOUND, "未找到指定房间", null);
        }
        // 房间已开启时的处理
        if (!"off".equals(room.getStatus())) {
            return createResponse(HttpStatus.BAD_REQUEST, "从控机已经是开启状态", null);
        }
        // 用户身份验证
        User user = userService.login(loginuser.getUsername(), loginuser.getPassword());
        // 用户名或密码错误的处理
        if (user == null) {
            return createResponse(HttpStatus.UNAUTHORIZED, "用户名或密码错误", null);
        }

        // 验证用户是否为指定房间的住户
        if (!user.getRoomId().equals(roomId)) {
            return createResponse(HttpStatus.FORBIDDEN, "您无权操作该房间", null);
        }

        CentralUnit centralUnit = centralUnitService.getById(1);
        if (Objects.equals(centralUnit.getStatus(), "off")) {
            return createResponse(HttpStatus.BAD_REQUEST, "中央空调未开启", null);
        } else {
            centralUnit = centralUnitService.authen(roomId);
        }
        return createResponse(HttpStatus.OK, "认证成功", centralUnit);
    }

    /**
     * 获取从控机状态
     * todo:主控机实时监测从控机
     * > 使用websocket实现
     * > 配置刷新频率
     *
     * @return ResponseEntity<Map < String, Object>>
     */
    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("中央空调获取从控机状态");
        List<Room> rooms = centralUnitService.getStatus();
        return createResponse(HttpStatus.OK, "获取从控机状态成功", rooms);
    }


    /**
     * 修改刷新频率
     * 单位为秒
     *
     */
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/frequency")
    public ResponseEntity<Map<String, Object>> setFrequency(@RequestParam int frequency) {
        log.info("设置刷新频率");
        CentralUnit centralUnit = centralUnitService.uodateFrequency(frequency);
        return createResponse(HttpStatus.OK, "设置刷新频率成功", centralUnit);
    }

    /**
     *接收温控请求
     *中央空调接受请求
     * 从控机修改目标温度后,加入判断(中央空调是否开启),
     *发送送风请求(目标温度,和当前的风速模式,默认是中风,在房间创建的时候设置),
     *后端判断合理后,设置属性(目标温度,服务状态为waiting)
     * 从控机修改风速模式请求后,加入判断(中央空调是否开启),发送送风请求,
     *后端判断合理之后,修改房间对应属性(风速模式,服务状态为waiting)并保存到数据库中
     *将请求加入到等待队列,并将之前的同一房间的等待中的请求删除队列
     */

    @PostMapping("/requests")
    @SendTo("/air/requestServing")
    public ResponseEntity<Map<String, Object>> getRequests(@RequestParam float targetTemperature, @RequestParam String fanSpeed) {
        log.info("中央空调接收请求");
        //从控机修改目标温度
        Response re = centralUnitService.requests(targetTemperature, fanSpeed);
        Room room = (Room) re.getData();
        if (Objects.equals(re.getCode(), 403)) {
            //中央空调未开启或者房间未认证
            return createResponse(HttpStatus.BAD_REQUEST, re.getMessage(), room);
        } else if (Objects.equals(re.getCode(), 404)) {
            //温度设置不合理
            return createResponse(HttpStatus.BAD_REQUEST, re.getMessage(), room);
        } else {
            //接收请求成功
            return createResponse(HttpStatus.OK, re.getMessage(), room);
        }
    }


}
