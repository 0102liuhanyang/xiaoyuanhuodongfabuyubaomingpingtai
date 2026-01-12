package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.EventStatus;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.EventMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.EventQuery;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Event;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {
    @Override
    public Page<Event> pageEvents(EventQuery query) {
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<>();
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
        } else {
            wrapper.eq(Event::getStatus, EventStatus.PUBLISHED);
        }
        if ("startTime".equalsIgnoreCase(query.getSortBy())) {
            wrapper.orderBy(query.getSortOrder() == null || !"asc".equalsIgnoreCase(query.getSortOrder()),
                    false, Event::getStartTime);
        } else {
            wrapper.orderByDesc(Event::getCreatedAt);
        }
        return this.page(new Page<>(query.getPage(), query.getSize()), wrapper);
    }
}
