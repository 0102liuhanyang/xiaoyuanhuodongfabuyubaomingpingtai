package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends IService<User>, UserDetailsService {
    User createUserWithRole(String username, String rawPassword, String name, String email, String phone, String roleCode);
    boolean isAdmin(org.springframework.security.core.userdetails.UserDetails principal);
}
