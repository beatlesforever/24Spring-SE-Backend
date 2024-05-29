package com.example.sebackend.schedule;

import com.example.sebackend.context.BaseContext;
import com.example.sebackend.context.FrequencyConstant;
import com.example.sebackend.entity.*;
import com.example.sebackend.service.*;
import com.example.sebackend.websocket.WebSocketRequest;
import com.example.sebackend.websocket.WebSocketStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
//import websocket.WebSocketServer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final ConcurrentHashMap<Integer, Room> roomMap;
    private final ConcurrentHashMap<Integer, Boolean> processingRooms;
    private String username;

    @Autowired
    public ScheduleTask(@Qualifier("roomQueue") ConcurrentHashMap<Integer, Room> roomQueue, @Qualifier("processingRooms") ConcurrentHashMap<Integer, Boolean> processingRooms) {
        this.roomMap = roomQueue;
        this.processingRooms = processingRooms;
    }

    @Autowired
    private WebSocketStatus webSocketStatus;
    @Autowired
    private WebSocketRequest webSocketRequest;

    // 静态变量，用于控制定时任务执行的频率和计数
    private static int count = 6;
    private static int frequency = 0;
    private static int currentFrequency = 0;

    @Autowired
    IControlLogService controlLogService;

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


    //从控机机工作状态下,房间的温度变化调整
    //10s检测房间温度,把请求放到调度队列中,10s处理一次房间的温度,60s计算一次房间能耗和费用
    @Async
    @Scheduled(fixedRate = 10000, initialDelay = 1000)
    public void adjustRoomTemperature() {
        List<Room> rooms = roomService.list();
        for (Room room : rooms) {
            final int roomId = room.getRoomId();
            ROOM_TEMPERATURE_EXECUTOR.execute(() -> {
                Thread.currentThread().setName("Room-" + roomId);
                //房间status为off/standby时,向环境温度靠近,将新的值写入数据库,在环境温度的实现类中完成
                //如果为standby模式,目标温度和当前温度差值为1,设置房间空调waiting,将请求加入到等待队列中
                if (Objects.equals(room.getStatus(), "standby")) {
                    if (Math.abs(room.getTargetTemperature() - room.getCurrentTemperature()) >= 1) {
                        room.setStatus("on");
                        room.setServiceStatus("waiting");
                        roomService.updateRoom(room);
                        roomMap.put(roomId, room);
                    }
                }
                if (Objects.equals(room.getStatus(), "on") && Objects.equals(room.getServiceStatus(), "serving")) {
                    //根据空调的制冷或制热模式,高速变化0.9,中速变化0.6,低速变化0.3,
                    //10s变化一次温度
                    //设置从控机的温度变化
                    Float currentTemperature = room.getCurrentTemperature();
                    if (Objects.equals(room.getMode(), "cooling")) {
                        if (Objects.equals(room.getFanSpeed(), "high")) {
                            room.setCurrentTemperature(Math.max(currentTemperature - 0.15f, room.getTargetTemperature()));
                        } else if (Objects.equals(room.getFanSpeed(), "medium")) {
                            room.setCurrentTemperature(Math.max(currentTemperature - 0.1f, room.getTargetTemperature()));
                        } else if (Objects.equals(room.getFanSpeed(), "low")) {
                            room.setCurrentTemperature(Math.max(currentTemperature - 0.05f, room.getTargetTemperature()));
                        }
                    } else if (Objects.equals(room.getMode(), "heating")) {
                        if (Objects.equals(room.getFanSpeed(), "high")) {
                            room.setCurrentTemperature(Math.min(currentTemperature + 0.15f, room.getTargetTemperature()));
                        } else if (Objects.equals(room.getFanSpeed(), "medium")) {
                            room.setCurrentTemperature(Math.min(currentTemperature + 0.1f, room.getTargetTemperature()));
                        } else if (Objects.equals(room.getFanSpeed(), "low")) {
                            room.setCurrentTemperature(Math.min(currentTemperature + 0.05f, room.getTargetTemperature()));
                        }
                    }
                    controlLogService.setLatestLogDuration(roomId);
//                    System.out.printf("房间%d当前温度为%.2f当前持续时间%d\n", roomId, currentTemperature, controlLogService.getLatestLog(roomId).getDuration());

                    //判断当前温度和目标温度相同,将房间空调设置成standby模式;
                    if (Math.abs(room.getTargetTemperature() - currentTemperature) <=0) {
                        room.setStatus("standby");
                        //设置controlLog结束
                        LocalDateTime endTime = LocalDateTime.now();
                        controlLogService.setLatestLog(roomId, endTime, true, currentTemperature);
//                        System.out.printf("房间%d当前温度为%.2f当前持续时间%d\n", roomId, currentTemperature, controlLogService.getLatestLog(roomId).getDuration());
                        //更新房间的累计费用
                        endTime= LocalDateTime.now();
                        roomService.setRoomCost(roomId, endTime);
                    }
                    roomService.updateRoom(room);
                }


            });
        }
    }

    /**
     * 定时计算各个房间的能源消耗和费用。
     * 该方法使用@Scheduled注解，表示其为一个定时任务，固定间隔执行。通过调用roomService列出所有房间，
     * 并对每个房间计算能源消耗和费用。此计算过程是异步执行的，通过ROOM_TEMPERATURE_EXECUTOR线程池进行。
     */
    @Async
    @Scheduled(fixedRate = 30000, initialDelay = 1000)
    public void calculateEnergyAndCost() {
        List<Room> rooms = roomService.list(); // 获取所有房间信息
        for (Room room : rooms) {
            final int roomId = room.getRoomId();
            ROOM_TEMPERATURE_EXECUTOR.execute(() -> {
                Thread.currentThread().setName("Room-cost-" + roomId); // 设置当前线程名称，便于日志定位
                // 只有房间状态为开启且服务状态为服务中时，才进行能源和费用的计算
                if (Objects.equals(room.getStatus(), "on") && Objects.equals(room.getServiceStatus(), "serving")) {
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
                        queryTime = LocalDateTime.now(); // 更新当前时间，计算持续时间
                        int duration = latestLog.getDuration();
                        // 根据风速调整能源消耗
                        if (Objects.equals(room.getFanSpeed(), "low")) {
                            totalEnergyConsumed += (float) (duration / 60 * 0.8);
                        } else if (Objects.equals(room.getFanSpeed(), "medium")) {
                            totalEnergyConsumed += (float) (duration / 60 * 1.0);
                        } else if (Objects.equals(room.getFanSpeed(), "high")) {
                            totalEnergyConsumed += (float) (duration / 60 * 1.2);
                        }
                        // 计算总费用
                        totalCost = (float) (totalEnergyConsumed * 5.0);
                    }

                    // 更新房间的能源消耗和累计费用
                    room.setEnergyConsumed(totalEnergyConsumed);
                    room.setCostAccumulated(totalCost);
                    roomService.updateRoom(room); // 保存更新
                }
                else {
                    roomService.setRoomCost(roomId, LocalDateTime.now());
                }
            });
        }
    }



    /**
     * 定时任务，用于扫描调度队列以管理空调状态。
     * 当队列为空时，设置空调为standby状态；当队列非空时，设置空调为on状态，并处理队列中的请求。
     * 处理过程包括设置空调状态为on，服务状态为serving，更新房间状态，记录控制日志，并通过WebSocket通知前端。
     * 使用线程池来并发处理队列中的请求，每次任务执行时，根据当前队列长度与最大线程数确定启动的线程数量。
     * 每个线程会获取当前用户的房间并尝试更新其状态。
     *
     * @Scheduled 注解指定了任务的执行周期为2000毫秒。
     */
    @Scheduled(fixedRate = 10000)
    public void checkSchedulerQueue() {
        CentralUnit centralUnit = centralUnitService.getById(1);
        if(centralUnit.getStatus().equals("on") || centralUnit.getStatus().equals("standby")){
            // 计算当前队列中的房间数量
            AtomicInteger queueLength = new AtomicInteger(roomMap.size());

            // 确定需要启动的线程数量，限制为队列长度与最大线程数中的较小值
            int threadsToStart = Math.min(queueLength.get(), MAX_THREADS);

            // 启动线程池中的线程以处理队列中的请求
            for (int i = 0; i < threadsToStart; i++) {
                AIR_CONDITIONER_EXECUTOR.execute(() -> {
                    Room room = roomService.current_userRoom(); // 获取请求用户的房间

                    // 如果找到房间，则处理该房间的请求
                    if (room != null) {
                        centralUnit.setStatus("on");
                        centralUnitService.updateById(centralUnit);
                        roomMap.computeIfPresent(room.getRoomId(), (id, r) -> {
                            log.info("{} :正在处理 {} 房间的请求",LocalDateTime.now(),room.getRoomId());
                            room.setStatus("on");
                            room.setServiceStatus("serving");
                            roomService.updateRoom(room);
                            // 创建新的记录控制日志
                            controlLogService.addControlLog(room);
                            // 在处理完成后，清除该房间的处理标记
                            processingRooms.remove(id);
                            // 通过WebSocket通知前端请求已完成
//                        log.info("username:{}",BaseContext.getCurrentUser());
//                        log.info("room:{}",room.getRoomId());
                            User user = userService.getUserByRoomId(room.getRoomId());
                            if (user != null) {
                                username = user.getUsername();
                                try {
                                    RoomVO roomVO = new RoomVO();
                                    //使用复制属性的方式，将room的属性复制到roomVO中
                                    BeanUtils.copyProperties(room, roomVO);
                                    webSocketRequest.sendMessage(username, new Response(200, "请求已完成", roomVO));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            return null; // 从房间映射中移除已处理的房间
                        });
                    }
                });
            }
            if(threadsToStart == 0) {
                // 只有所有房间不处于serving状态时，才设置空调为standby状态
                if (allRoomsNotServing()) {
                    centralUnit.setStatus("standby");
                    centralUnitService.updateById(centralUnit);
                }else{
                    centralUnit.setStatus("on");
                    centralUnitService.updateById(centralUnit);
                }
            }
        }

    }

    /**
     * 检查所有房间是否都不在服务状态。
     *
     * 该方法通过调用roomService.list()获取房间列表，并遍历每个房间，检查其服务状态和服务状态是否为"on"。
     * 如果发现任何房间的服务状态为"serving"且状态为"on"，则认为至少有一个房间在服务状态，返回false。
     * 如果所有房间都不在服务状态，则返回true。
     *
     * @return boolean 如果所有房间都不在服务状态，则返回true；如果至少有一个房间处于服务状态，则返回false。
     */
    private boolean allRoomsNotServing() {
        // 获取房间列表
        List<Room> rooms = roomService.list();
        for (Room room : rooms) {
            // 检查房间是否在服务状态
            if ("serving".equals(room.getServiceStatus()) && "on".equals(room.getStatus())) {
                return false;
            }
        }
        // 所有房间都不在服务状态，返回true
        return true;
    }




    /**
     * 根据更新频率，更新从控机的状态。
     * 每秒扫描一次，如果当前频率和更新频率不相同，需要进行累计，达到更新率的时候通知前端。
     * 如果频率相同，则不进行操作。
     */
    @Scheduled(fixedRate = 1000) // 每秒扫描一次
    public void updateRoomStatus() throws JsonProcessingException {
        // 检查当前频率是否与更新频率一致，若一致则直接返回
        if (FrequencyConstant.frequency != frequency) {
            // 若不一致，则更新频率并重置当前频率计数
            frequency = FrequencyConstant.frequency;
            currentFrequency = 0;
        } else {
            // 当累计的频率达到更新频率时，执行状态更新和通知操作
            if (currentFrequency == frequency) {
                // 获取从控机状态
                List<Room> rooms = roomService.list();
                //转换成roomVO
                List<RoomVO> roomVOList = new ArrayList<>();
                for (Room room : rooms) {
                    RoomVO roomVO = new RoomVO();
                    //使用复制属性的方式，将room的属性复制到roomVO中
                    BeanUtils.copyProperties(room, roomVO);
                    roomVOList.add(roomVO);
                }
                // 通知接口: /air/RoomStatus
                Response response = new Response(200, "从控机状态已更新", roomVOList);
                // 通过WebSocket通知前端
                webSocketStatus.sendMessage("admin", response);
                // 重置当前频率计数
                currentFrequency = 0;
//            template.convertAndSend("/air/RoomStatus", response);
            } else {
                currentFrequency++;
            }
        }


    }

    //检查调度队列为空时,设置空调为standby状态
//    @Scheduled(fixedRate = 1000) // 每秒扫描一次
//    public void checkQueue() {
//        if (roomMap.isEmpty()) {
//            CentralUnit centralUnit = centralUnitService.getById(1);
//            if (Objects.equals(centralUnit.getStatus(), "on")){
//                centralUnit.setStatus("standby");
//                centralUnitService.updateById(centralUnit);
//            }
//        }
//    }

}
