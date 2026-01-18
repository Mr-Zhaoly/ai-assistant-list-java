package com.zly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BusinessAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessAgentApplication.class, args);
    }
}
