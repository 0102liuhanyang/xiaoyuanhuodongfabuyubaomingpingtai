package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Registration;

public interface RegistrationService extends IService<Registration> {
    boolean canRegister(Long eventId, Long userId);
    Registration register(Long eventId, Long userId);
}
