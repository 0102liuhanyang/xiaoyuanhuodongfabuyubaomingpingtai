package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventQuery {
    private String keyword;
    private String category;
    private String tags;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer page = 1;
    private Integer size = 10;
    private String status;
    private String sortBy; // startTime/createTime
    private String sortOrder; // asc/desc
}
