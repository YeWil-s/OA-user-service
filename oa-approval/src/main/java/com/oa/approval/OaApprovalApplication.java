package com.oa.approval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OaApprovalApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaApprovalApplication.class, args);
    }
}
