package com.example.sebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.sebackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author zhouhaoran
 * @date 2024/5/8
 * @project SE-backend
 */
@Mapper
public interface UserMapper extends BaseMapper<User>{
    User findByUsername(String username);
    @Select("select * from user where room_id = #{roomId}")
    int getByRoomId(Integer roomId);

@Select("select room_id from user where username = #{currentUser}")
    int getByUsername(String currentUser);
}
