package com.oa.datavisual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.oa.common", "com.oa.datavisual"})
@MapperScan("com.oa.datavisual.mapper")
public class OaDataVisualApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaDataVisualApplication.class, args);
    }
}
