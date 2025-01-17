package com.example.sebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sebackend.context.BaseContext;
import com.example.sebackend.context.FrequencyConstant;
import com.example.sebackend.entity.CentralUnit;
import com.example.sebackend.entity.Response;
import com.example.sebackend.entity.Room;
import com.example.sebackend.entity.UsageRecord;
import com.example.sebackend.mapper.CentralUnitMapper;
import com.example.sebackend.mapper.RoomMapper;
import com.example.sebackend.mapper.UserMapper;
import com.example.sebackend.service.ICentralUnitService;
import com.example.sebackend.service.IControlLogService;
import com.example.sebackend.service.IUsageRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
//import com.example.sebackend.websocket.WebSocketSever;

import java.time.LocalDateTime;
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
    IControlLogService controlLogService;
    @Autowired
    private IUsageRecordService usageRecordService;
    @Autowired
    private RoomServiceImpl roomService;
    @Autowired
    private SchedulingQueueService schedulingQueueService;



    /**
     * 获取当前用户的房间信息。
     * 该方法首先通过用户名获取用户ID，然后根据用户ID获取相应的房间。
     *
     * @return Room 返回当前用户的房间对象。
     */
    private Room current_userRoom() {
        // 根据用户名获取用户ID
        int roomId = userMapper.getByUsername(BaseContext.getCurrentUser());
        // 根据房间ID获取房间对象
        return roomMapper.getId(roomId);
    }

    private boolean allRoomsNotServing() {
        List<Room> rooms = roomService.list();
        for (Room room : rooms) {
            if ("serving".equals(room.getServiceStatus()) && "on".equals(room.getStatus())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 打开中央空调，设置默认工作状态和温度。
     * 该方法会将中央空调的状态设置为开启，工作模式默认为制冷，初始温度设置为22度。
     * 同时设定最高温度为25度，最低温度为18度，并设置从控机最大数量为3。
     *
     * @return CentralUnit 返回更新后的中央空调对象。
     */
    @Override
    public CentralUnit turnOn() {
        // 获取当前中央单位实例
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        // 设置中央空调为开启状态
        //检查调度队列长度为空设置standby,不为空设置on
        if (allRoomsNotServing()) {
            centralUnit.setStatus("standby");
        } else {
            centralUnit.setStatus("on");
        }
//        centralUnit.setStatus("on");
        // 设置默认工作模式为制冷
        centralUnit.setMode("cooling");
        // 设置当前和默认温度为22度
        centralUnit.setCurrentTemperature(22.0F);
        centralUnit.setDefaultTemperature(22.0F);
        // 设置最高和最低温度限制
        centralUnit.setMaxTemperature(25.0F);
        centralUnit.setMinTemperature(18.0F);
        // 设置从控机最大数量为3
        centralUnit.setCapacity(3);
        // 更新中央单位实例的状态
        centralUnitMapper.update(centralUnit);

        return centralUnit;
    }


    /**
     * 关闭中央空调。
     * 该方法会将中央单元的状态更改为“关闭”，并更新数据库中的相应状态。
     *
     * @return CentralUnit 返回更新后的中央单元对象，其状态已更改为“关闭”。
     */
    @Override
    public CentralUnit turnOff() {
        // 获取当前的中央单元对象
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        // 将中央单元的状态设置为关闭
        centralUnit.setStatus("off");
        // 更新数据库中的中央单元状态
        centralUnitMapper.update(centralUnit);
        return centralUnit;
    }

    @Override
    public CentralUnit turnStandBy() {
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        centralUnit.setStatus("standby");
        return centralUnit;
    }


    /**
     * 对房间进行认证并根据目标温度和当前温度调整房间状态。
     * 该方法会将房间的目标温度设置为中央单元的缺省温度，并与中央单元的工作模式同步。
     * 根据目标温度与环境温度的比较，来更新房间的状态。
     *
     * @return CentralUnit 返回当前中央单元的信息。
     */
    @Override
    public CentralUnit authen(int roomId) {
        // 查找当前用户所在的房间
        Room room ;
        room = roomMapper.getId(roomId);

//        log.info("room");
        // 设置房间的工作模式和目标温度为中央单元的默认模式和温度
        room.setMode(centralUnitMapper.getCentral().getMode());
        room.setTargetTemperature(centralUnitMapper.getCentral().getDefaultTemperature());
        // 更新房间的最后活动时间
        room.setLastUpdate(LocalDateTime.now());
        // 比较目标温度和当前温度，以决定房间的状态
        if (Math.abs(room.getTargetTemperature() - room.getCurrentTemperature()) ==0) {
            roomMapper.update(room);
            room.setStatus("standby");
        } else {
            room.setStatus("on");
            room.setServiceStatus("waiting");
            roomMapper.update(room);
            // 将房间请求加入到请求队列中
            schedulingQueueService.addRoomToQueue(room.getRoomId());
            //添加到记录中
            //当前时间
//            LocalDateTime now = LocalDateTime.now();
//            controlLogService.setLatestLog(room.getRoomId(), now, true, room.getCurrentTemperature());
        }
        // 更新房间信息到数据库
//        roomMapper.update(room);
        // 写入开机使用记录
        UsageRecord usageRecord = new UsageRecord(room.getRoomId(), LocalDateTime.now());
        usageRecordService.save(usageRecord);
        // 返回中央单元的信息
        return centralUnitMapper.getCentral();
    }

    /**
     * 获取从控机状态，并将状态更新通过消息模板发送出去。
     *
     * @return 返回当前所有房间的最新状态列表。
     */
    @Override
    public List<Room> getStatus() {
        // 获取当前所有房间的状态
        List<Room> rooms = roomMapper.list();

        // 将房间状态封装成响应对象，发送到指定主题
        Response response = new Response(200, "从控机状态已更新", rooms);

        // 返回最新的房间状态列表
        return roomMapper.list();
    }

    @Override
    public CentralUnit uodateFrequency(int frequency) {
        centralUnitMapper.updateFrequency(frequency);
        //修改配置
        FrequencyConstant.frequency = frequency;
        return centralUnitMapper.getCentral();
    }

    /**
     * 修改中央空调的工作模式。
     *
     * @param mode 指定中央空调的新工作模式。
     *             该方法首先从中央单元管理器（centralUnitMapper）获取中央单元对象，
     *             然后设置中央单元的工作模式为指定的模式，
     *             最后更新中央单元对象在数据库中的状态。
     */
    @Override
    public void setMode(String mode) {
        // 获取当前的中央单元
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        // 设置新的工作模式
        centralUnit.setMode(mode);
        // 更新中央单元的工作模式到数据库
        centralUnitMapper.update(centralUnit);

    }


    /**
     * 设置中央空调的缺省温度。
     * 这个方法会检索中央单元，更新其缺省温度值，然后保存更改。
     *
     * @param defaultTemperature 指定的新缺省温度值，类型为浮点数。
     */
    @Override
    public void segfaultTemperature(float defaultTemperature) {
        // 获取当前中央单元
        CentralUnit centralUnit = centralUnitMapper.getCentral();
        // 更新中央单元的缺省温度
        centralUnit.setDefaultTemperature(defaultTemperature);
        // 保存更新后的中央单元
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
            return new Response(403, "中央空调已关机", room);
        } else if (room.getStatus().equals(("off"))) {
            //通知前端,房间已关机
            return new Response(403, "从控机未认证开机", room);
        } else {
            //判断合理性
            if (room.getMode().equals("cooling")) {
                if (targetTemperature < room.getCurrentTemperature() && targetTemperature <= 25 && targetTemperature >= 18) {
                    //设置属性
                    room.setTargetTemperature(targetTemperature);
                    room.setFanSpeed(fanSpeed);
                    room.setStatus("on");
                    room.setServiceStatus("waiting");
                    roomMapper.update(room);
                    //加入调度队列
                    schedulingQueueService.addRoomToQueue(room.getRoomId());
                    //设置controlLog结束
                    LocalDateTime endTime = LocalDateTime.now();
                    controlLogService.setLatestLog(room.getRoomId(), endTime, true, room.getCurrentTemperature());
                    //更新房间的累计费用
                    roomService.setRoomCost(room.getRoomId(), endTime);
                } else {
                    //返回目标温度设置不合理
                    return new Response(404, "目标温度设置不合理", room);
                }
            } else if (room.getMode().equals("heating")) {
                if (targetTemperature > room.getCurrentTemperature() && targetTemperature <= 30 && targetTemperature >= 25) {
                    //设置属性
                    room.setTargetTemperature(targetTemperature);
                    room.setFanSpeed(fanSpeed);
                    room.setServiceStatus("waiting");
                    room.setStatus("on");
                    roomMapper.update(room);
                    //加入调度队列
                    schedulingQueueService.addRoomToQueue(room.getRoomId());
                    //设置controlLog结束
                    LocalDateTime endTime = LocalDateTime.now();
                    controlLogService.setLatestLog(room.getRoomId(), endTime, true, room.getCurrentTemperature());
                    //更新房间的累计费用
                    roomService.setRoomCost(room.getRoomId(), endTime);
                } else {
                    //返回目标温度设置不合理
                    return new Response(404, "目标温度设置不合理", room);
                }
            }
            //将请求加入到请求队列中,并删除之前的同一房间的等待中的请求
            schedulingQueueService.addRoomToQueue(room.getRoomId());
            //通知前端,请求已加入等待队列
            return new Response(200, "请求已加入等待队列", room);
        }
    }


}