package com.oa.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OaAssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaAssetApplication.class, args);
    }
}
