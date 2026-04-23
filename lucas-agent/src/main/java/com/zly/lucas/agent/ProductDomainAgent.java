package com.zly.lucas.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 产品域 Agent（占位实现）
 * <p>
 * 负责处理产品查询、型号推荐、参数对比等请求。
 * <br>
 * 对应 Dify 节点：产品域意图 → 电池意图槽位提取 → 上下文槽位管理 → 推荐排序结果
 * </p>
 * <p>TODO: 实现以下能力：
 * <ul>
 *   <li>意图识别（推荐/查询/对比）</li>
 *   <li>槽位提取（电压/容量/应用场景/款式等）</li>
 *   <li>调用商品数据库搜索 MCP 工具</li>
 *   <li>结果排序与格式化</li>
 * </ul>
 * </p>
 *
 * @author zly
 */
@Slf4j
@Component
public class ProductDomainAgent implements DomainAgent {

    @Override
    public String getDomain() {
        return "product";
    }

    @Override
    public String process(String enrichedQuery, String sessionId, String userLanguage) {
        log.info("[Lucas][Product] 处理产品域请求 | sessionId={} | query={}", sessionId, enrichedQuery);
        // TODO: 实现产品域完整处理逻辑
        return "【产品域 Agent 占位】收到您的产品咨询，功能开发中...";
    }
}
