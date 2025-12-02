package com.zly.snowflake.component;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.zly.redis.util.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IdGeneratorSnowflake {
	@Value("${spring.application.name:default}")
    private String applicationName;

    private Snowflake snowflake = null;

    @Autowired
    private RedisUtil redisUtil;
    
    @PostConstruct
    public void initIdGeneratorSnowflake() {

        // 从redis中获取下一个序列号
        int seqId = getSnowFlakeKey();
        // 将序列值转换为2进制，取低10位数字，其中1-5转化为10进制后赋值给workerId，6-10转化为10进制后赋值给dataCenterId
        String binaryString = Integer.toBinaryString(seqId);

        String low;
        String hign;
        int length = binaryString.length();
        if (length>10) {
            low = binaryString.substring(length-5, length);
            hign = binaryString.substring(length-10, length-5);
        } else if (length>5) {
            low = binaryString.substring(length-5, length);
            hign = binaryString.substring(0, length-5);
        } else {
            low = binaryString;
            hign = "0";
        }
        // 将二进制数字转换为10进制
        int workerId = Integer.parseInt(hign, 2);
        int dataCenterId = Integer.parseInt(low, 2);
        log.info("雪花算法:应用名称:{},workerId:{},snowflake={}",applicationName, workerId, dataCenterId);
        snowflake = IdUtil.createSnowflake(workerId, dataCenterId);
    }

    private int getSnowFlakeKey() {
        // 没有应用一套序列号
        String redisKey = "starry:system:snowflake:" + applicationName;
        long seqId = RedisUtil.incr(redisKey, 1L);
        return (int)seqId;
    }

    /**
     * 获取下一个ID
     * @return
     */
    public synchronized String nextId() {
        return String.valueOf(snowflake.nextId());
    }

    /**
     * 获取下一个ID
     * @return
     */
    public synchronized Long nextLongId() {
        return snowflake.nextId();
    }
}
