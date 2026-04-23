package com.zly.lucas.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 方案域 Agent（占位实现）
 * <p>
 * 负责处理整套供电方案设计、系统设计、多设备全链路搭配等请求。
 * <br>
 * 对应 Dify 节点：设备兼容意图槽位提取 → 策略选择层 → 计算类解决方案/知识检索
 * </p>
 *
 * @author zly
 */
@Slf4j
@Component
public class SolutionDomainAgent implements DomainAgent {

    @Override
    public String getDomain() {
        return "solution";
    }

    @Override
    public String process(String enrichedQuery, String sessionId, String userLanguage) {
        log.info("[Lucas][Solution] 处理方案域请求 | sessionId={} | query={}", sessionId, enrichedQuery);
        // TODO: 实现方案域完整处理逻辑
        return "【方案域 Agent 占位】收到您的方案设计需求，功能开发中...";
    }
}
