package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.NotificationMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Notification;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    @Override
    public void notifyUser(Long userId, String type, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setReadFlag(false);
        n.setCreatedAt(LocalDateTime.now());
        this.save(n);
    }

    @Override
    public void notifyUsers(List<Long> userIds, String type, String title, String content) {
        if (userIds == null || userIds.isEmpty()) return;
        List<Notification> batch = new ArrayList<>();
        for (Long userId : userIds) {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setType(type);
            n.setTitle(title);
            n.setContent(content);
            n.setReadFlag(false);
            n.setCreatedAt(LocalDateTime.now());
            batch.add(n);
        }
        this.saveBatch(batch);
    }
}
