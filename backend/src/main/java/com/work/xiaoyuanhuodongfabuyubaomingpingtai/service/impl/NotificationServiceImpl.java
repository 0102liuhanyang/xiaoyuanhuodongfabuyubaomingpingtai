package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.NotificationMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Notification;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
}
