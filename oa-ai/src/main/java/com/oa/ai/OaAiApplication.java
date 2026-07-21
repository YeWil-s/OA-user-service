package com.oa.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OaAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaAiApplication.class, args);
    }
}
