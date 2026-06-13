package com.cas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.cas.**.mapper")
@EnableScheduling
public class CasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasApplication.class, args);
    }
}