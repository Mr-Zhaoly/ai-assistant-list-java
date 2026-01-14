package com.zly.config;

import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisSaverConfiguration {

    @Value("${REDIS_HOST}")
    private String host;

    @Value("${REDIS_PORT}")
    private String port;

    @Value("${REDIS_PWD}")
    private String password;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://" + host + ":" + port)
              .setPassword(password);
        return Redisson.create(config);
    }

    @Bean
    public RedisSaver redisSaver(RedissonClient redissonClient){
        return RedisSaver.builder().redisson(redissonClient).build();
    }
}