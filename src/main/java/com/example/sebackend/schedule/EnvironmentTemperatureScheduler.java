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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private static final Logger log = LoggerFactory.getLogger(EnvironmentTemperatureScheduler.class);

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


    /**
     * 初始化季节和温度。
     * 此方法根据当前月份和时间，设置当前季节和相应的起始温度。
     * 月份在4月至9月之间被认为是夏季，其它时间被认为是冬季。
     * 不接受参数，也不返回任何值。
     */
    private void initializeSeasonAndTemperature() {
        // 获取当前月份值
        int month = LocalDate.now().getMonthValue();

        if (month >= 4 && month <= 9) { // 判断当前月份是否在夏季范围内
            isSummerSeason = true;
            currentTemperature = 25.0f; // 设置夏季起始温度
        } else { // 默认为冬季
            isSummerSeason = false;
            currentTemperature = 15.0f; // 冬季起始温度
        }
        // 将当前温度设置为环境温度
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
//            room.setCurrentTemperature(27.0f);
            room.setTargetTemperature(EnvironmentConstant.environmentTemperature);
            room.setFanSpeed("medium");
            // 设置房间状态为"off"
            room.setStatus("off");
//            room.setStatus("standby");
            room.setServiceStatus("waiting");
            // 更新房间信息到数据库
            roomService.updateById(room);
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
//            unit.setStatus("on");
            centralUnitService.updateById(unit);
        });
    }



    @Scheduled(cron = "0 0 0 1 4 ?") // 4月1日凌晨0点开始夏季
    public void startSummerSeason() {
        isSummerSeason = true;
        currentTemperature = 25.0f; // 设置夏季起始温度
        // 同步更新全局环境温度常量
        EnvironmentConstant.environmentTemperature = currentTemperature;
    }

    @Scheduled(cron = "0 0 0 1 10 ?") // 10月1日凌晨0点开始冬季
    public void startWinterSeason() {
        isSummerSeason = false;
        currentTemperature = 15.0f; // 设置冬季起始温度
        // 同步更新全局环境温度常量
        EnvironmentConstant.environmentTemperature = currentTemperature;
    }

    /**
     * 定时更新环境温度。
     * 该方法每20s执行一次，根据当前季节和时间调整环境温度，并保存新的温度值。
     * 夏季温度会在白天上升，夜间下降，冬季则相反。
     * 并且温度值会受到限制，保持在合理的范围内。
     * 本方法不接受参数，也不返回任何值。
     * 利用定时任务框架（如Quartz或Spring的@Scheduled注解）每分钟调用以更新温度。
     */
    @Scheduled(fixedRate = 10000) // 每10秒执行一次
    public void updateEnvironmentTemperature() {
        float temperatureChange = 0.5f; // 每10秒温度变化量

        if (isSummerSeason) {
            currentTemperature = Math.min(currentTemperature + temperatureChange, 80.0f); // 夏季温度上升，限制最高温度
        } else {
            currentTemperature = Math.max(currentTemperature - temperatureChange, -40.0f); // 冬季温度下降，限制最低温度
        }

        // 更新全局环境温度变量
        EnvironmentConstant.environmentTemperature = currentTemperature;

        // 创建新的环境温度记录
        EnvironmentTemperature environmentTemperature = new EnvironmentTemperature();
        environmentTemperature.setTemperature(currentTemperature);

        // 更新所有房间的当前温度
        roomService.updateRoomTemperatures(currentTemperature, temperatureChange);
        log.info(String.format("环境温度为: %.1f", currentTemperature));
    }


}

