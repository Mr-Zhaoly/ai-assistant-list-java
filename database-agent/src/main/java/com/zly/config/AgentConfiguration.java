package com.zly.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.zly.interceptor.LogToolInterceptor;
import com.zly.tools.FileReadTool;
import com.zly.tools.FileWriteTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {
    private final ChatModel chatModel;

    public AgentConfiguration(DashScopeChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Bean
    public ReactAgent reactAgent() throws GraphStateException {
        return ReactAgent.builder()
                .name("database-agent")
                .description("对接数据库的agent")
                .model(chatModel)
                .saver(new MemorySaver())
                .tools(
                        new FileReadTool().toolCallback(),
                        new FileWriteTool().toolCallback()
                )
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("file_write", "Write File should be approved")
                        .build())
                .interceptors(new LogToolInterceptor())
                .build();
    }


}
