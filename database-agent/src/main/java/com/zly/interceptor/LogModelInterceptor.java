package com.zly.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;


public class LogModelInterceptor extends ModelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogModelInterceptor.class);


    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        List<Message> messages = request.getMessages();
        log.info("ModelInterceptor: Model {} is called with messages: {}, context: {}", request.getOptions().getModel(), messages, request.getContext());

        // 执行实际调用
        ModelResponse response = handler.call(request);

        log.info("ModelInterceptor: Model {} returned response: {}", request.getOptions().getModel(), response);
        return response;
    }

    @Override
    public String getName() {
        return "LogModelInterceptor";
    }
}
