package com.example.sebackend.config;

import com.example.sebackend.entity.Room;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Configuration
/*
    * 创建一个ConcurrentHashMap实例，用于存储房间信息。
    * 这个方法没有参数。
    * @return ConcurrentHashMap<Integer,Room> - 一个新的ConcurrentHashMap实例，用于存储房间信息。
    * 房间号和房间信息的映射
 */
public class QueueConfig {
    @Bean(name = "roomQueue")
    public ConcurrentHashMap<Integer,Room> roomQueue() {
        return new ConcurrentHashMap<>();
    }
    @Bean(name = "processingRooms")
    public ConcurrentHashMap<Integer,Boolean> processingRooms() {
        return new ConcurrentHashMap<>();
    }
}
