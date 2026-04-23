package com.zly.lucas.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通用域 Agent（占位实现）
 * <p>
 * 负责处理品牌咨询、问候、闲聊、感谢等通用请求，直接由 LLM 回复。
 * <br>
 * 对应 Dify 节点：通用域意图+槽位 → LLM正常回答
 * </p>
 *
 * @author zly
 */
@Slf4j
@Component
public class GeneralDomainAgent implements DomainAgent {

    @Override
    public String getDomain() {
        return "general";
    }

    @Override
    public String process(String enrichedQuery, String sessionId, String userLanguage) {
        log.info("[Lucas][General] 处理通用域请求 | sessionId={} | query={}", sessionId, enrichedQuery);
        // TODO: 直接调用 LLM 生成品牌介绍/闲聊回复
        return "【通用域 Agent 占位】您好！我是 LiTime 的智能客服 Lucas，有什么可以帮助您的？";
    }
}
