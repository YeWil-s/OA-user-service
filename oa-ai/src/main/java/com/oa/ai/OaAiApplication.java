package com.oa.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan({"com.oa.common", "com.oa.ai"})
@MapperScan("com.oa.ai.mapper")
public class OaAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaAiApplication.class, args);
    }
}
