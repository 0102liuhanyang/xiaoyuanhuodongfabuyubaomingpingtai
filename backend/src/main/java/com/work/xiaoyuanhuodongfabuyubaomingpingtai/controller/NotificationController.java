package com.work.xiaoyuanhuodongfabuyubaomingpingtai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.ApiResponse;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Notification;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.AuthUser;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<Page<Notification>> list(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Page<Notification> result = notificationService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, user.getUserId())
                        .orderByDesc(Notification::getCreatedAt));
        return ApiResponse.success(result);
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Notification n = notificationService.getById(id);
        if (n == null || !n.getUserId().equals(user.getUserId())) {
            return ApiResponse.failure("无权操作");
        }
        n.setReadFlag(true);
        notificationService.updateById(n);
        return ApiResponse.success();
    }
}
