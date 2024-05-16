package com.example.sebackend.context;

import com.example.sebackend.entity.EnvironmentTemperature;
import com.example.sebackend.service.IEnvironmentTemperatureService;
import com.example.sebackend.service.IRoomService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

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

    private float currentTemperature;
    private boolean isSummerSeason;

    @Override
    public void afterPropertiesSet() {
        // 组件初始化时设置初始温度
        initializeSeasonAndTemperature();
    }

    private void initializeSeasonAndTemperature() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 4 && month <= 9) { // 夏季月份4月到9月
            isSummerSeason = true;
            currentTemperature = 20.0f; // 夏季初始温度
        } else { // 冬季月份10月到3月
            isSummerSeason = false;
            currentTemperature = -5.0f; // 冬季初始温度
        }
    }

    @Scheduled(cron = "0 0 0 1 4 ?") // 4月1日凌晨0点开始夏季
    public void startSummerSeason() {
        isSummerSeason = true;
        currentTemperature = 20.0f; // 设置夏季起始温度
    }

    @Scheduled(cron = "0 0 0 1 10 ?") // 10月1日凌晨0点开始冬季
    public void startWinterSeason() {
        isSummerSeason = false;
        currentTemperature = -5.0f; // 设置冬季起始温度
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

        // 创建新的环境温度对象，并设置当前温度值
        EnvironmentTemperature environmentTemperature = new EnvironmentTemperature();
        environmentTemperature.setTemperature(currentTemperature);
        // 保存环境温度
        environmentTemperatureService.save(environmentTemperature);
        // 更新所有房间的温度
        roomService.updateRoomTemperatures(currentTemperature);
    }

}
