package com.zly.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginTokenVO {
    private String token;
    private Long expiresInSeconds;
}

