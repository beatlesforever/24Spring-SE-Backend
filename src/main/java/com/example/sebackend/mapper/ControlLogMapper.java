package com.example.sebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sebackend.entity.ControlLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    @Select("SELECT * FROM control_log WHERE room_id = #{roomId} AND is_completed = 0;")
    ControlLog getUnfinishedLog(int roomId);

    @Select("SELECT * FROM control_log WHERE room_id = #{roomId} AND is_completed = 1 AND request_time >= #{startTime} AND request_time <= #{endTime};")
    List<ControlLog> getFinishedLogs(@Param("roomId") int roomId,@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime);

    ControlLog getLatestLog(int roomId);
}
