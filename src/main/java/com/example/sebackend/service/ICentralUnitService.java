package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Response;
import com.example.sebackend.entity.Room;

import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
public interface ICentralUnitService extends IService<CentralUnit> {

    CentralUnit turnOn();

    CentralUnit turnOff();

    CentralUnit turnStandBy();


    CentralUnit authen(int roomId);

    List<Room> getStatus();

    CentralUnit uodateFrequency(int frequency);

    void setMode(String mode);

    void segfaultTemperature(float defaultTemperature);

    Response requests(float targetTemperature, String fanSpeed);

}
