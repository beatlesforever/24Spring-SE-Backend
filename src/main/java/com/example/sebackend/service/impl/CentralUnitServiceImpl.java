package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.context.BaseContext;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Room;
import com.example.sebackend.mapper.CentralUnitMapper;
import com.example.sebackend.mapper.RoomMapper;
import com.example.sebackend.mapper.UserMapper;
import com.example.sebackend.service.ICentralUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
@Slf4j

public class CentralUnitServiceImpl extends ServiceImpl<CentralUnitMapper, CentralUnit> implements ICentralUnitService {
    @Autowired
    CentralUnitMapper centralUnitMapper;
    @Autowired
    RoomMapper roomMapper;
    @Autowired
    UserMapper userMapper;


    //状态为0设置成失败,状态为1设置成成功
    @Override
    public int fulfill(float targetTemperature, String targetSpeed) {
        //从调用队列中获取到room请求,并进行处理
        if (extracted() != null) {
            Room room = extracted();
            Float currentTemperature = room.getCurrentTemperature();
            if (currentTemperature > targetTemperature && targetSpeed.equals("high")) {
                //不处理
                return 0;
            }
            if (currentTemperature < targetTemperature && targetSpeed.equals("low")) {
                //不处理
                return 0;
            }
            roomMapper.update(room);
            return 1;
        }
        return 404;//无房间号
    }

    //获取到请求,将请求信息添加到等待队列中,返回处理的请求
    public Room schedule() {

        return null;
    }

    private Room extracted() {
        log.info("User:{}", BaseContext.getCurrentUser());
        int roomId = userMapper.getByUsername(BaseContext.getCurrentUser());
        return roomMapper.getId(roomId);
    }


    @Override
    public CentralUnit turnOn() {
        //更改中央空调状态为开启,默认工作模式为制冷,设置默认工作温度为22度
        //设置最高工作温度为25度,设置最低工作温度为18度
        //根据环境温度设置
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        centralUnit.setStatus("on");
        centralUnit.setMode("cooling");
        centralUnit.setCurrentTemperature(22.0F);
        centralUnit.setDefaultTemperature(22.0F);
        centralUnit.setMaxTemperature(25.0F);
        centralUnit.setMinTemperature(18.0F);
        centralUnit.setCapacity(3);//从控机最大数量3
        centralUnitMapper.update(centralUnit);

        return centralUnit;

    }

    @Override
    public CentralUnit turnOff() {
        //更改中央空调状态为关闭
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        centralUnit.setStatus("off");
        centralUnitMapper.update(centralUnit);
        return centralUnit;
    }

    @Override
    public CentralUnit authen() {
        //查找用户
        Room room = extracted();
        log.info("room");
        //设置工作模式和温度
        room.setMode(centralUnitMapper.getCentral().getMode());
        room.setCurrentTemperature(centralUnitMapper.getCentral().getCurrentTemperature());
        roomMapper.update(room);
        return centralUnitMapper.getCentral();
    }

    @Override
    public List<Room> getStatus() {
        //获取从控机状态

        //8.	中央空调能够实时监测各房间的温度和状态，并要求实时刷新的频率能够进行配置
        return roomMapper.list();
    }

    @Override
    public CentralUnit uodateFrequency(int frequency) {
        centralUnitMapper.updateFrequency(frequency);
        return centralUnitMapper.getCentral();
    }

    @Override
    public void setMode(String mode) {
        //修改中央空调工作模式
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        centralUnit.setMode(mode);
        centralUnitMapper.update(centralUnit);

    }

    @Override
    public void segfaultTemperature(float defaultTemperature) {
        //设置中央空调的缺省温度
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        centralUnit.setDefaultTemperature(defaultTemperature);
        centralUnitMapper.update(centralUnit);
    }


}