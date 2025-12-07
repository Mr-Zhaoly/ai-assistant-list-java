package com.zly.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.zly.model.dto.QuestionRequestDTO;
import com.zly.service.IHealthQaService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Configuration
public class HealthQaServiceImpl implements IHealthQaService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @Autowired
    private ChatMemory chatMemory;

    @Value("classpath:/prompt/diet.st")
    private Resource resource;

    private ChatClient chatClient;
    @PostConstruct
    public void init() {
        chatClient = ChatClient
                .builder(dashScopeChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultSystem(resource)
                .build();
    }

    @Override
    public String getAnswer(QuestionRequestDTO requestDTO) {
        log.info("用户提问：{}", requestDTO.getQuestion());
        // Step 1: 检索相关文档
        List<Document> relevantDocs = vectorStore.similaritySearch(requestDTO.getQuestion());
        // Step 2: 构建上下文
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(""));
        // Step 3: 使用上下文生成答案
        String answer = chatClient.prompt()
                .system(p->p.param("context", context))
                .user(u -> u.text(requestDTO.getQuestion()))
                //根据用户ID保存会话
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,requestDTO.getUserId()))
                .call()
                .content();
        log.info("答案：{}", answer);
        return answer;
    }
}
