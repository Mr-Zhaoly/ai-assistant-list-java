package com.zly.lucas.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单域 Agent（占位实现）
 * <p>
 * 负责处理订单查询、物流追踪、退款进度、发票申请等请求。
 * <br>
 * 对应 Dify 节点：订单域-上下文处理 → 订单域-意图识别 → 订单域-槽位提取 → 订单域MCP路由调用
 * </p>
 * <p>TODO: 实现以下能力：
 * <ul>
 *   <li>订单域上下文处理（防跨域污染）</li>
 *   <li>意图识别（查询物流/退款进度/发票/地址修改等）</li>
 *   <li>槽位提取（订单号/邮箱/地址等）</li>
 *   <li>调用物流 MCP 接口查询实时数据</li>
 *   <li>无数据时触发转人工</li>
 * </ul>
 * </p>
 *
 * @author zly
 */
@Slf4j
@Component
public class OrderDomainAgent implements DomainAgent {

    @Override
    public String getDomain() {
        return "order";
    }

    @Override
    public String process(String enrichedQuery, String sessionId, String userLanguage) {
        log.info("[Lucas][Order] 处理订单域请求 | sessionId={} | query={}", sessionId, enrichedQuery);
        // TODO: 实现订单域完整处理逻辑
        return "【订单域 Agent 占位】收到您的订单咨询，功能开发中...";
    }
}
