package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Room;

import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
public interface ICentralUnitService extends IService<CentralUnit> {
     int fulfill(float targetTemperature, String targetSpeed);

    CentralUnit turnOn();

    CentralUnit turnOff();

    CentralUnit authen();

    List<Room> getStatus();

    CentralUnit uodateFrequency(int frequency);

    void setMode(String mode);

    void segfaultTemperature(float defaultTemperature);
}
