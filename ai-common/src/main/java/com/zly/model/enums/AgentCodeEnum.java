package com.zly.model.enums;

import lombok.Getter;

@Getter
public enum AgentCodeEnum {

    SUCCESS(200, "Success"),
    ERROR(500, "Error"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    NOT_FOUND(404, "未找到"),
    SERVER_ERROR(500, "服务器错误");

    private final int code;
    private final String message;

    AgentCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code获取对应的枚举值
     * @param code 状态码
     * @return 枚举值
     */
    public static AgentCodeEnum getByCode(int code) {
        for (AgentCodeEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
