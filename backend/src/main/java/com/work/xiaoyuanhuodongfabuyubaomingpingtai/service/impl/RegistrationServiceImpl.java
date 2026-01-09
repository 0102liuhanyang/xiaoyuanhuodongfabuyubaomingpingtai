package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.EventMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.RegistrationMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Event;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Registration;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.RegistrationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {
    private final EventMapper eventMapper;

    public RegistrationServiceImpl(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    @Override
    public boolean canRegister(Long eventId, Long userId) {
        Event event = eventMapper.selectById(eventId);
        if (event == null || !"published".equals(event.getStatus())) {
            return false;
        }
        long count = this.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getStatus, "registered"));
        if (count >= event.getCapacity()) {
            return false;
        }
        long existed = this.count(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getUserId, userId)
                .eq(Registration::getStatus, "registered"));
        return existed == 0;
    }

    @Override
    public Registration register(Long eventId, Long userId) {
        Registration r = new Registration();
        r.setEventId(eventId);
        r.setUserId(userId);
        r.setStatus("registered");
        r.setCreatedAt(LocalDateTime.now());
        this.save(r);
        return r;
    }
}
