package com.zly.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.zly.common.ai.base.PoemOutput;
import com.zly.hook.LoggingHook;
import com.zly.interceptor.LogModelInterceptor;
import com.zly.interceptor.LogToolInterceptor;
import com.zly.tools.SqlExecuteTool;
import com.zly.tools.TableSearchTool;
import com.zly.tools.TableStructureTool;
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
    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private RedisSaver redisSaver;

    @Autowired
    private SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    @Autowired
    private TableSearchTool tableSearchTool;

    @Autowired
    private TableStructureTool tableStructureTool;

    @Autowired
    private SqlExecuteTool sqlExecuteTool;

    @Bean
    public ReactAgent reactAgent() throws GraphStateException {
        // 组合 MCP 工具与本地文件工具
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        Collections.addAll(toolCallbacks, mcpToolCallbackProvider.getToolCallbacks());
        
        // 注册数据库查询工具
//        toolCallbacks.add(tableSearchTool.toolCallback());
//        toolCallbacks.add(tableStructureTool.toolCallback());
//        toolCallbacks.add(sqlExecuteTool.toolCallback());

        // 系统提示词：指导Agent按步骤执行非业务基础能力
        String systemPrompt = """
                你是一个专业的工具助手，当前主要负责数据库相关查询能力，后续会逐步扩展更多工具Agent。
                
                步骤1：从用户输入中提取与数据库表名相关的关键字
                - 分析用户的问题，识别出可能涉及的表名关键字（如"订单"、"用户"、"产品"等）
                - 使用 list_tables 工具，传入提取的关键字搜索匹配的表名
                
                步骤2：向用户展示找到的表名列表，等待用户确认
                - 将搜索到的表名列表清晰地展示给用户
                - 如果找到多个表，请列出所有选项供用户选择
                - 如果未找到表，请提示用户使用其他关键字
                
                步骤3：用户确认表名后，获取表结构
                - 等待用户明确确认要查询的表名
                - 使用 describe_table 工具获取该表的完整结构信息
                - 将表结构信息（字段名、类型、约束等）展示给用户
                
                步骤4：根据表结构和用户问题，生成SQL或执行查询
                - 基于获取的表结构信息，理解用户的问题
                - 生成相应的SQL查询语句（只能使用SELECT语句）
                - 使用 query 工具执行SQL并返回结果
                - 将查询结果以清晰、易读的方式展示给用户
                
                重要规则：
                - 必须按顺序执行以上步骤，不能跳过任何步骤
                - 在获取表结构前，必须等待用户明确确认表名
                - 只能执行SELECT查询语句，不允许执行INSERT、UPDATE、DELETE等修改操作
                - 如果用户的问题不明确，请主动询问以获取更多信息
                - 始终以友好、专业的方式与用户交流
                """;

        return ReactAgent.builder()
                .name("tool-agent")
                .description("工具Agent集合，当前聚焦数据库查询场景")
                .model(chatModel)
//                .outputType(PoemOutput.class)
                .systemPrompt(systemPrompt)
                .saver(redisSaver)
                .tools(toolCallbacks.toArray(new ToolCallback[0]))
                .hooks(HumanInTheLoopHook.builder()
//                        .approvalOn("table_structure", "请确认要查询的表名，确认后将获取该表的完整结构信息")
//                        .approvalOn("sql_execute", "请确认要执行的SQL查询语句，确认后将执行查询并返回结果")
                        .approvalOn("describe_table", "请确认要查询的表名，确认后将获取该表的完整结构信息")
                        .approvalOn("query", "请确认要执行的SQL查询语句，确认后将执行查询并返回结果")
                        .approvalOn("execute", "请确认要执行的SQL操作语句，确认后将执行操作并返回结果")
                        .build(), new LoggingHook())
                .interceptors(new LogToolInterceptor(), new LogModelInterceptor())
                .build();
    }


}
