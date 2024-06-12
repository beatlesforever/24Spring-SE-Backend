package com.example.sebackend.schedule;

import com.example.sebackend.context.FrequencyConstant;
import com.example.sebackend.entity.*;
import com.example.sebackend.service.*;
import com.example.sebackend.service.impl.SchedulingQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
//import websocket.WebSocketServer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class ScheduleTask {

    @Autowired
    private IRoomService roomService;
    @Autowired
    private ICentralUnitService centralUnitService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IUsageRecordService usageRecordService;

    @Autowired
    private SchedulingQueueService schedulingQueueService;

    @Autowired
    IControlLogService controlLogService;

    private final ExecutorService workerPool = Executors.newFixedThreadPool(3);

    private static final ExecutorService ROOM_TEMPERATURE_EXECUTOR = Executors.newFixedThreadPool(10);
    private static final int MAX_THREADS = 3;
    private static final ExecutorService AIR_CONDITIONER_EXECUTOR = Executors.newFixedThreadPool(MAX_THREADS);


    /**
     * 定时任务，用于每月第一天凌晨1点修改中央空调的设置。
     * 该方法没有参数，也没有返回值。
     * 根据当前月份决定空调模式（冷气或暖气），并设置默认温度。
     * 使用cron表达式 "0 0 1 1 * ?" 来定义执行时间。
     */
    @Async
    @Scheduled(cron = "0 0 1 1 * ?")
    public void modifyCentralAirConditionerSettings() {
        // 获取当前月份，根据月份决定空调模式（冷气或暖气）
        int currentMonth = LocalDate.now().getMonthValue();
        String mode = (currentMonth >= 10 || currentMonth <= 3) ? "cooling" : "heating";

        // 根据空调模式设置默认温度
        float defaultTemperature = (mode.equals("cooling")) ? 22.0f : 28.0f;

        // 更新中央单元的模式和温度设置
        centralUnitService.setMode(mode);
        centralUnitService.segfaultTemperature(defaultTemperature);

        // 打印日志，确认方法执行
        System.out.println("执行空调设置修改");
    }


    //定时任务,扫描房间状态,standby且目标温度和当前温度差值为1,将房间空调设置为waiting,将请求加入到调度队列中
    //5s检测一次房间状态
    @Scheduled(fixedRate = 5000, initialDelay = 1000)
    public void StandbyRoomRequest() {
        if (centralUnitService.getById(1).getStatus().equals("off")) {
            log.info("中央空调未开机，跳过本次调度。");
            return;
        }

        List<Room> rooms = roomService.list();
        for (Room room : rooms) {
            final int roomId = room.getRoomId();
            Thread.currentThread().setName("Room-" + roomId);
            if (Objects.equals(room.getStatus(), "standby")) {
                if (Math.abs(room.getTargetTemperature() - room.getCurrentTemperature()) >= 1) {
                    room.setStatus("on");
                    room.setServiceStatus("waiting");
                    roomService.updateRoom(room);
                    //将请求加入到调度队列中
                    schedulingQueueService.addRoomToQueue(roomId);
                    //创建新的记录控制日志
                    controlLogService.addControlLog(room);
                }
            }
        }
    }




    // 定时任务，每1秒扫描一次
    @Scheduled(fixedRate = 1000)
    public void processTask() {
        // 判断中央空调是否开机
        if (centralUnitService.getById(1).getStatus().equals("off")) {
            log.info("中央空调未开机，跳过本次调度。");
            return;
        }
        log.info("队列大小"+schedulingQueueService.getQueueSize());
        // 处理调度队列中的请求，最多处理三个线程
        for (int i = 0; i < 3 && !schedulingQueueService.isQueueEmpty(); i++) {
            Integer roomId = schedulingQueueService.removeFirstRoomFromQueue();
            if (roomId != null) {
                workerPool.submit(() -> processRoomRequest(roomId));

            } else {
                break;  // 如果队列为空，提前退出循环
            }
        }
    }
    //按照时间片长度执行一次
    private void processRoomRequest(int roomId) {
        Room room = roomService.getById(roomId);
        if (room != null) {
            synchronized (room) {
                if (room.getStatus().equals("on") && room.getServiceStatus().equals("waiting")) {
                    // 设置房间的服务状态为serving
                    room.setServiceStatus("serving");
                    roomService.updateRoom(room);  // 保存更新后的房间信息
                    //创建新的房间记录
                    controlLogService.addControlLog(room);
                    // 模拟时间片处理
                    try {
                        Thread.sleep(FrequencyConstant.getTime()); // 时间片为20秒
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    //房间可能发起关闭请求
                    //获取房间
                    room = roomService.getById(roomId);
                    //如果房间状态为关闭,不执行
                    if (Objects.equals(room.getStatus(), "off")) {
                        return;
                    }
                    // 更新房间温度
                    Float currentTemperature = setRoomTemp(room);
                    //更新服务时间
                    controlLogService.setLatestLogDuration(roomId);
                    //更新房间费用
                    setRoomCostEenergy(room);

                    //房间可能发起了关闭请求
                    room = roomService.getById(roomId);
                    if (!Objects.equals(room.getStatus(), "off")){

                        // 处理结束之后，如果房间的目标温度和当前温度不相同，将房间重新加入调度队列
                        if (Math.abs(room.getTargetTemperature() - room.getCurrentTemperature()) > 0) {
                            //温度不相同
                            room.setServiceStatus("waiting");
                            roomService.updateRoom(room);  // 保存更新后的房间信息
                            schedulingQueueService.addRoomToQueue(roomId);
                        } else {
                            // 温度相同
                            room.setStatus("standby");
                            roomService.updateRoom(room);  // 保存更新后的房间信息
                            //设置controlLog结束
                            LocalDateTime endTime = LocalDateTime.now();
                            controlLogService.setLatestLog(roomId, endTime, true, currentTemperature);
                            endTime = LocalDateTime.now();
                            roomService.setRoomCost(roomId, endTime);
                        }
                }
                } else {
                    roomService.setRoomCost(roomId, LocalDateTime.now());
                }
            }
        }
    }

    //调整房间温度
    //20s执行1次
    private Float setRoomTemp(Room room) {
        Float currentTemperature = room.getCurrentTemperature();
        float high = 1.5f;
        float medium = 1f;
        float low = 0.5f;
        if (Objects.equals(room.getMode(), "cooling")) {
            if (Objects.equals(room.getFanSpeed(), "high")) {
                room.setCurrentTemperature(Math.max(currentTemperature - high, room.getTargetTemperature()));
            } else if (Objects.equals(room.getFanSpeed(), "medium")) {
                room.setCurrentTemperature(Math.max(currentTemperature - medium, room.getTargetTemperature()));
            } else if (Objects.equals(room.getFanSpeed(), "low")) {
                room.setCurrentTemperature(Math.max(currentTemperature - low, room.getTargetTemperature()));
            }
        }
        else if (Objects.equals(room.getMode(), "heating")) {
            if (Objects.equals(room.getFanSpeed(), "high")) {
                room.setCurrentTemperature(Math.min(currentTemperature + high, room.getTargetTemperature()));
            } else if (Objects.equals(room.getFanSpeed(), "medium")) {
                room.setCurrentTemperature(Math.min(currentTemperature + medium, room.getTargetTemperature()));
            } else if (Objects.equals(room.getFanSpeed(), "low")) {
                room.setCurrentTemperature(Math.min(currentTemperature + low, room.getTargetTemperature()));
            }
        }
         room = roomService.updateRoom(room);
        return room.getCurrentTemperature();
    }

    //更新房间费用和能源消耗
    private void setRoomCostEenergy(Room room) {
        int roomId = room.getRoomId();
        //更新房间费用
        LocalDateTime startTime = LocalDateTime.of(1999, 1, 1, 0, 0, 0); // 设置查询的起始时间
        LocalDateTime queryTime = LocalDateTime.now(); // 获取当前时间作为查询的结束时间

        // 查询该房间在指定时间范围内的已完成的温控请求
        List<ControlLog> controlLogs = controlLogService.getFinishedLogs(roomId, startTime, queryTime);
        // 计算这些温控请求的总能量消耗和总费用
        float totalEnergyConsumed = 0.0f;
        float totalCost = 0.0f;
        for (ControlLog controlLog : controlLogs) {
            totalEnergyConsumed += controlLog.getEnergyConsumed();
            totalCost += controlLog.getCost();
        }
        // 根据当前房间的风速，计算持续时间内的额外能源消耗
        ControlLog latestLog = controlLogService.getLatestLog(roomId);
        if (latestLog != null) {
            latestLog.getEnergyConsumed();
            totalEnergyConsumed += latestLog.getEnergyConsumed();
            // 计算总费用
            totalCost += latestLog.getCost();
        }
        // 更新房间的能源消耗和累计费用
        room.setEnergyConsumed(totalEnergyConsumed);
        room.setCostAccumulated(totalCost);
        roomService.updateRoom(room);
    }


}
