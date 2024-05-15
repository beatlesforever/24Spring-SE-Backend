package com.example.sebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sebackend.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    @Select("select * from room where room.room_id = #{roomId}")
    Room getId(int roomId);
    //更新房间信

    void update( Room room);

    List<Room> list();
}
