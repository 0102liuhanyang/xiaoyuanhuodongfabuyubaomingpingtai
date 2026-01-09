package com.work.xiaoyuanhuodongfabuyubaomingpingtai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.ApiResponse;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.common.Roles;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.LoginRequest;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.dto.RegisterRequest;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.model.entity.User;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.AuthUser;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.security.JwtUtil;
import com.work.xiaoyuanhuodongfabuyubaomingpingtai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        User exist = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (exist != null) {
            return ApiResponse.failure("用户名已存在");
        }
        User user = userService.createUserWithRole(request.getUsername(), request.getPassword(), request.getName(), request.getEmail(), request.getPhone(), Roles.STUDENT);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), java.util.List.of(Roles.STUDENT));
        return ApiResponse.success(buildTokenResponse(token, user.getUsername(), request.getName(), Roles.STUDENT));
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AuthUser principal = (AuthUser) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal.getUserId(), principal.getUsername(), principal.getRoles());
        return ApiResponse.success(buildTokenResponse(token, principal.getUsername(), null, String.join(",", principal.getRoles())));
    }

    @GetMapping("/profile")
    public ApiResponse<?> profile(Authentication authentication) {
        if (authentication == null) {
            return ApiResponse.failure("未登录");
        }
        AuthUser user = (AuthUser) authentication.getPrincipal();
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", user.getUsername());
        profile.put("userId", user.getUserId());
        profile.put("roles", user.getRoles());
        return ApiResponse.success(profile);
    }

    private Map<String, Object> buildTokenResponse(String token, String username, String name, String roles) {
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("tokenType", "Bearer");
        map.put("username", username);
        map.put("roles", roles);
        if (name != null) {
            map.put("name", name);
        }
        return map;
    }
}
