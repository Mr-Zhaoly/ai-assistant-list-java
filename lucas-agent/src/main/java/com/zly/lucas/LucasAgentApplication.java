package com.zly.lucas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Lucas 智能客服机器人启动类
 * <p>
 * 基于 Spring AI Alibaba Multi-Agent Supervisor 架构，
 * 实现 LiTime 品牌的多域智能客服能力。
 * </p>
 *
 * @author zly
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LucasAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LucasAgentApplication.class, args);
    }
}
