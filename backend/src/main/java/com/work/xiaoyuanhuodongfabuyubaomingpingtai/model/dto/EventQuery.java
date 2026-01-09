package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventQuery {
    private String keyword;
    private String category;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer page = 1;
    private Integer size = 10;
}
