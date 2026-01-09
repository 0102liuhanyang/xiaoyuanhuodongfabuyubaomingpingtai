package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("registrations")
public class Registration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long eventId;
    private Long userId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime canceledAt;
}
