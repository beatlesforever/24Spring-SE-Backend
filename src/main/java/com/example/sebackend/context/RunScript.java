package com.example.sebackend.context;

import com.example.sebackend.entity.Room;
import com.example.sebackend.service.IControlLogService;
import com.example.sebackend.service.IRoomService;
import com.example.sebackend.service.IUsageRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class RunScript implements ApplicationRunner {
    @Autowired
    private IControlLogService controlLogService;
    @Autowired
    private IRoomService roomService;
    @Autowired
    IUsageRecordService usageRecordService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("在程序启动后执行:" + args);

    }

    @PreDestroy
    public void destory() {
        log.info("在程序关闭后执行");
        //程序关闭后,将房间的controlLog未完成记录写入
        //设置最新日志的结束时间(异常退出)
        //将不处于关机状态的房间的最新日志的结束时间设置为当前时间
        // 获取所有房间列表
        List<Room> rooms = roomService.list();
        // 遍历所有房间
        for (Room room : rooms) {
            // 获取房间的最新日志记录
            if (!Objects.equals(room.getStatus(), "off")) {
                //设置最新日志的结束时间

                if (Objects.equals(room.getStatus(), "on") ) {
                    //设置controlLog结束
                    LocalDateTime endTime = LocalDateTime.now();
                    controlLogService.setLatestLog(room.getRoomId(), endTime, true, room.getCurrentTemperature());
                    //更新房间的累计费用
                    endTime = LocalDateTime.now();
                    roomService.setRoomCost(room.getRoomId(), endTime);
                }
                //关机记录写入到数据库
                usageRecordService.saveEndRecord(room.getRoomId(), LocalDateTime.now());

            }
        }
    }
}

