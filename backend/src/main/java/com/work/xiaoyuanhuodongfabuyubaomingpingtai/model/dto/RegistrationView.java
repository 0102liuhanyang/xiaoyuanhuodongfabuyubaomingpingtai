package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationView {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String phone;
    private String email;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime checkinAt;
}
