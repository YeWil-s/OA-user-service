package com.oa.datavisual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OaDataVisualApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaDataVisualApplication.class, args);
    }
}
