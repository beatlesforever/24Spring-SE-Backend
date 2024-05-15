package com.example.sebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.example.sebackend.mapper")
@SpringBootApplication
@EnableScheduling
public class SeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeBackendApplication.class, args);
    }

}
