package com.zly.lucas.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 故障域 Agent（占位实现）
 * <p>
 * 负责处理设备故障、异常报错、BMS保护、充放电问题等请求（优先级最高域）。
 * <br>
 * 对应 Dify 节点：一级故障意图识别 → 二级故障意图识别 → 故障槽位提取 → 粗排召回 → 诊断推理与答案生成
 * </p>
 * <p>TODO: 实现以下能力：
 * <ul>
 *   <li>两级意图识别（一级：充电类/放电类/BMS类等；二级：精确 FAQ 匹配）</li>
 *   <li>故障槽位提取（型号/电压/症状/BMS状态/蓝牙连接等 20+ 槽位）</li>
 *   <li>Milvus 向量库 RAG 召回（192 条 FAQ 知识库）</li>
 *   <li>诊断推理与结构化答案生成</li>
 *   <li>必填槽位缺失时自动追问</li>
 * </ul>
 * </p>
 *
 * @author zly
 */
@Slf4j
@Component
public class TroubleshootingDomainAgent implements DomainAgent {

    @Override
    public String getDomain() {
        return "troubleshooting";
    }

    @Override
    public String process(String enrichedQuery, String sessionId, String userLanguage) {
        log.info("[Lucas][Troubleshooting] 处理故障域请求 | sessionId={} | query={}", sessionId, enrichedQuery);
        // TODO: 实现故障域完整处理逻辑（最复杂域，需结合 Milvus RAG）
        return "【故障域 Agent 占位】收到您的故障描述，功能开发中...";
    }
}
