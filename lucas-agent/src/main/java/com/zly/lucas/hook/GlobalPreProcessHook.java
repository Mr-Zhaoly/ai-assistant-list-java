package com.zly.lucas.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.zly.lucas.config.LucasUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Lucas 全局前置处理 Hook
 * <p>
 * 对应 Dify 工作流中的以下节点：
 * <ul>
 *   <li>多语言识别 → 检测用户输入语言，写入 {@link LucasUserContext#userLanguage}</li>
 *   <li>情绪识别 → 判断 normal/negative/angry/human，写入 {@link LucasUserContext#emotion}</li>
 *   <li>上下文工程 (Context Engineering) → 改写 Query，写入 {@link LucasUserContext#enrichedQuery}</li>
 * </ul>
 * 若情绪为 angry 或 human，抛出 {@link HumanHandoffException}，
 * 由 Controller 捕获后返回转人工响应。
 * </p>
 *
 * @author zly
 */
@Slf4j
@Component
public class GlobalPreProcessHook extends AgentHook {

    @Override
    public String getName() {
        return "lucas-global-pre-process-hook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        LucasUserContext ctx = LucasUserContext.get();
        if (ctx == null) {
            log.warn("[Lucas] LucasUserContext 未初始化，跳过全局前置处理");
            return CompletableFuture.completedFuture(Map.of());
        }

        String userInput = ctx.getEnrichedQuery() != null ? ctx.getEnrichedQuery() : "";
        log.info("[Lucas] 全局前置处理开始 | sessionId={} | input={}", ctx.getSessionId(), userInput);

        // Step 1: 情绪识别（TODO: 替换为真实 LLM 调用）
        String emotion = detectEmotion(userInput, ctx.getUserLanguage());
        ctx.setEmotion(emotion);
        log.info("[Lucas] 情绪识别结果: {}", emotion);

        // Step 2: 若需转人工，抛出异常中断流程
        if ("angry".equals(emotion) || "human".equals(emotion)) {
            log.info("[Lucas] 触发转人工，情绪={}", emotion);
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(new HumanHandoffException(emotion, ctx.getUserLanguage()));
            return future;
        }

        // Step 3: Context Engineering - Query 改写（TODO: 替换为真实 LLM 调用）
        String enrichedQuery = enrichQuery(userInput, ctx.getSessionId());
        ctx.setEnrichedQuery(enrichedQuery);
        log.info("[Lucas] Query 改写完成: {}", enrichedQuery);

        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterAgent(OverAllState state, RunnableConfig config) {
        // 清理 ThreadLocal，防止内存泄漏
        LucasUserContext.clear();
        return CompletableFuture.completedFuture(Map.of());
    }

    // ─── 私有方法（占位，后续替换为真实 LLM 调用）────────────────────────────────

    /**
     * 情绪识别（占位实现）
     * <p>TODO: 调用 LLM，注入 Dify 中 "情绪识别" 节点的 System Prompt</p>
     */
    private String detectEmotion(String input, String language) {
        // 占位：直接返回 normal，后续替换为 LLM 结构化输出
        return "normal";
    }

    /**
     * Query 改写（占位实现）
     * <p>TODO: 调用 LLM，注入 Dify 中 "上下文工程" 节点的 System Prompt</p>
     */
    private String enrichQuery(String input, String sessionId) {
        // 占位：直接返回原始输入，后续替换为 LLM 改写结果
        return input;
    }
}
