package com.zly.service.impl;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.zly.model.dto.QuestionRequestDTO;
import com.zly.service.IDatabaseQaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO 写明类的作用
 *
 * @author zhaoliangyu
 * @since 2025/12/12 10:16
 */
@Service
@Slf4j
public class DatabaseQaServiceImpl implements IDatabaseQaService {

    @Autowired
    private ReactAgent reactAgent;

    @Override
    public String getAnswer(QuestionRequestDTO request) {
        try {
            RunnableConfig runnableConfig = RunnableConfig.builder()
                    .threadId(request.getUserId())
                    .build();

            AssistantMessage response = reactAgent.call(request.getQuestion(), runnableConfig);
            return response.getText();
        }catch (Exception e){
            log.error("获取答案失败", e);
            return "获取答案失败";
        }
    }
}
