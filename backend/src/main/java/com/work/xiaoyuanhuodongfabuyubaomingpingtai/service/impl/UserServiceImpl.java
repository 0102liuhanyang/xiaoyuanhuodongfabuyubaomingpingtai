package com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.Roles;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.RoleMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.UserMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.mapper.UserRoleMapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.Role;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.User;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.UserRole;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.AuthUser;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRoleMapper userRoleMapper, RoleMapper roleMapper) {
        this.passwordEncoder = passwordEncoder;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<UserRole> links = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
        List<Long> roleIds = links.stream().map(UserRole::getRoleId).toList();
        List<String> roles = roleIds.isEmpty() ? List.of() :
                roleMapper.selectBatchIds(roleIds).stream().map(Role::getCode).toList();
        return new AuthUser(user.getId(), user.getUsername(), user.getPasswordHash(), Boolean.TRUE.equals(user.getEnabled()), roles);
    }

    @Override
    public User createUserWithRole(String username, String rawPassword, String name, String email, String phone, String roleCode) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        this.save(user);

        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode));
        if (role != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        }
        return user;
    }

    @Override
    public boolean isAdmin(UserDetails principal) {
        return principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + Roles.ADMIN));
    }
}
