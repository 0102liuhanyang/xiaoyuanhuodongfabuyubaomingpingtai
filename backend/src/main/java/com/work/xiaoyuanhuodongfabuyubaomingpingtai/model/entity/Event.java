package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("events")
public class Event {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orgId;
    private Long creatorId;
    private String title;
    private String description;
    private String category;
    private String tags; // 逗号分隔或 JSON
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
    private String status;
    private String coverUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
