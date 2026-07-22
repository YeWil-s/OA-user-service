package com.oa.notice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.oa.common", "com.oa.notice"})
@MapperScan("com.oa.notice.mapper")
public class OaNoticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaNoticeApplication.class, args);
    }
}
