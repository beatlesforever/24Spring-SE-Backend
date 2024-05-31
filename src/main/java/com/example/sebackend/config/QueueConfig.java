package com.example.sebackend.config;

import com.example.sebackend.entity.Room;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Configuration

public class QueueConfig {
    @Bean
    public ConcurrentLinkedQueue<Integer> schedulingQueue() {//存roomId
        return new ConcurrentLinkedQueue<>();
    }

}
