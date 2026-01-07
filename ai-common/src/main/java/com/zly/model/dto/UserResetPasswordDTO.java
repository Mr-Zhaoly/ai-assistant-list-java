package com.zly.model.dto;

import lombok.Data;

@Data
public class UserResetPasswordDTO {
    private String account; // username or email
}

