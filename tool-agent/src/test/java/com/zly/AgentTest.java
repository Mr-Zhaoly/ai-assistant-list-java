package com.zly;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;


@SpringBootTest(classes = ToolAgentApplication.class)
@ActiveProfiles("test")
public class AgentTest {

    @Autowired
    private ReactAgent reactAgent;

    @Test
    public void testCall() throws GraphRunnerException {
//        // 字符串输入
//        AssistantMessage response = reactAgent.call("杭州的天气怎么样？");
//        System.out.println(response.getText());

        // 多个消息
        List<Message> messages = List.of(
                new UserMessage("我想了解 Java 多线程"),
                new UserMessage("特别是线程池的使用")
        );
        RunnableConfig config = RunnableConfig.builder().threadId("test-thread-1").build();
        AssistantMessage response = reactAgent.call(messages, config);
        System.out.println(response.getText());
    }

    @Test
    public void testInvoke() throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder().threadId("test-thread-2").build();
        Optional<OverAllState> result = reactAgent.invoke("帮我写一首诗", config);
        if (result.isPresent()) {
            OverAllState state = result.get();

            // 访问消息历史
            Optional<Object> messages = state.value("messages");
            List<Message> messageList = (List<Message>) messages.get();

            // 访问自定义状态
            Optional<Object> customData = state.value("custom_key");

            System.out.println("完整状态：" + state);
        }
    }

    @Test
    public void testRunnableConfig() throws GraphRunnerException {
        String threadId = "2";
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(threadId)
                .addMetadata("userId", "1")
                .build();

        AssistantMessage response = reactAgent.call("帮我查询最近一个月的订单数据，不需要你去查询，直接给出SQL", runnableConfig);
        System.out.println(response.getText());
    }
}
