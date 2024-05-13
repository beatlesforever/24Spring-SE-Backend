package com.example.sebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sebackend.entity.ControlLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/12
 * @project SE-backend
 */
@Mapper
public interface ControlLogMapper extends BaseMapper<ControlLog> {
    @Select("SELECT * FROM control_log WHERE roomId = #{roomId} AND isCompleted = false;")
    ControlLog getUnfinishedLog(int roomId);

    @Select("SELECT * FROM control_log WHERE roomId = #{roomId} AND isCompleted = true AND requestTime >= #{startTime} AND requestTime <= #{endTime};")
    List<ControlLog> getFinishedLogs(int roomId, LocalDateTime startTime, LocalDateTime endTime);
}
