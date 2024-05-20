package com.example.sebackend.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;

@Data
public class Response implements java.io.Serializable {
    private Integer code;//状态码
    private String message;//消息
    private Object data;//数据
    public Response(int code, String message, Object room) {
        this.code = code;
        this.message = message;
        this.data = room;
    }
    //
    public Response(int code, String message, List<Room> rooms) {
        this.code = code;
        this.message = message;
        this.data = rooms;
    }
    // 将消息对象转换为JSON字符串
    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
