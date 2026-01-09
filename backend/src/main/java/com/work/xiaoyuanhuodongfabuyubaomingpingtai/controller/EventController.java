package com.work.xiaoyuanhuodongfabuyubaomingpingtai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.ApiResponse;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.Roles;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.EventQuery;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.EventRequest;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Event;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.AuthUser;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.EventService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ApiResponse<Page<Event>> list(EventQuery query) {
        return ApiResponse.success(eventService.pageEvents(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        Event event = eventService.getById(id);
        if (event == null) {
            return ApiResponse.failure("活动不存在");
        }
        return ApiResponse.success(event);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','organizer')")
    public ApiResponse<?> create(@Valid @RequestBody EventRequest request, Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Event event = new Event();
        copy(request, event);
        event.setCreatorId(user.getUserId());
        event.setStatus("published");
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
        if (!user.getRoles().contains(Roles.ADMIN) && !db.getCreatorId().equals(user.getUserId())) {
            return ApiResponse.failure("无权限修改");
        }
        copy(request, db);
        db.setUpdatedAt(LocalDateTime.now());
        eventService.updateById(db);
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
        db.setStatus("canceled");
        eventService.updateById(db);
        return ApiResponse.success();
    }

    private void copy(EventRequest src, Event dest) {
        dest.setTitle(src.getTitle());
        dest.setDescription(src.getDescription());
        dest.setCategory(src.getCategory());
        dest.setTags(src.getTags());
        dest.setLocation(src.getLocation());
        dest.setStartTime(src.getStartTime());
        dest.setEndTime(src.getEndTime());
        dest.setCapacity(src.getCapacity());
        dest.setCoverUrl(src.getCoverUrl());
        dest.setOrgId(src.getOrgId());
    }
}
