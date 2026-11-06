package com.volunteer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 志愿者活动服务平台主启动类
 * 
 * @author volunteer-team
 * @since 2025-01-17
 */
@SpringBootApplication
@MapperScan("com.volunteer.mapper")
@EnableCaching
public class VolunteerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VolunteerServiceApplication.class, args);
        System.out.println("=================================");
        System.out.println("志愿者活动服务平台启动成功！");
        System.out.println("访问地址：http://localhost:8080");
        System.out.println("=================================");
    }
}
