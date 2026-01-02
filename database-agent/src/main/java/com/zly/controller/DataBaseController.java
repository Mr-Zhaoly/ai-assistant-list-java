package com.zly.controller;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.zly.model.dto.QuestionRequestDTO;
import com.zly.service.IDatabaseQaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;

@Controller
@RequestMapping("/database")
public class DataBaseController {

    @Autowired
    private IDatabaseQaService databaseQaService;

    @Autowired
    private ReactAgent reactAgent;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@RequestBody QuestionRequestDTO request) throws GraphRunnerException {
        // 流式输出
        return reactAgent.stream(request.getQuestion())
                .filter(output -> output instanceof StreamingOutput)
                .map(output -> (StreamingOutput) output)
                .filter(streamingOutput -> streamingOutput.getOutputType() == OutputType.AGENT_MODEL_STREAMING)
                .map(streamingOutput -> ServerSentEvent.builder(streamingOutput.message().getText()).build());
    }
}