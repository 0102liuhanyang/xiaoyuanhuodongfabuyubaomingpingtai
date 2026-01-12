package com.work.xiaoyuanhuodongfabuyubaomingpingtai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.ApiResponse;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Notification;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.AuthUser;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.RoleMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.UserMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.UserRoleMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Role;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.User;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.UserRole;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public NotificationController(NotificationService notificationService, UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper) {
        this.notificationService = notificationService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @GetMapping
    public ApiResponse<Page<Notification>> list(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "10") long size,
                                                @RequestParam(required = false) Boolean read,
                                                Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, user.getUserId());
        if (read != null) {
            wrapper.eq(Notification::getReadFlag, read);
        }
        Page<Notification> result = notificationService.page(new Page<>(page, size),
                wrapper.orderByDesc(Notification::getCreatedAt));
        return ApiResponse.success(result);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        long count = notificationService.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, user.getUserId())
                .eq(Notification::getReadFlag, false));
        return ApiResponse.success(count);
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

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<?> broadcast(@RequestParam String title,
                                    @RequestParam String content,
                                    @RequestParam(required = false) String role) {
        List<Long> userIds;
        if (role != null && !role.isBlank()) {
            Role dbRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, role));
            if (dbRole == null) {
                return ApiResponse.failure("角色不存在");
            }
            List<Long> roleUserIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getRoleId, dbRole.getId()))
                    .stream()
                    .map(UserRole::getUserId)
                    .toList();
            userIds = roleUserIds.isEmpty() ? List.of() : userMapper.selectBatchIds(roleUserIds)
                    .stream().map(User::getId).collect(Collectors.toList());
        } else {
            userIds = userMapper.selectList(null).stream().map(User::getId).toList();
        }
        notificationService.notifyUsers(userIds, "broadcast", title, content);
        return ApiResponse.success();
    }
}
