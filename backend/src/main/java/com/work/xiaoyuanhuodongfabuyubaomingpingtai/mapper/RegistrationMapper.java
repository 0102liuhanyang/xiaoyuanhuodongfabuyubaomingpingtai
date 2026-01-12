package com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Registration;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.RegistrationView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
    Page<RegistrationView> selectEventRegistrations(Page<RegistrationView> page,
                                                   @Param("eventId") Long eventId,
                                                   @Param("status") String status,
                                                   @Param("keyword") String keyword);
}
