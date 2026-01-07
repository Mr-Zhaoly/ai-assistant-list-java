package com.zly.model.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String account; // username or email
    private String password;
    private String captcha;
}

