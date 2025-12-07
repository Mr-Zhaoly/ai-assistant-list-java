import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.zly.DatabaseAssistantApplication;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = DatabaseAssistantApplication.class)
public class MCPTest {

    @Autowired
    private SyncMcpToolCallbackProvider toolCallbackProvider;

    @Test
    public void testMCP(@Autowired DashScopeChatModel dashScopeChatModel) {
        String question = "返回gerpgo_shop的表结构";
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel)
                //日志拦截
//                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();
        System.out.println(answer);
    }
}
