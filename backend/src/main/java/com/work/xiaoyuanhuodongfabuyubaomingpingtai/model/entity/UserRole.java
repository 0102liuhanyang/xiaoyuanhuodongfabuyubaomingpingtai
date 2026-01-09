package com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_role")
public class UserRole {
    @TableId
    private Long id;
    private Long userId;
    private Long roleId;
}
