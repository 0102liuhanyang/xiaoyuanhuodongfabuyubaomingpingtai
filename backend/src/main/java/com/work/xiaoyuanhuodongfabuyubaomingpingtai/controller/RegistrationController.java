package com.work.xiaoyuanhuodongfabuyubaomingpingtai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.ApiResponse;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Registration;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.AuthUser;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.RegistrationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/events/{eventId}/registrations")
    public ApiResponse<?> register(@PathVariable Long eventId, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        if (!registrationService.canRegister(eventId, user.getUserId())) {
            return ApiResponse.failure("不可报名，可能容量已满或已报名");
        }
        Registration r = registrationService.register(eventId, user.getUserId());
        return ApiResponse.success(r);
    }

    @DeleteMapping("/events/{eventId}/registrations/my")
    public ApiResponse<Void> cancel(@PathVariable Long eventId, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Registration r = registrationService.getOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getUserId, user.getUserId())
                .eq(Registration::getStatus, "registered"));
        if (r == null) {
            return ApiResponse.failure("未报名该活动");
        }
        r.setStatus("canceled");
        registrationService.updateById(r);
        return ApiResponse.success();
    }

    @GetMapping("/registrations/my")
    public ApiResponse<Page<Registration>> my(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Page<Registration> result = registrationService.page(new Page<>(page, size),
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getUserId, user.getUserId())
                        .orderByDesc(Registration::getCreatedAt));
        return ApiResponse.success(result);
    }
}
