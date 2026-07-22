package com.oa.visual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.oa.api")
@SpringBootApplication(scanBasePackages = "com.oa")
public class VisualServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisualServiceApplication.class, args);
    }
}
