package com.example.sebackend.schedule;

import com.example.sebackend.entity.Response;
import com.example.sebackend.entity.Room;
import com.example.sebackend.service.ICentralUnitService;
import com.example.sebackend.service.IControlLogService;
import com.example.sebackend.service.IRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

    private final ConcurrentHashMap<Integer, Room> roomMap;
    private final ConcurrentHashMap<Integer, Boolean> processingRooms;
    @Autowired
    public ScheduleTask(@Qualifier("roomQueue") ConcurrentHashMap<Integer, Room> roomQueue,
                       @Qualifier("processingRooms") ConcurrentHashMap<Integer, Boolean> processingRooms) {
        this.roomMap = roomQueue;
        this.processingRooms = processingRooms;
    }

    @Autowired
    private SimpMessagingTemplate template;

    private static  int count = 6;

    @Autowired
    public void configureTemplate(SimpMessagingTemplate template) {
        this.template.setDefaultDestination("/air/requestServing");
    }

    @Autowired
    IControlLogService controlLogService;

    private static final ExecutorService ROOM_TEMPERATURE_EXECUTOR = Executors.newFixedThreadPool(10);
    private static final int MAX_THREADS = 3;
    private static final ExecutorService AIR_CONDITIONER_EXECUTOR = Executors.newFixedThreadPool(MAX_THREADS);



    @Async
    @Scheduled(cron = "0 0 1 1 * ?") // Execute at 1:00 AM on the first day of each month
    public void modifyCentralAirConditionerSettings() {
        int currentMonth = LocalDate.now().getMonthValue();
        String mode = (currentMonth >= 10 || currentMonth <= 3) ? "cooling" : "heating";
        float defaultTemperature = (mode.equals("cooling")) ? 22.0f : 28.0f;
        centralUnitService.setMode(mode);
        centralUnitService.segfaultTemperature(defaultTemperature);
        System.out.println("执行modifyCentralAirConditionerSettings");
    }

    //从控机机工作状态下,房间的温度变化调整
    //10s检测房间温度,把请求放到调度队列中,60s处理房间温度和记录消费
    @Async
    //延迟1s执行

    @Scheduled(fixedRate = 10000 ,initialDelay = 1000) // Execute every minute
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
                        System.out.printf("room %d is set in queue mode%n", roomId);
                    }
                }
                count--;
                if (count == 0) {
                    System.out.printf("count: %d%n", count);
                    System.out.println("adjustRoomTemperature");
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
                    count=6;//重置
                }


            });
        }
    }

    //多线程定时任务,不断的扫描调度队列的长度,如果为空设置空调为standby,否则设置为on
    //
    //取出队列中的请求,设置空调状态为on,服务为serving,
    // 将请求在队列中删除,填写usage_record表
    //
    //开始时间为当前时间,结束时间为下一个请求的开始时间
    //
    //使用线程池,获取队列中的第一个请求
    //
    //请求被处理后,使用websocket通知前端
    @Scheduled(fixedRate = 5000) // Execute every 3 seconds
    public void checkSchedulerQueue() {
        // 获取当前队列中的房间数量
        AtomicInteger queueLength = new AtomicInteger(roomMap.size());
        System.out.printf("当前队列中的房间数量: %d%n", queueLength.get());

        // 确定需要启动的线程数量
        int threadsToStart = Math.min(queueLength.get(), MAX_THREADS);
        System.out.printf("需要启动的线程数量: %d%n", threadsToStart);
        for (int i = 0; i < threadsToStart; i++) {
            AIR_CONDITIONER_EXECUTOR.execute(() -> {
                System.out.printf("当前线程: %s%n", Thread.currentThread().getName());
                Room room = roomService.current_userRoom(); // 使用方法获取当前用户的房间
                if (room != null) {
                    roomMap.computeIfPresent(room.getRoomId(), (id, r) -> {
                        log.info("Processing room: {}", room);
                        room.setStatus("on");
                        room.setServiceStatus("serving");
                        roomService.updateRoom(room);
                        controlLogService.addControlLog(room);
                        //清除标记
                        processingRooms.remove(id); // 清除处理标记
                        // 请求被处理后, 使用websocket通知前端
                        template.convertAndSend("/air/requestServing", new Response(200, "请求已完成", room));
                        return null; // 删除处理过的房间
                    });
                }

            });
        }
    }
    private void processRoom(Room room) {
        log.info("Processing room: {}", room);
        room.setStatus("on");
        room.setServiceStatus("serving");

        roomService.updateRoom(room);
        controlLogService.addControlLog(room);
        // 请求被处理后, 使用websocket通知前端
        template.convertAndSend("/air/requestServing", new Response(200, "请求已完成", room));
    }



}
