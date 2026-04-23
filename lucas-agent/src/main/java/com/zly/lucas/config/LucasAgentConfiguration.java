package com.zly.lucas.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lucas Agent 核心配置
 * <p>
 * 构建 Lucas 机器人的 ReactAgent，负责：
 * <ol>
 *   <li>配置 Supervisor System Prompt（对应 Dify 的 "域分类" 节点）</li>
 *   <li>注册各垂直域子 Agent 的 Tool（product/troubleshooting/order/policy/installation/solution/general）</li>
 *   <li>绑定 Redis 持久化记忆</li>
 * </ol>
 * </p>
 *
 * @author zly
 */
@Slf4j
@Configuration
public class LucasAgentConfiguration {

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private RedisSaver redisSaver;

    /**
     * Lucas 主 Agent
     * <p>
     * 当前为骨架实现，Tools 均为占位，后续按域逐步填充。
     * </p>
     */
    @Bean("lucasAgent")
    public ReactAgent lucasAgent() throws GraphStateException {
        String systemPrompt = buildSupervisorSystemPrompt();

        return ReactAgent.builder()
                .name("lucas-agent")
                .description("LiTime 品牌 Lucas 智能客服机器人，支持产品推荐、故障排查、订单查询、政策咨询、安装指导等多域服务")
                .model(chatModel)
                .systemPrompt(systemPrompt)
                .saver(redisSaver)
                .tools(new ToolCallback[0])
                .hooks(HumanInTheLoopHook.builder().build())
                .build();
    }

    // ─── 私有方法 ────────────────────────────────────────────────────────────────

    /**
     * 构建 Supervisor 的 System Prompt
     * <p>
     * 对应 Dify 中 "域分类（轻量级）" 节点的完整 Prompt，
     * 包含 7 大业务域的判断规则和边界说明。
     * </p>
     */
    private String buildSupervisorSystemPrompt() {
        return """
                # 角色
                你是 LiTime 锂电池品牌的专业智能客服 Lucas，你的名字是 Lucas。
                你的任务是：理解用户输入，判断其所属业务域，并调用对应的专业工具为用户提供准确、自然、友好的回答。
                
                # 品牌信息
                LiTime 是一家专注于锂电池能源解决方案的科技品牌。
                - 品牌口号："Explore Possibility of Life"（探索生活的可能性）
                - 核心理念："Tech Driven, User Focused"（技术驱动，用户至上）
                - 全球服务：30+ 本地仓库，覆盖美国、加拿大、澳大利亚、欧洲多国，提供免费快速配送、5年质保和30天免费退货。
                
                # 业务域说明（用于路由决策）
                - product：产品查询、型号/规格/参数、功能特性、对比、推荐、适配性
                - troubleshooting：故障、异常、不工作、报错、无法充/放电、BMS 保护、冒烟鼓包漏液（优先级最高）
                - order：订单、物流、配送、送达、发货、发票、地址修改、订单状态
                - policy：保修、退换货、退款流程、运输限制、合规政策、支付方式
                - installation：安装、接线、并联、串联、蓝牙/APP 配置、设置步骤、通用原理科普
                - solution：整套供电方案设计、系统设计、多设备全链路搭配
                - general：品牌通用咨询、问候、闲聊、感谢
                
                # 回复要求
                1. 始终使用用户的语言回复。
                2. 语气友好、专业、简洁。
                3. 若工具返回需要追问的信息，请自然地向用户追问缺失的关键信息。
                4. 若无法解决，引导用户联系人工客服。
                """;
    }
}
