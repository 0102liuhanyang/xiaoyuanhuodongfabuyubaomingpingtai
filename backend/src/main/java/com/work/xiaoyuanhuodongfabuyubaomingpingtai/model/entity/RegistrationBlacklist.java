package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("registration_blacklist")
public class RegistrationBlacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String reason;
    private LocalDateTime createdAt;
}
