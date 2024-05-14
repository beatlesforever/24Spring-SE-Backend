package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Room;
import com.example.sebackend.entity.User;
import com.example.sebackend.mapper.CentralUnitMapper;
import com.example.sebackend.mapper.RoomMapper;
import com.example.sebackend.mapper.UserMapper;
import com.example.sebackend.service.ICentralUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Service
public class CentralUnitServiceImpl extends ServiceImpl<CentralUnitMapper, CentralUnit> implements ICentralUnitService {
    @Autowired
    CentralUnitMapper centralUnitMapper;
    @Autowired
    RoomMapper roomMapper;
    @Autowired
    UserMapper userMapper;



    @Override
    public CentralUnit turnOn() {
        //更改中央空调状态为开启
        //设置默认工作模式为制冷
        //设置默认工作温度为22度
        //设置最高工作温度为25度
        //设置最低工作温度为18度
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
        CentralUnit centralUnit =centralUnitMapper.getCentral();
        centralUnit.setStatus("off");
        centralUnitMapper.update(centralUnit);
        return null;
    }

    @Override
    public CentralUnit authen(User user) {
        //查找用户
        int roomId = userMapper.getByRoomId(user.getRoomId());
        //查找房间
        Room room = roomMapper.getId(roomId);
        //设置工作模式和温度
        room.setMode(centralUnitMapper.getCentral().getMode());
        room.setCurrentTemperature(centralUnitMapper.getCentral().getCurrentTemperature());
        roomMapper.update(room);
        return null;
    }
}