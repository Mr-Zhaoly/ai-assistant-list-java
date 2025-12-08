package com.zly.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.zly.common.ai.base.PoemOutput;
import com.zly.hook.LoggingHook;
import com.zly.interceptor.LogToolInterceptor;
import com.zly.tools.FileReadTool;
import com.zly.tools.FileWriteTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {
    @Autowired
    private ChatModel chatModel;

    @Autowired
    private RedisSaver redisSaver;

    @Bean
    public ReactAgent reactAgent() throws GraphStateException {
        return ReactAgent.builder()
                .name("database-agent")
                .description("对接数据库的agent")
                .model(chatModel)
                .outputType(PoemOutput.class)
//                .systemPrompt("你是一个数据库管理助手。请准确、简洁地回答问题。")
                .saver(redisSaver)
                .tools(
                        new FileReadTool().toolCallback(),
                        new FileWriteTool().toolCallback()
                )
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("file_write", "Write File should be approved")
                        .build(), new LoggingHook())
                .interceptors(new LogToolInterceptor())
                .build();
    }


}
