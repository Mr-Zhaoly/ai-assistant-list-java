package com.zly.config;

import com.alibaba.cloud.ai.agent.studio.loader.AgentLoader;
import com.alibaba.cloud.ai.graph.agent.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class StudioConfiguration {
    @Autowired
    private Agent reactAgent;
    
    @Bean
    public AgentLoader agentLoader() {
        return new AgentLoader() {
            @Override
            public Agent loadAgent(String agentId) {
                return reactAgent;
            }
            
            @Override
            public List<String> listAgents() {
                return List.of("tool-agent");
            }
        };
    }
}
