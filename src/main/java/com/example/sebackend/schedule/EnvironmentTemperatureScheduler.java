package com.example.sebackend.schedule;

import com.example.sebackend.context.EnvironmentConstant;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.EnvironmentTemperature;
import com.example.sebackend.entity.Room;
import com.example.sebackend.service.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/16
 * @project SE-backend
 */
@Component
public class EnvironmentTemperatureScheduler implements InitializingBean {
    @Autowired
    private IEnvironmentTemperatureService environmentTemperatureService;

    @Autowired
    private IRoomService roomService;

    @Autowired
    private ICentralUnitService centralUnitService;
    private float currentTemperature;
    private boolean isSummerSeason;
    @Autowired
    IUsageRecordService usageRecordService;
    @Autowired
    private IControlLogService controlLogService;

    /**
     * 当组件属性设置完成后执行的初始化操作。
     * 该方法不接受参数，也不返回任何值。
     * 主要完成以下初始化工作：
     * 1. 初始化季节和温度设置。
     * 2. 将所有房间重置为默认状态。
     * 3. 将所有中央单元重置为默认状态。
     */
    @Override
    public void afterPropertiesSet() {
        // 初始化季节和温度
        initializeSeasonAndTemperature();
        // 重置所有房间为默认状态
        resetAllRoomsToDefaultState();
        // 重置所有中央单元为默认状态
        resetAllCentralUnitsToDefaultState();
    }


    private void initializeSeasonAndTemperature() {
        int month = LocalDate.now().getMonthValue();
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        if (month >= 4 && month <= 9) { // 夏季月份4月到9月
            isSummerSeason = true;
            // 判断当前时间是否在白天
            if (hour >= 6 && hour < 18) {
                currentTemperature = 20.0f; // 夏季白天起始温度
            } else {
                currentTemperature = 35.0f; // 夏季夜晚起始温度
            }
        } else { // 冬季月份10月到3月
            isSummerSeason = false;
            // 判断当前时间是否在白天
            if (hour >= 6 && hour < 18) {
                currentTemperature = -5.0f; // 冬季白天起始温度
            } else {
                currentTemperature = 10.0f; // 冬季夜晚起始温度
            }
        }
        EnvironmentConstant.environmentTemperature = currentTemperature;

    }

    /**
     * 重置所有房间到默认状态
     * 该方法会将所有房间的当前温度、目标温度设置为环境温度，并将房间状态设置为"off"，然后更新到数据库。
     * 注意：该方法不接受任何参数，也没有返回值。
     */
    private void resetAllRoomsToDefaultState() {
        // 获取所有房间列表
        List<Room> rooms = roomService.list();

        // 遍历房间列表，重置每个房间的状态
        for (Room room : rooms) {
            // 设置当前温度和目标温度为环境温度
            room.setCurrentTemperature(EnvironmentConstant.environmentTemperature);
//            room.setCurrentTemperature(25.0f);
            room.setTargetTemperature(EnvironmentConstant.environmentTemperature);
            room.setFanSpeed("medium");
            // 设置房间状态为"off"
            room.setStatus("off");
//            room.setStatus("standby");
            // 更新房间信息到数据库
            roomService.updateById(room);
            //关机记录写入到数据库
            usageRecordService.saveEndRecord(room.getRoomId(), LocalDateTime.now());
            //设置controlLog结束时间
//            controlLogService.setLatestLog(room.getRoomId(), LocalDateTime.now(), true, room.getCurrentTemperature());

        }
    }

    /**
     * 重置所有中心单元至默认状态
     * 该方法遍历所有中心单元，并将它们的状态设置为"off"，然后更新到数据库。
     * 注意：此方法没有参数，也没有返回值。
     */
    private void resetAllCentralUnitsToDefaultState() {
        // 从中央单元服务获取所有中央单元的列表
        List<CentralUnit> units = centralUnitService.list();

        // 遍历列表，将每个单元的状态设置为"off"，并更新到数据库
        units.forEach(unit -> {
            unit.setStatus("off");
            centralUnitService.updateById(unit);
        });
    }



    @Scheduled(cron = "0 0 0 1 4 ?") // 4月1日凌晨0点开始夏季
    public void startSummerSeason() {
        isSummerSeason = true;
        currentTemperature = 20.0f; // 设置夏季起始温度
        // 同步更新全局环境温度常量
        EnvironmentConstant.environmentTemperature = currentTemperature;
    }

    @Scheduled(cron = "0 0 0 1 10 ?") // 10月1日凌晨0点开始冬季
    public void startWinterSeason() {
        isSummerSeason = false;
        currentTemperature = -5.0f; // 设置冬季起始温度
        // 同步更新全局环境温度常量
        EnvironmentConstant.environmentTemperature = currentTemperature;
    }

    /**
     * 定时更新环境温度。
     * 该方法每分钟执行一次，根据当前季节和时间调整环境温度，并保存新的温度值。
     * 夏季温度会在白天上升，夜间下降，冬季则相反。
     * 并且温度值会受到限制，保持在合理的范围内。
     */
    @Scheduled(fixedRate = 60000) // 每1分钟执行一次
    public void updateEnvironmentTemperature() {
        LocalTime now = LocalTime.now(); // 获取当前时间
        int hour = now.getHour(); // 获取当前小时数
        float temperatureChange = 0.1f; // 每分钟温度变化量
        System.out.println("环境温度为: " + currentTemperature);
        // 根据季节调整温度
        if (isSummerSeason) {
            // 夏季温度变化逻辑
            currentTemperature += (hour >= 6 && hour < 18) ? temperatureChange : -temperatureChange;
            // 限制夏季温度范围
            currentTemperature = Math.min(Math.max(currentTemperature, 20.0f), 35.0f);
        } else {
            // 冬季温度变化逻辑
            currentTemperature += (hour >= 6 && hour < 18) ? temperatureChange : -temperatureChange;
            // 限制冬季温度范围
            currentTemperature = Math.min(Math.max(currentTemperature, -5.0f), 10.0f);
        }

        // 同步更新全局环境温度常量
        EnvironmentConstant.environmentTemperature = currentTemperature;

        // 创建并保存环境温度记录
        EnvironmentTemperature environmentTemperature = new EnvironmentTemperature();
        environmentTemperature.setTemperature(currentTemperature);

        // 保存环境温度
        // environmentTemperatureService.save(environmentTemperature);
        // 更新所有房间的温度
        roomService.updateRoomTemperatures(currentTemperature);
    }

}

