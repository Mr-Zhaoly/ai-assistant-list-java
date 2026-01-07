package com.zly.model.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String name;
    private String email;
    private Integer page = 1;
    private Integer size = 10;
}

