package com.zly.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson2.JSON;
import com.zly.model.dto.QuestionRequestDTO;
import com.zly.model.enums.AgentCodeEnum;
import com.zly.model.vo.ToolAgentVO;
import com.zly.service.IDatabaseQaService;
import com.zly.utils.InterruptionMetadataUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/database")
public class DataBaseController {

    @Autowired
    private IDatabaseQaService databaseQaService;

    @Autowired
    private ReactAgent reactAgent;

    private final Map<String, InterruptionMetadata> interruptionStore = new ConcurrentHashMap<>();

    private final Map<String, Boolean> stopSignals = new ConcurrentHashMap<>();

    @PostMapping(value = "/stream")
    public Flux<String> stream(@RequestBody QuestionRequestDTO request) throws GraphRunnerException {
        // 流式输出
        return reactAgent.stream(request.getQuestion())
                .filter(output -> output instanceof StreamingOutput)
                .map(output -> (StreamingOutput) output)
                .filter(streamingOutput -> streamingOutput.getOutputType() == OutputType.AGENT_MODEL_STREAMING)
                .map(streamingOutput -> streamingOutput.message().getText());
    }

    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody QuestionRequestDTO request) throws GraphRunnerException {
        // 根据用户ID和会话ID生成唯一的threadId，实现数据隔离
        String threadId = request.getUserId() + "_" + request.getSessionId();
        
        // 开启新会话前，确保清除旧的停止信号
        stopSignals.remove(threadId);

        // 传入threadId以支持记忆功能和人工介入(HITL)
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        return reactAgent.stream(request.getQuestion(), config)
                .takeUntil(output -> Boolean.TRUE.equals(stopSignals.get(threadId)))
                .map(output -> {
                    String node = output.node();
                    if (output instanceof StreamingOutput streamingOutput) {
                        if (streamingOutput.getOutputType() == OutputType.AGENT_MODEL_STREAMING) {
                            ToolAgentVO agentVO = ToolAgentVO.builder()
                                    .code(AgentCodeEnum.SUCCESS.getCode())
                                    .node(node)
                                    .chunk(streamingOutput.message().getText())
                                    .build();
                            return JSON.toJSONString(agentVO);
                        }
                    } else if (output instanceof InterruptionMetadata metadata) {
                        interruptionStore.put(threadId, metadata);
                        ToolAgentVO agentVO = ToolAgentVO.builder()
                                .code(AgentCodeEnum.SUCCESS.getCode())
                                .node(node)
                                .chunk("")
                                .toolFeedback(InterruptionMetadataUtil.conversionToolFeedbackVO(metadata))
                                .build();
                        return JSON.toJSONString(agentVO);
                    }
                    return "{}";
                })
                .filter(s -> !s.isEmpty())
                .doFinally(signalType -> stopSignals.remove(threadId));
    }

    @PostMapping("/stop")
    public String stop(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String sessionId = request.get("sessionId");
        String threadId = userId + "_" + sessionId;

        // 1. 设置停止信号，打断正在进行的 Flux 流
        stopSignals.put(threadId, true);

        // 2. 尝试从 interruptionStore 中移除，打断等待人工介入的流程
        InterruptionMetadata removed = interruptionStore.remove(threadId);

        if (removed != null) {
            return JSON.toJSONString(Map.of("code", 200, "message", "Successfully interrupted the session (HITL and Streaming)."));
        }

        return JSON.toJSONString(Map.of("code", 200, "message", "Stop signal sent for session: " + threadId));
    }

    @PostMapping("/feedback")
    public Flux<String> feedback(@RequestBody FeedbackRequest request) throws GraphRunnerException {
        String threadId = request.getUserId() + "_" + request.getSessionId();
        InterruptionMetadata metadata = interruptionStore.get(threadId);
        if (metadata == null) {
            return Flux.just(JSON.toJSONString(Map.of("type", "error", "content", "No pending interruption found for this session.")));
        }

        InterruptionMetadata.Builder newBuilder = InterruptionMetadata.builder()
                .nodeId(metadata.node())
                .state(metadata.state());

        if (request.getFeedbacks() != null) {
            for (int i = 0; i < request.getFeedbacks().size(); i++) {
                if (i >= metadata.toolFeedbacks().size()) {
                    break;
                }
                var toolFeedback = metadata.toolFeedbacks().get(i);
                var userFeedback = request.getFeedbacks().get(i);

                InterruptionMetadata.ToolFeedback.Builder editedFeedbackBuilder =
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback);

                if (userFeedback.isApproved()) {
                    editedFeedbackBuilder.result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED);
                } else {
                    editedFeedbackBuilder.result(InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED)
                            .description(userFeedback.getFeedback());
                }

                newBuilder.addToolFeedback(editedFeedbackBuilder.build());
            }
        }

        RunnableConfig resumeRunnableConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, newBuilder.build())
                .build();

        interruptionStore.remove(threadId);

        return reactAgent.stream("", resumeRunnableConfig)
                .map(output -> {
                    String node = output.node();
                    if (output instanceof StreamingOutput streamingOutput) {
                        if (streamingOutput.getOutputType() == OutputType.AGENT_MODEL_STREAMING) {
                            ToolAgentVO agentVO = ToolAgentVO.builder()
                                    .code(AgentCodeEnum.SUCCESS.getCode())
                                    .node(node)
                                    .chunk(streamingOutput.message().getText())
                                    .build();
                            return JSON.toJSONString(agentVO);
                        }
                    } else if (output instanceof InterruptionMetadata newMetadata) {
                        interruptionStore.put(threadId, newMetadata);
                        ToolAgentVO agentVO = ToolAgentVO.builder()
                                .code(AgentCodeEnum.SUCCESS.getCode())
                                .node(node)
                                .chunk("")
                                .toolFeedback(InterruptionMetadataUtil.conversionToolFeedbackVO(newMetadata))
                                .build();
                        return JSON.toJSONString(agentVO);
                    }
                    return "{}";
                })
                .filter(s -> !s.isEmpty());
    }

    @Data
    public static class FeedbackRequest {
        private String userId;
        private String sessionId;
        private List<UserFeedback> feedbacks;
    }

    @Data
    public static class UserFeedback {
        private boolean approved;
        private String feedback;
    }
}
