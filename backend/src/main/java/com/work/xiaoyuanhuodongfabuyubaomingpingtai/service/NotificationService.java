package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Notification;

public interface NotificationService extends IService<Notification> {
    void notifyUser(Long userId, String type, String title, String content);
    void notifyUsers(java.util.List<Long> userIds, String type, String title, String content);
}
