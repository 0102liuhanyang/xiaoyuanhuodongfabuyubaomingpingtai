package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.EventQuery;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Event;

public interface EventService extends IService<Event> {
    Page<Event> pageEvents(EventQuery query);
}
