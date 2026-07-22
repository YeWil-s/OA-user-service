package com.oa.notice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.mybatis.spring.annotation.MapperScan;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.oa.api")
@MapperScan("com.oa.notice.mapper")
@SpringBootApplication(scanBasePackages = "com.oa")
public class NoticeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeServiceApplication.class, args);
    }
}
