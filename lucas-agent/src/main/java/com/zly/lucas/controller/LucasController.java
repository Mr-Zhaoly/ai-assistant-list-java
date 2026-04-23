package com.zly.lucas.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson2.JSON;
import com.zly.lucas.config.LucasUserContext;
import com.zly.lucas.hook.HumanHandoffException;
import com.zly.lucas.model.dto.LucasRequestDTO;
import com.zly.lucas.model.vo.LucasResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Lucas 机器人主控制器
 * <p>
 * 提供以下接口：
 * <ul>
 *   <li>POST /lucas/stream  - SSE 流式对话（主接口）</li>
 *   <li>POST /lucas/chat    - 同步对话（调试用）</li>
 * </ul>
 * </p>
 *
 * @author zly
 */
@Slf4j
@RestController
@RequestMapping("/lucas")
public class LucasController {

    @Autowired
    @Qualifier("lucasAgent")
    private ReactAgent lucasAgent;

    /**
     * SSE 流式对话接口
     * <p>
     * 前端通过 EventSource 或 fetch + ReadableStream 接收流式响应。
     * 若触发转人工，返回包含 {@code humanService=true} 的 JSON 事件。
     * </p>
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody LucasRequestDTO request) throws GraphRunnerException {
        // 初始化用户上下文
        LucasUserContext ctx = buildUserContext(request);
        LucasUserContext.set(ctx);

        String threadId = buildThreadId(request.getUserId(), request.getSessionId());
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        return lucasAgent.stream(request.getQuestion(), config)
                .filter(output -> output instanceof StreamingOutput)
                .map(output -> (StreamingOutput) output)
                .filter(streamingOutput -> streamingOutput.getOutputType() == OutputType.AGENT_MODEL_STREAMING)
                .map(streamingOutput -> {
                    String chunk = streamingOutput.message().getText();
                    LucasResponseVO vo = LucasResponseVO.builder()
                            .code(200)
                            .chunk(chunk)
                            .humanService(false)
                            .build();
                    return "data:" + vo.toJsonString() + "\n\n";
                })
                .onErrorResume(HumanHandoffException.class, ex -> {
                    log.info("[Lucas] 转人工触发 | emotion={} | lang={}", ex.getEmotion(), ex.getUserLanguage());
                    LucasResponseVO vo = LucasResponseVO.builder()
                            .code(200)
                            .chunk(ex.getHandoffMessage())
                            .humanService(true)
                            .build();
                    return Flux.just("data:" + vo.toJsonString() + "\n\n");
                })
                .doFinally(signal -> LucasUserContext.clear());
    }

    /**
     * 同步对话接口（调试 / 测试用）
     */
    @PostMapping("/chat")
    public LucasResponseVO chat(@RequestBody LucasRequestDTO request) throws GraphRunnerException {
        LucasUserContext ctx = buildUserContext(request);
        LucasUserContext.set(ctx);

        String threadId = buildThreadId(request.getUserId(), request.getSessionId());
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        try {
            AssistantMessage result = lucasAgent.call(request.getQuestion(), config);
            return LucasResponseVO.builder()
                    .code(200)
                    .chunk(result.getText())
                    .humanService(false)
                    .build();
        } catch (HumanHandoffException ex) {
            return LucasResponseVO.builder()
                    .code(200)
                    .chunk(ex.getHandoffMessage())
                    .humanService(true)
                    .build();
        } finally {
            LucasUserContext.clear();
        }
    }

    // ─── 私有工具方法 ────────────────────────────────────────────────────────────

    private LucasUserContext buildUserContext(LucasRequestDTO request) {
        LucasUserContext ctx = new LucasUserContext();
        ctx.setUserId(request.getUserId());
        ctx.setSessionId(request.getSessionId());
        ctx.setUserLanguage(request.getUserLanguage() != null ? request.getUserLanguage() : "en");
        ctx.setEnrichedQuery(request.getQuestion());
        return ctx;
    }

    private String buildThreadId(Long userId, String sessionId) {
        return "lucas_" + userId + "_" + sessionId;
    }
}
