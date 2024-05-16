package com.example.sebackend.schedule;

import com.example.sebackend.context.EnvironmentConstant;
import com.example.sebackend.entity.Room;
import com.example.sebackend.service.ICentralUnitService;
import com.example.sebackend.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ScheduleTask {

    @Autowired
    private final IRoomService roomService;
    @Autowired
    private ICentralUnitService centralUnitService;
    @Autowired
    private final ConcurrentHashMap<Integer,Room> roomMap;

    private static final ExecutorService ROOM_TEMPERATURE_EXECUTOR = Executors.newFixedThreadPool(10);
    private static final ExecutorService AIR_CONDITIONER_EXECUTOR = Executors.newFixedThreadPool(3);

    public ScheduleTask(IRoomService roomService, ConcurrentHashMap<Integer, Room> roomMap) {
        this.roomService = roomService;
        this.roomMap = roomMap;
    }

    @Async
    @Scheduled(cron = "0 0 1 1 * ?") // Execute at 1:00 AM on the first day of each month
    public void modifyCentralAirConditionerSettings() {
        int currentMonth = LocalDate.now().getMonthValue();
        String mode = (currentMonth >= 11 || currentMonth <= 1) ? "cooling" : "heating";
        float defaultTemperature = (mode.equals("cooling")) ? 22.0f : 28.0f;
        centralUnitService.setMode(mode);
        centralUnitService.segfaultTemperature(defaultTemperature);
        EnvironmentConstant.environmentTemperature =(mode.equals("cooling")) ? 30.0f : 10.0f ;
        System.out.print("执行modifyCentralAirConditionerSettings");
//        for (Room room : rooms) {
//            room.setMode(mode);
//            roomService.updateRoom(room);
//        }
    }

    @Async
    @Scheduled(fixedRate = 60000) // Execute every minute
    public void adjustRoomTemperature() {
        //定时任务,一分钟,高速变化0.6,中速变化0.5,低速变化0.4-多线程
        //
        //设置环境温度->获取环境温度
        Float environmentTemperature = EnvironmentConstant.environmentTemperature;
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
                //房间status为off/standby
                if (Objects.equals(room.getStatus(), "off") || Objects.equals(room.getStatus(), "standby")){
                    //定时任务每分钟变化0.5度,向环境温度靠近,将新的值写入数据库
                    if(room.getCurrentTemperature() < environmentTemperature){
                        room.setCurrentTemperature(room.getCurrentTemperature() + 0.5f);
                    }else if(room.getCurrentTemperature() > environmentTemperature){
                        room.setCurrentTemperature(room.getCurrentTemperature() - 0.5f);
                    }
                    System.out.printf("room:"+room);
                    roomService.updateRoom(room);
                    //如果为standby模式,目标温度和当前温度差值为1,设置房间空调waiting,将请求加入到等待队列中
                    if (Math.abs(room.getTargetTemperature() - room.getCurrentTemperature()) >= 1){
                        room.setServiceStatus("waiting");
                        roomService.updateRoom(room);
                        roomMap.put(roomId, room);
                    }
                }

                if (Objects.equals(room.getStatus(), "on") && Objects.equals(room.getServiceStatus(), "serving")){
                    //根据空调的制冷或制热模式,高速变化0.6,中速变化0.5,低速变化0.4,
                    if (Objects.equals(room.getMode(), "cooling")){
                        if (Objects.equals(room.getFanSpeed(), "high")){
                            room.setCurrentTemperature(room.getCurrentTemperature() - 0.6f);
                        }else if (Objects.equals(room.getFanSpeed(), "medium")){
                            room.setCurrentTemperature(room.getCurrentTemperature() - 0.5f);
                        }else if (Objects.equals(room.getFanSpeed(), "low")){
                            room.setCurrentTemperature(room.getCurrentTemperature() - 0.4f);
                        }
                    }else if (Objects.equals(room.getMode(), "heating")){
                        if (Objects.equals(room.getFanSpeed(), "high")){
                            room.setCurrentTemperature(room.getCurrentTemperature() + 0.6f);
                        }else if (Objects.equals(room.getFanSpeed(), "medium")){
                            room.setCurrentTemperature(room.getCurrentTemperature() + 0.5f);
                        }else if (Objects.equals(room.getFanSpeed(), "low")){
                            room.setCurrentTemperature(room.getCurrentTemperature() + 0.4f);
                        }
                    }
                    //根据风速将energyConsumed(+0.8,1,1.2),
                    if (Objects.equals(room.getFanSpeed(), "high")){
                        room.setEnergyConsumed(room.getEnergyConsumed() + 0.8f);
                    }else if (Objects.equals(room.getFanSpeed(), "medium")){
                        room.setEnergyConsumed(room.getEnergyConsumed() + 1.0f);
                    }else if (Objects.equals(room.getFanSpeed(), "low")){
                        room.setEnergyConsumed(room.getEnergyConsumed() + 1.2f);
                    }
                    //重新计算costAccumulated,并写入(5元/一个标准功率),
                    room.setCostAccumulated(room.getCostAccumulated() + room.getEnergyConsumed() * 5);
                    //判断当前温度和目标温度相同,将房间空调设置成standby模式;
                    if (Math.abs(room.getTargetTemperature() - room.getCurrentTemperature()) < 1){
                        room.setStatus("standby");
                    }
                    roomService.updateRoom(room);
                }
            });
        }
    }

    @Scheduled(fixedRate = 10000) // Execute every 10 seconds
    public void checkSchedulerQueue() {
        for (int i = 1; i <= 3; i++) {
            final String queueName = "queue" + i;
            AIR_CONDITIONER_EXECUTOR.execute(() -> {
//                int queueLength = // Get the length of the scheduler queue "queueName"
                // Your implementation here
            });
        }
    }

}
