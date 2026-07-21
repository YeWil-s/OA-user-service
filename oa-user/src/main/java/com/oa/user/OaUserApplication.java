package com.oa.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.oa.common", "com.oa.user"})
@MapperScan("com.oa.user.mapper")
public class OaUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaUserApplication.class, args);
    }
}
