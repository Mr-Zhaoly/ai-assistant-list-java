package com.zly.lucas.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 安装域 Agent（占位实现）
 * <p>
 * 负责处理安装指导、接线方案、并/串联配置、蓝牙APP配置、通用原理科普等请求。
 * <br>
 * 对应 Dify 节点：安装域上下文处理 → 意图识别 → 槽位提取 → MCP工具调用/知识库检索 → 答案生成
 * </p>
 *
 * @author zly
 */
@Slf4j
@Component
public class InstallationDomainAgent implements DomainAgent {

    @Override
    public String getDomain() {
        return "installation";
    }

    @Override
    public String process(String enrichedQuery, String sessionId, String userLanguage) {
        log.info("[Lucas][Installation] 处理安装域请求 | sessionId={} | query={}", sessionId, enrichedQuery);
        // TODO: 实现安装域完整处理逻辑
        return "【安装域 Agent 占位】收到您的安装咨询，功能开发中...";
    }
}
