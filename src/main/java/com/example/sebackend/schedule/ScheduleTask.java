package com.example.sebackend.schedule;

import com.example.sebackend.context.BaseContext;
import com.example.sebackend.context.FrequencyConstant;
import com.example.sebackend.entity.Response;
import com.example.sebackend.entity.Room;
import com.example.sebackend.entity.RoomVO;
import com.example.sebackend.entity.User;
import com.example.sebackend.service.ICentralUnitService;
import com.example.sebackend.service.IControlLogService;
import com.example.sebackend.service.IRoomService;
import com.example.sebackend.service.IUserService;
import com.example.sebackend.websocket.WebSocketRequest;
import com.example.sebackend.websocket.WebSocketStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
//import websocket.WebSocketServer;

import java.time.LocalDate;
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

    private final ConcurrentHashMap<Integer, Room> roomMap;
    private final ConcurrentHashMap<Integer, Boolean> processingRooms;
    private String username;

    @Autowired
    public ScheduleTask(@Qualifier("roomQueue") ConcurrentHashMap<Integer, Room> roomQueue,
                        @Qualifier("processingRooms") ConcurrentHashMap<Integer, Boolean> processingRooms) {
        this.roomMap = roomQueue;
        this.processingRooms = processingRooms;
    }

//    @Autowired
//    private SimpMessagingTemplate template;
    @Autowired
    private WebSocketStatus webSocketStatus;
    @Autowired
    private WebSocketRequest webSocketRequest;


    private static int count = 6;
    private static int frequency = 0;
    private static int currentFrequency = 0;

//    @Autowired
//    public void configureTemplate(SimpMessagingTemplate template) {
//        this.template.setDefaultDestination("/air/requestServing");
//    }

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
    //10s检测房间温度,把请求放到调度队列中,60s处理房间温度和记录消费
    @Async
    @Scheduled(fixedRate = 10000, initialDelay = 1000) // Execute every minute
    public void adjustRoomTemperature() {
        //定时任务,一分钟,高速变化0.6,中速变化0.5,低速变化0.4-多线程
        //
        //房间status为off/standby时,定时任务每分钟变化0.5度,向环境温度靠近,将新的值写入数据库
        //如果为standby模式,目标温度和当前温度差值为1,设置房间空调waiting,将请求加入到等待队列中
        //
        //房间status为on状态,且服务状态为serving时
        //
        //定时任务(定时从数据库中把房间取出,修改之后再存回数据库):
        // 根据空调的制冷或制热模式,高速变化0.6,中速变化0.5,低速变化0.4,
        // 根据风速将energyConsumed(+0.8,1,1.2),
        // 重新计算costAccumulated,并写入(5元/一个标准功率),
        // 判断当前温度和目标温度相同,将房间空调设置成standby模式;

        List<Room> rooms = roomService.list();
        for (Room room : rooms) {
            final int roomId = room.getRoomId();
            ROOM_TEMPERATURE_EXECUTOR.execute(() -> {
                Thread.currentThread().setName("Room-" + roomId);
                //房间status为off/standby时,向环境温度靠近,将新的值写入数据库,在环境温度的实现类中完成
                //如果为standby模式,目标温度和当前温度差值为1,设置房间空调waiting,将请求加入到等待队列中
                if (Objects.equals(room.getStatus(), "standby")) {
                    if (Math.abs(room.getTargetTemperature() - room.getCurrentTemperature()) >= 1) {
                        room.setServiceStatus("waiting");
                        roomService.updateRoom(room);
                        roomMap.put(roomId, room);
//                        System.out.printf("room %d is set in queue mode%n", roomId);
                    }
                }
                count--;
                if (count == 0) {
//                    System.out.printf("count: %d%n", count);
//                    System.out.println("adjustRoomTemperature");
                    //如果房间的温度是on且从控机的请求已经被实现
                    if (Objects.equals(room.getStatus(), "on") && Objects.equals(room.getServiceStatus(), "serving")) {
                        //根据空调的制冷或制热模式,高速变化0.6,中速变化0.5,低速变化0.4,
                        //设置从控机的温度变化
                        if (Objects.equals(room.getMode(), "cooling")) {
                            if (Objects.equals(room.getFanSpeed(), "high")) {
                                room.setCurrentTemperature(room.getCurrentTemperature() - 0.6f);
                            } else if (Objects.equals(room.getFanSpeed(), "medium")) {
                                room.setCurrentTemperature(room.getCurrentTemperature() - 0.5f);
                            } else if (Objects.equals(room.getFanSpeed(), "low")) {
                                room.setCurrentTemperature(room.getCurrentTemperature() - 0.4f);
                            }
                        } else if (Objects.equals(room.getMode(), "heating")) {
                            if (Objects.equals(room.getFanSpeed(), "high")) {
                                room.setCurrentTemperature(room.getCurrentTemperature() + 0.6f);
                            } else if (Objects.equals(room.getFanSpeed(), "medium")) {
                                room.setCurrentTemperature(room.getCurrentTemperature() + 0.5f);
                            } else if (Objects.equals(room.getFanSpeed(), "low")) {
                                room.setCurrentTemperature(room.getCurrentTemperature() + 0.4f);
                            }
                        }
                        //设置从控机的能量消耗
                        //根据风速将energyConsumed(+0.8,1,1.2),
                        if (Objects.equals(room.getFanSpeed(), "high")) {
                            room.setEnergyConsumed(room.getEnergyConsumed() + 0.8f);
                        } else if (Objects.equals(room.getFanSpeed(), "medium")) {
                            room.setEnergyConsumed(room.getEnergyConsumed() + 1.0f);
                        } else if (Objects.equals(room.getFanSpeed(), "low")) {
                            room.setEnergyConsumed(room.getEnergyConsumed() + 1.2f);
                        }
                        //设置从控机的消费的金额
                        //重新计算costAccumulated,并写入(5元/一个标准功率),
                        room.setCostAccumulated(room.getCostAccumulated() + room.getEnergyConsumed() * 5);
                        //判断当前温度和目标温度相同,将房间空调设置成standby模式;
                        if (Math.abs(room.getTargetTemperature() - room.getCurrentTemperature()) < 1) {
                            room.setStatus("standby");
                        }
                        roomService.updateRoom(room);
                    }
                    count = 6;//重置
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
     * @Scheduled 注解指定了任务的执行周期为5000毫秒。
     */
    @Scheduled(fixedRate = 5000)
    public void checkSchedulerQueue() {
        // 计算当前队列中的房间数量
        AtomicInteger queueLength = new AtomicInteger(roomMap.size());
        System.out.printf("当前队列中的房间数量: %d%n", queueLength.get());

        // 确定需要启动的线程数量，限制为队列长度与最大线程数中的较小值
        int threadsToStart = Math.min(queueLength.get(), MAX_THREADS);
        System.out.printf("需要启动的线程数量: %d%n", threadsToStart);

        // 启动线程池中的线程以处理队列中的请求
        for (int i = 0; i < threadsToStart; i++) {
            AIR_CONDITIONER_EXECUTOR.execute(() -> {
                System.out.printf("当前线程: %s%n", Thread.currentThread().getName());
                Room room = roomService.current_userRoom(); // 获取请求用户的房间

                // 如果找到房间，则处理该房间的请求
                if (room != null) {
                    roomMap.computeIfPresent(room.getRoomId(), (id, r) -> {
//                        log.info("Processing room: {}", room);
                        room.setStatus("on");
                        room.setServiceStatus("serving");
                        roomService.updateRoom(room);
                        controlLogService.addControlLog(room);
                        // 在处理完成后，清除该房间的处理标记
                        processingRooms.remove(id);
                        // 通过WebSocket通知前端请求已完成
//                        log.info("username:{}",BaseContext.getCurrentUser());
//                        log.info("room:{}",room.getRoomId());
                        User user=userService.getUserByRoomId(room.getRoomId());
                        if (user!=null) {
                            username= user.getUsername();
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
                log.info("WebSocketStatus.sendMessage");
                webSocketStatus.sendMessage("admin", response);
                // 重置当前频率计数
                currentFrequency = 0;
//            template.convertAndSend("/air/RoomStatus", response);
            }else {
                currentFrequency++;
            }
        }



    }

}
