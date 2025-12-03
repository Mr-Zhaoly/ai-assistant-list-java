package com.zly.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.zly.model.dto.QuestionRequestDTO;
import com.zly.service.IMysqlQaService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MysqlQaServiceImpl implements IMysqlQaService {

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private SyncMcpToolCallbackProvider toolCallbackProvider;

    @Autowired
    private VectorStore mysqlVectorStore;

    private ChatClient chatClient;
    @PostConstruct
    public void init() {
        chatClient = ChatClient
                .builder(dashScopeChatModel)
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }

    @Override
    public String getAnswer(QuestionRequestDTO requestDTO) {
        log.info("用户提问：{}", requestDTO.getQuestion());

        List<Document> results = mysqlVectorStore.similaritySearch(SearchRequest.builder().query(requestDTO.getQuestion()).topK(3).build());
        // Step 2: 构建上下文
        String context = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("/n"));
        // Step 3: 使用上下文生成答案
        String answer = chatClient.prompt()
                .user(u -> u.text(requestDTO.getQuestion()))
                //根据用户ID保存会话
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,requestDTO.getUserId()))
                .system(s -> s.text("#检索到的表结构：/n" + context))
                .call()
                .content();
        log.info("答案：{}", answer);
        return answer;
    }
}
