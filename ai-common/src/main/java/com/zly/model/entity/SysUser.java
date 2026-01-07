package com.zly.model.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class SysUser implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String status; // ACTIVE, DISABLED
    private Integer deleted; // 0 not deleted, 1 deleted
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

