package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.context.BaseContext;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Response;
import com.example.sebackend.entity.Room;
import com.example.sebackend.mapper.CentralUnitMapper;
import com.example.sebackend.mapper.RoomMapper;
import com.example.sebackend.mapper.UserMapper;
import com.example.sebackend.service.ICentralUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
    @Autowired
    private final ConcurrentHashMap<Integer,Room> roomMap;
    @Autowired
    private SimpMessagingTemplate template;


    public CentralUnitServiceImpl(ConcurrentHashMap<Integer, Room> roomMap) {
        this.roomMap = roomMap;
    }


    //状态为0设置成失败,状态为1设置成成功
    @Override
    public int fulfill(float targetTemperature, String targetSpeed) {
        //从调用队列中获取到room请求,并进行处理
        if (current_userRoom() != null) {
            Room room = current_userRoom();
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

    private Room current_userRoom() {
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

    //,将房间的目标温度设置成缺省温度和工作模式和中央空调同步
    // ,对比目标温度和环境温度,
    // 相同将房间温度设置成standby,
    // 不相同将将房间的状态设置成on,将请求加入到请求队列中,设置房间waiting
    @Override
    public CentralUnit authen() {
        //查找用户
        Room room = current_userRoom();
        log.info("room");
        //设置工作模式和温度
        room.setMode(centralUnitMapper.getCentral().getMode());
        room.setTargetTemperature(centralUnitMapper.getCentral().getDefaultTemperature());
        //对比目标温度和环境温度
        if (((room.getTargetTemperature()).compareTo(room.getCurrentTemperature()) )==0) {
            room.setStatus("standby");
        } else {
            room.setStatus("on");
            room.setServiceStatus("waiting");
            //将请求加入到请求队列中
            roomMap.put(room.getRoomId(), room);
        }
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

    //从控机修改目标温度后,加入判断(中央空调是否开启),
    // 发送送风请求(目标温度,和当前的风速模式,默认是中风,在房间创建的时候设置),
    // 后端判断合理后,设置属性(目标温度,服务状态为waiting)
    //从控机修改风速模式请求后,加入判断(中央空调是否开启),
    // 发送送风请求,后端判断合理之后,修改房间对应属性(风速模式,服务状态为waiting)并保存到数据库中,
    // 将请求加入到等待队列,并将之前的同一房间的等待中的请求删除队列
    //中央空调判断合理的逻辑:
    //
    //加热模式:目标温度<房间温度,目标温度25°C～30°C
    //
    //制冷模式:目标温度>房间温度,目标温度18°C～25°C
    //
    //返回目标温度设置不合理
    //
    //目标温度=房间温度,返回已到达目标温度
    @Override
    public Response requests(float targetTemperature, String fanSpeed) {
        //修改房间温度
        Room room = current_userRoom();
        room.setTargetTemperature(targetTemperature);
        //判断中央空调是否开启
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        if (centralUnit.getStatus().equals("off")) {
            //通知前端,中央空调已关机
            Response response = new Response(403, "中央空调已关机", room);
            template.convertAndSend("/air/requestServing", response);
            return response;
        }else {
            //判断合理性
            if (room.getMode().equals("cooling")) {
                if (targetTemperature < room.getCurrentTemperature() && targetTemperature <= 25 && targetTemperature >= 18) {
                    //设置属性
                    room.setTargetTemperature(targetTemperature);
                    room.setFanSpeed(fanSpeed);
                    room.setServiceStatus("waiting");
                    roomMapper.update(room);
                } else {
                    //返回目标温度设置不合理
                    Response response = new Response(404, "目标温度设置不合理", room);
                    template.convertAndSend("/air/requestServing", response);
                    return response;
                }
            } else if (room.getMode().equals("heating")) {
                if (targetTemperature > room.getCurrentTemperature() && targetTemperature <= 30 && targetTemperature >= 25) {
                    //设置属性
                    room.setTargetTemperature(targetTemperature);
                    room.setFanSpeed(fanSpeed);
                    room.setServiceStatus("waiting");
                    room.setStatus("on");
                    roomMapper.update(room);
                } else {
                    //返回目标温度设置不合理
                    Response response = new Response(404, "目标温度设置不合理", room);
                    template.convertAndSend("/air/requestServing", response);
                    return response;
                }
            }
            //将请求加入到请求队列中
            //删除之前的同一房间的等待中的请求,使用Map存储
            roomMap.put(room.getRoomId(), room);
            //通知前端,请求已加入等待队列
            Response response = new Response(200, "请求已加入等待队列", room);
            template.convertAndSend("/air/requestServing", response);
            return response;
        }
    }


}