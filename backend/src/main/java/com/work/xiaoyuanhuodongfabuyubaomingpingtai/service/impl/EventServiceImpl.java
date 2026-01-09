package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        if (query.getStart() != null) {
            wrapper.ge(Event::getStartTime, query.getStart());
        }
        if (query.getEnd() != null) {
            wrapper.le(Event::getEndTime, query.getEnd());
        }
        wrapper.orderByDesc(Event::getCreatedAt);
        return this.page(new Page<>(query.getPage(), query.getSize()), wrapper);
    }
}
