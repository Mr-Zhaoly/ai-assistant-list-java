package com.zly.model.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class BusinessAgentVO implements Serializable {

    private Integer code;

    private String node;

    private String chunk;

    private List<ToolFeedbackVO> toolFeedback;
}
