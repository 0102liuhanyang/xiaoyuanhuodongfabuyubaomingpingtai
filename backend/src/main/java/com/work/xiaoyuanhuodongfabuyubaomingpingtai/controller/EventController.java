package com.work.xiaoyuanhuodongfabuyubaomingpingtai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.ApiResponse;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.EventStatus;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.Roles;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.EventQuery;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.EventRequest;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Event;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Organization;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Registration;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.AuthUser;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.EventService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.RegistrationService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.NotificationService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.OrganizationMapper;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final NotificationService notificationService;
    private final OrganizationMapper organizationMapper;

    public EventController(EventService eventService, RegistrationService registrationService, NotificationService notificationService, OrganizationMapper organizationMapper) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.notificationService = notificationService;
        this.organizationMapper = organizationMapper;
    }

    @GetMapping
    public ApiResponse<Page<Event>> list(EventQuery query) {
        return ApiResponse.success(eventService.pageEvents(query));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<Page<Event>> mine(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "20") long size,
                                         Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Page<Event> result = eventService.page(new Page<>(page, size),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Event>()
                        .eq(Event::getCreatorId, user.getUserId())
                        .orderByDesc(Event::getCreatedAt));
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id, Authentication authentication) {
        Event event = eventService.getById(id);
        if (event == null) {
            return ApiResponse.failure("活动不存在");
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("event", event);
        long regCount = registrationService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, id)
                .eq(Registration::getStatus, "registered"));
        resp.put("registeredCount", regCount);
        if (authentication != null) {
            AuthUser user = (AuthUser) authentication.getPrincipal();
            Registration reg = registrationService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                    .eq(Registration::getEventId, id)
                    .eq(Registration::getUserId, user.getUserId())
                    .in(Registration::getStatus, "registered", "waitlisted"));
            if (reg != null) {
                resp.put("myStatus", reg.getStatus());
                resp.put("checkinAt", reg.getCheckinAt());
            }
        }
        return ApiResponse.success(resp);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> create(@Valid @RequestBody EventRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Event event = new Event();
        copy(request, event);
        event.setCreatorId(user.getUserId());
        event.setStatus(user.getRoles().contains(Roles.ADMIN) ? EventStatus.PUBLISHED : EventStatus.DRAFT);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        eventService.save(event);
        return ApiResponse.success(event);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody EventRequest request, Authentication authentication) {
        Event db = eventService.getById(id);
        if (db == null) {
            return ApiResponse.failure("活动不存在");
        }
        AuthUser user = (AuthUser) authentication.getPrincipal();
        boolean isAdmin = user.getRoles().contains(Roles.ADMIN);
        if (!isAdmin && !db.getCreatorId().equals(user.getUserId())) {
            return ApiResponse.failure("无权限修改");
        }
        if (!isAdmin && EventStatus.ARCHIVED.equals(db.getStatus())) {
            return ApiResponse.failure("已归档的活动不可编辑");
        }
        boolean timeChanged = request.getStartTime() != null && !request.getStartTime().equals(db.getStartTime())
                || request.getEndTime() != null && !request.getEndTime().equals(db.getEndTime());
        boolean locationChanged = request.getLocation() != null && !request.getLocation().equals(db.getLocation());
        long currentRegistered = registrationService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, id)
                .eq(Registration::getStatus, "registered"));
        if (request.getCapacity() != null && request.getCapacity() < currentRegistered) {
            return ApiResponse.failure("容量不能小于已报名人数");
        }
        copy(request, db);
        db.setUpdatedAt(LocalDateTime.now());
        eventService.updateById(db);
        if (request.getCapacity() != null && request.getCapacity() > currentRegistered) {
            registrationService.promoteWaitlist(id, request.getCapacity());
        }
        if (EventStatus.PUBLISHED.equals(db.getStatus()) && (timeChanged || locationChanged)) {
            registrationService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                    .eq(Registration::getEventId, id)
                    .in(Registration::getStatus, "registered", "waitlisted")).forEach(r ->
                    notificationService.notifyUser(r.getUserId(), "event", "活动变更",
                            "活动 [" + db.getTitle() + "] 时间或地点已更新"));
        }
        return ApiResponse.success(db);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> delete(@PathVariable Long id, Authentication authentication) {
        Event db = eventService.getById(id);
        if (db == null) {
            return ApiResponse.failure("活动不存在");
        }
        AuthUser user = (AuthUser) authentication.getPrincipal();
        if (!user.getRoles().contains(Roles.ADMIN) && !db.getCreatorId().equals(user.getUserId())) {
            return ApiResponse.failure("无权限删除");
        }
        db.setStatus(EventStatus.CANCELED);
        eventService.updateById(db);
        registrationService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, id)).forEach(r ->
                notificationService.notifyUser(r.getUserId(), "event", "活动取消", "活动 [" + db.getTitle() + "] 已取消"));
        return ApiResponse.success();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> submit(@PathVariable Long id, Authentication authentication) {
        Event db = eventService.getById(id);
        if (db == null) {
            return ApiResponse.failure("活动不存在");
        }
        AuthUser user = (AuthUser) authentication.getPrincipal();
        if (!user.getRoles().contains(Roles.ADMIN) && !db.getCreatorId().equals(user.getUserId())) {
            return ApiResponse.failure("无权限提交");
        }
        db.setStatus(user.getRoles().contains(Roles.ADMIN) ? EventStatus.PUBLISHED : EventStatus.PENDING);
        db.setUpdatedAt(LocalDateTime.now());
        eventService.updateById(db);
        return ApiResponse.success(db);
    }

    @PostMapping("/{id}/checkin-code")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> genCheckinCode(@PathVariable Long id, Authentication authentication) {
        Event db = eventService.getById(id);
        if (db == null) return ApiResponse.failure("活动不存在");
        AuthUser user = (AuthUser) authentication.getPrincipal();
        if (!user.getRoles().contains(Roles.ADMIN) && !db.getCreatorId().equals(user.getUserId())) {
            return ApiResponse.failure("无权限生成签到码");
        }
        String code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6).toUpperCase();
        db.setCheckinCode(code);
        db.setCheckinValidUntil(LocalDateTime.now().plusHours(2));
        db.setUpdatedAt(LocalDateTime.now());
        eventService.updateById(db);
        notificationService.notifyUser(db.getCreatorId(), "event", "签到码已生成", "活动 [" + db.getTitle() + "] 签到码：" + code + " 有效期2小时");
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", code);
        resp.put("qrText", code);
        resp.put("validUntil", db.getCheckinValidUntil());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> stats(@PathVariable Long id, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Event event = eventService.getById(id);
        if (event == null) return ApiResponse.failure("活动不存在");
        if (!user.getRoles().contains(Roles.ADMIN) && !event.getCreatorId().equals(user.getUserId())) {
            return ApiResponse.failure("无权限查看");
        }
        long registered = registrationService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, id)
                .eq(Registration::getStatus, "registered"));
        long waitlisted = registrationService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, id)
                .eq(Registration::getStatus, "waitlisted"));
        long checkedIn = registrationService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, id)
                .isNotNull(Registration::getCheckinAt));
        Map<String, Object> data = Map.of(
                "registered", registered,
                "waitlisted", waitlisted,
                "checkedIn", checkedIn
        );
        return ApiResponse.success(data);
    }

    @GetMapping("/stats/overview")
    @PreAuthorize("hasAnyRole('admin')")
    public ApiResponse<?> overview(@RequestParam(required = false) Long orgId,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Event>();
        if (orgId != null) {
            wrapper.eq(Event::getOrgId, orgId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(Event::getStartTime, LocalDateTime.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(Event::getEndTime, LocalDateTime.parse(endDate));
        }
        var events = eventService.list(wrapper);
        long published = events.stream().filter(e -> EventStatus.PUBLISHED.equals(e.getStatus())).count();
        long total = events.size();
        long totalRegistrations = 0;
        long totalCheckin = 0;
        for (Event e : events) {
            totalRegistrations += registrationService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                    .eq(Registration::getEventId, e.getId())
                    .eq(Registration::getStatus, "registered"));
            totalCheckin += registrationService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Registration>()
                    .eq(Registration::getEventId, e.getId())
                    .isNotNull(Registration::getCheckinAt));
        }
        Map<String, Object> data = Map.of(
                "events", total,
                "published", published,
                "registrations", totalRegistrations,
                "checkins", totalCheckin
        );
        return ApiResponse.success(data);
    }

    @GetMapping("/stats/orgs")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<List<Organization>> orgs() {
        return ApiResponse.success(organizationMapper.selectList(null));
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Page<Event>> pending(EventQuery query) {
        if (query == null) {
            query = new EventQuery();
        }
        query.setStatus(EventStatus.PENDING);
        return ApiResponse.success(eventService.pageEvents(query));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Page<Event>> adminAll(EventQuery query) {
        if (query == null) {
            query = new EventQuery();
        }
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Event>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Event::getTitle, query.getKeyword());
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(Event::getCategory, query.getCategory());
        }
        if (StringUtils.hasText(query.getTags())) {
            wrapper.like(Event::getTags, query.getTags());
        }
        if (query.getStart() != null) {
            wrapper.ge(Event::getStartTime, query.getStart());
        }
        if (query.getEnd() != null) {
            wrapper.le(Event::getEndTime, query.getEnd());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Event::getStatus, query.getStatus());
        }
        if ("startTime".equalsIgnoreCase(query.getSortBy())) {
            wrapper.orderBy(query.getSortOrder() == null || !"asc".equalsIgnoreCase(query.getSortOrder()),
                    false, Event::getStartTime);
        } else {
            wrapper.orderByDesc(Event::getCreatedAt);
        }
        Page<Event> result = eventService.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return ApiResponse.success(result);
    }

    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<?> approve(@PathVariable Long id) {
        Event db = eventService.getById(id);
        if (db == null) {
            return ApiResponse.failure("活动不存在");
        }
        if (!EventStatus.PENDING.equals(db.getStatus())) {
            return ApiResponse.failure("当前状态不可审核");
        }
        db.setStatus(EventStatus.PUBLISHED);
        db.setUpdatedAt(LocalDateTime.now());
        eventService.updateById(db);
        notificationService.notifyUser(db.getCreatorId(), "event", "活动审核通过", "您的活动 [" + db.getTitle() + "] 已审核通过");
        return ApiResponse.success(db);
    }

    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<?> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        Event db = eventService.getById(id);
        if (db == null) {
            return ApiResponse.failure("活动不存在");
        }
        if (!EventStatus.PENDING.equals(db.getStatus())) {
            return ApiResponse.failure("当前状态不可驳回");
        }
        db.setStatus(EventStatus.REJECTED);
        db.setUpdatedAt(LocalDateTime.now());
        eventService.updateById(db);
        String content = "您的活动 [" + db.getTitle() + "] 审核未通过";
        if (reason != null && !reason.isBlank()) {
            content += "，原因：" + reason;
        }
        notificationService.notifyUser(db.getCreatorId(), "event", "活动审核未通过", content);
        return ApiResponse.success(db);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> archive(@PathVariable Long id, Authentication authentication) {
        Event db = eventService.getById(id);
        if (db == null) {
            return ApiResponse.failure("活动不存在");
        }
        AuthUser user = (AuthUser) authentication.getPrincipal();
        if (!user.getRoles().contains(Roles.ADMIN) && !db.getCreatorId().equals(user.getUserId())) {
            return ApiResponse.failure("无权限归档");
        }
        db.setStatus(EventStatus.ARCHIVED);
        db.setUpdatedAt(LocalDateTime.now());
        eventService.updateById(db);
        return ApiResponse.success(db);
    }

    private void copy(EventRequest src, Event dest) {
        dest.setTitle(src.getTitle());
        dest.setDescription(src.getDescription());
        dest.setCategory(src.getCategory());
        dest.setTags(src.getTags());
        dest.setLocation(src.getLocation());
        dest.setSignupStartTime(src.getSignupStartTime());
        dest.setSignupEndTime(src.getSignupEndTime());
        dest.setStartTime(src.getStartTime());
        dest.setEndTime(src.getEndTime());
        dest.setCapacity(src.getCapacity());
        dest.setOrgId(src.getOrgId());
    }
}
