package com.zly.lucas.model.vo;

import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Lucas 机器人对话响应 VO
 * <p>
 * 用于 SSE 流式和同步接口的统一响应格式。
 * 当 {@code humanService=true} 时，前端应触发转人工流程。
 * </p>
 *
 * @author zly
 */
@Data
@Builder
public class LucasResponseVO implements Serializable {

    /** 响应码（200 正常，500 异常） */
    private Integer code;

    /** 当前流式 chunk 内容或完整回复内容 */
    private String chunk;

    /**
     * 是否需要转人工
     * <p>true 时前端应显示转人工提示并启动人工客服会话</p>
     */
    private Boolean humanService;

    /** 当前处理的业务域（可选，用于前端调试） */
    private String domain;

    /**
     * 序列化为 JSON 字符串（用于 SSE 事件 data 字段）
     */
    public String toJsonString() {
        return JSON.toJSONString(this);
    }
}
