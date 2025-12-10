package com.zly.config;

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
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class AgentConfiguration {
    @Autowired
    private ChatModel chatModel;

    @Autowired
    private RedisSaver redisSaver;

    @Autowired
    private SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    @Bean
    public ReactAgent reactAgent() throws GraphStateException {
        // 组合 MCP 工具与本地文件工具
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        Collections.addAll(toolCallbacks, mcpToolCallbackProvider.getToolCallbacks());
        toolCallbacks.add(new FileReadTool().toolCallback());
        toolCallbacks.add(new FileWriteTool().toolCallback());

        return ReactAgent.builder()
                .name("database-agent")
                .description("对接数据库的agent")
                .model(chatModel)
                .outputType(PoemOutput.class)
//                .systemPrompt("你是一个数据库管理助手，请依据查询出来的表结构来回答问题")
                .saver(new MemorySaver())
                .tools(toolCallbacks.toArray(new ToolCallback[0]))
                .hooks(HumanInTheLoopHook.builder()
                        .approvalOn("file_write", "Write File should be approved")
                        .build(), new LoggingHook())
                .interceptors(new LogToolInterceptor())
                .build();
    }


}
