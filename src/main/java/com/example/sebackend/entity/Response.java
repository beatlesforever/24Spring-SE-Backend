package com.example.sebackend.entity;

import lombok.Data;

@Data
public class Response implements java.io.Serializable {
    private Integer code;
    private String message;
    private Object data;
    public Response(int code, String message, Room room) {
        this.code = code;
        this.message = message;
        this.data = room;
    }
}
