package com.zly;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = ToolAgentApplication.class)
public class PromptTest {

    @Test
    public void test1(@Autowired DashScopeChatModel dashScopeChatModel) {
        String question = "查询一个月的订单，并给出SQL？";
//        // Step 1: 检索相关文档
//        List<Document> relevantDocs = vectorStore.similaritySearch(question);
//        // Step 2: 构建上下文
//        String context = relevantDocs.stream()
//                .map(Document::getText)
//                .collect(Collectors.joining(""));
        // Step 3: 使用上下文生成答案
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();
        String answer = chatClient.prompt(question)
                .call()
                .content();
        System.out.println(answer);
    }
}
