package com.example.sebackend.schedule;

import com.example.sebackend.context.EnvironmentConstant;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.EnvironmentTemperature;
import com.example.sebackend.entity.Room;
import com.example.sebackend.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class EnvironmentTemperatureScheduler {
    private static final Logger log = LoggerFactory.getLogger(EnvironmentTemperatureScheduler.class);

    @Autowired
    private IEnvironmentTemperatureService environmentTemperatureService;

    @Autowired
    private IRoomService roomService;

    @Autowired
    private ICentralUnitService centralUnitService;

    @Autowired
    IUsageRecordService usageRecordService;

    @Autowired
    private IControlLogService controlLogService;

    private float currentTemperature;
    private boolean isSummerSeason;

    /**
     * 使用Spring事件监听器在上下文刷新完成后执行初始化。
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initializeSeasonAndTemperature();
        resetAllRoomsToDefaultState();
        resetAllCentralUnitsToDefaultState();
    }

    private void initializeSeasonAndTemperature() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 4 && month <= 9) {
            isSummerSeason = true;
            currentTemperature = 25.0f;
        } else {
            isSummerSeason = false;
            currentTemperature = 15.0f;
        }
        EnvironmentConstant.environmentTemperature = currentTemperature;
    }

    private void resetAllRoomsToDefaultState() {
        List<Room> rooms = roomService.list();
        for (Room room : rooms) {
            room.setCurrentTemperature(EnvironmentConstant.environmentTemperature);
            room.setTargetTemperature(EnvironmentConstant.environmentTemperature);
            room.setFanSpeed("medium");
            room.setStatus("off");
            room.setServiceStatus("waiting");
            roomService.updateById(room);
        }
    }

    private void resetAllCentralUnitsToDefaultState() {
        List<CentralUnit> units = centralUnitService.list();
        units.forEach(unit -> {
            unit.setStatus("off");
            centralUnitService.updateById(unit);
        });
    }

    /**
     * 定时任务：每月4日0点启动夏季季节设置。
     * 该方法通过设置标志和当前温度来开启夏季季节模式。
     * 同时，也将当前温度更新到全局环境温度变量中，以影响整个系统的温度表现。
     * 使用CRON表达式 "0 0 0 1 4 ?" 定义了执行周期，即每月4日的凌晨0点。
     */
    @Scheduled(cron = "0 0 0 1 4 ?")
    public void startSummerSeason() {
        isSummerSeason = true;
        currentTemperature = 25.0f;
        EnvironmentConstant.environmentTemperature = currentTemperature;
    }


    @Scheduled(cron = "0 0 0 1 10 ?")
    public void startWinterSeason() {
        isSummerSeason = false;
        currentTemperature = 15.0f;
        EnvironmentConstant.environmentTemperature = currentTemperature;
    }

    /**
     * 定时任务，每隔10秒执行一次，用于更新环境温度。
     * 根据当前季节调整温度，并更新环境温度常量及房间温度。
     * 在夏季，温度会上升；在非夏季，温度会下降。
     * 调整后的温度不会超过80华氏度或低于-40华氏度。
     */
    @Scheduled(fixedRate = 10000)
    public void updateEnvironmentTemperature() {
        // 定义温度变化量
        float temperatureChange = 0.5f;
        // 根据季节判断是增加温度还是减少温度
        if (isSummerSeason) {
            currentTemperature = Math.min(currentTemperature + temperatureChange, 80.0f);
        } else {
            currentTemperature = Math.max(currentTemperature - temperatureChange, -40.0f);
        }
        // 更新环境温度常量
        EnvironmentConstant.environmentTemperature = currentTemperature;
        // 创建一个新的EnvironmentTemperature对象，并设置温度
        EnvironmentTemperature environmentTemperature = new EnvironmentTemperature();
        environmentTemperature.setTemperature(currentTemperature);
        // 通知房间服务更新所有房间的温度
        roomService.updateRoomTemperatures(currentTemperature, temperatureChange);
        // 记录当前环境温度
        log.info(String.format("环境温度为: %.1f", currentTemperature));
    }

}
