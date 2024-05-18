package com.example.sebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sebackend.entity.UsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Mapper
public interface UsageRecordMapper extends BaseMapper<UsageRecord> {

    //通过房间号查询时间范围内的使用记录
    @Select("SELECT * FROM usage_record WHERE room_id = #{roomId} AND start_time >= #{startTime} AND end_time <= #{endTime};")
    List<UsageRecord> getUsageRecords(@Param("roomId") int roomId,@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime);

    //按照房间号查找所有房间号相同的记录
    List<UsageRecord> saveEndRecord(int roomId);

}
