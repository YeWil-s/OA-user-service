package com.oa.approval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.oa.common", "com.oa.approval"})
@MapperScan("com.oa.approval.mapper")
public class OaApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaApprovalApplication.class, args);
    }
}
