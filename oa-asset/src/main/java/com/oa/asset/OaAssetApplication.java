package com.oa.asset;

import com.oa.common.exception.GlobalExceptionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.oa.common", "com.oa.asset"})
@MapperScan("com.oa.asset.mapper")
@Import(GlobalExceptionHandler.class)
public class OaAssetApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaAssetApplication.class, args);
    }
}
