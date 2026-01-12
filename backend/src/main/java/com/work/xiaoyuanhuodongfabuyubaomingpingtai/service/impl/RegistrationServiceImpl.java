package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.EventStatus;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.EventMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.RegistrationMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.RegistrationBlacklistMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Event;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.RegistrationBlacklist;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Registration;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.RegistrationService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {
    private final EventMapper eventMapper;
    private final RegistrationBlacklistMapper blacklistMapper;
    private final NotificationService notificationService;

    public RegistrationServiceImpl(EventMapper eventMapper, RegistrationBlacklistMapper blacklistMapper, NotificationService notificationService) {
        this.eventMapper = eventMapper;
        this.blacklistMapper = blacklistMapper;
        this.notificationService = notificationService;
    }

    @Override
    public boolean canRegister(Long eventId, Long userId) {
        long blocked = blacklistMapper.selectCount(new LambdaQueryWrapper<RegistrationBlacklist>().eq(RegistrationBlacklist::getUserId, userId));
        if (blocked > 0) {
            return false;
        }
        Event event = eventMapper.selectById(eventId);
        if (event == null || !EventStatus.PUBLISHED.equals(event.getStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (event.getSignupStartTime() != null && now.isBefore(event.getSignupStartTime())) {
            return false;
        }
        if (event.getSignupEndTime() != null && now.isAfter(event.getSignupEndTime())) {
            return false;
        }
        if (event.getStartTime() != null && now.isAfter(event.getStartTime())) {
            return false;
        }
        if (event.getEndTime() != null && now.isAfter(event.getEndTime())) {
            return false;
        }
        long existed = this.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getUserId, userId)
                .in(Registration::getStatus, "registered", "waitlisted"));
        if (existed > 0) {
            return false;
        }
        long count = this.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getStatus, "registered"));
        if (count >= event.getCapacity()) {
            return true; // 可以进入候补
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Registration register(Long eventId, Long userId) {
        Registration r = new Registration();
        r.setEventId(eventId);
        r.setUserId(userId);
        long registeredCount = this.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getStatus, "registered"));
        Event event = eventMapper.selectById(eventId);
        boolean full = event != null && registeredCount >= event.getCapacity();
        r.setStatus(full ? "waitlisted" : "registered");
        r.setCreatedAt(LocalDateTime.now());
        this.save(r);
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Registration cancel(Long eventId, Long userId) {
        Registration r = this.getOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getUserId, userId)
                .in(Registration::getStatus, "registered", "waitlisted"));
        if (r == null) {
            return null;
        }
        String previous = r.getStatus();
        r.setStatus("canceled");
        r.setCanceledAt(LocalDateTime.now());
        this.updateById(r);
        if ("registered".equals(previous)) {
            promoteWaitlist(eventId, eventMapper.selectById(eventId).getCapacity());
        }
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void promoteWaitlist(Long eventId, int capacity) {
        long registeredCount = this.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getStatus, "registered"));
        if (registeredCount >= capacity) {
            return;
        }
        int slots = (int) (capacity - registeredCount);
        var waits = this.list(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getStatus, "waitlisted")
                .orderByAsc(Registration::getCreatedAt)
                .last("limit " + slots));
        for (Registration wait : waits) {
            wait.setStatus("registered");
            wait.setCanceledAt(null);
            this.updateById(wait);
            notificationService.notifyUser(wait.getUserId(), "registration", "候补转正", "您在活动ID " + eventId + " 的报名已转正");
        }
    }
}
