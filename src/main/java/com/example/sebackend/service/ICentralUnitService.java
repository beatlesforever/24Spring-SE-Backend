package com.example.sebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.User;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
public interface ICentralUnitService extends IService<CentralUnit> {
    CentralUnit turnOn();

    CentralUnit turnOff();

    CentralUnit authen(User user);
}
