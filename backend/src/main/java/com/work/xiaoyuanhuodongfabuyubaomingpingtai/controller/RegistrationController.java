package com.work.xiaoyuanhuodongfabuyubaomingpingtai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.ApiResponse;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Event;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.RegistrationBlacklist;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Registration;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.AuthUser;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.RegistrationService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.EventService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.RegistrationBlacklistMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.RegistrationMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.RegistrationView;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    private static final ConcurrentHashMap<String, Long> RATE_LIMIT_CACHE = new ConcurrentHashMap<>();
    private final RegistrationService registrationService;
    private final EventService eventService;
    private final RegistrationBlacklistMapper blacklistMapper;
    private final RegistrationMapper registrationMapper;

    public RegistrationController(RegistrationService registrationService, EventService eventService, RegistrationBlacklistMapper blacklistMapper, RegistrationMapper registrationMapper) {
        this.registrationService = registrationService;
        this.eventService = eventService;
        this.blacklistMapper = blacklistMapper;
        this.registrationMapper = registrationMapper;
    }

    @PostMapping("/events/{eventId}/registrations")
    @PreAuthorize("hasRole('student')")
    public ApiResponse<?> register(@PathVariable Long eventId, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        if (!registrationService.canRegister(eventId, user.getUserId())) {
            return ApiResponse.failure("不可报名，可能报名时间未开放、已满或已报名");
        }
        Registration r = registrationService.register(eventId, user.getUserId());
        return ApiResponse.success(r);
    }

    @DeleteMapping("/events/{eventId}/registrations/my")
    @PreAuthorize("hasRole('student')")
    public ApiResponse<Void> cancel(@PathVariable Long eventId, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Registration r = registrationService.cancel(eventId, user.getUserId());
        if (r == null) {
            return ApiResponse.failure("未报名该活动或已取消");
        }
        return ApiResponse.success();
    }

    @GetMapping("/registrations/my")
    @PreAuthorize("hasRole('student')")
    public ApiResponse<?> my(@RequestParam(defaultValue = "1") long page,
                             @RequestParam(defaultValue = "10") long size,
                             @RequestParam(required = false) String status,
                             Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        var wrapper = new LambdaQueryWrapper<Registration>()
                .eq(Registration::getUserId, user.getUserId());
        if (StringUtils.hasText(status)) {
            wrapper.eq(Registration::getStatus, status);
        }
        Page<Registration> result = registrationService.page(new Page<>(page, size),
                wrapper.orderByDesc(Registration::getCreatedAt));
        Page<Registration> pageData = result;
        Page<Map<String, Object>> view = new Page<>(pageData.getCurrent(), pageData.getSize(), pageData.getTotal());
        List<Map<String, Object>> records = pageData.getRecords().stream().map(r -> {
            Event ev = eventService.getById(r.getEventId());
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", r.getEventId());
            map.put("registrationId", r.getId());
            map.put("title", ev != null ? ev.getTitle() : "");
            map.put("startTime", ev != null ? ev.getStartTime() : null);
            map.put("location", ev != null ? ev.getLocation() : "");
            map.put("status", r.getStatus());
            map.put("checkinAt", r.getCheckinAt());
            return map;
        }).toList();
        view.setRecords(records);
        return ApiResponse.success(view);
    }

    @GetMapping(value = "/events/{eventId}/registrations/export", produces = "text/csv")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ResponseEntity<byte[]> export(@PathVariable Long eventId, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Event event = eventService.getById(eventId);
        if (event == null) {
            return ResponseEntity.badRequest().body("活动不存在".getBytes(StandardCharsets.UTF_8));
        }
        if (!user.getRoles().contains("admin") && !event.getCreatorId().equals(user.getUserId())) {
            return ResponseEntity.status(403).body("无权限导出".getBytes(StandardCharsets.UTF_8));
        }
        List<Registration> regs = registrationService.list(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .orderByAsc(Registration::getCreatedAt));
        String header = "userId,status,createdAt\n";
        String body = regs.stream()
                .map(r -> String.format("%s,%s,%s", r.getUserId(), r.getStatus(), r.getCreatedAt()))
                .collect(Collectors.joining("\n"));
        String csv = header + body;
        String filename = "registrations-" + eventId + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.TEXT_PLAIN_VALUE.equals(MediaType.TEXT_PLAIN_VALUE) ? MediaType.TEXT_PLAIN : MediaType.APPLICATION_OCTET_STREAM)
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/events/{eventId}/registrations")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> listRegistrations(@PathVariable Long eventId,
                                            @RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword,
                                            Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Event event = eventService.getById(eventId);
        if (event == null) {
            return ApiResponse.failure("活动不存在");
        }
        if (!user.getRoles().contains("admin") && !event.getCreatorId().equals(user.getUserId())) {
            return ApiResponse.failure("无权限查看");
        }
        Page<RegistrationView> result = registrationMapper.selectEventRegistrations(new Page<>(page, size), eventId, status, keyword);
        return ApiResponse.success(result);
    }

    @GetMapping("/events/registrations/organizer")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> listOrganizerRegistrations(@RequestParam(defaultValue = "1") long page,
                                                     @RequestParam(defaultValue = "10") long size,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) String checkin,
                                                     Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Page<RegistrationView> result = registrationMapper.selectOrganizerRegistrations(
                new Page<>(page, size),
                user.getUserId(),
                status,
                keyword,
                checkin
        );
        return ApiResponse.success(result);
    }

    @PostMapping("/events/{eventId}/checkin")
    @PreAuthorize("hasRole('student')")
    public ApiResponse<?> checkin(@PathVariable Long eventId, @RequestParam String code, Authentication authentication,
                                  HttpServletRequest request) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Event event = eventService.getById(eventId);
        if (event == null) return ApiResponse.failure("活动不存在");
        if (!StringUtils.hasText(code) || event.getCheckinCode() == null || event.getCheckinValidUntil() == null) {
            return ApiResponse.failure("签到码无效");
        }
        if (LocalDateTime.now().isAfter(event.getCheckinValidUntil())) {
            return ApiResponse.failure("签到码已过期");
        }
        if (!code.equalsIgnoreCase(event.getCheckinCode())) {
            return ApiResponse.failure("签到码错误");
        }
        String key = "checkin:" + eventId + ":" + user.getUserId();
        Long last = RATE_LIMIT_CACHE.get(key);
        long now = System.currentTimeMillis();
        if (last != null && now - last < 10_000) { // 10s limit
            return ApiResponse.failure("操作过于频繁，请稍后再试");
        }
        Registration r = registrationService.getOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getUserId, user.getUserId())
                .eq(Registration::getStatus, "registered"));
        if (r == null) return ApiResponse.failure("未报名或未转正");
        r.setCheckinAt(LocalDateTime.now());
        registrationService.updateById(r);
        RATE_LIMIT_CACHE.put(key, now);
        return ApiResponse.success(r);
    }

    @PostMapping("/admin/registrations/blacklist")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<?> addBlacklist(@RequestParam Long userId, @RequestParam(required = false) String reason) {
        RegistrationBlacklist item = new RegistrationBlacklist();
        item.setUserId(userId);
        item.setReason(reason);
        item.setCreatedAt(java.time.LocalDateTime.now());
        blacklistMapper.insert(item);
        return ApiResponse.success();
    }

    @DeleteMapping("/admin/registrations/blacklist/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<?> removeBlacklist(@PathVariable Long userId) {
        blacklistMapper.delete(new LambdaQueryWrapper<RegistrationBlacklist>().eq(RegistrationBlacklist::getUserId, userId));
        return ApiResponse.success();
    }

    @GetMapping("/admin/registrations/blacklist")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Page<RegistrationBlacklist>> listBlacklist(@RequestParam(defaultValue = "1") long page,
                                                                  @RequestParam(defaultValue = "10") long size,
                                                                  @RequestParam(required = false) String keyword) {
        var wrapper = new LambdaQueryWrapper<RegistrationBlacklist>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(RegistrationBlacklist::getReason, keyword);
        }
        Page<RegistrationBlacklist> result = blacklistMapper.selectPage(new Page<>(page, size),
                wrapper.orderByDesc(RegistrationBlacklist::getCreatedAt));
        return ApiResponse.success(result);
    }
}
