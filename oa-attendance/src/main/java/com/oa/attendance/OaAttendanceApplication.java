package com.oa.attendance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.oa.common", "com.oa.attendance"})
@MapperScan("com.oa.attendance.mapper")
public class OaAttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaAttendanceApplication.class, args);
    }
}
