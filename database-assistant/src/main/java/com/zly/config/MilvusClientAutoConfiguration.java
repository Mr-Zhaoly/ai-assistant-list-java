package com.zly.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusClientAutoConfiguration {

    @Value("${milvus.client.url}")
    private String url;

    @Value("${milvus.client.userName}")
    private String userName;

    @Value("${milvus.client.password}")
    private String password;

    @Bean(name = "milvusClient")
    public MilvusClientV2 milvusClient() {
        ConnectConfig config = ConnectConfig.builder()
                .uri(url)
                .token(String.format("%s:%s", userName, password))
                .dbName("ai_health_assistant")
                .build();
        return new MilvusClientV2(config);
    }
}
