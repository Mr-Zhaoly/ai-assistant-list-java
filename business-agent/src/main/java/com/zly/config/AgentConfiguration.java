package com.zly.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AgentConfiguration {
    @Autowired
    private ChatModel chatModel;

    @Autowired
    private RedisSaver redisSaver;

    @Bean
    public ReactAgent reactAgent() throws GraphStateException {
        List<ToolCallback> toolCallbacks = new ArrayList<>();

        String systemPrompt = """
                你是一个专业的业务助手，负责处理通用的业务请求。
                
                请根据用户的需求提供帮助。如果需要执行具体的操作，请使用相应的工具。
                目前你支持回答一般性的业务咨询。
                
                始终以友好、专业的方式与用户交流。
                """;

        return ReactAgent.builder()
                .name("business-agent")
                .description("通用业务处理Agent")
                .model(chatModel)
                .systemPrompt(systemPrompt)
                .saver(redisSaver)
                .tools(toolCallbacks.toArray(new ToolCallback[0]))
                .hooks(HumanInTheLoopHook.builder()
                        // .approvalOn("some_tool", "Description")
                        .build())
                .build();
    }
}
