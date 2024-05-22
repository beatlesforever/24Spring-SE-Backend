package com.example.sebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.example.sebackend.mapper")
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SeBackendApplication {
    public static void main(String[] args) {

        ConfigurableApplicationContext run = SpringApplication.run(SeBackendApplication.class, args);
//        run.close();
    }

}
