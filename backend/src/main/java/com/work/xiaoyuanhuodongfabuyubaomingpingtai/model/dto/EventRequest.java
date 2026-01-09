package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {
    private Long id;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private String category;
    private String tags;
    @NotBlank
    private String location;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    @Future
    private LocalDateTime endTime;
    @NotNull
    @Min(1)
    private Integer capacity;
    private String coverUrl;
    private Long orgId;
}
