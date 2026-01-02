package com.zly.model.vo;

import com.zly.model.enums.FeedbackResultEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ToolFeedbackVO implements Serializable {

    private String id;

    private String name;

    private String arguments;

    private String result;

    private String description;
}
