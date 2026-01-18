package com.zly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ToolAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolAgentApplication.class, args);
    }
}
