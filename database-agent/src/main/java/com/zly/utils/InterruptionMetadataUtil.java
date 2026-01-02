package com.zly.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.zly.model.vo.ToolFeedbackVO;

import java.util.ArrayList;
import java.util.List;

public class InterruptionMetadataUtil {

    public static List<ToolFeedbackVO> conversionToolFeedbackVO(InterruptionMetadata metadata) {
        List<ToolFeedbackVO> toolFeedbackVOList = new ArrayList<>();
        if(metadata == null || CollectionUtil.isEmpty(metadata.toolFeedbacks())){
            return toolFeedbackVOList;
        }

        for (InterruptionMetadata.ToolFeedback toolFeedback : metadata.toolFeedbacks()){
            ToolFeedbackVO toolFeedbackVO = ToolFeedbackVO.builder()
                    .id(toolFeedback.getId())
                    .name(toolFeedback.getName())
                    .arguments(toolFeedback.getArguments())
                    .result(toolFeedback.getResult().name())
                    .description(toolFeedback.getDescription())
                    .build();

            toolFeedbackVOList.add(toolFeedbackVO);
        }
        return toolFeedbackVOList;
    }
}
