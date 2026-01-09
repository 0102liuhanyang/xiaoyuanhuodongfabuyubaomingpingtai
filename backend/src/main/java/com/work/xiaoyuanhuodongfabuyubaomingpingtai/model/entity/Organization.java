package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("organizations")
public class Organization {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String contact;
    private LocalDateTime createdAt;
}
