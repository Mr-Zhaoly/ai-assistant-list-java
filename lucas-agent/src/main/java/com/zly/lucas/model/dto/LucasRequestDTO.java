package com.zly.lucas.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Lucas 机器人对话请求 DTO
 *
 * @author zly
 */
@Data
public class LucasRequestDTO implements Serializable {

    /** 用户问题 */
    private String question;

    /** 用户 ID */
    private Long userId;

    /** 会话 ID（用于多轮对话记忆隔离） */
    private String sessionId;

    /**
     * 用户语言（可选，若前端未传则由 Hook 自动识别）
     * 取值：zh / en / ja / es / fr / de 等
     */
    private String userLanguage;

    /** 站点标识（如 US / EU / JP，用于区分不同站点的政策和物流规则） */
    private String site;
}
